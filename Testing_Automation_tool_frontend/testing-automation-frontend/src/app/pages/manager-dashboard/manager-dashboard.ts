import { Component, OnInit, OnDestroy, inject, signal, ChangeDetectorRef, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd, RouterModule } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of, Subscription, Observable, interval } from 'rxjs';
import { catchError, filter, map } from 'rxjs/operators';
import { AuthService } from '../../core/services/authService';
import { ProjectService } from '../../core/services/project.service';
import { TestCaseService } from '../../core/services/testcase.service';
import { BugService, DeveloperUser } from '../../core/services/bug.service';
import { FeatureService } from '../../core/services/feature.service';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationsModalComponent } from '../tester-workspace/components/notifications-modal/notifications-modal.component';
import { environment } from '../../../environments/environment';
import { UserProfile } from '../../model/auth/userProfile';
import { Project, TestCase, Bug, InAppNotification, ProjectPatchRequest } from '../../model/workspace/workspace.model';

interface SidebarNavItem {
  id: string;
  label: string;
  icon: string;
  route?: string;
  isCreateAction?: boolean;
}

export interface ProjectOption {
  projectId: number;
  projectName: string;
}

export interface FeatureOption {
  featureId: number;
  featureName: string;
  sprint: number;
  projectId: number;
  projectName?: string;
}

export interface TesterPerformance {
  id: string;
  name: string;
  username: string;
  email?: string;
  initials: string;
  avatarColor: string;
  isUnassigned?: boolean;
  total: number;
  todoCount: number;
  inProgressCount: number;
  doneCount: number;
  todoPercent: number;
  inProgressPercent: number;
  donePercent: number;
  testCases: TestCase[];
}

export interface DeveloperPerformance {
  id: string;
  name: string;
  username: string;
  email?: string;
  initials: string;
  avatarColor: string;
  isUnassigned?: boolean;
  total: number;
  openCount: number;
  inProgressCount: number;
  resolvedCount: number;
  openPercent: number;
  inProgressPercent: number;
  resolvedPercent: number;
  bugs: Bug[];
}

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NotificationsModalComponent],
  templateUrl: './manager-dashboard.html',
  styleUrl: './manager-dashboard.css'
})
export class ManagerDashboard implements OnInit, OnDestroy {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly elementRef = inject(ElementRef);
  private readonly projectService = inject(ProjectService);
  private readonly testCaseService = inject(TestCaseService);
  private readonly bugService = inject(BugService);
  private readonly featureService = inject(FeatureService);
  private readonly notificationService = inject(NotificationService);

  private readonly apiBase = environment.apiGatewayUrl;
  private routerSub?: Subscription;

  // Loading & Error States
  isLoading = true;
  hasError = false;
  errorMessage = '';

  // Summary Metrics (Row 1)
  totalCount = 0;
  passedCount = 0;
  failedCount = 0;
  blockedCount = 0;
  notExecutedCount = 0;

  passedPercent = 0;
  failedPercent = 0;
  blockedPercent = 0;
  notExecutedPercent = 0;

  // QA Dashboard Charts (Row 2)
  qaTestExecutionSegments: Array<{ color: string; dasharray: string; dashoffset: number; key?: string }> = [];
  qaDefectsSegments: Array<{ color: string; dasharray: string; dashoffset: number; key?: string }> = [];
  qaOpenDefectsCount = 0;
  qaCriticalCount = 0;
  qaHighCount = 0;
  qaMediumCount = 0;
  qaLowCount = 0;
  qaCriticalPercent = 0;
  qaHighPercent = 0;
  qaMediumPercent = 0;
  qaLowPercent = 0;
  isQaDefectsLoading = false;

  // QA Dashboard Chart Drilldown Details Table State (Dedicated per chart)
  qaTestDrilldown: {
    active: boolean;
    statusKey: string;
    title: string;
    badgeClass: string;
    badgeLabel: string;
  } | null = null;
  isQaTestLoading = false;
  qaTestError = '';
  qaTestSearchTerm = '';
  qaTestFilterFeature: string = '';
  qaTestFilterType: string = '';
  qaTestFilterPriority: string = '';
  qaTestPage = 0;
  readonly qaTestPageSize = 8;
  qaDrilldownTestCases: TestCase[] = [];

  qaDefectDrilldown: {
    active: boolean;
    statusKey: string;
    title: string;
    badgeClass: string;
    badgeLabel: string;
  } | null = null;
  qaDefectSearchTerm = '';
  qaDefectFilterSeverity: string = '';
  qaDefectFilterStatus: string = '';
  qaDefectPage = 0;
  readonly qaDefectPageSize = 8;
  qaDrilldownBugs: Bug[] = [];
  qaScopedOpenBugs: Bug[] = [];

  // Active Performance Role View ('TESTER' | 'DEVELOPER')
  activePerformanceTab: 'TESTER' | 'DEVELOPER' = 'TESTER';

  // Tester Performance Analysis State
  testerPerformanceList: TesterPerformance[] = [];
  isTesterMetricsLoading = false;
  testerMetricsError = '';
  selectedTester: TesterPerformance | null = null;
  showAllTesters = false;
  readonly initialTesterLimit = 3;

  // Selected Tester Drilldown Table State
  testerDetailsSearchTerm = '';
  testerDetailsPage = 0;
  readonly testerDetailsPageSize = 8;

  // Developer Performance Analysis State
  developerPerformanceList: DeveloperPerformance[] = [];
  isDeveloperMetricsLoading = false;
  developerMetricsError = '';
  selectedDeveloper: DeveloperPerformance | null = null;
  showAllDevelopers = false;
  readonly initialDeveloperLimit = 3;

  // Selected Developer Drilldown Table State
  developerDetailsSearchTerm = '';
  developerDetailsPage = 0;
  readonly developerDetailsPageSize = 8;

  // Filter Data Sources
  allProjects: ProjectOption[] = [];
  allFeatures: FeatureOption[] = [];
  availableFeatures: FeatureOption[] = [];
  availableSprints: number[] = [];

  // Filter State
  selectedProjectId: number | null = null;
  selectedFeatureId: number | null = null;
  selectedSprint: number | null = null;
  showFiltersDropdown = false;

  // User Profile
  currentUser = signal<UserProfile | null>(null);
  userName = '';
  userRole = '';
  userInitials = '';
  userEmail = '';
  isProfileLoading = true;
  showProfileMenu = false;
  showExportDropdown = false;

  get isManager(): boolean {
    const signalUser = this.currentUser();
    const signalRole = (signalUser?.role || '').toUpperCase();
    if (signalRole) {
      return signalRole.includes('MANAGER') || signalRole.includes('ADMIN');
    }
    const storedRole = (
      (typeof localStorage !== 'undefined' ? (localStorage.getItem('tat_user_role') || localStorage.getItem('role')) : '') ||
      ''
    ).toUpperCase();
    if (storedRole) {
      return storedRole.includes('MANAGER') || storedRole.includes('ADMIN');
    }
    const currentRole = (this.userRole || '').toUpperCase();
    return currentRole.includes('MANAGER') || currentRole.includes('ADMIN');
  }

  // Header Notifications & Dropdown (Aligned with Manager Workflows)
  isSidebarOpen = true;
  notifications: InAppNotification[] = [];
  unreadNotificationCount = 0;
  isNotificationsModalOpen = false;
  isNotificationsDropdownOpen = false;
  activeNotifTab: 'ALL' | 'APPROVALS' | 'SYSTEM' = 'ALL';
  isRefreshingNotifs = false;
  private pollingSub?: Subscription;
  // Active Navigation
  activeSidebarItem = 'QA Dashboard';
  activeHeaderNav = 'Dashboards';

  // Toast / Info Message
  toastMessage = '';
  private toastTimer: any = null;

  // Sidebar navigation configuration
  readonly sidebarNavItems: SidebarNavItem[] = [
    { id: 'system-dashboard', label: 'User Management', icon: 'fa-regular fa-compass', route: '/manager/system-dashboard' },
    { id: 'project-dashboard', label: 'Project Management', icon: 'fa-solid fa-table-columns', route: '/manager/project-dashboard' },
    { id: 'qa-dashboard', label: 'QA Dashboard', icon: 'fa-regular fa-square-check', route: '/manager-dashboard' },
    { id: 'tester-workspace', label: 'Tester Workspace', icon: 'fa-solid fa-vial-circle-check', route: '/tester-workspace' }
  ];

  readonly createNavItem: SidebarNavItem = {
    id: 'create-dashboard',
    label: 'Create dashboard',
    icon: 'fa-solid fa-plus',
    route: '/manager/create-dashboard',
    isCreateAction: false
  };

  get pendingApprovalsCount(): number {
    return this.pendingUsers ? this.pendingUsers.length : 0;
  }

  get totalNotificationCount(): number {
    return this.unreadNotificationCount + this.pendingApprovalsCount;
  }

  get displayedNotificationCount(): number {
    if (this.activeNotifTab === 'APPROVALS') {
      return this.pendingApprovalsCount;
    }
    if (this.activeNotifTab === 'SYSTEM') {
      return this.notifications.length;
    }
    return this.pendingApprovalsCount + this.notifications.length;
  }

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadFilterData();
    this.loadDashboardMetrics();
    this.loadNotifications();
    this.loadPendingApprovals();
    this.syncRouteWithView();

    this.routerSub = this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe(() => {
      this.syncRouteWithView();
    });

    // Gentle polling for pending approval updates and system notifications (every 60s)
    this.pollingSub = interval(60000).subscribe(() => {
      this.loadPendingApprovals(true);
      this.loadNotifications(true);
    });
  }

  // ==========================================
  // SIDEBAR & HEADER ACTIONS
  // ==========================================
  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.cdr.markForCheck();
  }

  // ==========================================
  // NOTIFICATIONS & PENDING APPROVAL REQUESTS
  // ==========================================
  loadNotifications(silent = false): void {
    this.notificationService.getNotifications().subscribe({
      next: (notifs) => {
        this.notifications = notifs;
        this.unreadNotificationCount = notifs.filter(n => n.notificationStatus === 'UNREAD').length;
        this.cdr.markForCheck();
      },
      error: () => {
        if (!silent) {
          this.notifications = [];
          this.unreadNotificationCount = 0;
        }
        this.cdr.markForCheck();
      }
    });
  }

  loadPendingApprovals(silent = false): void {
    this.http.get<any>(`${this.apiBase}/manager/users/pending`).pipe(
      catchError(err => {
        if (!silent) console.warn('Failed to load pending users for notifications:', err);
        return of({ data: [] });
      })
    ).subscribe({
      next: (res) => {
        const rawPending = res?.data || res || [];
        this.pendingUsers = Array.isArray(rawPending) ? rawPending : [];
        this.pendingUsers.forEach(u => {
          if (!this.pendingRoleSelections[u.userId]) {
            this.pendingRoleSelections[u.userId] = 'TESTER';
          }
        });
        this.cdr.markForCheck();
      }
    });
  }

  toggleNotificationsDropdown(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.isNotificationsDropdownOpen = !this.isNotificationsDropdownOpen;
    if (this.isNotificationsDropdownOpen) {
      this.showProfileMenu = false;
      this.showFiltersDropdown = false;
      this.showExportDropdown = false;
      this.loadPendingApprovals(true);
      this.loadNotifications(true);
    }
    this.cdr.markForCheck();
  }

  refreshAllNotifications(): void {
    this.isRefreshingNotifs = true;
    this.loadPendingApprovals(true);
    this.loadNotifications(true);
    setTimeout(() => {
      this.isRefreshingNotifs = false;
      this.cdr.markForCheck();
    }, 600);
  }

  navigateToPendingApprovals(user?: any): void {
    this.isNotificationsDropdownOpen = false;
    this.isNotificationsModalOpen = false;
    this.activeSidebarItem = 'User Management';
    this.systemTab = 'pending';
    this.router.navigate(['/manager/system-dashboard']);
    this.loadSystemDashboardData();

    if (user && user.userId) {
      setTimeout(() => {
        const row = document.getElementById(`pending-user-row-${user.userId}`);
        if (row) {
          row.scrollIntoView({ behavior: 'smooth', block: 'center' });
          row.classList.add('highlight-pulse');
          setTimeout(() => row.classList.remove('highlight-pulse'), 3000);
        }
      }, 350);
    }
  }

  handleNotificationItemClick(n: InAppNotification): void {
    if (n.notificationStatus === 'UNREAD') {
      this.handleToggleNotificationRead(n);
    }
    this.isNotificationsDropdownOpen = false;
    if (n.bugId || n.bugFormatId) {
      this.activeSidebarItem = 'Defect Management';
      this.loadDefectManagementData();
      this.router.navigate(['/manager/defect-management']);
    }
  }

  openNotificationsModal(): void {
    this.isNotificationsDropdownOpen = false;
    this.isNotificationsModalOpen = true;
    this.cdr.markForCheck();
  }

  closeNotificationsModal(): void {
    this.isNotificationsModalOpen = false;
    this.cdr.markForCheck();
  }

  handleToggleNotificationRead(n: InAppNotification): void {
    n.notificationStatus = n.notificationStatus === 'UNREAD' ? 'READ' : 'UNREAD';
    this.unreadNotificationCount = this.notifications.filter(item => item.notificationStatus === 'UNREAD').length;
    this.notificationService.markAsRead(n.notificationId).subscribe();
    this.cdr.markForCheck();
  }

  handleDismissNotification(n: InAppNotification): void {
    this.notifications = this.notifications.filter(item => item.notificationId !== n.notificationId);
    this.unreadNotificationCount = this.notifications.filter(item => item.notificationStatus === 'UNREAD').length;
    this.notificationService.deleteNotification(n.notificationId).subscribe();
    this.cdr.markForCheck();
  }

  handleMarkAllNotificationsRead(): void {
    this.notifications.forEach(n => {
      if (n.notificationStatus === 'UNREAD') {
        n.notificationStatus = 'READ';
        this.notificationService.markAsRead(n.notificationId).subscribe();
      }
    });
    this.unreadNotificationCount = 0;
    this.showToast('All system notifications marked as read');
    this.cdr.markForCheck();
  }

  // ==========================================
  // PROFILE MENU NAVIGATION (Same as Tester Workspace)
  // ==========================================
  goToProfile(event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.showProfileMenu = false;
    this.router.navigate(['/profile']);
  }

  navigateToTesterWorkspace(event?: Event): void {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }
    this.showProfileMenu = false;
    this.router.navigate(['/tester-workspace']);
  }

  ngOnDestroy(): void {
    if (this.routerSub) {
      this.routerSub.unsubscribe();
    }
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
    }
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
  }

  /**
   * Synchronize active sidebar item and fetch view data based on the active router URL
   */
  private syncRouteWithView(): void {
    const url = this.router.url;
    if (url.includes('system-dashboard')) {
      this.activeSidebarItem = 'User Management';
      this.loadSystemDashboardData();
    } else if (url.includes('project-dashboard')) {
      this.activeSidebarItem = 'Project Management';
      this.loadProjectDashboardData();
    } else if (url.includes('tester-workspace')) {
      this.activeSidebarItem = 'Tester Workspace';
    } else if (url.includes('test-execution')) {
      this.activeSidebarItem = 'Test Execution';
      this.loadTestExecutionData();
    } else if (url.includes('defect-management')) {
      this.activeSidebarItem = 'Defect Management';
      this.loadDefectManagementData();
    } else if (url.includes('automation-reports')) {
      this.activeSidebarItem = 'Automation Reports';
      this.loadAutomationReportsData();
    } else if (url.includes('create-dashboard')) {
      this.activeSidebarItem = 'Create dashboard';
      this.openCreateDashboardModal();
    } else {
      this.activeSidebarItem = 'QA Dashboard';
    }
    this.cdr.markForCheck();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const filterWrapper = this.elementRef.nativeElement.querySelector('.filters-nav-wrapper');
    const profileWrapper = this.elementRef.nativeElement.querySelector('.profile-avatar-wrapper');
    const notifWrapper = this.elementRef.nativeElement.querySelector('.header-notif-wrapper');
    const exportWrapper = this.elementRef.nativeElement.querySelector('.export-dropdown-wrapper');

    if (filterWrapper && !filterWrapper.contains(target)) {
      this.showFiltersDropdown = false;
    }
    if (profileWrapper && !profileWrapper.contains(target)) {
      this.showProfileMenu = false;
    }
    if (notifWrapper && !notifWrapper.contains(target)) {
      this.isNotificationsDropdownOpen = false;
    }
    if (exportWrapper && !exportWrapper.contains(target)) {
      this.showExportDropdown = false;
    }
  }

  @HostListener('document:keydown.escape')
  onKeydownEscape(): void {
    if (this.isNotificationsDropdownOpen) {
      this.isNotificationsDropdownOpen = false;
    }
    if (this.showProfileMenu) {
      this.showProfileMenu = false;
    }
    if (this.qaTestDrilldown?.active) {
      this.closeQaTestDrilldown();
    }
    if (this.qaDefectDrilldown?.active) {
      this.closeQaDefectDrilldown();
    }
  }

  loadUserProfile(): void {
    const cachedUser = this.authService.currentUser();
    if (cachedUser) {
      this.setUserDetails(cachedUser);
    } else {
      this.isProfileLoading = true;
      this.authService.getProfile().subscribe({
        next: (res) => {
          if (res?.data) {
            this.setUserDetails(res.data);
          } else {
            this.isProfileLoading = false;
            this.cdr.markForCheck();
          }
        },
        error: () => {
          this.isProfileLoading = false;
          this.cdr.markForCheck();
        }
      });
    }
  }

  private setUserDetails(user: UserProfile): void {
    this.currentUser.set(user);
    this.userName = (user.fullName || user.username || '').trim();

    const rawRole = (user.role || '').toUpperCase();
    if (rawRole.includes('MANAGER')) {
      this.userRole = 'Manager';
    } else if (rawRole.includes('ADMIN')) {
      this.userRole = 'Admin';
    } else if (rawRole.includes('TESTER')) {
      this.userRole = 'Tester';
    } else if (rawRole) {
      this.userRole = rawRole.replace('ROLE_', '');
    } else {
      this.userRole = '';
    }

    this.userEmail = user.email || user.username || '';

    if (this.userName) {
      const names = this.userName.split(/\s+/).filter(n => n.length > 0);
      if (names.length >= 2) {
        this.userInitials = (names[0][0] + names[names.length - 1][0]).toUpperCase();
      } else if (names.length === 1 && names[0].length >= 2) {
        this.userInitials = names[0].substring(0, 2).toUpperCase();
      } else if (names.length === 1) {
        this.userInitials = names[0].toUpperCase();
      } else {
        this.userInitials = this.userRole ? this.userRole.charAt(0) : 'M';
      }
    } else if (this.userRole) {
      this.userInitials = this.userRole.charAt(0);
    } else {
      this.userInitials = '';
    }

    this.isProfileLoading = false;
    this.cdr.markForCheck();
  }

  /**
   * Load Projects and Features from backend APIs for the Filters dropdown
   */
  loadFilterData(): void {
    const projects$ = this.http.get<any>(`${this.apiBase}/project`).pipe(
      catchError((err) => {
        console.warn('Failed to load projects for filter:', err);
        return of({ data: [] });
      })
    );

    const features$ = this.http.get<any>(`${this.apiBase}/feature`).pipe(
      catchError((err) => {
        console.warn('Failed to load features for filter:', err);
        return of({ data: [] });
      })
    );

    forkJoin({ projects: projects$, features: features$ }).subscribe({
      next: (res) => {
        // Map Projects
        const rawProjects = res.projects?.data || res.projects || [];
        this.allProjects = (Array.isArray(rawProjects) ? rawProjects : []).map((p: any) => {
          const item = p.project || p;
          return {
            projectId: item.projectId || item.id,
            projectName: item.projectName || `Project #${item.projectId}`
          };
        });

        // Map Features
        const rawFeatures = res.features?.data || res.features || [];
        this.allFeatures = (Array.isArray(rawFeatures) ? rawFeatures : []).map((f: any) => {
          const feat = f.feature || f;
          const proj = f.project || {};
          return {
            featureId: feat.featureId || feat.id,
            featureName: feat.featureName || `Feature #${feat.featureId}`,
            sprint: feat.sprint != null ? feat.sprint : 1,
            projectId: proj.projectId || feat.projectId || 0,
            projectName: proj.projectName || ''
          };
        });

        this.updateAvailableFeaturesAndSprints();
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Update dependent filter options when Project, Feature, or Sprint selections change
   */
  private updateAvailableFeaturesAndSprints(): void {
    // 1. Filter features by selected project
    if (this.selectedProjectId != null) {
      this.availableFeatures = this.allFeatures.filter(
        (f) => f.projectId === this.selectedProjectId
      );
    } else {
      this.availableFeatures = [...this.allFeatures];
    }

    // If sprint is selected, further narrow features if needed
    if (this.selectedSprint != null) {
      this.availableFeatures = this.availableFeatures.filter(
        (f) => f.sprint === this.selectedSprint
      );
    }

    // 2. Extract unique sprints from features matching the selected project
    const featuresForSprints = this.selectedProjectId != null
      ? this.allFeatures.filter((f) => f.projectId === this.selectedProjectId)
      : this.allFeatures;

    const sprintSet = new Set<number>();
    featuresForSprints.forEach((f) => {
      if (f.sprint != null) {
        sprintSet.add(f.sprint);
      }
    });
    this.availableSprints = Array.from(sprintSet).sort((a, b) => a - b);
  }

  onProjectSelect(projectId: number | null): void {
    this.selectedProjectId = projectId;

    // Reset feature if it doesn't belong to the newly selected project
    if (this.selectedFeatureId != null) {
      const feat = this.allFeatures.find((f) => f.featureId === this.selectedFeatureId);
      if (feat && projectId != null && feat.projectId !== projectId) {
        this.selectedFeatureId = null;
      }
    }

    this.updateAvailableFeaturesAndSprints();

    // Reset sprint if not available in project
    if (this.selectedSprint != null && !this.availableSprints.includes(this.selectedSprint)) {
      this.selectedSprint = null;
    }
  }

  onFeatureSelect(featureId: number | null): void {
    this.selectedFeatureId = featureId;

    if (featureId != null) {
      const feat = this.allFeatures.find((f) => f.featureId === featureId);
      if (feat) {
        // Auto-align project and sprint to this feature
        if (this.selectedProjectId !== feat.projectId) {
          this.selectedProjectId = feat.projectId;
        }
        if (this.selectedSprint !== feat.sprint) {
          this.selectedSprint = feat.sprint;
        }
      }
    }

    this.updateAvailableFeaturesAndSprints();
  }

  onSprintSelect(sprint: number | null): void {
    this.selectedSprint = sprint;

    // Reset feature if it does not belong to the selected sprint
    if (this.selectedFeatureId != null) {
      const feat = this.allFeatures.find((f) => f.featureId === this.selectedFeatureId);
      if (feat && sprint != null && feat.sprint !== sprint) {
        this.selectedFeatureId = null;
      }
    }

    this.updateAvailableFeaturesAndSprints();
  }

  applyFilters(): void {
    this.showFiltersDropdown = false;
    this.loadDashboardMetrics();
  }

  resetFilters(): void {
    this.selectedProjectId = null;
    this.selectedFeatureId = null;
    this.selectedSprint = null;
    this.updateAvailableFeaturesAndSprints();
    this.showFiltersDropdown = false;
    this.loadDashboardMetrics();
  }

  removeProjectFilter(): void {
    this.onProjectSelect(null);
    this.loadDashboardMetrics();
  }

  removeFeatureFilter(): void {
    this.onFeatureSelect(null);
    this.loadDashboardMetrics();
  }

  removeSprintFilter(): void {
    this.onSprintSelect(null);
    this.loadDashboardMetrics();
  }

  onProjectSelectAndReload(projectId: number | null): void {
    this.onProjectSelect(projectId);
    this.loadDashboardMetrics();
  }

  onFeatureSelectAndReload(featureId: number | null): void {
    this.onFeatureSelect(featureId);
    this.loadDashboardMetrics();
  }

  onSprintSelectAndReload(sprint: number | null): void {
    this.onSprintSelect(sprint);
    this.loadDashboardMetrics();
  }

  hasActiveFilters(): boolean {
    return this.selectedProjectId != null || this.selectedFeatureId != null || this.selectedSprint != null;
  }

  activeFilterCount(): number {
    let count = 0;
    if (this.selectedProjectId != null) count++;
    if (this.selectedFeatureId != null) count++;
    if (this.selectedSprint != null) count++;
    return count;
  }

  getProjectName(projectId: number | null | undefined): string {
    if (!projectId) return 'All Projects';
    const proj = this.allProjects.find((p) => p.projectId === projectId);
    return proj ? proj.projectName : `Project #${projectId}`;
  }

  getFeatureName(featureId: number | null | undefined): string {
    if (!featureId) return '—';
    const feat = this.allFeatures.find((f) => f.featureId === featureId);
    return feat ? feat.featureName : `Feature #${featureId}`;
  }

  toggleFiltersDropdown(): void {
    this.showFiltersDropdown = !this.showFiltersDropdown;
    if (this.showFiltersDropdown) {
      this.showProfileMenu = false;
    }
  }

  /**
   * Main dashboard metric loader:
   * 1. Unfiltered: Queries global /testcase without featureId across all projects.
   * 2. Single Feature: Queries /testcase?featureId={id}.
   * 3. Project / Sprint: Finds matching features and queries accurately.
   */
  loadDashboardMetrics(): void {
    this.isLoading = true;
    this.hasError = false;
    this.errorMessage = '';
    this.loadQaDefectSeverityMetrics();
    this.loadTesterPerformanceMetrics();
    this.loadDeveloperPerformanceMetrics();
    this.loadDefectManagementData();

    if (this.qaTestDrilldown?.active) {
      this.loadQaTestCasesDrilldown(this.qaTestDrilldown.statusKey);
    }

    // Case A: No filter selected (Default All Test Cases)
    if (this.selectedProjectId == null && this.selectedFeatureId == null && this.selectedSprint == null) {
      this.fetchGlobalMetrics();
      return;
    }

    // Case B: Explicit single Feature selected
    if (this.selectedFeatureId != null) {
      this.fetchFeatureMetrics(this.selectedFeatureId);
      return;
    }

    // Case C: Project and/or Sprint selected
    let matching = [...this.allFeatures];
    if (this.selectedProjectId != null) {
      matching = matching.filter((f) => f.projectId === this.selectedProjectId);
    }
    if (this.selectedSprint != null) {
      matching = matching.filter((f) => f.sprint === this.selectedSprint);
    }

    if (matching.length === 0) {
      this.setMetrics(0, 0, 0, 0, 0);
      this.isLoading = false;
      this.cdr.markForCheck();
      return;
    }

    if (matching.length === 1) {
      this.fetchFeatureMetrics(matching[0].featureId);
      return;
    }

    // Multiple matching features: aggregate counts
    this.fetchMultiFeatureMetrics(matching.map((f) => f.featureId));
  }

  /**
   * Fetches metrics across ALL test cases in the database
   */
  private fetchGlobalMetrics(): void {
    const total$ = this.http.get<any>(`${this.apiBase}/testcase?page=0&size=1`).pipe(
      catchError((err) => {
        console.warn('Total test cases fetch warning:', err);
        return of({ data: { totalElements: 0 } });
      })
    );

    const passed$ = this.http.get<any>(`${this.apiBase}/testcase?status=PASSED&page=0&size=1`).pipe(
      catchError((err) => {
        console.warn('Passed test cases fetch warning:', err);
        return of({ data: { totalElements: 0 } });
      })
    );

    const failed$ = this.http.get<any>(`${this.apiBase}/testcase?status=FAILED&page=0&size=1`).pipe(
      catchError((err) => {
        console.warn('Failed test cases fetch warning:', err);
        return of({ data: { totalElements: 0 } });
      })
    );

    const blocked$ = this.http.get<any>(`${this.apiBase}/testcase?status=BLOCKED&page=0&size=1`).pipe(
      catchError((err) => {
        console.warn('Blocked test cases fetch warning:', err);
        return of({ data: { totalElements: 0 } });
      })
    );

    const notExecuted$ = this.http.get<any>(`${this.apiBase}/testcase?status=NO_RUN&page=0&size=1`).pipe(
      catchError((err) => {
        console.warn('Not Executed test cases fetch warning:', err);
        return of({ data: { totalElements: 0 } });
      })
    );

    forkJoin({
      total: total$,
      passed: passed$,
      failed: failed$,
      blocked: blocked$,
      notExecuted: notExecuted$
    }).subscribe({
      next: (results) => {
        const total = this.extractCount(results.total);
        const passed = this.extractCount(results.passed);
        const failed = this.extractCount(results.failed);
        const blocked = this.extractCount(results.blocked);
        const notExecuted = this.extractCount(results.notExecuted);

        this.setMetrics(total, passed, failed, blocked, notExecuted);
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load global metrics:', err);
        this.isLoading = false;
        this.hasError = true;
        this.errorMessage = 'Unable to connect to backend test case service.';
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Fetches metrics for a specific single featureId
   */
  private fetchFeatureMetrics(featureId: number): void {
    const total$ = this.http.get<any>(`${this.apiBase}/testcase?featureId=${featureId}&page=0&size=1`).pipe(
      catchError(() => of({ data: { totalElements: 0 } }))
    );
    const passed$ = this.http.get<any>(`${this.apiBase}/testcase?featureId=${featureId}&status=PASSED&page=0&size=1`).pipe(
      catchError(() => of({ data: { totalElements: 0 } }))
    );
    const failed$ = this.http.get<any>(`${this.apiBase}/testcase?featureId=${featureId}&status=FAILED&page=0&size=1`).pipe(
      catchError(() => of({ data: { totalElements: 0 } }))
    );
    const blocked$ = this.http.get<any>(`${this.apiBase}/testcase?featureId=${featureId}&status=BLOCKED&page=0&size=1`).pipe(
      catchError(() => of({ data: { totalElements: 0 } }))
    );
    const notExecuted$ = this.http.get<any>(`${this.apiBase}/testcase?featureId=${featureId}&status=NO_RUN&page=0&size=1`).pipe(
      catchError(() => of({ data: { totalElements: 0 } }))
    );

    forkJoin({
      total: total$,
      passed: passed$,
      failed: failed$,
      blocked: blocked$,
      notExecuted: notExecuted$
    }).subscribe({
      next: (results) => {
        const total = this.extractCount(results.total);
        const passed = this.extractCount(results.passed);
        const failed = this.extractCount(results.failed);
        const blocked = this.extractCount(results.blocked);
        const notExecuted = this.extractCount(results.notExecuted);

        this.setMetrics(total, passed, failed, blocked, notExecuted);
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load feature metrics:', err);
        this.isLoading = false;
        this.hasError = true;
        this.errorMessage = 'Unable to load test metrics for the selected feature.';
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Aggregates metrics across multiple featureIds
   */
  private fetchMultiFeatureMetrics(featureIds: number[]): void {
    const queries = featureIds.map((fid) =>
      forkJoin({
        total: this.http.get<any>(`${this.apiBase}/testcase?featureId=${fid}&page=0&size=1`).pipe(catchError(() => of({ data: { totalElements: 0 } }))),
        passed: this.http.get<any>(`${this.apiBase}/testcase?featureId=${fid}&status=PASSED&page=0&size=1`).pipe(catchError(() => of({ data: { totalElements: 0 } }))),
        failed: this.http.get<any>(`${this.apiBase}/testcase?featureId=${fid}&status=FAILED&page=0&size=1`).pipe(catchError(() => of({ data: { totalElements: 0 } }))),
        blocked: this.http.get<any>(`${this.apiBase}/testcase?featureId=${fid}&status=BLOCKED&page=0&size=1`).pipe(catchError(() => of({ data: { totalElements: 0 } }))),
        notExecuted: this.http.get<any>(`${this.apiBase}/testcase?featureId=${fid}&status=NO_RUN&page=0&size=1`).pipe(catchError(() => of({ data: { totalElements: 0 } })))
      })
    );

    forkJoin(queries).subscribe({
      next: (resultsList) => {
        let total = 0;
        let passed = 0;
        let failed = 0;
        let blocked = 0;
        let notExecuted = 0;

        for (const res of resultsList) {
          total += this.extractCount(res.total);
          passed += this.extractCount(res.passed);
          failed += this.extractCount(res.failed);
          blocked += this.extractCount(res.blocked);
          notExecuted += this.extractCount(res.notExecuted);
        }

        this.setMetrics(total, passed, failed, blocked, notExecuted);
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to aggregate multi-feature metrics:', err);
        this.isLoading = false;
        this.hasError = true;
        this.errorMessage = 'Unable to aggregate metrics for the selected filters.';
        this.cdr.markForCheck();
      }
    });
  }

  /**
   * Assign counts and compute percentages safely
   */
  private setMetrics(total: number, passed: number, failed: number, blocked: number, notExecuted: number): void {
    this.totalCount = total;
    this.passedCount = passed;
    this.failedCount = failed;
    this.blockedCount = blocked;
    this.notExecutedCount = notExecuted;

    if (total > 0) {
      this.passedPercent = Math.round((passed / total) * 100);
      this.failedPercent = Math.round((failed / total) * 100);
      this.blockedPercent = Math.round((blocked / total) * 100);
      this.notExecutedPercent = Math.round((notExecuted / total) * 100);
    } else {
      this.passedPercent = 0;
      this.failedPercent = 0;
      this.blockedPercent = 0;
      this.notExecutedPercent = 0;
    }

    this.qaTestExecutionSegments = this.computeDonutSegments([
      { value: this.passedCount, color: '#22a06b', key: 'PASSED' },
      { value: this.failedCount, color: '#de350b', key: 'FAILED' },
      { value: this.blockedCount, color: '#8993a4', key: 'BLOCKED' },
      { value: this.notExecutedCount, color: '#388bff', key: 'NO_RUN' }
    ]);
  }

  private extractCount(res: any): number {
    if (!res) return 0;
    if (typeof res?.data?.totalElements === 'number') {
      return res.data.totalElements;
    }
    if (typeof res?.totalElements === 'number') {
      return res.totalElements;
    }
    if (Array.isArray(res?.data?.content)) {
      return res.data.content.length;
    }
    if (Array.isArray(res?.data)) {
      return res.data.length;
    }
    return 0;
  }

  onSelectSidebarItem(item: SidebarNavItem): void {
    if (item.route) {
      this.activeSidebarItem = item.label;
      this.router.navigate([item.route]);
    }
  }

  // =========================================================================
  // 1. User Management (Platform Approvals, User Directory, Health)
  // =========================================================================
  activeUsers: any[] = [];
  pendingUsers: any[] = [];
  isSystemLoading = false;
  systemError = '';
  userSearchTerm = '';
  userRoleFilter = 'ALL';
  pendingRoleSelections: { [userId: number]: string } = {};
  systemTab: 'pending' | 'active' | 'overview' = 'pending';

  loadSystemDashboardData(): void {
    this.isSystemLoading = true;
    this.systemError = '';

    forkJoin({
      active: this.http.get<any>(`${this.apiBase}/manager/users/active`).pipe(
        catchError(err => {
          console.warn('Failed to load active users:', err);
          return of({ data: [] });
        })
      ),
      pending: this.http.get<any>(`${this.apiBase}/manager/users/pending`).pipe(
        catchError(err => {
          console.warn('Failed to load pending users:', err);
          return of({ data: [] });
        })
      )
    }).subscribe({
      next: (res) => {
        const rawActive = res.active?.data || res.active || [];
        const rawPending = res.pending?.data || res.pending || [];
        const activeList = Array.isArray(rawActive) ? rawActive : [];
        // Only list Tester, Developer, and Manager (exclude Admin)
        this.activeUsers = activeList.filter(u => {
          const roleStr = typeof u.role === 'object'
            ? (u.role?.role || u.role?.roleName || '')
            : (u.role || '');
          const roleUpper = roleStr.toUpperCase();
          return !roleUpper.includes('ADMIN') && (roleUpper.includes('TESTER') || roleUpper.includes('DEVELOPER') || roleUpper.includes('DEV') || roleUpper.includes('MANAGER'));
        });
        this.pendingUsers = Array.isArray(rawPending) ? rawPending : [];

        // Pre-fill default role for pending users (default to TESTER)
        this.pendingUsers.forEach(u => {
          if (!this.pendingRoleSelections[u.userId]) {
            this.pendingRoleSelections[u.userId] = 'TESTER';
          }
        });

        this.isSystemLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load User Management users:', err);
        this.systemError = 'Unable to retrieve user directory or pending approvals.';
        this.isSystemLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  approvePendingUser(user: any): void {
    const role = this.pendingRoleSelections[user.userId] || 'TESTER';
    this.http.put<any>(`${this.apiBase}/manager/userConfirmation`, {
      userId: user.userId,
      role: role
    }).subscribe({
      next: () => {
        this.showToast(`User ${user.fullName || user.username} approved as ${role} successfully.`);
        this.loadSystemDashboardData();
      },
      error: (err) => {
        console.error('Failed to confirm user:', err);
        this.showToast(err?.error?.message || 'Failed to approve user registration.');
      }
    });
  }

  rejectPendingUser(user: any): void {
    if (!confirm(`Are you sure you want to reject registration for ${user.fullName || user.username}?`)) {
      return;
    }
    this.http.delete<any>(`${this.apiBase}/manager/userRejection/${user.userId}`).subscribe({
      next: () => {
        this.showToast(`User ${user.fullName || user.username} registration rejected.`);
        this.loadSystemDashboardData();
      },
      error: (err) => {
        console.error('Failed to reject user:', err);
        this.showToast(err?.error?.message || 'Failed to reject user registration.');
      }
    });
  }

  isCurrentLoggedInManager(u: any): boolean {
    if (!u) return false;
    const current = this.currentUser();
    const currentId = current?.userId != null ? String(current.userId) : null;
    const currentUsername = (current?.username || '').toLowerCase().trim();
    const currentEmail = (current?.email || this.userEmail || '').toLowerCase().trim();
    const currentFullName = (current?.fullName || this.userName || '').toLowerCase().trim();

    if (currentId && u.userId != null && String(u.userId) === currentId) {
      return true;
    }
    if (currentUsername && u.username && u.username.toLowerCase().trim() === currentUsername) {
      return true;
    }
    if (currentEmail && u.email && u.email.toLowerCase().trim() === currentEmail) {
      return true;
    }
    if (currentFullName && u.fullName && u.fullName.toLowerCase().trim() === currentFullName) {
      const uRole = typeof u.role === 'object' ? (u.role?.role || u.role?.roleName || '') : (u.role || '');
      if (uRole.toUpperCase().includes('MANAGER')) {
        return true;
      }
    }
    return false;
  }

  get filteredActiveUsers(): any[] {
    const s = this.userSearchTerm.toLowerCase().trim();
    const filtered = this.activeUsers.filter(u => {
      const roleStr = typeof u.role === 'object'
        ? (u.role?.role || u.role?.roleName || '')
        : (u.role || '');
      const matchRole = this.userRoleFilter === 'ALL' || roleStr.toUpperCase().includes(this.userRoleFilter);
      const matchSearch = !s ||
        (u.username && u.username.toLowerCase().includes(s)) ||
        (u.fullName && u.fullName.toLowerCase().includes(s)) ||
        (u.email && u.email.toLowerCase().includes(s));
      return matchRole && matchSearch;
    });

    const managerIndex = filtered.findIndex(u => this.isCurrentLoggedInManager(u));
    if (managerIndex > 0) {
      const managerUser = filtered[managerIndex];
      return [managerUser, ...filtered.slice(0, managerIndex), ...filtered.slice(managerIndex + 1)];
    }

    return filtered;
  }

  // =========================================================================
  // 2. Project Management (Directory, Module Structure, Regional Status)
  // =========================================================================
  projectsList: Project[] = [];
  isProjectsLoading = false;
  projectsError = '';
  projectSearchTerm = '';
  projectStatusFilter = 'ALL';
  projectRegionFilter = 'ALL';

  loadProjectDashboardData(): void {
    this.isProjectsLoading = true;
    this.projectsError = '';
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        const rawProjs = projects || [];
        this.projectsList = this.isManager
          ? rawProjs
          : rawProjs.filter(p => (p.status || 'ACTIVE').toUpperCase() === 'ACTIVE' && p.isActive !== false);
        this.isProjectsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load projects:', err);
        this.projectsError = 'Unable to retrieve projects from server.';
        this.isProjectsLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  showStatusConfirmModal = false;
  projectForStatusChange: Project | null = null;
  targetProjectStatus: 'ACTIVE' | 'INACTIVE' = 'ACTIVE';
  isUpdatingStatus = false;
  statusUpdateError = '';

  openStatusConfirmModal(project: Project): void {
    if (!this.isManager) return;
    if (this.isUpdatingStatus) return;
    this.projectForStatusChange = project;
    const current = (project.status || 'ACTIVE').toUpperCase();
    this.targetProjectStatus = current === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    this.statusUpdateError = '';
    this.showStatusConfirmModal = true;
    this.cdr.markForCheck();
  }

  cancelStatusConfirm(): void {
    if (this.isUpdatingStatus) return;
    this.showStatusConfirmModal = false;
    this.projectForStatusChange = null;
    this.statusUpdateError = '';
    this.cdr.markForCheck();
  }

  confirmStatusChange(): void {
    if (!this.isManager) return;
    if (!this.projectForStatusChange || this.isUpdatingStatus) return;

    const project = this.projectForStatusChange;
    const newStatus = this.targetProjectStatus;
    this.isUpdatingStatus = true;
    this.statusUpdateError = '';

    const patchReq: ProjectPatchRequest = {
      status: newStatus
    };

    this.projectService.patchProject(project.projectId, patchReq).subscribe({
      next: () => {
        // Update the status badge only after the API confirms success
        project.status = newStatus;
        project.isActive = newStatus === 'ACTIVE';
        this.showToast(`Project "${project.projectName}" status updated to ${newStatus}.`);
        this.isUpdatingStatus = false;
        this.showStatusConfirmModal = false;
        this.projectForStatusChange = null;
        this.loadProjectDashboardData();
        this.cdr.markForCheck();
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to change project status:', err);
        this.isUpdatingStatus = false;
        const msg = err?.error?.message || err?.message || 'Failed to update project status. Please try again.';
        this.statusUpdateError = msg;
        this.showToast(msg);
        this.cdr.markForCheck();
      }
    });
  }

  toggleProjectStatus(project: Project): void {
    this.openStatusConfirmModal(project);
  }

  showProjectDeleteModal = false;
  projectToDelete: Project | null = null;
  isDeletingProject = false;
  projectDeleteError = '';

  openDeleteProjectModal(project: Project): void {
    if (!this.isManager) return;
    if (this.isDeletingProject) return;
    this.projectToDelete = project;
    this.projectDeleteError = '';
    this.showProjectDeleteModal = true;
    this.cdr.markForCheck();
  }

  cancelProjectDelete(): void {
    if (this.isDeletingProject) return;
    this.showProjectDeleteModal = false;
    this.projectToDelete = null;
    this.projectDeleteError = '';
    this.cdr.markForCheck();
  }

  confirmProjectDelete(): void {
    if (!this.isManager) return;
    if (!this.projectToDelete || this.isDeletingProject) return;

    const project = this.projectToDelete;
    this.isDeletingProject = true;
    this.projectDeleteError = '';

    this.projectService.softDeleteProject(project.projectId).subscribe({
      next: () => {
        this.showToast(`Project "${project.projectName}" has been deleted successfully.`);
        this.isDeletingProject = false;
        this.showProjectDeleteModal = false;
        this.projectToDelete = null;
        this.loadProjectDashboardData();
        this.cdr.markForCheck();
      },
      error: (err: HttpErrorResponse) => {
        console.error('Failed to delete project:', err);
        this.isDeletingProject = false;
        const msg = err?.error?.message || err?.message || 'Failed to delete project. Please try again.';
        this.projectDeleteError = msg;
        this.showToast(msg);
        this.cdr.markForCheck();
      }
    });
  }









  get filteredProjectsList(): Project[] {
    const s = this.projectSearchTerm.toLowerCase().trim();
    return this.projectsList.filter(p => {
      if (!this.isManager && ((p.status || 'ACTIVE').toUpperCase() !== 'ACTIVE' || p.isActive === false)) {
        return false;
      }
      const matchStatus = this.projectStatusFilter === 'ALL' || (p.status || 'ACTIVE').toUpperCase() === this.projectStatusFilter;
      const matchRegion = this.projectRegionFilter === 'ALL' || (p.region || 'DEFAULT').toUpperCase() === this.projectRegionFilter;
      const matchSearch = !s ||
        p.projectName.toLowerCase().includes(s) ||
        (p.module && p.module.toLowerCase().includes(s)) ||
        (p.subModule && p.subModule.toLowerCase().includes(s)) ||
        (p.region && p.region.toLowerCase().includes(s)) ||
        (p.comments && p.comments.toLowerCase().includes(s));
      return matchStatus && matchRegion && matchSearch;
    });
  }

  get projectRegionsList(): string[] {
    const regions = new Set<string>();
    this.projectsList.forEach(p => {
      if (p.region && p.region.trim()) {
        regions.add(p.region.trim());
      }
    });
    return Array.from(regions);
  }

  get activeProjectsCount(): number {
    return this.projectsList.filter(p => p.status === 'ACTIVE').length;
  }

  get inactiveProjectsCount(): number {
    return this.projectsList.filter(p => p.status === 'INACTIVE').length;
  }

  // =========================================================================
  // 4. TEST EXECUTION (Live Test Cases, Status Transitions, Outcomes)
  // =========================================================================
  testCasesList: TestCase[] = [];
  isTestCasesLoading = false;
  testCasesError = '';
  tcSearchTerm = '';
  tcStatusFilter = 'ALL';
  tcPriorityFilter = 'ALL';
  tcFeatureFilter: number | null = null;
  tcPage = 0;
  tcSize = 15;
  tcTotalElements = 0;
  tcTotalPages = 1;

  loadTestExecutionData(): void {
    this.isTestCasesLoading = true;
    this.testCasesError = '';

    let url = `${this.apiBase}/testcase?page=${this.tcPage}&size=${this.tcSize}`;
    if (this.tcFeatureFilter) {
      url += `&featureId=${this.tcFeatureFilter}`;
    }
    if (this.tcStatusFilter !== 'ALL') {
      url += `&status=${this.tcStatusFilter}`;
    }
    if (this.tcPriorityFilter !== 'ALL') {
      url += `&priority=${this.tcPriorityFilter}`;
    }

    this.http.get<any>(url).subscribe({
      next: (res) => {
        const pageData = res?.data || res;
        const rawList = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        this.tcTotalElements = pageData?.totalElements || rawList.length;
        this.tcTotalPages = Math.max(1, pageData?.totalPages || 1);

        this.testCasesList = rawList.map((item: any) => {
          const tc = item.testCase || item;
          const ex = item.execution || item.testingExecution || {};
          return {
            testcaseId: tc.testcaseId || item.testcaseId || tc.id || item.id,
            testcaseFormatId: tc.testcaseFormatId || item.testcaseFormatId || `TC_${tc.testcaseId || tc.id}`,
            featureId: tc.featureId || item.featureId,
            title: tc.title || item.title || '',
            testType: tc.testType || item.testType || 'UI',
            testPriority: tc.testPriority || item.testPriority || 'MEDIUM',
            testcaseStatus: tc.testcaseStatus || item.testcaseStatus || 'PASSED',
            executionStatus: ex.executionStatus || item.executionStatus || tc.testcaseStatus || 'UNEXECUTED',
            comments: tc.comments || ex.comments || item.comments || ''
          } as TestCase;
        });

        this.isTestCasesLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load test cases:', err);
        this.testCasesError = 'Unable to load test cases for execution.';
        this.isTestCasesLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  quickExecute(tc: TestCase, status: 'PASS' | 'FAIL' | 'DESCOPE'): void {
    this.testCaseService.executeTestCase(tc.testcaseId, status).subscribe({
      next: () => {
        tc.executionStatus = status;
        tc.testcaseStatus = status === 'PASS' ? 'PASSED' : (status === 'FAIL' ? 'FAILED' : 'DESCOPE');
        this.showToast(`Test Case ${tc.testcaseFormatId} marked as ${status}.`);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to execute test case:', err);
        this.showToast(err?.error?.message || 'Failed to record execution result.');
      }
    });
  }

  onTcFilterChange(): void {
    this.tcPage = 0;
    this.loadTestExecutionData();
  }

  nextTcPage(): void {
    if (this.tcPage + 1 < this.tcTotalPages) {
      this.tcPage++;
      this.loadTestExecutionData();
    }
  }

  prevTcPage(): void {
    if (this.tcPage > 0) {
      this.tcPage--;
      this.loadTestExecutionData();
    }
  }

  get filteredTestCases(): TestCase[] {
    const s = this.tcSearchTerm.toLowerCase().trim();
    if (!s) return this.testCasesList;
    return this.testCasesList.filter(tc =>
      (tc.testcaseFormatId && tc.testcaseFormatId.toLowerCase().includes(s)) ||
      (tc.title && tc.title.toLowerCase().includes(s)) ||
      (tc.testType && tc.testType.toLowerCase().includes(s)) ||
      (tc.comments && tc.comments.toLowerCase().includes(s))
    );
  }

  // =========================================================================
  // 5. DEFECT MANAGEMENT (Bug Triage, Severity, Assignee Allocation)
  // =========================================================================
  bugsList: Bug[] = [];
  developersList: DeveloperUser[] = [];
  isBugsLoading = false;
  bugsError = '';
  bugSearchTerm = '';
  bugSeverityFilter = 'ALL';
  bugStatusFilter = 'ALL';

  loadDefectManagementData(): void {
    this.isBugsLoading = true;
    this.bugsError = '';

    forkJoin({
      bugs: this.bugService.getAllBugs().pipe(catchError(() => of([]))),
      devs: this.bugService.getDevelopers().pipe(catchError(() => of([])))
    }).subscribe({
      next: (res) => {
        this.bugsList = res.bugs;
        this.developersList = res.devs;
        this.isBugsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load defects:', err);
        this.bugsError = 'Unable to retrieve defect list from server.';
        this.isBugsLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  onUpdateBugStatus(bug: Bug, newStatus: string): void {
    this.bugService.patchBugStatus(bug.bugId, newStatus).subscribe({
      next: () => {
        bug.status = newStatus;
        this.showToast(`Bug ${bug.bugFormatId} status set to ${newStatus}.`);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to update bug status:', err);
        this.showToast('Failed to update bug status.');
      }
    });
  }

  onUpdateBugAssignee(bug: Bug, devUserId: string): void {
    const devId = Number(devUserId);
    if (!devId) return;
    this.bugService.patchBugAssignee(bug.bugId, devId).subscribe({
      next: () => {
        const found = this.developersList.find(d => d.userId === devId);
        bug.assignedTo = found?.fullName || found?.username || `User #${devId}`;
        this.showToast(`Bug ${bug.bugFormatId} assigned to ${bug.assignedTo}.`);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to assign bug:', err);
        this.showToast('Failed to assign developer.');
      }
    });
  }

  get filteredBugsList(): Bug[] {
    const s = this.bugSearchTerm.toLowerCase().trim();
    return this.bugsList.filter(b => {
      const matchSeverity = this.bugSeverityFilter === 'ALL' || (b.severity || 'HIGH').toUpperCase() === this.bugSeverityFilter;
      const matchStatus = this.bugStatusFilter === 'ALL' || (b.status || 'OPEN').toUpperCase() === this.bugStatusFilter;
      const matchSearch = !s ||
        (b.bugFormatId && b.bugFormatId.toLowerCase().includes(s)) ||
        (b.title && b.title.toLowerCase().includes(s)) ||
        (b.assignedTo && b.assignedTo.toLowerCase().includes(s));
      return matchSeverity && matchStatus && matchSearch;
    });
  }

  get criticalBugsCount(): number {
    return this.bugsList.filter(b => b.severity === 'CRITICAL' || b.severity === 'HIGH').length;
  }

  get openBugsCount(): number {
    return this.bugsList.filter(b => b.status === 'OPEN').length;
  }

  // =========================================================================
  // SPLIT DASHBOARD: RECENT DEFECTS / GETALLBUGS WIDGET
  // =========================================================================
  recentBugsSearch = '';
  recentBugsSeverityFilter = 'ALL';
  recentBugsStatusFilter = 'ALL';
  recentBugsPage = 0;
  recentBugsPageSize = 5;

  get recentBugsFiltered(): Bug[] {
    const s = (this.recentBugsSearch || '').toLowerCase().trim();
    const sorted = [...this.bugsList].sort((a, b) => (b.bugId || 0) - (a.bugId || 0));
    return sorted.filter(b => {
      const bSev = (b.severity || 'HIGH').toString().toUpperCase();
      const bStat = (b.status || 'OPEN').toString().toUpperCase();
      const matchSeverity = this.recentBugsSeverityFilter === 'ALL' || bSev === this.recentBugsSeverityFilter;
      const matchStatus = this.recentBugsStatusFilter === 'ALL' || bStat === this.recentBugsStatusFilter;
      const matchSearch = !s ||
        (b.bugFormatId && b.bugFormatId.toLowerCase().includes(s)) ||
        (b.title && b.title.toLowerCase().includes(s)) ||
        (b.featureName && b.featureName.toLowerCase().includes(s)) ||
        (b.assignedTo && b.assignedTo.toLowerCase().includes(s)) ||
        (b.reportedBy && b.reportedBy.toLowerCase().includes(s));
      return matchSeverity && matchStatus && matchSearch;
    });
  }

  get paginatedRecentBugs(): Bug[] {
    const start = this.recentBugsPage * this.recentBugsPageSize;
    return this.recentBugsFiltered.slice(start, start + this.recentBugsPageSize);
  }

  get recentBugsTotalPages(): number {
    return Math.max(1, Math.ceil(this.recentBugsFiltered.length / this.recentBugsPageSize));
  }

  get recentBugsCriticalCount(): number {
    return this.bugsList.filter(b => {
      const s = (b.severity || '').toString().toUpperCase();
      return s === 'CRITICAL' || s === 'HIGH';
    }).length;
  }

  get recentBugsOpenCount(): number {
    return this.bugsList.filter(b => {
      const s = (b.status || '').toString().toUpperCase();
      return s === 'OPEN' || s === 'IN_PROGRESS';
    }).length;
  }

  prevRecentBugsPage(): void {
    if (this.recentBugsPage > 0) {
      this.recentBugsPage--;
      this.cdr.markForCheck();
    }
  }

  nextRecentBugsPage(): void {
    if (this.recentBugsPage < this.recentBugsTotalPages - 1) {
      this.recentBugsPage++;
      this.cdr.markForCheck();
    }
  }

  // =========================================================================
  // 6. AUTOMATION REPORTS (Coverage, Feasibility, Summary Export)
  // =========================================================================
  automationFeatures: any[] = [];
  isReportsLoading = false;
  reportsError = '';
  reportSelectedProjectId: number | null = null;

  loadAutomationReportsData(): void {
    this.isReportsLoading = true;
    this.reportsError = '';

    this.http.get<any>(`${this.apiBase}/feature`).subscribe({
      next: (res) => {
        const raw = res?.data || res || [];
        const features = Array.isArray(raw) ? raw : [];
        this.automationFeatures = features.map((f: any) => {
          const feat = f.feature || f;
          const proj = f.project || {};
          return {
            featureId: feat.featureId || feat.id,
            featureName: feat.featureName || `Feature #${feat.featureId}`,
            sprint: feat.sprint != null ? feat.sprint : 1,
            projectId: proj.projectId || feat.projectId || 0,
            projectName: proj.projectName || ''
          };
        });
        this.isReportsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load automation features:', err);
        this.reportsError = 'Unable to load feature data for automation reports.';
        this.isReportsLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  downloadFeatureSummaryReport(featureId: number, featureName: string): void {
    this.showToast(`Generating report for ${featureName}...`);
    this.http.get(`${this.apiBase}/feature/${featureId}/export`, { responseType: 'blob' }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `QA_Summary_Feature_${featureName.replace(/\s+/g, '_')}_${featureId}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.showToast(`Download complete: ${featureName} Summary Report.`);
      },
      error: (err) => {
        console.error('Failed to export feature report:', err);
        this.showToast('Failed to export feature report.');
      }
    });
  }

  downloadFeatureTemplate(featureId: number): void {
    this.showToast('Downloading testcase Excel template...');
    this.testCaseService.downloadTemplate(featureId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `testcase_template_feature_${featureId}.xlsx`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.showToast('Template downloaded.');
      },
      error: () => {
        this.showToast('Failed to download testcase template.');
      }
    });
  }

  get filteredAutomationFeatures(): any[] {
    if (!this.reportSelectedProjectId) {
      return this.automationFeatures;
    }
    return this.automationFeatures.filter(f => f.projectId === this.reportSelectedProjectId);
  }

  // =========================================================================
  // QA DASHBOARD: DEFECTS BY SEVERITY & CHART SEGMENTS
  // =========================================================================
  loadQaDefectSeverityMetrics(): void {
    this.isQaDefectsLoading = true;
    this.cdr.markForCheck();

    let scopedFeatureIds: number[] | null = null;
    if (this.selectedFeatureId != null) {
      scopedFeatureIds = [this.selectedFeatureId];
    } else if (this.selectedProjectId != null || this.selectedSprint != null) {
      let matching = [...this.allFeatures];
      if (this.selectedProjectId != null) {
        matching = matching.filter(f => f.projectId === this.selectedProjectId);
      }
      if (this.selectedSprint != null) {
        matching = matching.filter(f => f.sprint === this.selectedSprint);
      }
      scopedFeatureIds = matching.map(f => f.featureId);
    }

    this.http.get<any>(`${this.apiBase}/bug?page=0&size=500`).pipe(
      catchError(err => {
        console.warn('Failed to load defects for QA Dashboard chart:', err);
        return of({ data: { content: [] } });
      }),
      map(res => {
        const pageData = res?.data || res;
        return pageData?.content || (Array.isArray(pageData) ? pageData : []);
      })
    ).subscribe({
      next: (bugs) => {
        let crit = 0, high = 0, med = 0, low = 0;
        const scopedList: Bug[] = [];

        for (const item of bugs) {
          const b = item.bug || item;
          const status = (b.status || '').toUpperCase();
          const isOpen = status === 'OPEN' || status === 'IN_PROGRESS' || status === 'REOPENED' ||
            (status !== 'CLOSED' && status !== 'RESOLVED' && status !== 'FIXED' && status !== 'NOT_A_BUG' && status !== 'DEFERRED');

          if (!isOpen) continue;

          if (scopedFeatureIds !== null) {
            const fid = b.featureId || (b.testCase ? b.testCase.featureId : null);
            if (!fid || !scopedFeatureIds.includes(fid)) {
              continue;
            }
          }

          const sev = (b.severity || b.priority || 'MEDIUM').toUpperCase();
          if (sev === 'CRITICAL' || sev === 'BLOCKER') {
            crit++;
          } else if (sev === 'HIGH') {
            high++;
          } else if (sev === 'MEDIUM') {
            med++;
          } else if (sev === 'LOW' || sev === 'TRIVIAL') {
            low++;
          } else {
            med++;
          }

          scopedList.push({
            bugId: b.bugId || item.bugId || b.id || item.id,
            bugFormatId: b.bugFormatId || item.bugFormatId || `BUG_${b.bugId || item.bugId}`,
            testcaseId: b.testcaseId || (b.testCase ? b.testCase.testcaseId : null),
            featureId: b.featureId || (b.testCase ? b.testCase.featureId : null),
            title: b.title || item.title || '',
            description: b.description || item.description || '',
            severity: sev,
            status: status || 'OPEN',
            assignedTo: b.assignedTo || (b.assignedUser ? (b.assignedUser.fullName || b.assignedUser.username) : '') || 'Unassigned',
            createdAt: b.createdAt || item.createdAt || ''
          } as Bug);
        }

        this.qaScopedOpenBugs = scopedList;
        this.qaCriticalCount = crit;
        this.qaHighCount = high;
        this.qaMediumCount = med;
        this.qaLowCount = low;
        this.qaOpenDefectsCount = crit + high + med + low;

        if (this.qaOpenDefectsCount > 0) {
          this.qaCriticalPercent = Math.round((crit / this.qaOpenDefectsCount) * 100);
          this.qaHighPercent = Math.round((high / this.qaOpenDefectsCount) * 100);
          this.qaMediumPercent = Math.round((med / this.qaOpenDefectsCount) * 100);
          this.qaLowPercent = Math.round((low / this.qaOpenDefectsCount) * 100);
        } else {
          this.qaCriticalPercent = 0;
          this.qaHighPercent = 0;
          this.qaMediumPercent = 0;
          this.qaLowPercent = 0;
        }

        this.qaDefectsSegments = this.computeDonutSegments([
          { value: this.qaCriticalCount, color: '#de350b', key: 'CRITICAL' },
          { value: this.qaHighCount, color: '#e86328', key: 'HIGH' },
          { value: this.qaMediumCount, color: '#f5a623', key: 'MEDIUM' },
          { value: this.qaLowCount, color: '#388bff', key: 'LOW' }
        ]);

        if (this.qaDefectDrilldown?.active) {
          this.applyDefectSeverityFilter(this.qaDefectDrilldown.statusKey);
        }

        this.isQaDefectsLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isQaDefectsLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  computeDonutSegments(slices: Array<{ value: number; color: string; key?: string }>): Array<{ color: string; dasharray: string; dashoffset: number; key?: string }> {
    const total = slices.reduce((sum, s) => sum + (s.value > 0 ? s.value : 0), 0);
    if (total <= 0) {
      return [];
    }

    const r = 52;
    const circumference = 2 * Math.PI * r; // ~326.726
    const nonZeroCount = slices.filter(s => s.value > 0).length;
    const gap = nonZeroCount > 1 ? 2.5 : 0;

    let currentOffset = 0;
    const segments: Array<{ color: string; dasharray: string; dashoffset: number; key?: string }> = [];

    for (const slice of slices) {
      if (slice.value <= 0) {
        continue;
      }
      const fraction = slice.value / total;
      const length = fraction * circumference;
      const visibleLength = Math.max(0.1, length - gap);

      segments.push({
        color: slice.color,
        dasharray: `${visibleLength.toFixed(2)} ${(circumference - visibleLength).toFixed(2)}`,
        dashoffset: -currentOffset,
        key: slice.key
      });

      currentOffset += length;
    }

    return segments;
  }


  // =========================================================================
  // QA DASHBOARD DRILLDOWN INTERACTIONS & DETAILS TABLE
  // =========================================================================
  selectTestExecutionStatus(status: string): void {
    if (this.qaTestDrilldown?.active && this.qaTestDrilldown.statusKey === status) {
      this.closeQaTestDrilldown();
      return;
    }
    this.closeQaDefectDrilldown();

    const badgeMap: Record<string, { title: string; badgeClass: string; badgeLabel: string }> = {
      PASSED: { title: 'Test Execution Details', badgeClass: 'badge-passed', badgeLabel: 'Passed' },
      FAILED: { title: 'Test Execution Details', badgeClass: 'badge-failed', badgeLabel: 'Failed' },
      BLOCKED: { title: 'Test Execution Details', badgeClass: 'badge-blocked', badgeLabel: 'Blocked' },
      NO_RUN: { title: 'Test Execution Details', badgeClass: 'badge-norun', badgeLabel: 'Not Executed' }
    };

    const config = badgeMap[status] || { title: 'Test Execution Details', badgeClass: 'badge-neutral', badgeLabel: status };
    this.qaTestDrilldown = {
      active: true,
      statusKey: status,
      title: config.title,
      badgeClass: config.badgeClass,
      badgeLabel: config.badgeLabel
    };
    this.qaTestSearchTerm = '';
    this.qaTestFilterFeature = '';
    this.qaTestFilterType = '';
    this.qaTestFilterPriority = '';
    this.qaTestPage = 0;
    this.loadQaTestCasesDrilldown(status);
  }

  closeQaTestDrilldown(): void {
    this.qaTestDrilldown = null;
    this.qaDrilldownTestCases = [];
    this.qaTestSearchTerm = '';
    this.qaTestFilterFeature = '';
    this.qaTestFilterType = '';
    this.qaTestFilterPriority = '';
    this.qaTestPage = 0;
    this.cdr.markForCheck();
  }

  refreshQaTestDrilldown(): void {
    if (this.qaTestDrilldown?.active) {
      this.loadQaTestCasesDrilldown(this.qaTestDrilldown.statusKey);
    }
  }

  selectDefectSeverity(severity: string): void {
    if (this.qaDefectDrilldown?.active && this.qaDefectDrilldown.statusKey === severity) {
      this.closeQaDefectDrilldown();
      return;
    }
    this.closeQaTestDrilldown();

    const badgeMap: Record<string, { title: string; badgeClass: string; badgeLabel: string }> = {
      CRITICAL: { title: 'Open Defects Breakdown', badgeClass: 'sev-critical', badgeLabel: 'Critical' },
      HIGH: { title: 'Open Defects Breakdown', badgeClass: 'sev-high', badgeLabel: 'High' },
      MEDIUM: { title: 'Open Defects Breakdown', badgeClass: 'sev-medium', badgeLabel: 'Medium' },
      LOW: { title: 'Open Defects Breakdown', badgeClass: 'sev-low', badgeLabel: 'Low' }
    };

    const config = badgeMap[severity] || { title: 'Open Defects Breakdown', badgeClass: 'badge-neutral', badgeLabel: severity };
    this.qaDefectDrilldown = {
      active: true,
      statusKey: severity,
      title: config.title,
      badgeClass: config.badgeClass,
      badgeLabel: config.badgeLabel
    };
    this.qaDefectSearchTerm = '';
    this.qaDefectFilterSeverity = '';
    this.qaDefectFilterStatus = '';
    this.qaDefectPage = 0;
    this.applyDefectSeverityFilter(severity);
  }

  closeQaDefectDrilldown(): void {
    this.qaDefectDrilldown = null;
    this.qaDrilldownBugs = [];
    this.qaDefectSearchTerm = '';
    this.qaDefectFilterSeverity = '';
    this.qaDefectFilterStatus = '';
    this.qaDefectPage = 0;
    this.cdr.markForCheck();
  }

  refreshQaDefectDrilldown(): void {
    if (this.qaDefectDrilldown?.active) {
      this.loadQaDefectSeverityMetrics();
    }
  }

  private applyDefectSeverityFilter(severity: string): void {
    this.qaDrilldownBugs = this.qaScopedOpenBugs.filter(b => {
      const s = (b.severity || 'MEDIUM').toUpperCase();
      if (severity === 'CRITICAL') return s === 'CRITICAL' || s === 'BLOCKER';
      if (severity === 'HIGH') return s === 'HIGH';
      if (severity === 'MEDIUM') return s === 'MEDIUM';
      if (severity === 'LOW') return s === 'LOW' || s === 'TRIVIAL';
      return s === severity;
    });
    this.cdr.markForCheck();
  }

  loadQaTestCasesDrilldown(status: string): void {
    this.isQaTestLoading = true;
    this.qaTestError = '';
    this.qaDrilldownTestCases = [];
    this.cdr.markForCheck();

    if (this.selectedFeatureId != null) {
      this.fetchTestCasesByUrl(`${this.apiBase}/testcase?featureId=${this.selectedFeatureId}&status=${status}&page=0&size=100`);
      return;
    }

    if (this.selectedProjectId != null || this.selectedSprint != null) {
      let matching = [...this.allFeatures];
      if (this.selectedProjectId != null) {
        matching = matching.filter(f => f.projectId === this.selectedProjectId);
      }
      if (this.selectedSprint != null) {
        matching = matching.filter(f => f.sprint === this.selectedSprint);
      }

      if (matching.length === 0) {
        this.qaDrilldownTestCases = [];
        this.isQaTestLoading = false;
        this.cdr.markForCheck();
        return;
      }

      if (matching.length === 1) {
        this.fetchTestCasesByUrl(`${this.apiBase}/testcase?featureId=${matching[0].featureId}&status=${status}&page=0&size=100`);
        return;
      }

      const queries = matching.map(f =>
        this.http.get<any>(`${this.apiBase}/testcase?featureId=${f.featureId}&status=${status}&page=0&size=50`).pipe(
          catchError(() => of({ data: { content: [] } }))
        )
      );

      forkJoin(queries).subscribe({
        next: (responses) => {
          const allList: TestCase[] = [];
          for (const res of responses) {
            const pageData = res?.data || res;
            const items = pageData?.content || (Array.isArray(pageData) ? pageData : []);
            for (const item of items) {
              allList.push(this.mapToTestCase(item));
            }
          }
          this.qaDrilldownTestCases = allList;
          this.isQaTestLoading = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Failed to load multi-feature test cases for drilldown:', err);
          this.qaTestError = 'Unable to load test cases for selected filters.';
          this.isQaTestLoading = false;
          this.cdr.markForCheck();
        }
      });
      return;
    }

    this.fetchTestCasesByUrl(`${this.apiBase}/testcase?status=${status}&page=0&size=100`);
  }

  private fetchTestCasesByUrl(url: string): void {
    this.http.get<any>(url).pipe(
      catchError(err => {
        console.warn('Failed to fetch test cases for drilldown:', err);
        return of({ data: { content: [] } });
      })
    ).subscribe({
      next: (res) => {
        const pageData = res?.data || res;
        const items = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        this.qaDrilldownTestCases = items.map((item: any) => this.mapToTestCase(item));
        this.isQaTestLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.qaTestError = 'Failed to load test cases.';
        this.isQaTestLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  private mapToTestCase(item: any): TestCase {
    const tc = item.testCase || item;
    const ex = item.execution || item.testingExecution || {};
    const val = item.validations || {};
    const auto = item.automation || {};
    const bugSum = item.bugSummary || {};
    const audit = item.audit || {};
    const dynamic = item.dynamicFields || {};

    const tcId = tc.testcaseId || item.testcaseId || tc.id || item.id;
    const tcFormat = tc.testcaseFormatId || item.testcaseFormatId || (tcId ? `TC_${tcId}` : 'TC');

    return {
      testcaseId: tcId,
      testcaseFormatId: tcFormat,
      featureId: tc.featureId || item.featureId,
      title: tc.title || item.title || '',
      testType: tc.testType || item.testType || 'UI',
      testPriority: tc.testPriority || item.testPriority || 'MEDIUM',
      testcaseStatus: tc.testcaseStatus || item.testcaseStatus || 'PASSED',
      executionStatus: ex.executionStatus || item.executionStatus || tc.testcaseStatus || 'UNEXECUTED',
      comments: tc.comments || ex.comments || item.comments || '',
      testData: ex.testData || item.testData || '',
      automationFeasibility: auto.automationFeasibility || item.automationFeasibility || '',
      bugsCount: bugSum.bugsCount ?? item.bugsCount ?? 0,
      createdBy: audit.createdBy || item.createdBy || tc.createdBy || '',
      updatedBy: audit.updatedBy || item.updatedBy || tc.updatedBy || '',
      assignee: dynamic.assignedTo || dynamic.assignee || audit.createdBy || item.createdBy || tc.createdBy || '',
      execution: {
        executionId: ex.executionId || item.executionId,
        executionNumber: ex.executionNumber || 1,
        precondition: ex.precondition || item.precondition || '',
        executionSteps: ex.executionSteps || item.executionSteps || '',
        uiValidations: val.uiValidations || ex.uiValidations || item.uiValidations || '',
        dbValidations: val.dbValidations || ex.dbValidations || item.dbValidations || '',
        comments: ex.comments || tc.comments || item.comments || ''
      }
    } as TestCase;
  }

  // =========================================================================
  // QA DRILLDOWN DYNAMIC FILTER OPTIONS & GETTERS
  // =========================================================================
  get qaTestCaseFeatureOptions(): Array<{ id: number; name: string }> {
    const map = new Map<number, string>();
    for (const tc of this.qaDrilldownTestCases) {
      if (tc.featureId != null && !map.has(tc.featureId)) {
        map.set(tc.featureId, this.getFeatureName(tc.featureId));
      }
    }
    return Array.from(map.entries())
      .map(([id, name]) => ({ id, name }))
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  get qaTestCaseTypeOptions(): string[] {
    const set = new Set<string>();
    for (const tc of this.qaDrilldownTestCases) {
      if (tc.testType && tc.testType.trim()) {
        set.add(tc.testType.trim().toUpperCase());
      }
    }
    return Array.from(set).sort();
  }

  get qaTestCasePriorityOptions(): string[] {
    const set = new Set<string>();
    for (const tc of this.qaDrilldownTestCases) {
      if (tc.testPriority && tc.testPriority.trim()) {
        set.add(tc.testPriority.trim().toUpperCase());
      }
    }
    const priorityOrder = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
    return Array.from(set).sort((a, b) => {
      const ia = priorityOrder.indexOf(a);
      const ib = priorityOrder.indexOf(b);
      if (ia !== -1 && ib !== -1) return ia - ib;
      if (ia !== -1) return -1;
      if (ib !== -1) return 1;
      return a.localeCompare(b);
    });
  }

  hasActiveQaTestFilters(): boolean {
    return (this.qaTestFilterFeature !== '' && this.qaTestFilterFeature != null) ||
      !!this.qaTestFilterType ||
      !!this.qaTestFilterPriority;
  }

  resetQaTestFilters(): void {
    this.qaTestFilterFeature = '';
    this.qaTestFilterType = '';
    this.qaTestFilterPriority = '';
    this.qaTestPage = 0;
    this.cdr.markForCheck();
  }

  get filteredQaTestCases(): TestCase[] {
    let result = this.qaDrilldownTestCases;

    if (this.qaTestFilterFeature !== '' && this.qaTestFilterFeature != null) {
      result = result.filter(tc => String(tc.featureId) === String(this.qaTestFilterFeature));
    }

    if (this.qaTestFilterType) {
      const type = this.qaTestFilterType.toUpperCase();
      result = result.filter(tc => (tc.testType || '').trim().toUpperCase() === type);
    }

    if (this.qaTestFilterPriority) {
      const prio = this.qaTestFilterPriority.toUpperCase();
      result = result.filter(tc => (tc.testPriority || '').trim().toUpperCase() === prio);
    }

    return result;
  }

  get pagedQaTestCases(): TestCase[] {
    const start = this.qaTestPage * this.qaTestPageSize;
    return this.filteredQaTestCases.slice(start, start + this.qaTestPageSize);
  }

  get qaTestTotalPages(): number {
    return Math.ceil(this.filteredQaTestCases.length / this.qaTestPageSize) || 1;
  }

  get qaDefectSeverityOptions(): string[] {
    const set = new Set<string>();
    for (const b of this.qaDrilldownBugs) {
      if (b.severity && b.severity.trim()) {
        set.add(b.severity.trim().toUpperCase());
      }
    }
    const order = ['CRITICAL', 'BLOCKER', 'HIGH', 'MEDIUM', 'LOW', 'TRIVIAL'];
    return Array.from(set).sort((a, b) => {
      const ia = order.indexOf(a);
      const ib = order.indexOf(b);
      if (ia !== -1 && ib !== -1) return ia - ib;
      if (ia !== -1) return -1;
      if (ib !== -1) return 1;
      return a.localeCompare(b);
    });
  }

  get qaDefectStatusOptions(): string[] {
    const set = new Set<string>();
    for (const b of this.qaDrilldownBugs) {
      if (b.status && b.status.trim()) {
        set.add(b.status.trim().toUpperCase());
      }
    }
    return Array.from(set).sort();
  }

  hasActiveQaDefectFilters(): boolean {
    return !!this.qaDefectFilterSeverity || !!this.qaDefectFilterStatus;
  }

  resetQaDefectFilters(): void {
    this.qaDefectFilterSeverity = '';
    this.qaDefectFilterStatus = '';
    this.qaDefectPage = 0;
    this.cdr.markForCheck();
  }

  get filteredQaBugs(): Bug[] {
    let result = this.qaDrilldownBugs;

    if (this.qaDefectFilterSeverity) {
      const sev = this.qaDefectFilterSeverity.toUpperCase();
      result = result.filter(b => {
        const s = (b.severity || '').trim().toUpperCase();
        if (sev === 'CRITICAL') return s === 'CRITICAL' || s === 'BLOCKER';
        if (sev === 'LOW') return s === 'LOW' || s === 'TRIVIAL';
        return s === sev;
      });
    }

    if (this.qaDefectFilterStatus) {
      const stat = this.qaDefectFilterStatus.toUpperCase();
      result = result.filter(b => (b.status || '').trim().toUpperCase() === stat);
    }

    return result;
  }

  get pagedQaBugs(): Bug[] {
    const start = this.qaDefectPage * this.qaDefectPageSize;
    return this.filteredQaBugs.slice(start, start + this.qaDefectPageSize);
  }

  get qaDefectTotalPages(): number {
    return Math.ceil(this.filteredQaBugs.length / this.qaDefectPageSize) || 1;
  }

  min(a: number, b: number): number {
    return Math.min(a, b);
  }

  get activeFilterDescription(): string {
    const parts: string[] = [];
    if (this.selectedProjectId != null) {
      parts.push(this.getProjectName(this.selectedProjectId));
    }
    if (this.selectedFeatureId != null) {
      parts.push(this.getFeatureName(this.selectedFeatureId));
    }
    if (this.selectedSprint != null) {
      parts.push(`Sprint ${this.selectedSprint}`);
    }
    return parts.length > 0 ? parts.join(' • ') : 'All Projects, Features & Sprints';
  }

  // =========================================================================
  // 7. CREATE DASHBOARD (Session Custom Dashboard Management)
  // =========================================================================
  showCreateDashboardModal = false;
  newDashboardName = '';
  newDashboardDescription = '';
  newDashboardProjectId: number | null = null;
  newDashboardWidgets = {
    metrics: true,
    defects: true,
    automation: true,
    burndown: true
  };

  customDashboards: Array<{
    id: string;
    name: string;
    description: string;
    projectId: number | null;
    createdAt: string;
    widgets: {
      metrics: boolean;
      defects: boolean;
      automation: boolean;
      burndown: boolean;
    };
  }> = [];

  openCreateDashboardModal(): void {
    this.showCreateDashboardModal = true;
    this.newDashboardName = '';
    this.newDashboardDescription = '';
    this.newDashboardProjectId = this.allProjects[0]?.projectId || null;
    this.newDashboardWidgets = {
      metrics: true,
      defects: true,
      automation: true,
      burndown: true
    };
    this.cdr.markForCheck();
  }

  closeCreateDashboardModal(): void {
    this.showCreateDashboardModal = false;
    this.cdr.markForCheck();
  }

  createCustomDashboard(): void {
    const name = this.newDashboardName.trim();
    if (!name) {
      this.showToast('Please enter a dashboard name.');
      return;
    }

    const newDash = {
      id: `custom-${Date.now()}`,
      name: name,
      description: this.newDashboardDescription.trim(),
      projectId: this.newDashboardProjectId,
      createdAt: new Date().toLocaleDateString(),
      widgets: { ...this.newDashboardWidgets }
    };

    this.customDashboards.push(newDash);
    this.showCreateDashboardModal = false;
    this.showToast(`Dashboard "${name}" created for this session. (Note: Persistent server-side storage is not currently supported by backend API).`);
    this.cdr.markForCheck();
  }

  deleteCustomDashboard(idx: number): void {
    if (idx >= 0 && idx < this.customDashboards.length) {
      const removed = this.customDashboards.splice(idx, 1);
      this.showToast(`Dashboard "${removed[0]?.name}" removed from session.`);
      this.cdr.markForCheck();
    }
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    if (this.toastTimer) {
      clearTimeout(this.toastTimer);
    }
    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
      this.cdr.markForCheck();
    }, 3500);
    this.cdr.markForCheck();
  }

  toggleProfileMenu(): void {
    this.showProfileMenu = !this.showProfileMenu;
    if (this.showProfileMenu) {
      this.showFiltersDropdown = false;
      this.showExportDropdown = false;
    }
  }

  toggleExportDropdown(): void {
    this.showExportDropdown = !this.showExportDropdown;
    if (this.showExportDropdown) {
      this.showFiltersDropdown = false;
      this.showProfileMenu = false;
    }
  }

  onRefresh(): void {
    this.loadFilterData();
    this.loadDashboardMetrics();
    this.showToast('Dashboard metrics refreshed with latest data.');
  }

  onShare(): void {
    if (typeof navigator !== 'undefined' && navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(window.location.href)
        .then(() => {
          this.showToast('Dashboard URL copied to clipboard!');
        })
        .catch(() => {
          this.fallbackCopyText(window.location.href);
        });
    } else {
      this.fallbackCopyText(window.location.href);
    }
  }

  private fallbackCopyText(text: string): void {
    try {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-9999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      const successful = document.execCommand('copy');
      document.body.removeChild(textArea);
      if (successful) {
        this.showToast('Dashboard URL copied to clipboard!');
      } else {
        this.showToast('Failed to copy dashboard URL.');
      }
    } catch {
      this.showToast('Unable to copy URL to clipboard.');
    }
  }

  exportSummary(format: 'csv' | 'json'): void {
    this.showExportDropdown = false;
    const filterInfo = {
      project: this.selectedProjectId ? this.getProjectName(this.selectedProjectId) : 'All Projects',
      feature: this.selectedFeatureId ? this.getFeatureName(this.selectedFeatureId) : 'All Features',
      sprint: this.selectedSprint != null ? `Sprint ${this.selectedSprint}` : 'All Sprints'
    };

    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');

    if (format === 'csv') {
      const csvLines = [
        'QA Manager Dashboard Summary Report',
        `Exported At,"${new Date().toLocaleString()}"`,
        `Filter Project,"${filterInfo.project}"`,
        `Filter Feature,"${filterInfo.feature}"`,
        `Filter Sprint,"${filterInfo.sprint}"`,
        '',
        'Metric,Count,Percentage',
        `Total Test Cases,${this.totalCount},100%`,
        `Passed,${this.passedCount},${this.passedPercent}%`,
        `Failed,${this.failedCount},${this.failedPercent}%`,
        `Blocked,${this.blockedCount},${this.blockedPercent}%`,
        `Not Executed,${this.notExecutedCount},${this.notExecutedPercent}%`
      ];

      const blob = new Blob([csvLines.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
      this.downloadFile(blob, `qa-dashboard-summary-${timestamp}.csv`);
      this.showToast('Summary exported as CSV successfully.');
    } else {
      const jsonData = {
        report: 'QA Manager Dashboard Summary',
        exportedAt: new Date().toISOString(),
        filters: filterInfo,
        summaryCards: {
          totalTestCases: this.totalCount,
          passed: { count: this.passedCount, percentage: this.passedPercent },
          failed: { count: this.failedCount, percentage: this.failedPercent },
          blocked: { count: this.blockedCount, percentage: this.blockedPercent },
          notExecuted: { count: this.notExecutedCount, percentage: this.notExecutedPercent }
        }
      };

      const blob = new Blob([JSON.stringify(jsonData, null, 2)], { type: 'application/json;charset=utf-8;' });
      this.downloadFile(blob, `qa-dashboard-summary-${timestamp}.json`);
      this.showToast('Summary exported as JSON successfully.');
    }
  }

  private downloadFile(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  // =========================================================================
  // TESTER PERFORMANCE ANALYSIS
  // =========================================================================
  private readonly testerAvatarColors = [
    '#0052cc', '#00875a', '#6554c0', '#ff5630', '#00a3bf',
    '#ff8b00', '#36b37e', '#403294', '#de350b', '#2684ff'
  ];

  getTesterAvatarColor(name: string): string {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const idx = Math.abs(hash) % this.testerAvatarColors.length;
    return this.testerAvatarColors[idx];
  }

  getInitialsForName(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/).filter((p) => p.length > 0);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return name.substring(0, Math.min(2, name.length)).toUpperCase();
  }

  mapStatusToCategory(testCaseStatus?: string, executionStatus?: string): 'TODO' | 'IN_PROGRESS' | 'DONE' {
    const ex = (executionStatus || '').toUpperCase().trim();
    const tc = (testCaseStatus || '').toUpperCase().trim();

    if (
      ex === 'PASS' ||
      ex === 'PASSED' ||
      tc === 'PASSED' ||
      ex === 'FAIL' ||
      ex === 'FAILED' ||
      tc === 'FAILED' ||
      ex === 'DESCOPE' ||
      tc === 'DESCOPE'
    ) {
      return 'DONE';
    }
    if (
      ex === 'IN_PROGRESS' ||
      tc === 'IN_PROGRESS' ||
      ex === 'BLOCK' ||
      ex === 'BLOCKED' ||
      tc === 'BLOCKED'
    ) {
      return 'IN_PROGRESS';
    }
    return 'TODO';
  }

  loadTesterPerformanceMetrics(): void {
    this.isTesterMetricsLoading = true;
    this.testerMetricsError = '';

    const users$ = this.http.get<any>(`${this.apiBase}/manager/users/active`).pipe(
  map((res) => ({
    ...res,
    data: (res?.data ?? []).filter(
      (user: any) => user.role === 'TESTER'
    )
  })),
  catchError((err) => {
    console.warn('Failed to load active testers for tester performance:', err);
    return of({ data: [] });
  })
);

    let testCases$: Observable<any[]>;

    if (this.selectedFeatureId != null) {
      testCases$ = this.http
        .get<any>(`${this.apiBase}/testcase?featureId=${this.selectedFeatureId}&page=0&size=200`)
        .pipe(
          map((res) => {
            const pageData = res?.data || res;
            return pageData?.content || (Array.isArray(pageData) ? pageData : []);
          }),
          catchError(() => of([]))
        );
    } else if (this.selectedProjectId != null || this.selectedSprint != null) {
      let matching = [...this.allFeatures];
      if (this.selectedProjectId != null) {
        matching = matching.filter((f) => f.projectId === this.selectedProjectId);
      }
      if (this.selectedSprint != null) {
        matching = matching.filter((f) => f.sprint === this.selectedSprint);
      }

      if (matching.length === 0) {
        testCases$ = of([]);
      } else {
        const featureQueries = matching.map((f) =>
          this.http
            .get<any>(`${this.apiBase}/testcase?featureId=${f.featureId}&page=0&size=200`)
            .pipe(
              map((res) => {
                const pageData = res?.data || res;
                return pageData?.content || (Array.isArray(pageData) ? pageData : []);
              }),
              catchError(() => of([]))
            )
        );
        testCases$ = forkJoin(featureQueries).pipe(
          map((results) => results.flat())
        );
      }
    } else {
      testCases$ = this.http.get<any>(`${this.apiBase}/testcase?page=0&size=200`).pipe(
        map((res) => {
          const pageData = res?.data || res;
          return pageData?.content || (Array.isArray(pageData) ? pageData : []);
        }),
        catchError(() => of([]))
      );
    }

    forkJoin({ users: users$, testCases: testCases$ }).subscribe({
      next: ({ users, testCases }) => {
        const rawUsers = users?.data || users || [];
        const userList: any[] = Array.isArray(rawUsers) ? rawUsers : [];
        const testerUsers = userList.filter((u) => {
          const roleStr = typeof u.role === 'object' ? (u.role?.role || u.role?.roleName || '') : (u.role || '');
          return roleStr.toUpperCase().includes('TESTER');
        });

        const mappedTestCases: TestCase[] = testCases.map((item) => this.mapToTestCase(item));
        this.processTesterPerformanceData(testerUsers, mappedTestCases);
        this.isTesterMetricsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load tester performance data:', err);
        this.testerMetricsError = 'Unable to load tester performance data.';
        this.isTesterMetricsLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  private processTesterPerformanceData(testerUsers: any[], testCases: TestCase[]): void {
    const testerMap = new Map<string, TesterPerformance>();

    testerUsers.forEach((u) => {
      const username = (u.username || '').toLowerCase().trim();
      const displayName = (u.fullName || u.username || 'Tester').trim();
      const idKey = username || `user_${u.userId}`;
      testerMap.set(idKey, {
        id: idKey,
        name: displayName,
        username: u.username || '',
        email: u.email || '',
        initials: this.getInitialsForName(displayName),
        avatarColor: this.getTesterAvatarColor(displayName),
        isUnassigned: false,
        total: 0,
        todoCount: 0,
        inProgressCount: 0,
        doneCount: 0,
        todoPercent: 0,
        inProgressPercent: 0,
        donePercent: 0,
        testCases: []
      });
    });

    const unassignedTestCases: TestCase[] = [];

    testCases.forEach((tc) => {
      // Calculate tester performance using existing audit properties (createdBy / updatedBy)
      // Note: TestCase does not have an explicit assignee field; authoring/execution is tracked via audit fields
      const rawAssignee = (tc.createdBy || tc.updatedBy || '').trim();
      const rawLower = rawAssignee.toLowerCase();

      let matchedTester: TesterPerformance | undefined;
      if (rawLower) {
        if (testerMap.has(rawLower)) {
          matchedTester = testerMap.get(rawLower);
        } else {
          for (const t of testerMap.values()) {
            if (t.name.toLowerCase() === rawLower || t.username.toLowerCase() === rawLower) {
              matchedTester = t;
              break;
            }
          }
        }
      }

      if (matchedTester) {
        matchedTester.testCases.push(tc);
      } else if (rawAssignee && !rawLower.includes('admin')) {
        const newTester: TesterPerformance = {
          id: rawLower,
          name: rawAssignee,
          username: rawAssignee,
          initials: this.getInitialsForName(rawAssignee),
          avatarColor: this.getTesterAvatarColor(rawAssignee),
          isUnassigned: false,
          total: 0,
          todoCount: 0,
          inProgressCount: 0,
          doneCount: 0,
          todoPercent: 0,
          inProgressPercent: 0,
          donePercent: 0,
          testCases: [tc]
        };
        testerMap.set(rawLower, newTester);
      } else {
        unassignedTestCases.push(tc);
      }
    });

    const list: TesterPerformance[] = [];

    testerMap.forEach((tester) => {
      tester.total = tester.testCases.length;
      let todo = 0;
      let inProgress = 0;
      let done = 0;

      tester.testCases.forEach((tc) => {
        const cat = this.mapStatusToCategory(tc.testcaseStatus, tc.executionStatus);
        if (cat === 'DONE') done++;
        else if (cat === 'IN_PROGRESS') inProgress++;
        else todo++;
      });

      tester.todoCount = todo;
      tester.inProgressCount = inProgress;
      tester.doneCount = done;

      if (tester.total > 0) {
        tester.todoPercent = Math.round((todo / tester.total) * 100);
        tester.inProgressPercent = Math.round((inProgress / tester.total) * 100);
        tester.donePercent = Math.round((done / tester.total) * 100);
      } else {
        tester.todoPercent = 0;
        tester.inProgressPercent = 0;
        tester.donePercent = 0;
      }

      list.push(tester);
    });

    if (unassignedTestCases.length > 0) {
      let uTodo = 0;
      let uInProgress = 0;
      let uDone = 0;
      unassignedTestCases.forEach((tc) => {
        const cat = this.mapStatusToCategory(tc.testcaseStatus, tc.executionStatus);
        if (cat === 'DONE') uDone++;
        else if (cat === 'IN_PROGRESS') uInProgress++;
        else uTodo++;
      });
      const uTotal = unassignedTestCases.length;
      list.push({
        id: 'unassigned',
        name: 'Unassigned',
        username: 'unassigned',
        initials: 'UA',
        avatarColor: '#8993a4',
        isUnassigned: true,
        total: uTotal,
        todoCount: uTodo,
        inProgressCount: uInProgress,
        doneCount: uDone,
        todoPercent: uTotal > 0 ? Math.round((uTodo / uTotal) * 100) : 0,
        inProgressPercent: uTotal > 0 ? Math.round((uInProgress / uTotal) * 100) : 0,
        donePercent: uTotal > 0 ? Math.round((uDone / uTotal) * 100) : 0,
        testCases: unassignedTestCases
      });
    }

    list.sort((a, b) => {
      if (a.isUnassigned) return 1;
      if (b.isUnassigned) return -1;
      if (b.total !== a.total) return b.total - a.total;
      return a.name.localeCompare(b.name);
    });

    this.testerPerformanceList = list;

    if (this.selectedTester) {
      const updated = this.testerPerformanceList.find((t) => t.id === this.selectedTester!.id);
      this.selectedTester = updated || null;
    }
  }

  get testerSummaryActiveTesters(): number {
    return this.testerPerformanceList.filter((t) => !t.isUnassigned && t.total > 0).length;
  }

  get testerSummaryTotalAssigned(): number {
    return this.testerPerformanceList.reduce((sum, t) => sum + (t.isUnassigned ? 0 : t.total), 0);
  }

  get testerSummaryTotalInProgress(): number {
    return this.testerPerformanceList.reduce((sum, t) => sum + (t.isUnassigned ? 0 : t.inProgressCount), 0);
  }

  get testerSummaryTotalCompleted(): number {
    return this.testerPerformanceList.reduce((sum, t) => sum + (t.isUnassigned ? 0 : t.doneCount), 0);
  }

  get displayedTesters(): TesterPerformance[] {
    if (this.showAllTesters || this.testerPerformanceList.length <= this.initialTesterLimit) {
      return this.testerPerformanceList;
    }
    return this.testerPerformanceList.slice(0, this.initialTesterLimit);
  }

  selectTester(tester: TesterPerformance): void {
    if (this.selectedTester?.id === tester.id) {
      this.clearSelectedTester();
      return;
    }
    this.selectedTester = tester;
    this.testerDetailsSearchTerm = '';
    this.testerDetailsPage = 0;
    this.cdr.markForCheck();
  }

  clearSelectedTester(): void {
    this.selectedTester = null;
    this.testerDetailsSearchTerm = '';
    this.testerDetailsPage = 0;
    this.cdr.markForCheck();
  }

  get filteredTesterTestCases(): TestCase[] {
    if (!this.selectedTester) return [];
    const s = this.testerDetailsSearchTerm.toLowerCase().trim();
    if (!s) return this.selectedTester.testCases;
    return this.selectedTester.testCases.filter(
      (tc) =>
        (tc.title && tc.title.toLowerCase().includes(s)) ||
        (tc.testcaseFormatId && tc.testcaseFormatId.toLowerCase().includes(s)) ||
        (tc.testcaseStatus && tc.testcaseStatus.toLowerCase().includes(s)) ||
        (tc.executionStatus && tc.executionStatus.toLowerCase().includes(s)) ||
        (tc.testPriority && tc.testPriority.toLowerCase().includes(s))
    );
  }

  get paginatedTesterTestCases(): TestCase[] {
    const start = this.testerDetailsPage * this.testerDetailsPageSize;
    return this.filteredTesterTestCases.slice(start, start + this.testerDetailsPageSize);
  }

  get totalTesterDetailsPages(): number {
    return Math.ceil(this.filteredTesterTestCases.length / this.testerDetailsPageSize) || 1;
  }

  nextTesterDetailsPage(): void {
    if (this.testerDetailsPage < this.totalTesterDetailsPages - 1) {
      this.testerDetailsPage++;
    }
  }

  prevTesterDetailsPage(): void {
    if (this.testerDetailsPage > 0) {
      this.testerDetailsPage--;
    }
  }

  getSprintForFeature(featureId: number | null | undefined): string {
    if (!featureId) return '—';
    const feat = this.allFeatures.find((f) => f.featureId === featureId);
    return feat && feat.sprint != null ? `Sprint ${feat.sprint}` : '—';
  }

  onTesterProjectChange(projectId: number | null): void {
    this.onProjectSelect(projectId);
    this.loadDashboardMetrics();
  }

  onTesterFeatureChange(featureId: number | null): void {
    this.onFeatureSelect(featureId);
    this.loadDashboardMetrics();
  }

  // =========================================================================
  // DEVELOPER PERFORMANCE ANALYSIS
  // =========================================================================
  loadDeveloperPerformanceMetrics(): void {
    this.isDeveloperMetricsLoading = true;
    this.developerMetricsError = '';

    const activeUsers$ = this.http.get<any>(`${this.apiBase}/manager/users/active`).pipe(
      map((res) => {
        const raw = res?.data || res || [];
        const arr: any[] = Array.isArray(raw) ? raw : [];
        return arr.filter((u: any) => {
          const roleStr = typeof u.role === 'object' ? (u.role?.role || u.role?.roleName || '') : (u.role || '');
          return roleStr.toUpperCase().includes('DEV');
        });
      }),
      catchError(() => of([]))
    );

    const devs$ = this.bugService.getDevelopers().pipe(catchError(() => of([])));
    const bugs$ = this.bugService.getAllBugs().pipe(catchError(() => of([])));

    forkJoin({ activeUsers: activeUsers$, devs: devs$, bugs: bugs$ }).subscribe({
      next: ({ activeUsers, devs, bugs }) => {
        const devMap = new Map<string, { id: number | string; name: string; username: string; email?: string }>();

        activeUsers.forEach((u: any) => {
          const username = (u.username || '').trim();
          const name = (u.fullName || u.username || 'Developer').trim();
          const key = username.toLowerCase() || String(u.userId);
          devMap.set(key, { id: u.userId, name, username, email: u.email });
        });

        devs.forEach((d: any) => {
          const username = (d.username || '').trim();
          const name = (d.fullName || d.username || 'Developer').trim();
          const key = username.toLowerCase() || String(d.userId);
          if (!devMap.has(key)) {
            devMap.set(key, { id: d.userId, name, username, email: d.email });
          }
        });

        let scopedBugs = bugs;
        if (this.selectedFeatureId != null) {
          scopedBugs = bugs.filter((b: Bug) => b.featureId === this.selectedFeatureId);
        } else if (this.selectedProjectId != null) {
          scopedBugs = bugs.filter((b: Bug) => {
            if (b.projectName) {
              const proj = this.allProjects.find(p => p.projectId === this.selectedProjectId);
              return proj && b.projectName.toLowerCase() === proj.projectName.toLowerCase();
            }
            return true;
          });
        }

        this.processDeveloperPerformanceData(Array.from(devMap.values()), scopedBugs);
        this.isDeveloperMetricsLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load developer performance data:', err);
        this.developerMetricsError = 'Unable to load developer performance data.';
        this.isDeveloperMetricsLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  mapBugStatusToCategory(status?: string): 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' {
    const s = (status || '').toUpperCase().trim();
    if (s === 'RESOLVED' || s === 'CLOSED' || s === 'NOT_A_BUG' || s ==='DEFERED') {
      return 'RESOLVED';
    }
    if (s === 'IN_PROGRESS' || s === 'FIXED' ) {
      return 'IN_PROGRESS';
    }
    if( s=== 'OPEN' || s === 'REOPENED'){
      return 'OPEN';
    }
    return 'OPEN';
  }

  private processDeveloperPerformanceData(devUsers: any[], bugs: Bug[]): void {
    const devMap = new Map<string, DeveloperPerformance>();

    devUsers.forEach((u) => {
      const username = (u.username || '').toLowerCase().trim();
      const displayName = (u.name || u.fullName || u.username || 'Developer').trim();
      const idKey = username || `user_${u.id}`;
      devMap.set(idKey, {
        id: idKey,
        name: displayName,
        username: u.username || '',
        email: u.email || '',
        initials: this.getInitialsForName(displayName),
        avatarColor: this.getTesterAvatarColor(displayName),
        isUnassigned: false,
        total: 0,
        openCount: 0,
        inProgressCount: 0,
        resolvedCount: 0,
        openPercent: 0,
        inProgressPercent: 0,
        resolvedPercent: 0,
        bugs: []
      });
    });

    const unassignedBugs: Bug[] = [];

    bugs.forEach((bug) => {
      const rawAssignee = (bug.assignedTo || '').trim();
      const rawLower = rawAssignee.toLowerCase();
      const devId = bug.assignedToId ? `user_${bug.assignedToId}` : '';

      let matchedDev: DeveloperPerformance | undefined;
      if (devId && devMap.has(devId)) {
        matchedDev = devMap.get(devId);
      } else if (rawLower && devMap.has(rawLower)) {
        matchedDev = devMap.get(rawLower);
      } else if (rawLower) {
        for (const d of devMap.values()) {
          if (d.name.toLowerCase() === rawLower || d.username.toLowerCase() === rawLower) {
            matchedDev = d;
            break;
          }
        }
      }

      if (matchedDev) {
        matchedDev.bugs.push(bug);
      } else if (rawAssignee) {
        const newDev: DeveloperPerformance = {
          id: rawLower,
          name: rawAssignee,
          username: rawAssignee,
          initials: this.getInitialsForName(rawAssignee),
          avatarColor: this.getTesterAvatarColor(rawAssignee),
          isUnassigned: false,
          total: 0,
          openCount: 0,
          inProgressCount: 0,
          resolvedCount: 0,
          openPercent: 0,
          inProgressPercent: 0,
          resolvedPercent: 0,
          bugs: [bug]
        };
        devMap.set(rawLower, newDev);
      } else {
        unassignedBugs.push(bug);
      }
    });

    const list: DeveloperPerformance[] = [];

    devMap.forEach((dev) => {
      dev.total = dev.bugs.length;
      let open = 0;
      let inProgress = 0;
      let resolved = 0;

      dev.bugs.forEach((b) => {
        const cat = this.mapBugStatusToCategory(b.status);
        if (cat === 'RESOLVED') resolved++;
        else if (cat === 'IN_PROGRESS') inProgress++;
        else open++;
      });

      dev.openCount = open;
      dev.inProgressCount = inProgress;
      dev.resolvedCount = resolved;

      if (dev.total > 0) {
        dev.openPercent = Math.round((open / dev.total) * 100);
        dev.inProgressPercent = Math.round((inProgress / dev.total) * 100);
        dev.resolvedPercent = Math.round((resolved / dev.total) * 100);
      } else {
        dev.openPercent = 0;
        dev.inProgressPercent = 0;
        dev.resolvedPercent = 0;
      }

      list.push(dev);
    });

    if (unassignedBugs.length > 0) {
      let uOpen = 0;
      let uInProgress = 0;
      let uResolved = 0;
      unassignedBugs.forEach((b) => {
        const cat = this.mapBugStatusToCategory(b.status);
        if (cat === 'RESOLVED') uResolved++;
        else if (cat === 'IN_PROGRESS') uInProgress++;
        else uOpen++;
      });
      const uTotal = unassignedBugs.length;
      list.push({
        id: 'unassigned',
        name: 'Unassigned',
        username: 'unassigned',
        initials: 'UA',
        avatarColor: '#8993a4',
        isUnassigned: true,
        total: uTotal,
        openCount: uOpen,
        inProgressCount: uInProgress,
        resolvedCount: uResolved,
        openPercent: uTotal > 0 ? Math.round((uOpen / uTotal) * 100) : 0,
        inProgressPercent: uTotal > 0 ? Math.round((uInProgress / uTotal) * 100) : 0,
        resolvedPercent: uTotal > 0 ? Math.round((uResolved / uTotal) * 100) : 0,
        bugs: unassignedBugs
      });
    }

    list.sort((a, b) => {
      if (a.isUnassigned) return 1;
      if (b.isUnassigned) return -1;
      return b.total - a.total;
    });

    this.developerPerformanceList = list;
  }

  get developerSummaryActiveDevs(): number {
    return this.developerPerformanceList.filter((d) => !d.isUnassigned && d.total > 0).length;
  }

  get developerSummaryTotalAssigned(): number {
    return this.developerPerformanceList.reduce((sum, d) => sum + (d.isUnassigned ? 0 : d.total), 0);
  }

  get developerSummaryTotalInProgress(): number {
    return this.developerPerformanceList.reduce((sum, d) => sum + (d.isUnassigned ? 0 : d.inProgressCount), 0);
  }

  get developerSummaryTotalResolved(): number {
    return this.developerPerformanceList.reduce((sum, d) => sum + (d.isUnassigned ? 0 : d.resolvedCount), 0);
  }

  get displayedDevelopers(): DeveloperPerformance[] {
    if (this.showAllDevelopers || this.developerPerformanceList.length <= this.initialDeveloperLimit) {
      return this.developerPerformanceList;
    }
    return this.developerPerformanceList.slice(0, this.initialDeveloperLimit);
  }

  selectDeveloper(dev: DeveloperPerformance): void {
    if (this.selectedDeveloper?.id === dev.id) {
      this.clearSelectedDeveloper();
      return;
    }
    this.selectedDeveloper = dev;
    this.developerDetailsSearchTerm = '';
    this.developerDetailsPage = 0;
    this.cdr.markForCheck();
  }

  clearSelectedDeveloper(): void {
    this.selectedDeveloper = null;
    this.developerDetailsSearchTerm = '';
    this.developerDetailsPage = 0;
    this.cdr.markForCheck();
  }

  get filteredDeveloperBugs(): Bug[] {
    if (!this.selectedDeveloper) return [];
    const s = this.developerDetailsSearchTerm.toLowerCase().trim();
    if (!s) return this.selectedDeveloper.bugs;
    return this.selectedDeveloper.bugs.filter(
      (b) =>
        (b.title && b.title.toLowerCase().includes(s)) ||
        (b.bugFormatId && b.bugFormatId.toLowerCase().includes(s)) ||
        (b.status && b.status.toLowerCase().includes(s)) ||
        (b.severity && b.severity.toLowerCase().includes(s)) ||
        (b.featureName && b.featureName.toLowerCase().includes(s)) ||
        (b.reportedBy && b.reportedBy.toLowerCase().includes(s))
    );
  }

  get paginatedDeveloperBugs(): Bug[] {
    const start = this.developerDetailsPage * this.developerDetailsPageSize;
    return this.filteredDeveloperBugs.slice(start, start + this.developerDetailsPageSize);
  }

  get totalDeveloperDetailsPages(): number {
    return Math.ceil(this.filteredDeveloperBugs.length / this.developerDetailsPageSize) || 1;
  }

  nextDeveloperDetailsPage(): void {
    if (this.developerDetailsPage < this.totalDeveloperDetailsPages - 1) {
      this.developerDetailsPage++;
    }
  }

  prevDeveloperDetailsPage(): void {
    if (this.developerDetailsPage > 0) {
      this.developerDetailsPage--;
    }
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: () => {
        this.authService.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }
}
