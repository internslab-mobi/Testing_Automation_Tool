import { Component, OnInit, OnDestroy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, Subscription, forkJoin, of } from 'rxjs';
import { debounceTime, catchError } from 'rxjs/operators';

// Sub-components
import { NavbarComponent } from './components/navbar/navbar.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { BugChatModalComponent } from './components/bug-chat-modal/bug-chat-modal.component';
import { TestCaseDetailModalComponent } from './components/testcase-detail-modal/testcase-detail-modal.component';
import { DefectDetailModalComponent } from './components/defect-detail-modal/defect-detail-modal.component';
import { AttachmentsModalComponent } from './components/attachments-modal/attachments-modal.component';
import { NotificationsModalComponent } from './components/notifications-modal/notifications-modal.component';
import { ProjectDetailModalComponent } from './components/project-detail-modal/project-detail-modal.component';

// Services & Models
import { AuthService } from '../../core/services/authService';
import { ProjectService } from '../../core/services/project.service';
import { FeatureService } from '../../core/services/feature.service';
import { TestCaseService } from '../../core/services/testcase.service';
import { BugService, DeveloperUser } from '../../core/services/bug.service';
import { NotificationService } from '../../core/services/notification.service';
import {
  Project,
  Feature,
  TestCase,
  Bug,
  AttachmentItem,
  BugComment,
  InAppNotification,
  ProjectCreateRequest,
  ProjectPatchRequest,
  FeatureCreateRequest,
  FeaturePatchRequest,
  TestCaseExecutionRequest,
  TestCaseCreatePayload,
  TestCasePutRequest,
  TestCasePatchRequest,
  BugRequest,
  BugCreateRequest,
  BugSeverity,
  BugPriority,
  BugCategory,
  BugStatus,
  BugPutRequest,
  BugPatchRequest,
  ModuleItem,
  SubModuleItem,
  RegionItem
} from '../../model/workspace/workspace.model';

interface ToastAlert {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

@Component({
  selector: 'app-tester-workspace',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,
    SidebarComponent,
    ProjectDetailModalComponent,
    BugChatModalComponent,
    TestCaseDetailModalComponent,
    DefectDetailModalComponent,
    AttachmentsModalComponent,
    NotificationsModalComponent
  ],
  templateUrl: './tester-workspace.html',
  styleUrl: './tester-workspace.css'
})
export class TesterWorkspace implements OnInit, OnDestroy {
  readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly projectService = inject(ProjectService);
  private readonly featureService = inject(FeatureService);
  private readonly testCaseService = inject(TestCaseService);
  private readonly bugService = inject(BugService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);

  // User Profile
  isAuthenticated = false;
  userName = 'Tester';
  userRole = 'TESTER';
  userInitials = 'TS';

  get isManager(): boolean {
    const signalUser = this.authService.currentUser();
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

  navigateToManagerDashboard(): void {
    this.router.navigate(['/manager-dashboard']);
  }

  // Responsive Sidebar (open by default on desktop)
  isSidebarOpen = typeof window !== 'undefined' ? window.innerWidth > 1024 : true;

  // Projects & Dynamic Regions
  projects: Project[] = [];
  availableRegions: string[] = ['India', 'Singapore', 'Malaysia', 'Indonesia', 'Vietnam'];
  selectedRegion = 'India';
  activeProject: Project | null = null;
  projectAttachmentCount = 0;

  // DB Modules, SubModules, Regions for Project Creation & Editing
  dbModules: ModuleItem[] = [];
  dbSubModules: SubModuleItem[] = [];
  dbRegions: RegionItem[] = [];

  selectedModuleId: number | null = null;
  selectedSubModuleId: number | null = null;
  selectedRegionId: number | null = null;

  editSelectedModuleId: number | null = null;
  editSelectedSubModuleId: number | null = null;
  editSelectedRegionId: number | null = null;
  editSubModules: SubModuleItem[] = [];

  // Inline Creation Triggers & Inputs
  isAddingNewModule = false;
  newModuleNameInput = '';
  isAddingNewSubModule = false;
  newSubModuleNameInput = '';
  isAddingNewRegion = false;
  newRegionNameInput = '';

  // Inline Creation Triggers for Edit Project
  isAddingNewSubModuleEdit = false;
  newSubModuleNameInputEdit = '';

  // Features
  features: Feature[] = [];
  activeFeature: Feature | null = null;
  featureAttachmentCount = 0;
  isLoadingFeatures = true;

  // Active Workspace Sub-Tab ('testcases' | 'bugs')
  activeSubTab: 'testcases' | 'bugs' = 'testcases';

  // Global All-Entity Overview State (Active when no feature is selected or in All-Entities view)
  activeOverviewTab: 'projects' | 'features' | 'testcases' | 'bugs' = 'projects';
  allProjectsList: Project[] = [];
  allFeaturesList: (Feature & { projectName?: string; region?: string })[] = [];
  allTestCasesList: (TestCase & { featureName?: string; projectName?: string })[] = [];
  allBugsList: (Bug & { featureName?: string; projectName?: string })[] = [];
  isOverviewLoading = false;
  isOverviewMode = false;

  // Search & Filter for All-Entity Lists
  allProjSearch = '';
  allProjStatusFilter = 'ALL';
  allFeatSearch = '';
  allFeatProjFilter = 'ALL';
  allTcSearch = '';
  allTcTypeFilter = 'ALL';
  allTcPrioFilter = 'ALL';
  allTcStatusFilter = 'ALL';
  allBugSearch = '';
  allBugSevFilter = 'ALL';
  allBugStatusFilter = 'ALL';

  // Pagination for All-Entity Lists (Default size 5)
  readonly overviewPageSizeOptions = [5, 10, 20, 50, 100];
  overviewProjPage = 1;
  overviewProjPageSize = 5;
  overviewFeatPage = 1;
  overviewFeatPageSize = 5;
  overviewTcPage = 1;
  overviewTcPageSize = 5;
  overviewBugPage = 1;
  overviewBugPageSize = 5;

  get overviewProjStartIndex(): number {
    if (this.filteredAllProjects.length === 0) return 0;
    return (this.overviewProjPage - 1) * this.overviewProjPageSize + 1;
  }
  get overviewProjEndIndex(): number {
    return Math.min(this.overviewProjPage * this.overviewProjPageSize, this.filteredAllProjects.length);
  }

  get overviewFeatStartIndex(): number {
    if (this.filteredAllFeatures.length === 0) return 0;
    return (this.overviewFeatPage - 1) * this.overviewFeatPageSize + 1;
  }
  get overviewFeatEndIndex(): number {
    return Math.min(this.overviewFeatPage * this.overviewFeatPageSize, this.filteredAllFeatures.length);
  }

  get overviewTcStartIndex(): number {
    if (this.filteredAllTestCases.length === 0) return 0;
    return (this.overviewTcPage - 1) * this.overviewTcPageSize + 1;
  }
  get overviewTcEndIndex(): number {
    return Math.min(this.overviewTcPage * this.overviewTcPageSize, this.filteredAllTestCases.length);
  }

  get overviewBugStartIndex(): number {
    if (this.filteredAllBugs.length === 0) return 0;
    return (this.overviewBugPage - 1) * this.overviewBugPageSize + 1;
  }
  get overviewBugEndIndex(): number {
    return Math.min(this.overviewBugPage * this.overviewBugPageSize, this.filteredAllBugs.length);
  }

  // Test Cases
  testcases: TestCase[] = [];
  filteredTestCases: TestCase[] = [];
  tcSearch = '';
  tcFilterType = 'ALL';
  tcFilterPriority = 'ALL';
  tcFilterStatus = 'ALL';
  isLoadingTestCases = false;

  // Defects / Bugs
  bugs: Bug[] = [];
  filteredBugs: Bug[] = [];
  bugSearch = '';
  bugFilterSeverity = 'ALL';
  bugFilterStatus = 'ALL';
  developers: DeveloperUser[] = [];
  isLoadingBugs = false;

  // Pagination: Test Cases
  tcCurrentPage = 1;
  tcPageSize = 5;
  readonly tcPageSizeOptions = [5, 10, 20, 50, 100];
  tcTotalRecords = 0;
  private testCaseSub?: Subscription;
  private tcSearchSubject = new Subject<void>();
  private tcSearchSub?: Subscription;

  // Pagination: Defects
  bugCurrentPage = 1;
  bugPageSize = 5;
  readonly bugPageSizeOptions = [5, 10, 20, 50, 100];

  // Summary Metrics
  totalTestCasesCount = 0;
  passedCount = 0;
  failedCount = 0;
  descopeCount = 0;
  totalDefectsCount = 0;
  openDefectsCount = 0;

  // In-Memory Performance Cache (Instant sub-10ms Switching)
  private featureCache = new Map<number, Feature[]>();
  private testcaseCache = new Map<number, TestCase[]>();
  private bugCache = new Map<number, Bug[]>();

  // Modals Visibility
  isProjectDetailsModalOpen = false;
  isProjectCreateModalOpen = false;
  isProjectEditModalOpen = false;
  isFeatureCreateModalOpen = false;
  isFeatureEditModalOpen = false;
  isTestCaseCreateModalOpen = false;
  isExcelModalOpen = false;
  isBugCreateModalOpen = false;
  isTestCaseDetailModalOpen = false;
  testCaseStartInEdit = false;
  isDefectDetailModalOpen = false;
  isAttachmentsModalOpen = false;
  isChatModalOpen = false;
  isNotificationsModalOpen = false;
  isMoveTestCasesModalOpen = false;

  // Selected Test Cases for bulk operations (mapping to other feature, bulk delete)
  selectedTestCaseIds: Set<number> = new Set<number>();
  isMovingTestCases = false;
  targetMoveProjectId: number | null = null;
  targetMoveFeatureId: number | null = null;
  availableMoveFeatures: Feature[] = [];
  isLoadingMoveFeatures = false;
  moveErrorMessage = '';

  // Bulk Delete Test Cases Modal
  isBulkDeleteModalOpen = false;
  isBulkDeletingTestCases = false;
  bulkDeleteErrorMessage = '';

  // Selected Defects / Bugs for bulk operations (bulk delete)
  selectedBugIds: Set<number> = new Set<number>();
  isBulkDeleteBugsModalOpen = false;
  isBulkDeletingBugs = false;
  bulkDeleteBugsErrorMessage = '';

  // Modal State & Forms
  activeTestCaseDetail: TestCase | null = null;
  activeDefectDetail: Bug | null = null;
  activeChatBug: Bug | null = null;
  chatComments: BugComment[] = [];
  isChatLoading = false;

  // Attachments Modal State
  attachmentsModalTitle = 'Attachments';
  attachmentsModalSubtitle = '';
  attachmentsModalTargetType: 'project' | 'feature' | 'bug' = 'feature';
  attachmentsModalTargetId: number | null = null;
  attachmentsList: AttachmentItem[] = [];
  isUploadingAttachments = false;

  // In-App Notifications
  notifications: InAppNotification[] = [];
  unreadNotificationCount = 0;

  // Form Models
  newProjectForm: ProjectCreateRequest = {
    projectName: '',
    region: 'India',
    module: '',
    subModule: '',
    description: '',
    comments: '',
    status: 'ACTIVE'
  };

  editProjectForm: Partial<ProjectCreateRequest> = {};

  newFeatureForm: FeatureCreateRequest = {
    projectId: 0,
    featureName: '',
    sprint: 1,
    featureVersion: 'v1.0',
    description: '',
    comments: ''
  };

  editFeatureForm: Partial<FeatureCreateRequest> = {};

  newTcForm = {
    // TestCase fields
    testcaseFormatId: '',
    title: '',
    testType: 'UI',
    testPriority: 'MEDIUM',
    testcaseStatus: 'NO_RUN',
    comments: '',
    // TestingExecutionRequest fields
    automationFeasibility: 'YES',
    testExecution: '',
    testValidation: '',
    precondition: 'Verified environment state',
    testData: '',
    executionSteps: '',
    uiValidations: '',
    dbValidations: '',
    executionComments: ''
  };

  // Dynamic Key-Value Fields for TestCase creation
  dynamicFieldsList: Array<{ key: string; value: string }> = [];

  addDynamicField(): void {
    this.dynamicFieldsList.push({ key: '', value: '' });
    this.cdr.markForCheck();
  }

  removeDynamicField(index: number): void {
    this.dynamicFieldsList.splice(index, 1);
    this.cdr.markForCheck();
  }

  newBugForm: BugRequest = {
    title: '',
    description: '',
    severity: BugSeverity.HIGH,
    priority: BugPriority.HIGH,
    category: BugCategory.PRE_PRODUCTION,
    status: BugStatus.OPEN,
    comments: ''
  };
  newBugAssignedToUserId: number | null = null;
  bugOriginTcId = 0;
  bugOriginTcFormatId = '';
  bugMappedFormatId = '';

  // Dynamic Key-Value Fields for Bug creation
  bugDynamicFieldsList: Array<{ key: string; value: string }> = [];

  addBugDynamicField(): void {
    this.bugDynamicFieldsList.push({ key: '', value: '' });
    this.cdr.markForCheck();
  }

  removeBugDynamicField(index: number): void {
    this.bugDynamicFieldsList.splice(index, 1);
    this.cdr.markForCheck();
  }

  // Toast Alerts
  toasts: ToastAlert[] = [];
  private toastCounter = 0;

  ngOnInit(): void {
    this.initUserProfile();
    this.refreshAvailableRegions();
    this.ensureAuthSession(() => {
      this.loadModules();
      this.loadRegions();
      this.loadDevelopers();
      this.loadProjects();
      this.loadNotifications();
    });
    this.tcSearchSub = this.tcSearchSubject.pipe(
      debounceTime(300)
    ).subscribe(() => {
      this.tcCurrentPage = 1;
      this.fetchTestCasesPage();
    });
    this.warmupCsrf();
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    this.testCaseSub?.unsubscribe();
    this.tcSearchSub?.unsubscribe();
  }

  // ==========================================
  // INITIALIZATION & CSRF WARMUP (PREVENTS ACCESS DENIED FIRST CLICK)
  // ==========================================
  private initUserProfile(): void {
    const signalUser = this.authService.currentUser();
    if (signalUser) {
      this.isAuthenticated = true;
      this.userName = (signalUser.fullName || signalUser.username || 'User').trim();
      const rawRole = (signalUser.role || '').toUpperCase();
      this.userRole = rawRole.includes('MANAGER') ? 'Manager' : rawRole.includes('ADMIN') ? 'Admin' : 'TESTER';
      this.userInitials = this.computeInitials(this.userName);
      return;
    }
    if (typeof localStorage !== 'undefined') {
      this.userName = localStorage.getItem('tat_user_fullname') || localStorage.getItem('tat_user_name') || 'Tester';
      const storedRole = (localStorage.getItem('tat_user_role') || 'TESTER').toUpperCase();
      this.userRole = storedRole.includes('MANAGER') ? 'Manager' : storedRole.includes('ADMIN') ? 'Admin' : 'TESTER';
      this.userInitials = this.computeInitials(this.userName);
    }
  }

  private ensureAuthSession(onReady?: () => void): void {
    const cachedUser = this.authService.currentUser();
    if (cachedUser) {
      this.initUserProfile();
      if (onReady) onReady();
      return;
    }

    // Attempt to verify session using profile API (HttpOnly cookies sent via withCredentials)
    this.authService.getProfile().subscribe({
      next: (res) => {
        if (res?.data) {
          this.initUserProfile();
          if (onReady) onReady();
          this.cdr.markForCheck();
        } else {
          this.router.navigate(['/login']);
        }
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  private computeInitials(name: string): string {
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return (name.substring(0, 2) || 'QA').toUpperCase();
  }

  private warmupCsrf(): void {
    // A quick lightweight GET ensures Spring Security's DeferredCsrfToken is resolved
    // and Set-Cookie: XSRF-TOKEN is placed in document.cookie before user mutates any entity
    this.notificationService.getNotifications().subscribe({
      next: () => this.cdr.markForCheck(),
      error: () => { }
    });
  }

  loadModules(): void {
    this.projectService.getModules().subscribe({
      next: (mods) => {
        this.dbModules = mods && Array.isArray(mods) ? mods : [];
        if (this.dbModules.length > 0) {
          if (!this.selectedModuleId || !this.dbModules.some(m => m.moduleId === this.selectedModuleId)) {
            this.selectedModuleId = this.dbModules[0].moduleId;
          }
          this.loadSubModulesForModule(this.selectedModuleId);
        } else {
          this.selectedModuleId = null;
          this.dbSubModules = [];
          this.selectedSubModuleId = null;
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load modules from db:', err);
        this.dbModules = [];
        this.selectedModuleId = null;
        this.dbSubModules = [];
        this.selectedSubModuleId = null;
        this.cdr.markForCheck();
      }
    });
  }

  refreshAvailableRegions(): void {
    const regionSet = new Set<string>();

    // 1. Regions from DB
    if (this.dbRegions && this.dbRegions.length > 0) {
      for (const r of this.dbRegions) {
        const name = (r.countryName || (r as any).regionName || (r as any).name || '').trim();
        if (name && name.toUpperCase() !== 'ALL' && name.toUpperCase() !== 'GLOBAL') {
          regionSet.add(name);
        }
      }
    }

    // 2. Regions from active projects
    if (this.projects && this.projects.length > 0) {
      for (const p of this.projects) {
        const name = (p.region || '').trim();
        if (name && name.toUpperCase() !== 'DEFAULT' && name.toUpperCase() !== 'ALL' && name.toUpperCase() !== 'GLOBAL') {
          regionSet.add(name);
        }
      }
    }

    // 3. Defaults if still empty
    if (regionSet.size === 0) {
      ['India', 'Singapore', 'Malaysia', 'Indonesia', 'Vietnam'].forEach(r => regionSet.add(r));
    } else if (!Array.from(regionSet).some(r => r.toLowerCase() === 'india')) {
      regionSet.add('India');
    }

    this.availableRegions = Array.from(regionSet);

    // Ensure dbRegions has corresponding items so modals have region options
    if (this.dbRegions.length === 0) {
      this.dbRegions = this.availableRegions.map((name, idx) => ({
        countryId: idx + 1,
        countryName: name
      }));
    } else {
      const existing = new Set(this.dbRegions.map(d => (d.countryName || '').toLowerCase()));
      this.availableRegions.forEach((name, idx) => {
        if (!existing.has(name.toLowerCase())) {
          this.dbRegions.push({
            countryId: this.dbRegions.length + idx + 1,
            countryName: name
          });
        }
      });
    }

    // Sync selectedRegion to match casing with availableRegions
    if (this.selectedRegion !== 'ALL') {
      const matched = this.availableRegions.find(r => r.toLowerCase() === this.selectedRegion.toLowerCase());
      if (matched) {
        this.selectedRegion = matched;
      } else if (this.availableRegions.length > 0) {
        this.selectedRegion = this.availableRegions[0];
      }
    }

    // Pre-sync selectedRegionId for Create Project modal
    const activeDb = this.dbRegions.find(r => (r.countryName || '').toLowerCase() === this.selectedRegion.toLowerCase());
    if (activeDb) {
      this.selectedRegionId = activeDb.countryId;
    } else if (this.dbRegions.length > 0 && !this.selectedRegionId) {
      this.selectedRegionId = this.dbRegions[0].countryId;
    }

    this.cdr.markForCheck();
  }

  loadRegions(): void {
    this.projectService.getRegions().subscribe({
      next: (regs) => {
        if (regs && Array.isArray(regs) && regs.length > 0) {
          this.dbRegions = regs;
        }
        this.refreshAvailableRegions();

        // Auto-select project matching selected region
        if (this.projects && this.projects.length > 0 && !this.activeProject) {
          const matchProj = this.projects.find(p => (p.region || '').toLowerCase() === this.selectedRegion.toLowerCase()) || this.projects[0];
          this.selectProject(matchProj, false);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load regions from db:', err);
        this.refreshAvailableRegions();
        this.cdr.markForCheck();
      }
    });
  }

  loadSubModulesForModule(moduleId: number): void {
    if (!moduleId) {
      this.dbSubModules = [];
      this.selectedSubModuleId = null;
      this.cdr.markForCheck();
      return;
    }
    this.projectService.getSubModules(moduleId).subscribe({
      next: (subs) => {
        this.dbSubModules = subs && Array.isArray(subs) ? subs : [];
        if (this.dbSubModules.length > 0) {
          this.selectedSubModuleId = this.dbSubModules[0].subModuleId;
        } else {
          this.selectedSubModuleId = null;
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load sub-modules from db:', err);
        this.dbSubModules = [];
        this.selectedSubModuleId = null;
        this.cdr.markForCheck();
      }
    });
  }

  onModuleSelectChange(val: any): void {
    if (val === -1 || val === '__NEW__') {
      this.isAddingNewModule = true;
      this.newModuleNameInput = '';
      this.selectedModuleId = null;
      this.selectedSubModuleId = null;
      this.dbSubModules = [];
    } else {
      this.isAddingNewModule = false;
      const modId = Number(val);
      if (modId) {
        this.selectedModuleId = modId;
        this.selectedSubModuleId = null;
        this.loadSubModulesForModule(modId);
      } else {
        this.selectedModuleId = null;
        this.selectedSubModuleId = null;
        this.dbSubModules = [];
      }
    }
    this.cdr.markForCheck();
  }

  saveNewModuleInline(): void {
    const name = this.newModuleNameInput.trim();
    if (!name) {
      this.showToast('Please enter a module name', 'warning');
      return;
    }
    this.projectService.createModule(name).subscribe({
      next: (created) => {
        this.showToast(`Module "${created.moduleName}" created!`, 'success');
        this.dbModules.push(created);
        this.selectedModuleId = created.moduleId;
        this.isAddingNewModule = false;
        this.newModuleNameInput = '';
        this.loadSubModulesForModule(created.moduleId);
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.showToast(err?.error?.message || 'Failed to create module in database', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  onSubModuleSelectChange(val: any): void {
    if (val === -1 || val === '__NEW__') {
      if (!this.selectedModuleId) {
        this.showToast('Please select a module first', 'warning');
        return;
      }
      this.isAddingNewSubModule = true;
      this.newSubModuleNameInput = '';
      this.selectedSubModuleId = null;
    } else {
      this.isAddingNewSubModule = false;
      const smId = Number(val);
      this.selectedSubModuleId = smId ? smId : null;
    }
    this.cdr.markForCheck();
  }

  saveNewSubModuleInline(): void {
    if (!this.selectedModuleId) {
      this.showToast('Please select a module first', 'warning');
      return;
    }
    const name = this.newSubModuleNameInput.trim();
    if (!name) {
      this.showToast('Please enter a sub-module name', 'warning');
      return;
    }
    this.projectService.createSubModule(this.selectedModuleId, name).subscribe({
      next: (created) => {
        this.showToast(`Sub-Module "${created.subModuleName}" created!`, 'success');
        this.dbSubModules.push(created);
        this.selectedSubModuleId = created.subModuleId;
        this.isAddingNewSubModule = false;
        this.newSubModuleNameInput = '';
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.showToast(err?.error?.message || 'Failed to create sub-module in database', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  onRegionSelectChange(val: any): void {
    if (val === -1 || val === '__NEW__') {
      this.isAddingNewRegion = true;
      this.newRegionNameInput = '';
      this.selectedRegionId = null;
    } else {
      this.isAddingNewRegion = false;
      const regId = Number(val);
      this.selectedRegionId = regId ? regId : null;
    }
    this.cdr.markForCheck();
  }

  saveNewRegionInline(): void {
    const name = this.newRegionNameInput.trim();
    if (!name) {
      this.showToast('Please enter a region name', 'warning');
      return;
    }
    this.projectService.createRegion(name).subscribe({
      next: (created) => {
        this.showToast(`Region "${created.countryName}" created!`, 'success');
        this.dbRegions.push(created);
        this.selectedRegionId = created.countryId;
        this.isAddingNewRegion = false;
        this.newRegionNameInput = '';
        this.refreshAvailableRegions();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.showToast(err?.error?.message || 'Failed to create region in database', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  loadDevelopers(): void {
    this.bugService.getDevelopers().subscribe({
      next: (devs) => {
        this.developers = devs || [];
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load developers:', err);
        this.developers = [];
        this.cdr.markForCheck();
      }
    });
  }

  // ==========================================
  // PAGINATION GETTERS & HELPERS
  // ==========================================
  get paginatedTestCases(): TestCase[] {
    return this.filteredTestCases;
  }

  get totalTcPages(): number {
    const size = Number(this.tcPageSize) || 5;
    return Math.max(1, Math.ceil(this.tcTotalRecords / size));
  }

  get tcStartIndex(): number {
    if (this.tcTotalRecords === 0) return 0;
    const size = Number(this.tcPageSize) || 5;
    return (this.tcCurrentPage - 1) * size + 1;
  }

  get tcEndIndex(): number {
    const size = Number(this.tcPageSize) || 5;
    return Math.min(this.tcCurrentPage * size, this.tcTotalRecords);
  }

  get tcPageNumbers(): number[] {
    const total = this.totalTcPages;
    const current = this.tcCurrentPage;
    const pages: number[] = [];
    const maxButtons = 5;
    let start = Math.max(1, current - Math.floor(maxButtons / 2));
    let end = Math.min(total, start + maxButtons - 1);
    if (end - start + 1 < maxButtons) {
      start = Math.max(1, end - maxButtons + 1);
    }
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToTcPage(page: number): void {
    if (page >= 1 && page <= this.totalTcPages && page !== this.tcCurrentPage) {
      this.tcCurrentPage = page;
      this.fetchTestCasesPage();
    }
  }

  onTcPageSizeChange(): void {
    this.tcPageSize = Number(this.tcPageSize) || 5;
    this.tcCurrentPage = 1;
    this.fetchTestCasesPage();
  }

  get paginatedBugs(): Bug[] {
    const startIndex = (this.bugCurrentPage - 1) * this.bugPageSize;
    return this.filteredBugs.slice(startIndex, startIndex + this.bugPageSize);
  }

  get totalBugPages(): number {
    return Math.max(1, Math.ceil(this.filteredBugs.length / this.bugPageSize));
  }

  get bugStartIndex(): number {
    if (this.filteredBugs.length === 0) return 0;
    return (this.bugCurrentPage - 1) * this.bugPageSize + 1;
  }

  get bugEndIndex(): number {
    return Math.min(this.bugCurrentPage * this.bugPageSize, this.filteredBugs.length);
  }

  get bugPageNumbers(): number[] {
    const total = this.totalBugPages;
    const current = this.bugCurrentPage;
    const pages: number[] = [];
    const maxButtons = 5;
    let start = Math.max(1, current - Math.floor(maxButtons / 2));
    let end = Math.min(total, start + maxButtons - 1);
    if (end - start + 1 < maxButtons) {
      start = Math.max(1, end - maxButtons + 1);
    }
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToBugPage(page: number): void {
    if (page >= 1 && page <= this.totalBugPages) {
      this.bugCurrentPage = page;
      this.cdr.markForCheck();
    }
  }

  onBugPageSizeChange(): void {
    this.bugCurrentPage = 1;
    this.cdr.markForCheck();
  }

  // ==========================================
  // PROJECTS & REGIONS
  // ==========================================
  loadProjects(): void {
    this.projectService.getProjects().subscribe({
      next: (projs) => {
        this.projects = projs || [];
        this.refreshAvailableRegions();
        if (this.projects.length > 0 && !this.activeProject) {
          const indiaProj = this.projects.find(p => (p.region || '').toLowerCase() === this.selectedRegion.toLowerCase()) || this.projects[0];
          this.selectProject(indiaProj, false);
        } else if (this.projects.length === 0) {
          this.isLoadingFeatures = false;
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load projects:', err);
        this.projects = [];
        this.isLoadingFeatures = false;
        this.refreshAvailableRegions();
        this.cdr.markForCheck();
      }
    });
  }

  onRegionChange(region: string): void {
    this.selectedRegion = region;
    const isGlobal = !region || region === 'ALL' || region.toUpperCase() === 'GLOBAL';
    const matching = this.projects.filter(p => isGlobal || (p.region || '').trim().toLowerCase() === region.trim().toLowerCase());
    if (matching.length > 0) {
      const currentMatches = this.activeProject && (isGlobal || (this.activeProject.region || '').trim().toLowerCase() === region.trim().toLowerCase());
      if (!currentMatches) {
        this.selectProject(matching[0], false);
      }
    } else {
      this.activeProject = null;
      this.features = [];
      this.activeFeature = null;
      this.testcases = [];
      this.filteredTestCases = [];
      this.bugs = [];
      this.filteredBugs = [];
      this.isLoadingFeatures = false;
      this.updateMetrics();
    }
    this.cdr.markForCheck();
  }

  selectProject(p: Project, openDetailsModal = false): void {
    this.isOverviewMode = false;
    this.activeProject = p;
    this.loadProjectAttachmentCount(p.projectId);
    if (p.status === 'INACTIVE') {
      this.features = [];
      this.activeFeature = null;
      this.testcases = [];
      this.filteredTestCases = [];
      this.bugs = [];
      this.filteredBugs = [];
      this.isLoadingFeatures = false;
      this.updateMetrics();
    } else {
      this.isLoadingFeatures = true;
      this.loadFeatures(p.projectId);
    }
    if (openDetailsModal) {
      this.isProjectDetailsModalOpen = true;
    }
    if (typeof window !== 'undefined' && window.innerWidth <= 768) {
      this.isSidebarOpen = false;
    }
    this.cdr.markForCheck();
  }

  openProjectDetailsModal(p?: Project): void {
    if (p) {
      this.activeProject = p;
    }
    this.isProjectDetailsModalOpen = true;
    this.cdr.markForCheck();
  }

  loadProjectAttachmentCount(projectId: number): void {
    this.projectService.getProjectAttachments(projectId).subscribe({
      next: (files) => {
        this.projectAttachmentCount = files.length;
        this.cdr.markForCheck();
      },
      error: () => {
        this.projectAttachmentCount = 0;
        this.cdr.markForCheck();
      }
    });
  }

  openCreateProjectModal(): void {
    if (this.dbModules.length === 0) {
      this.loadModules();
    }
    if (this.dbRegions.length === 0) {
      this.loadRegions();
    }
    this.refreshAvailableRegions();

    // Default region is India from DB
    const india = this.dbRegions.find(r => (r.countryName || '').toLowerCase() === 'india');
    this.selectedRegionId = india ? india.countryId : (this.dbRegions.length > 0 ? this.dbRegions[0].countryId : null);

    // Default module is first module from DB
    this.selectedModuleId = this.dbModules.length > 0 ? this.dbModules[0].moduleId : null;
    if (this.selectedModuleId) {
      this.loadSubModulesForModule(this.selectedModuleId);
    } else {
      this.dbSubModules = [];
      this.selectedSubModuleId = null;
    }

    this.newProjectForm = {
      projectName: '',
      region: this.selectedRegionId,
      module: this.selectedModuleId,
      subModule: this.selectedSubModuleId,
      description: '',
      comments: '',
      status: 'ACTIVE'
    };
    this.isAddingNewModule = false;
    this.isAddingNewSubModule = false;
    this.isAddingNewRegion = false;
    this.newModuleNameInput = '';
    this.newSubModuleNameInput = '';
    this.newRegionNameInput = '';
    this.isProjectCreateModalOpen = true;
    this.cdr.markForCheck();
  }

  saveNewProject(): void {
    if (!this.newProjectForm.projectName.trim()) {
      this.showToast('Please enter a project name', 'error');
      return;
    }
    if (!this.selectedRegionId) {
      this.showToast('Please select or add a Region', 'error');
      return;
    }
    if (!this.selectedModuleId) {
      this.showToast('Please select or add a Module', 'error');
      return;
    }
    if (!this.selectedSubModuleId) {
      this.showToast('Please select or add a Sub-Module', 'error');
      return;
    }

    const payload: ProjectCreateRequest = {
      projectName: this.newProjectForm.projectName.trim(),
      region: this.selectedRegionId,
      module: this.selectedModuleId,
      subModule: this.selectedSubModuleId,
      description: this.newProjectForm.description?.trim() || '',
      comments: this.newProjectForm.comments?.trim() || '',
      status: 'ACTIVE'
    };

    this.projectService.createProject(payload).subscribe({
      next: (created) => {
        this.showToast(`Project "${created.projectName}" created successfully!`, 'success');
        this.isProjectCreateModalOpen = false;
        this.loadProjects();
        this.selectProject(created, false);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error creating project:', err);
        this.showToast(err?.error?.message || 'Failed to create project', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  openEditProject(p: Project): void {
    this.isProjectDetailsModalOpen = false;
    this.refreshAvailableRegions();
    if (this.dbModules.length === 0) {
      this.loadModules();
    }
    if (this.dbRegions.length === 0) {
      this.loadRegions();
    }

    // Find matching region id
    const reg = this.dbRegions.find(r => (r.countryName || '').toLowerCase() === (p.region || '').toLowerCase() || r.countryId === p.regionId);
    this.editSelectedRegionId = reg ? reg.countryId : (this.dbRegions.length > 0 ? this.dbRegions[0].countryId : 1);

    // Find matching module id
    const mod = this.dbModules.find(m => m.moduleName.toLowerCase() === (p.module || '').toLowerCase() || m.moduleId === p.moduleId);
    this.editSelectedModuleId = mod ? mod.moduleId : (this.dbModules.length > 0 ? this.dbModules[0].moduleId : null);

    this.editSelectedSubModuleId = null;
    this.editSubModules = [];
    this.isAddingNewSubModuleEdit = false;
    this.newSubModuleNameInputEdit = '';

    if (this.editSelectedModuleId) {
      this.loadSubModulesForEditProject(this.editSelectedModuleId, p.subModule, p.subModuleId);
    }

    this.editProjectForm = {
      projectName: p.projectName,
      region: this.editSelectedRegionId,
      module: this.editSelectedModuleId,
      subModule: '',
      description: p.description || '',
      comments: p.comments || '',
      status: p.status || 'ACTIVE'
    };
    this.isProjectEditModalOpen = true;
    this.cdr.markForCheck();
  }

  loadSubModulesForEditProject(moduleId: number, targetSubModuleName?: string, targetSubModuleId?: number): void {
    if (!moduleId) {
      this.editSubModules = [];
      this.editSelectedSubModuleId = null;
      this.cdr.markForCheck();
      return;
    }
    this.projectService.getSubModules(moduleId).subscribe({
      next: (subs) => {
        this.editSubModules = subs && Array.isArray(subs) ? subs : [];
        if (this.editSubModules.length > 0) {
          const found = this.editSubModules.find(s =>
            (targetSubModuleId && s.subModuleId === targetSubModuleId) ||
            (targetSubModuleName && s.subModuleName.toLowerCase() === targetSubModuleName.toLowerCase())
          );
          this.editSelectedSubModuleId = found ? found.subModuleId : this.editSubModules[0].subModuleId;
        } else {
          this.editSelectedSubModuleId = null;
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.editSubModules = [];
        this.editSelectedSubModuleId = null;
        this.cdr.markForCheck();
      }
    });
  }

  onEditModuleChange(moduleId: number): void {
    this.editSelectedModuleId = moduleId ? Number(moduleId) : null;
    if (this.editSelectedModuleId) {
      this.loadSubModulesForEditProject(this.editSelectedModuleId);
    } else {
      this.editSubModules = [];
      this.editSelectedSubModuleId = null;
    }
    this.cdr.markForCheck();
  }

  saveNewSubModuleInlineEdit(): void {
    const name = (this.newSubModuleNameInputEdit || '').trim();
    if (!name) return;
    if (!this.editSelectedModuleId) {
      this.showToast('Please select a module first', 'warning');
      return;
    }
    this.projectService.createSubModule(this.editSelectedModuleId, name).subscribe({
      next: (created) => {
        this.showToast(`Sub-module "${created.subModuleName}" created successfully!`, 'success');
        this.editSubModules.push(created);
        this.editSelectedSubModuleId = created.subModuleId;
        this.newSubModuleNameInputEdit = '';
        this.isAddingNewSubModuleEdit = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to create sub-module:', err);
        this.showToast(err?.error?.message || 'Failed to create sub-module', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  saveProjectEdit(forcePut: boolean = false): void {
    if (!this.activeProject) return;

    const handleSuccess = (msg: string = 'Project updated successfully!') => {
      this.showToast(msg, 'success');
      this.isProjectEditModalOpen = false;
      const targetStatus = this.editProjectForm.status || 'ACTIVE';
      this.activeProject!.status = targetStatus;
      if (targetStatus === 'INACTIVE') {
        this.features = [];
        this.activeFeature = null;
        this.testcases = [];
        this.filteredTestCases = [];
        this.bugs = [];
        this.filteredBugs = [];
        this.updateMetrics();
      } else {
        this.loadFeatures(this.activeProject!.projectId);
      }
      this.loadProjects();
      this.cdr.markForCheck();
    };

    const patchReq: ProjectPatchRequest = {};

    // Only include fields that actually changed from current project state
    const newComments = (this.editProjectForm.comments || '').trim();
    const oldComments = (this.activeProject.comments || '').trim();
    if (newComments !== oldComments) {
      patchReq.comments = this.editProjectForm.comments;
    }

    const newName = (this.editProjectForm.projectName || '').trim();
    const oldName = (this.activeProject.projectName || '').trim();
    if (newName && newName !== oldName) {
      patchReq.projectName = this.editProjectForm.projectName;
    }

    const newDesc = (this.editProjectForm.description || '').trim();
    const oldDesc = (this.activeProject.description || '').trim();
    if (newDesc !== oldDesc) {
      patchReq.description = this.editProjectForm.description;
    }

    // Ensure numeric Integer IDs
    if (this.editSelectedRegionId) {
      const currentReg = this.dbRegions.find(r => (r.countryName || '').toLowerCase() === (this.activeProject!.region || '').toLowerCase());
      const currentRegId = this.activeProject.regionId || (currentReg ? currentReg.countryId : null);
      if (Number(this.editSelectedRegionId) !== currentRegId) {
        patchReq.region = Number(this.editSelectedRegionId);
      }
    }

    let moduleChanged = false;
    if (this.editSelectedModuleId) {
      const currentMod = this.dbModules.find(m => m.moduleName.toLowerCase() === (this.activeProject!.module || '').toLowerCase());
      const currentModId = this.activeProject.moduleId || (currentMod ? currentMod.moduleId : null);
      if (Number(this.editSelectedModuleId) !== currentModId) {
        patchReq.module = Number(this.editSelectedModuleId);
        moduleChanged = true;
      }
    }

    if (this.editSelectedSubModuleId) {
      const currentSub = this.editSubModules.find(s => s.subModuleName.toLowerCase() === (this.activeProject!.subModule || '').toLowerCase());
      const currentSubId = this.activeProject.subModuleId || (currentSub ? currentSub.subModuleId : null);
      if (Number(this.editSelectedSubModuleId) !== currentSubId || moduleChanged) {
        patchReq.subModule = Number(this.editSelectedSubModuleId);
        // Backend requires module whenever subModule is patched to validate submodule belongs to module
        if (this.editSelectedModuleId) {
          patchReq.module = Number(this.editSelectedModuleId);
        }
      }
    }

    const statusChanged = this.editProjectForm.status && this.editProjectForm.status !== this.activeProject.status;

    // If nothing changed, close modal
    if (Object.keys(patchReq).length === 0 && !statusChanged) {
      this.isProjectEditModalOpen = false;
      return;
    }

    // If only status changed
    if (Object.keys(patchReq).length === 0 && statusChanged) {
      this.projectService.patchProjectAccess(this.activeProject.projectId, this.editProjectForm.status as any).subscribe({
        next: () => handleSuccess('Project status updated successfully!'),
        error: (err) => {
          this.showToast(err?.error?.message || 'Failed to update project status', 'error');
          this.cdr.markForCheck();
        }
      });
      return;
    }

    // Use PATCH endpoint for partial updates
    this.projectService.patchProject(this.activeProject.projectId, patchReq).subscribe({
      next: () => {
        if (statusChanged) {
          this.projectService.patchProjectAccess(this.activeProject!.projectId, this.editProjectForm.status as any).subscribe({
            next: () => handleSuccess('Project updated successfully!'),
            error: () => handleSuccess('Project updated successfully!')
          });
        } else {
          handleSuccess('Project updated successfully!');
        }
      },
      error: (err) => {
        // Fallback to updateProject (PUT) with strictly numeric IDs if PATCH is rejected
        const req: any = {
          projectName: this.editProjectForm.projectName,
          region: this.editSelectedRegionId ? Number(this.editSelectedRegionId) : 1,
          module: this.editSelectedModuleId ? Number(this.editSelectedModuleId) : 1,
          subModule: this.editSelectedSubModuleId ? Number(this.editSelectedSubModuleId) : 1,
          description: this.editProjectForm.description,
          comments: this.editProjectForm.comments
        };
        this.projectService.updateProject(this.activeProject!.projectId, req).subscribe({
          next: () => {
            if (statusChanged) {
              this.projectService.patchProjectAccess(this.activeProject!.projectId, this.editProjectForm.status as any).subscribe({
                next: () => handleSuccess('Project updated successfully!'),
                error: () => handleSuccess('Project updated successfully!')
              });
            } else {
              handleSuccess('Project updated successfully!');
            }
          },
          error: (putErr) => {
            console.error('Error updating project:', putErr);
            this.showToast(putErr?.error?.message || err?.error?.message || 'Failed to update project', 'error');
            this.cdr.markForCheck();
          }
        });
      }
    });
  }

  patchProjectComments(projectId: number, comments: string): void {
    this.projectService.patchProject(projectId, { comments }).subscribe({
      next: () => {
        this.showToast('Project comments updated!', 'success');
        if (this.activeProject && this.activeProject.projectId === projectId) {
          this.activeProject.comments = comments;
        }
        this.loadProjects();
        this.cdr.markForCheck();
      },
      error: (err) => this.showToast(err?.error?.message || 'Failed to update comment', 'error')
    });
  }

  // ==========================================
  // FEATURES
  // ==========================================
  loadFeatures(projectId: number): void {
    if (this.featureCache.has(projectId)) {
      this.features = this.featureCache.get(projectId)!;
      if (this.features.length > 0) {
        this.selectFeature(this.features[0]);
      } else {
        this.activeFeature = null;
        this.testcases = [];
        this.filteredTestCases = [];
        this.bugs = [];
        this.filteredBugs = [];
        this.updateMetrics();
      }
      this.isLoadingFeatures = false;
      this.cdr.markForCheck();
      return;
    }

    this.isLoadingFeatures = true;
    this.cdr.markForCheck();

    this.featureService.getFeaturesByProject(projectId).subscribe({
      next: (feats) => {
        const list = (feats && feats.length > 0) ? feats : [];
        this.features = list;
        this.featureCache.set(projectId, list);
        if (this.features.length > 0) {
          this.selectFeature(this.features[0]);
        } else {
          this.activeFeature = null;
          this.testcases = [];
          this.filteredTestCases = [];
          this.bugs = [];
          this.filteredBugs = [];
          this.updateMetrics();
        }
        this.isLoadingFeatures = false;
        this.cdr.markForCheck();
      },
      error: () => {
        const list: Feature[] = [];
        this.features = list;
        this.featureCache.set(projectId, list);
        this.activeFeature = null;
        this.testcases = [];
        this.filteredTestCases = [];
        this.bugs = [];
        this.filteredBugs = [];
        this.updateMetrics();
        this.isLoadingFeatures = false;
        this.cdr.markForCheck();
      }
    });
  }

  selectFeature(f: Feature): void {
    this.isOverviewMode = false;
    if (this.activeProject?.status === 'INACTIVE') {
      this.activeFeature = null;
      this.testcases = [];
      this.bugs = [];
      this.syncFeatureTimer();
      this.cdr.markForCheck();
      return;
    }
    this.activeFeature = f;
    this.syncFeatureTimer();
    this.loadFeatureAttachmentCount(f.featureId);
    this.loadTestCases(f.featureId);
    this.loadBugs(f.featureId);
    this.cdr.markForCheck();
  }

  loadFeatureAttachmentCount(featureId: number): void {
    this.featureService.getFeatureAttachments(featureId).subscribe({
      next: (files) => {
        this.featureAttachmentCount = files.length;
        this.cdr.markForCheck();
      },
      error: () => {
        this.featureAttachmentCount = 0;
        this.cdr.markForCheck();
      }
    });
  }

  openCreateFeatureModal(): void {
    if (!this.activeProject || this.activeProject.status === 'INACTIVE') {
      this.showToast('Cannot create features for an INACTIVE project.', 'warning');
      return;
    }
    this.newFeatureForm = {
      projectId: this.activeProject.projectId,
      featureName: '',
      sprint: 1,
      featureVersion: 'v1.0',
      description: '',
      comments: ''
    };
    this.isFeatureCreateModalOpen = true;
    this.cdr.markForCheck();
  }

  saveNewFeature(): void {
    if (!this.newFeatureForm.featureName.trim() || !this.activeProject) {
      this.showToast('Please enter feature name', 'error');
      return;
    }
    this.newFeatureForm.projectId = this.activeProject.projectId;
    this.featureService.createFeature(this.newFeatureForm).subscribe({
      next: (created) => {
        this.showToast(`Feature "${created.featureName}" created!`, 'success');
        this.isFeatureCreateModalOpen = false;
        this.featureCache.delete(this.activeProject!.projectId);
        this.loadFeatures(this.activeProject!.projectId);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error creating feature:', err);
        this.showToast(err?.error?.message || 'Failed to create feature', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  openEditFeature(f: Feature): void {
    this.editFeatureForm = {
      featureName: f.featureName,
      sprint: f.sprint,
      featureVersion: f.featureVersion,
      description: f.description || '',
      comments: f.comments || ''
    };
    const totalSecs = f.duration || 0;
    this.editFeatureDays = Math.floor(totalSecs / 86400);
    this.editFeatureHours = Math.floor((totalSecs % 86400) / 3600);
    this.editFeatureMinutes = Math.floor((totalSecs % 3600) / 60);
    this.isFeatureEditModalOpen = true;
    this.cdr.markForCheck();
  }

  saveFeatureEdit(forcePut: boolean = false): void {
    if (!this.activeFeature || !this.activeProject) return;

    const handleSuccess = (msg: string = 'Feature updated successfully!') => {
      this.showToast(msg, 'success');
      this.isFeatureEditModalOpen = false;
      this.featureCache.delete(this.activeProject!.projectId);
      this.loadFeatures(this.activeProject!.projectId);
      this.cdr.markForCheck();
    };

    if (forcePut) {
      this.featureService.updateFeature(this.activeFeature.featureId, this.editFeatureForm).subscribe({
        next: () => handleSuccess('Feature updated successfully!'),
        error: (putErr) => {
          console.error('Error updating feature:', putErr);
          this.showToast(putErr?.error?.message || 'Failed to update feature', 'error');
          this.cdr.markForCheck();
        }
      });
      return;
    }

    const patchReq: FeaturePatchRequest = {};

    const newComments = (this.editFeatureForm.comments || '').trim();
    const oldComments = (this.activeFeature.comments || '').trim();
    if (newComments !== oldComments) {
      patchReq.comments = this.editFeatureForm.comments;
    }

    const newName = (this.editFeatureForm.featureName || '').trim();
    const oldName = (this.activeFeature.featureName || '').trim();
    if (newName && newName !== oldName) {
      patchReq.featureName = this.editFeatureForm.featureName;
    }

    if (this.editFeatureForm.sprint && this.editFeatureForm.sprint !== this.activeFeature.sprint) {
      patchReq.sprint = this.editFeatureForm.sprint;
    }

    if (this.editFeatureForm.featureVersion && this.editFeatureForm.featureVersion !== this.activeFeature.featureVersion) {
      patchReq.featureVersion = this.editFeatureForm.featureVersion;
    }

    const newDesc = (this.editFeatureForm.description || '').trim();
    const oldDesc = (this.activeFeature.description || '').trim();
    if (newDesc !== oldDesc) {
      patchReq.description = this.editFeatureForm.description;
    }

    const newDays = Math.max(0, Number(this.editFeatureDays) || 0);
    const newHours = Math.max(0, Math.min(23, Number(this.editFeatureHours) || 0));
    const newMins = Math.max(0, Math.min(59, Number(this.editFeatureMinutes) || 0));
    const newManualSecs = (newDays * 86400) + (newHours * 3600) + (newMins * 60);
    if (newManualSecs !== (this.activeFeature.duration || 0)) {
      patchReq.days = newDays;
      patchReq.hours = newHours;
      patchReq.minutes = newMins;
    }

    if (Object.keys(patchReq).length === 0) {
      this.isFeatureEditModalOpen = false;
      return;
    }

    // Use PATCH endpoint for partial updates
    this.featureService.patchFeature(this.activeFeature.featureId, patchReq).subscribe({
      next: () => {
        handleSuccess('Feature updated successfully!');
      },
      error: (err) => {
        // Fallback to updateFeature if PATCH is rejected
        this.featureService.updateFeature(this.activeFeature!.featureId, this.editFeatureForm).subscribe({
          next: () => handleSuccess('Feature updated successfully!'),
          error: (putErr) => {
            console.error('Error updating feature:', putErr);
            this.showToast(putErr?.error?.message || err?.error?.message || 'Failed to update feature', 'error');
            this.cdr.markForCheck();
          }
        });
      }
    });
  }

  patchFeatureComments(featureId: number, comments: string): void {
    this.featureService.patchFeature(featureId, { comments }).subscribe({
      next: () => {
        this.showToast('Feature comments updated!', 'success');
        if (this.activeFeature && this.activeFeature.featureId === featureId) {
          this.activeFeature.comments = comments;
        }
        if (this.activeProject) {
          this.featureCache.delete(this.activeProject.projectId);
          this.loadFeatures(this.activeProject.projectId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => this.showToast(err?.error?.message || 'Failed to update comment', 'error')
    });
  }

  // ==========================================
  // TEST CASES
  // ==========================================
  loadTestCases(featureId: number, resetPage: boolean = true): void {
    if (resetPage) {
      this.tcCurrentPage = 1;
    }
    this.loadFeatureMetrics(featureId);
    this.fetchTestCasesPage();
  }

  loadFeatureMetrics(featureId: number): void {
    if (!featureId) return;

    this.testCaseService.getTestCases(featureId, 0, 1).subscribe({
      next: (res) => {
        this.totalTestCasesCount = res.totalElements;
        if (!this.tcSearch && (!this.tcFilterStatus || this.tcFilterStatus === 'ALL') && (!this.tcFilterType || this.tcFilterType === 'ALL') && (!this.tcFilterPriority || this.tcFilterPriority === 'ALL')) {
          this.tcTotalRecords = res.totalElements;
        }
        this.cdr.markForCheck();
      }
    });

    this.testCaseService.getTestCases(featureId, 0, 1, { status: 'PASSED' }).subscribe({
      next: (res) => {
        this.passedCount = res.totalElements;
        this.cdr.markForCheck();
      }
    });

    this.testCaseService.getTestCases(featureId, 0, 1, { status: 'FAILED' }).subscribe({
      next: (res) => {
        this.failedCount = res.totalElements;
        this.cdr.markForCheck();
      }
    });

    this.testCaseService.getTestCases(featureId, 0, 1, { status: 'DESCOPE' }).subscribe({
      next: (res) => {
        this.descopeCount = res.totalElements;
        this.cdr.markForCheck();
      }
    });
  }

  fetchTestCasesPage(): void {
    if (!this.activeFeature) {
      this.testcases = [];
      this.filteredTestCases = [];
      this.tcTotalRecords = 0;
      this.totalTestCasesCount = 0;
      this.isLoadingTestCases = false;
      this.cdr.markForCheck();
      return;
    }

    const featureId = this.activeFeature.featureId;
    const pageIndex = Math.max(0, this.tcCurrentPage - 1);
    const pageSize = Number(this.tcPageSize) || 5;

    let statusParam: string | undefined = undefined;
    if (this.tcFilterStatus && this.tcFilterStatus !== 'ALL') {
      const s = this.tcFilterStatus.toUpperCase();
      if (s === 'PASS') statusParam = 'PASSED';
      else if (s === 'FAIL') statusParam = 'FAILED';
      else if (s === 'BLOCK') statusParam = 'BLOCKED';
      else statusParam = s;
    }

    const typeParam = (this.tcFilterType && this.tcFilterType !== 'ALL') ? this.tcFilterType : undefined;
    const priorityParam = (this.tcFilterPriority && this.tcFilterPriority !== 'ALL') ? this.tcFilterPriority : undefined;
    const searchParam = this.tcSearch ? this.tcSearch.trim() : undefined;

    this.isLoadingTestCases = true;
    this.cdr.markForCheck();

    if (this.testCaseSub) {
      this.testCaseSub.unsubscribe();
    }

    this.testCaseSub = this.testCaseService.getTestCases(
      featureId,
      pageIndex,
      pageSize,
      {
        keyword: searchParam,
        status: statusParam,
        type: typeParam,
        priority: priorityParam,
        sort: 'testcaseId,desc'
      }
    ).subscribe({
      next: (pageRes) => {
        const list = pageRes.content || [];
        this.testcases = list;
        this.filteredTestCases = list;
        this.tcTotalRecords = pageRes.totalElements;
        if (!searchParam && !statusParam && !typeParam && !priorityParam) {
          this.totalTestCasesCount = pageRes.totalElements;
        }
        this.updateMetrics();
        this.isLoadingTestCases = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load test cases page:', err);
        this.testcases = [];
        this.filteredTestCases = [];
        this.tcTotalRecords = 0;
        this.isLoadingTestCases = false;
        this.cdr.markForCheck();
      }
    });
  }

  onTestCaseFilterChange(): void {
    this.tcSearchSubject.next();
  }

  openCreateTestCaseModal(): void {
    if (!this.activeFeature) return;
    this.newTcForm = {
      testcaseFormatId: '',
      title: '',
      testType: 'UI',
      testPriority: 'MEDIUM',
      testcaseStatus: 'NO_RUN',
      comments: '',
      automationFeasibility: 'YES',
      testExecution: '',
      testValidation: '',
      precondition: 'Verified environment state',
      testData: '',
      executionSteps: '',
      uiValidations: '',
      dbValidations: '',
      executionComments: ''
    };
    this.dynamicFieldsList = [];
    this.isTestCaseCreateModalOpen = true;
    this.cdr.markForCheck();
  }

  saveNewTestCase(): void {
    if (!this.newTcForm.title.trim() || !this.activeFeature) {
      this.showToast('Please enter test case title', 'error');
      return;
    }

    // Convert dynamicFieldsList array into a dynamicFields map
    const dynamicFieldsObj: Record<string, any> = {};
    for (const item of this.dynamicFieldsList) {
      if (item.key && item.key.trim()) {
        dynamicFieldsObj[item.key.trim()] = item.value;
      }
    }

    const payload: TestCaseExecutionRequest = {
      testCase: {
        featureId: this.activeFeature.featureId,
        testcaseFormatId: this.newTcForm.testcaseFormatId?.trim() || undefined,
        title: this.newTcForm.title.trim(),
        testType: this.newTcForm.testType,
        testPriority: this.newTcForm.testPriority,
        testcaseStatus: this.newTcForm.testcaseStatus || 'NO_RUN',
        comments: this.newTcForm.comments?.trim() || undefined,
        dynamicFields: Object.keys(dynamicFieldsObj).length > 0 ? dynamicFieldsObj : undefined
      },
      testingExecutionRequest: {
        automationFeasibility: this.newTcForm.automationFeasibility,
        testExecution: this.newTcForm.testExecution?.trim() || undefined,
        testValidation: this.newTcForm.testValidation?.trim() || undefined,
        precondition: this.newTcForm.precondition?.trim() || '',
        testData: this.newTcForm.testData?.trim() || undefined,
        executionSteps: this.newTcForm.executionSteps?.trim() || '',
        uiValidations: this.newTcForm.uiValidations?.trim() || '',
        dbValidations: this.newTcForm.dbValidations?.trim() || '',
        comments: this.newTcForm.executionComments?.trim() || undefined
      }
    };

    this.testCaseService.createTestCase(payload).subscribe({
      next: (created) => {
        this.showToast(`Test case "${created.title || created.testcaseFormatId}" created successfully!`, 'success');
        this.isTestCaseCreateModalOpen = false;
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error creating test case:', err);
        this.showToast(err?.error?.message || 'Failed to create test case', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // Click row to open Detail Card Modal
  viewTestCaseDetail(tc: TestCase, editMode: boolean = false): void {
    this.activeTestCaseDetail = tc;
    this.testCaseStartInEdit = editMode;
    this.isTestCaseDetailModalOpen = true;
    this.cdr.markForCheck();
  }

  // API 1a on Testcase Card: Selective Edit Testcase (PATCH /testcases/{id})
  handlePatchTestCase(event: { testcaseId: number; data: TestCasePatchRequest }): void {
    const changedFields = Object.keys(event.data).join(', ');
    this.testCaseService.patchTestCase(event.testcaseId, event.data).subscribe({
      next: (res) => {
        this.showToast('Test case updated successfully!', 'success');
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }
        if (this.activeTestCaseDetail && this.activeTestCaseDetail.testcaseId === event.testcaseId) {
          if (event.data.title !== undefined) this.activeTestCaseDetail.title = event.data.title;
          if (event.data.testType !== undefined) this.activeTestCaseDetail.testType = event.data.testType as any;
          if (event.data.testPriority !== undefined) this.activeTestCaseDetail.testPriority = event.data.testPriority as any;
          if (event.data.comments !== undefined) {
            this.activeTestCaseDetail.comments = event.data.comments;
            if (this.activeTestCaseDetail.execution) this.activeTestCaseDetail.execution.comments = event.data.comments;
          }
          if (this.activeTestCaseDetail.execution) {
            if (event.data.executionSteps !== undefined) this.activeTestCaseDetail.execution.executionSteps = event.data.executionSteps;
            if (event.data.precondition !== undefined) this.activeTestCaseDetail.execution.precondition = event.data.precondition;
            if (event.data.uiValidations !== undefined) this.activeTestCaseDetail.execution.uiValidations = event.data.uiValidations;
            if (event.data.dbValidations !== undefined) this.activeTestCaseDetail.execution.dbValidations = event.data.dbValidations;
          }
          if (event.data.executionStatus !== undefined) {
            this.activeTestCaseDetail.executionStatus = event.data.executionStatus;
          }
        }
        this.isTestCaseDetailModalOpen = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error updating test case:', err);
        this.showToast(err?.error?.message || 'Failed to update test case', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // API 1b on Testcase Card: Full Overwrite Testcase (PUT /testcases/{id})
  handleUpdateTestCase(event: { testcaseId: number; data: TestCasePutRequest }): void {
    this.testCaseService.updateTestCase(event.testcaseId, event.data).subscribe({
      next: () => {
        this.showToast('Test case updated successfully!', 'success');
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }
        const found = this.allTestCasesList.find(t => t.testcaseId === event.testcaseId);
        if (found) {
          if (event.data.title !== undefined) found.title = event.data.title;
          if (event.data.testType !== undefined) found.testType = event.data.testType;
          if (event.data.testPriority !== undefined) found.testPriority = event.data.testPriority;
          if (event.data.automationFeasibility !== undefined) found.automationFeasibility = event.data.automationFeasibility;
          if (event.data.dynamicFields !== undefined) found.dynamicFields = event.data.dynamicFields;
        }
        this.isTestCaseDetailModalOpen = false;
        this.testCaseStartInEdit = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error updating test case:', err);
        this.showToast(err?.error?.message || 'Failed to update test case', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // API 2 on Testcase Card: Soft Delete (DELETE /testcases/soft/{id})
  handleSoftDeleteTestCase(testcaseId: number): void {
    this.testCaseService.softDeleteTestCase(testcaseId).subscribe({
      next: () => {
        this.showToast('Test case deleted successfully!', 'success');
        this.isTestCaseDetailModalOpen = false;
        if (this.activeFeature) {
          if (this.filteredTestCases.length === 1 && this.tcCurrentPage > 1) {
            this.tcCurrentPage--;
          }
          this.loadFeatureMetrics(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId, false);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error deleting test case:', err);
        this.showToast(err?.error?.message || 'Failed to delete test case', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  handleExecuteTestCase(event: { testcaseId: number; status: 'PASS' | 'FAIL' | 'DESCOPE' | 'IN_PROGRESS' | 'BLOCK' | string }): void {
    const tc = this.testcases.find(t => t.testcaseId === event.testcaseId);
    if (tc) {
      tc.executionStatus = event.status;
      if (event.status === 'PASS') {
        tc.testcaseStatus = 'PASSED';
      } else if (event.status === 'FAIL') {
        tc.testcaseStatus = 'FAILED';
      } else if (event.status === 'DESCOPE') {
        tc.testcaseStatus = 'DESCOPE';
      } else if (event.status === 'IN_PROGRESS') {
        tc.testcaseStatus = 'IN_PROGRESS';
      } else if (event.status === 'BLOCK') {
        tc.testcaseStatus = 'BLOCKED';
      }
      this.updateMetrics();
      this.cdr.markForCheck();
    }
    if (this.activeTestCaseDetail && this.activeTestCaseDetail.testcaseId === event.testcaseId) {
      this.activeTestCaseDetail.executionStatus = event.status;
      if (event.status === 'PASS') {
        this.activeTestCaseDetail.testcaseStatus = 'PASSED';
      } else if (event.status === 'FAIL') {
        this.activeTestCaseDetail.testcaseStatus = 'FAILED';
      } else if (event.status === 'DESCOPE') {
        this.activeTestCaseDetail.testcaseStatus = 'DESCOPE';
      } else if (event.status === 'IN_PROGRESS') {
        this.activeTestCaseDetail.testcaseStatus = 'IN_PROGRESS';
      } else if (event.status === 'BLOCK') {
        this.activeTestCaseDetail.testcaseStatus = 'BLOCKED';
      }
    }

    this.testCaseService.executeTestCase(event.testcaseId, event.status as any).subscribe({
      next: () => {
        this.showToast(`Test case marked as ${event.status}`, 'success');
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId, false);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Execution error:', err);
        this.showToast('Execution update failed', 'error');
        if (this.activeFeature) {
          this.loadTestCases(this.activeFeature.featureId, false);
        }
        this.cdr.markForCheck();
      }
    });
  }

  onTestCaseStatusChange(tc: TestCase, newStatus: string): void {
    if (!tc || !newStatus || newStatus === 'NO_RUN') return;
    if (tc.executionStatus === newStatus) return;
    this.handleExecuteTestCase({ testcaseId: tc.testcaseId, status: newStatus as any });
  }

  // Export Test Cases & Bug Data via Summary Export Service (Download Testcase and Bug Data)
  exportTestCasesSummary(): void {
    if (!this.activeFeature) return;
    this.showToast('Preparing Testcase and Bug summary export...', 'info');
    const featId = this.activeFeature.featureId;
    const featName = (this.activeFeature.featureName || 'Feature').replace(/[^a-zA-Z0-9_-]/g, '_');
    this.testCaseService.exportTestCases(featId).subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, `QA_Summary_Report_${featName}.xlsx`);
        this.showToast('Testcase and Bug data downloaded successfully!', 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('Backend summary export failed, exporting client data to CSV:', err);
        this.exportFeatureSummaryToCsv(`QA_Summary_${featName}.csv`);
        this.showToast('Exported testcase and bug summary to CSV!', 'success');
        this.cdr.markForCheck();
      }
    });
  }

  // Bulk Excel Upload Modal & Download Template
  openExcelUploadModal(): void {
    this.isExcelModalOpen = true;
    this.cdr.markForCheck();
  }

  onExcelFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !input.files.length || !this.activeFeature) return;
    const file = input.files[0];

    this.showToast('Uploading Excel spreadsheet...', 'info');
    this.testCaseService.uploadBulkExcel(this.activeFeature.featureId, file).subscribe({
      next: () => {
        this.showToast('Bulk test cases imported successfully!', 'success');
        this.isExcelModalOpen = false;
        input.value = '';
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Excel upload error:', err);
        this.showToast(err?.error?.message || 'Failed to parse Excel spreadsheet', 'error');
        input.value = '';
        this.cdr.markForCheck();
      }
    });
  }

  downloadTestCaseTemplate(): void {
    this.showToast('Preparing standardized test case template...', 'info');
    this.testCaseService.downloadBlankTemplate().subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, 'TestCase_Upload_Template.xlsx');
        this.showToast('Standardized template downloaded successfully!', 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('Backend blank template API failed, attempting feature template or CSV fallback:', err);
        if (this.activeFeature) {
          this.testCaseService.downloadTemplate(this.activeFeature.featureId).subscribe({
            next: (blob) => {
              this.triggerBlobDownload(blob, 'TestCase_Upload_Template.xlsx');
              this.showToast('Standardized template downloaded successfully!', 'success');
              this.cdr.markForCheck();
            },
            error: () => {
              this.generateAndDownloadTemplateCsv();
              this.cdr.markForCheck();
            }
          });
        } else {
          this.generateAndDownloadTemplateCsv();
          this.cdr.markForCheck();
        }
      }
    });
  }

  downloadFeatureTestCases(): void {
    if (!this.activeFeature) {
      this.showToast('Please select a feature first.', 'warning');
      return;
    }
    const featId = this.activeFeature.featureId;
    const featName = (this.activeFeature.featureName || 'Feature').replace(/[^a-zA-Z0-9_-]/g, '_');
    this.showToast('Downloading feature test cases...', 'info');

    // Current link for get template /testcase/{featureId}/template
    this.testCaseService.downloadTemplate(featId).subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, `${featName}_TestCases.xlsx`);
        this.showToast('Test cases downloaded successfully!', 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('Backend download template failed, falling back to loaded test cases:', err);
        if (this.testcases && this.testcases.length > 0) {
          this.exportTestCasesToCsv(`${featName}_TestCases.csv`);
          this.showToast('Test cases exported to CSV successfully!', 'success');
        } else {
          this.showToast('No test cases available to download.', 'warning');
        }
        this.cdr.markForCheck();
      }
    });
  }

  private generateAndDownloadTemplateCsv(): void {
    const headers = [
      'TestcaseID',
      'Title',
      'Test Type',
      'Test Execution',
      'Test Validation',
      'Automation Priority',
      'Pre Condition',
      'Test data',
      'Execution steps',
      'UI Validations',
      'DB Validations',
      'Automation Status',
      'Actual Status',
      'Comments',
      'Sample Extra Column'
    ];

    const sampleRow = [
      'TC-001',
      'Verify user login with valid credentials',
      'UI',
      'Automated / Manual',
      'Verify dashboard redirection',
      'HIGH',
      'User is registered',
      'username=test@example.com',
      '1. Open /login; 2. Enter credentials; 3. Submit',
      'Dashboard displayed',
      'DB record updated',
      'YES',
      'PASS',
      'Standard template row format',
      'Optional extra text'
    ];

    const escapeCsv = (val: any) => `"${String(val ?? '').replace(/"/g, '""')}"`;
    const csvContent = '\uFEFF' + [
      headers.map(escapeCsv).join(','),
      sampleRow.map(escapeCsv).join(',')
    ].join('\r\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    this.triggerBlobDownload(blob, 'TestCase_Upload_Template.csv');
    this.showToast('Downloaded standardized template (CSV)', 'info');
  }

  private exportTestCasesToCsv(fileName: string): void {
    if (!this.testcases || this.testcases.length === 0) return;
    const escapeCsv = (val: any) => `"${String(val ?? '').replace(/"/g, '""')}"`;

    const headers = [
      'TestcaseID',
      'Title',
      'Test Type',
      'Test Execution',
      'Test Validation',
      'Automation Priority',
      'Pre Condition',
      'Test data',
      'Execution steps',
      'UI Validations',
      'DB Validations',
      'Automation Status',
      'Actual Status',
      'Comments'
    ];

    const rows = this.testcases.map(tc => [
      tc.testcaseFormatId || tc.testcaseId || '',
      tc.title || '',
      tc.testType || '',
      tc.testExecution || tc.execution?.testExecution || '',
      tc.testValidation || tc.execution?.testValidation || '',
      tc.testPriority || '',
      tc.execution?.precondition || (tc as any).preCondition || '',
      tc.testData || tc.execution?.testData || '',
      tc.execution?.executionSteps || (tc as any).executionSteps || '',
      tc.execution?.uiValidations || (tc as any).uiValidations || '',
      tc.execution?.dbValidations || (tc as any).dbValidations || '',
      tc.automationFeasibility || tc.execution?.automationFeasibility || (tc as any).automationStatus || '',
      tc.testcaseStatus || tc.executionStatus || 'NO_RUN',
      tc.comments || tc.execution?.comments || ''
    ]);

    const csvContent = '\uFEFF' + [
      headers.map(escapeCsv).join(','),
      ...rows.map(r => r.map(escapeCsv).join(','))
    ].join('\r\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    this.triggerBlobDownload(blob, fileName);
  }

  private exportFeatureSummaryToCsv(fileName: string): void {
    const escapeCsv = (val: any) => `"${String(val ?? '').replace(/"/g, '""')}"`;
    const featName = this.activeFeature?.featureName || 'Feature';

    const lines: string[] = [
      `"FEATURE REPORT: ${featName}"`,
      `"Export Date: ${new Date().toISOString()}"`,
      `"Total Test Cases: ${this.testcases.length}, Total Defects: ${this.bugs.length}"`,
      '',
      '"--- TEST CASES ---"',
      ['TestcaseID', 'Title', 'Type', 'Priority', 'Status', 'Pre Condition', 'Steps'].map(escapeCsv).join(',')
    ];

    this.testcases.forEach(tc => {
      lines.push([
        tc.testcaseFormatId || tc.testcaseId,
        tc.title,
        tc.testType,
        tc.testPriority,
        tc.testcaseStatus || tc.executionStatus || 'NO_RUN',
        tc.execution?.precondition || (tc as any).preCondition || '',
        tc.execution?.executionSteps || (tc as any).executionSteps || ''
      ].map(escapeCsv).join(','));
    });

    lines.push('');
    lines.push('"--- DEFECTS & BUGS ---"');
    lines.push(['BugID', 'Title', 'Severity', 'Priority', 'Status', 'Category', 'TestcaseID'].map(escapeCsv).join(','));

    this.bugs.forEach(b => {
      lines.push([
        b.bugFormatId || b.bugId,
        b.title,
        b.severity,
        b.priority,
        b.status,
        b.category,
        b.testcaseFormatId || b.testcaseId || ''
      ].map(escapeCsv).join(','));
    });

    const blob = new Blob(['\uFEFF' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
    this.triggerBlobDownload(blob, fileName);
  }

  // Get multiple bugs linked to a testcase
  getLinkedDefects(testcaseId: number): Bug[] {
    return this.bugs.filter(b => b.testcaseId === testcaseId);
  }

  // ==========================================
  // DEFECTS (BUGS)
  // ==========================================
  loadBugs(featureId: number): void {
    if (this.bugCache.has(featureId)) {
      this.bugs = this.bugCache.get(featureId)!;
      this.applyBugFilters();
      this.updateMetrics();
      this.isLoadingBugs = false;
      this.cdr.markForCheck();
      return;
    }

    this.isLoadingBugs = true;
    this.cdr.markForCheck();

    this.bugService.getBugsByFeature(featureId).subscribe({
      next: (bList) => {
        const list = (bList && bList.length > 0) ? bList : [];
        this.bugs = list;
        this.bugCache.set(featureId, list);
        this.applyBugFilters();
        this.updateMetrics();
        this.isLoadingBugs = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('Error loading defects:', err);
        const list: Bug[] = [];
        this.bugs = list;
        this.bugCache.set(featureId, list);
        this.applyBugFilters();
        this.updateMetrics();
        this.isLoadingBugs = false;
        this.cdr.markForCheck();
      }
    });
  }

  applyBugFilters(): void {
    const s = this.bugSearch.toLowerCase().trim();
    this.filteredBugs = this.bugs.filter(b => {
      if (!s) {
        const matchSev = this.bugFilterSeverity === 'ALL' || b.severity === this.bugFilterSeverity;
        const matchStatus = this.bugFilterStatus === 'ALL' || b.status === this.bugFilterStatus;
        return matchSev && matchStatus;
      }

      // 1. Defect own fields
      const matchTitle = b.title ? b.title.toLowerCase().includes(s) : false;
      const matchBugFormat = b.bugFormatId ? b.bugFormatId.toLowerCase().includes(s) : false;
      const matchBugId = b.bugId ? ('bug_' + b.bugId).toLowerCase().includes(s) || String(b.bugId) === s : false;
      const matchDesc = b.description ? b.description.toLowerCase().includes(s) : false;

      // 2. Mapped testcase fields on bug entity
      const matchTcFormat = b.testcaseFormatId ? b.testcaseFormatId.toLowerCase().includes(s) : false;
      const matchTcId = b.testcaseId ? ('tc_' + b.testcaseId).toLowerCase().includes(s) || String(b.testcaseId) === s : false;

      // 3. Mapped testcase title/name from loaded testcases suite
      const linkedTc = b.testcaseId
        ? this.testcases.find(t => t.testcaseId === b.testcaseId)
        : (b.testcaseFormatId ? this.testcases.find(t => t.testcaseFormatId?.toLowerCase() === b.testcaseFormatId?.toLowerCase()) : null);
      const matchTcName = linkedTc?.title ? linkedTc.title.toLowerCase().includes(s) : false;
      const matchLinkedTcFormat = linkedTc?.testcaseFormatId ? linkedTc.testcaseFormatId.toLowerCase().includes(s) : false;

      const matchSearch = matchTitle || matchBugFormat || matchBugId || matchDesc || matchTcFormat || matchTcId || matchTcName || matchLinkedTcFormat;
      const matchSev = this.bugFilterSeverity === 'ALL' || b.severity === this.bugFilterSeverity;
      const matchStatus = this.bugFilterStatus === 'ALL' || b.status === this.bugFilterStatus;
      return matchSearch && matchSev && matchStatus;
    });
  }

  onBugFilterChange(): void {
    this.applyBugFilters();
    this.cdr.markForCheck();
  }

  filterDefectsByTestCase(tc: TestCase): void {
    if (!tc) return;
    this.activeSubTab = 'bugs';
    this.bugFilterSeverity = 'ALL';
    this.bugFilterStatus = 'ALL';
    this.bugSearch = tc.testcaseFormatId || ('TC_' + tc.testcaseId);
    this.bugCurrentPage = 1;
    this.applyBugFilters();
    this.cdr.markForCheck();
  }

  navigateToTestCaseFromBug(b: Bug): void {
    if (!b) return;

    // Close defect detail modal if open
    this.isDefectDetailModalOpen = false;

    // If bug belongs to a feature different from current, switch to it
    if (b.featureId && (!this.activeFeature || this.activeFeature.featureId !== b.featureId)) {
      const feat = this.features.find(f => f.featureId === b.featureId)
        || this.allFeaturesList.find(f => f.featureId === b.featureId);
      if (feat) {
        this.selectFeature(feat);
      }
    }

    // Switch to Test Cases tab and reset type/priority/status filters to ALL
    this.activeSubTab = 'testcases';
    this.tcFilterType = 'ALL';
    this.tcFilterPriority = 'ALL';
    this.tcFilterStatus = 'ALL';

    // Find testcase in loaded suite
    const targetTc = (b.testcaseId ? this.testcases.find(t => t.testcaseId === b.testcaseId) : null)
      || (b.testcaseFormatId ? this.testcases.find(t => t.testcaseFormatId?.toLowerCase() === b.testcaseFormatId?.toLowerCase()) : null);

    this.tcSearch = b.testcaseFormatId || targetTc?.testcaseFormatId || targetTc?.title || '';
    this.tcCurrentPage = 1;
    this.fetchTestCasesPage();

    // Open detail card for the test case
    if (targetTc) {
      this.viewTestCaseDetail(targetTc);
    } else if (b.testcaseId) {
      this.testCaseService.getTestCaseById(b.testcaseId).subscribe({
        next: (found) => {
          if (found) {
            this.viewTestCaseDetail(found);
          }
        }
      });
    }
    this.cdr.markForCheck();
  }

  getLinkedTestCaseTitle(b: Bug): string {
    if (!b) return '';
    const tc = (b.testcaseId ? this.testcases.find(t => t.testcaseId === b.testcaseId) : null)
      || (b.testcaseFormatId ? this.testcases.find(t => t.testcaseFormatId?.toLowerCase() === b.testcaseFormatId?.toLowerCase()) : null);
    return tc?.title || '';
  }

  // Log Defect (Multiple bugs per testcase supported!)
  openLogDefectModal(originTc?: TestCase): void {
    if (!this.activeFeature) return;
    this.bugOriginTcId = originTc ? originTc.testcaseId : (this.testcases[0]?.testcaseId || 0);
    this.bugOriginTcFormatId = originTc?.testcaseFormatId || (this.testcases[0]?.testcaseFormatId || 'TC_GENERAL');
    this.bugMappedFormatId = `BUG_${this.bugOriginTcId}_${Date.now().toString().slice(-4)}`;

    this.newBugForm = {
      title: originTc ? `Defect: ${originTc.title}` : '',
      description: originTc?.execution?.executionSteps ? `Observed failure during steps:\n${originTc.execution.executionSteps}` : '',
      severity: BugSeverity.HIGH,
      priority: BugPriority.HIGH,
      category: BugCategory.PRE_PRODUCTION,
      status: BugStatus.OPEN,
      comments: ''
    };
    this.newBugAssignedToUserId = null;
    this.bugDynamicFieldsList = [];
    if (!this.developers || this.developers.length === 0) {
      this.loadDevelopers();
    }
    this.isBugCreateModalOpen = true;
    this.cdr.markForCheck();
  }

  saveNewDefect(): void {
    if (!this.newBugForm.title.trim()) {
      this.showToast('Please enter defect title', 'error');
      return;
    }

    // Convert bugDynamicFieldsList array into Map<string, any>
    const dynamicFieldsObj: Record<string, any> = {};
    for (const item of this.bugDynamicFieldsList) {
      if (item.key && item.key.trim()) {
        dynamicFieldsObj[item.key.trim()] = item.value;
      }
    }

    const payload: BugRequest = {
      title: this.newBugForm.title.trim(),
      description: this.newBugForm.description?.trim() || undefined,
      severity: this.newBugForm.severity || BugSeverity.MEDIUM,
      priority: this.newBugForm.priority || BugPriority.MEDIUM,
      category: this.newBugForm.category || BugCategory.PRE_PRODUCTION,
      status: this.newBugForm.status || BugStatus.OPEN,
      comments: this.newBugForm.comments?.trim() || undefined,
      assignedTo: this.newBugAssignedToUserId || undefined,
      dynamicFields: Object.keys(dynamicFieldsObj).length > 0 ? dynamicFieldsObj : undefined
    };

    this.bugService.createBug(this.bugOriginTcId, payload).subscribe({
      next: (created) => {
        this.showToast(`Defect [${created?.bugFormatId || 'BUG'}] logged successfully!`, 'success');
        this.isBugCreateModalOpen = false;
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        this.activeSubTab = 'bugs';
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error logging defect:', err);
        this.showToast(err?.error?.message || 'Failed to log defect on server', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // Click row to open Defect Detail Card Modal
  viewDefectDetail(b: Bug): void {
    if (!this.developers || this.developers.length === 0) {
      this.loadDevelopers();
    }
    this.activeDefectDetail = b;
    this.isDefectDetailModalOpen = true;
    this.cdr.markForCheck();
  }

  // API 1a on Defect Card: Selective Edit Defect (PATCH /bugs/{bugId})
  handlePatchBug(event: { bugId: number; data: BugPatchRequest }): void {
    const changedFields = Object.keys(event.data).join(', ');
    this.bugService.patchBug(event.bugId, event.data).subscribe({
      next: (res) => {
        this.showToast('Defect updated successfully!', 'success');
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        if (this.activeDefectDetail && this.activeDefectDetail.bugId === event.bugId) {
          if (event.data.title !== undefined) this.activeDefectDetail.title = event.data.title;
          if (event.data.description !== undefined) this.activeDefectDetail.description = event.data.description;
          if (event.data.severity !== undefined) this.activeDefectDetail.severity = event.data.severity as any;
          if (event.data.priority !== undefined) this.activeDefectDetail.priority = event.data.priority as any;
          if (event.data.category !== undefined) this.activeDefectDetail.category = event.data.category as any;
          if (event.data.status !== undefined) this.activeDefectDetail.status = event.data.status as any;
          if (event.data.comments !== undefined) this.activeDefectDetail.comments = event.data.comments;
        }
        this.isDefectDetailModalOpen = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error updating defect:', err);
        this.showToast(err?.error?.message || 'Failed to update defect', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // API 1b on Defect Card: Full Overwrite Defect (PUT /bugs/{bugId})
  handleUpdateBug(event: { bugId: number; data: BugPutRequest }): void {
    this.bugService.updateBug(event.bugId, event.data).subscribe({
      next: () => {
        this.showToast('Defect updated successfully!', 'success');
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        this.isDefectDetailModalOpen = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error updating defect:', err);
        this.showToast(err?.error?.message || 'Failed to update defect', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // API 2 on Defect Card: Soft Delete (DELETE /bugs/delete/{bugId})
  handleSoftDeleteBug(bugId: number): void {
    this.bugService.softDeleteBug(bugId).subscribe({
      next: () => {
        this.showToast('Defect deleted successfully!', 'success');
        this.isDefectDetailModalOpen = false;
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error deleting bug:', err);
        this.showToast(err?.error?.message || 'Failed to delete defect', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  handleChangeBugStatus(event: { bugId: number; status: string }): void {
    this.bugService.patchBugStatus(event.bugId, event.status).subscribe({
      next: () => {
        this.showToast(`Defect status updated to ${event.status}`, 'success');
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        if (this.activeDefectDetail) {
          this.activeDefectDetail.status = event.status;
          if (event.status === 'REOPENED') {
            this.activeDefectDetail.bugOccurrence = (this.activeDefectDetail.bugOccurrence || 1) + 1;
          }
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error changing bug status:', err);
        this.showToast('Failed to change status', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  handleChangeBugAssignee(event: { bugId: number; assignedTo: number }): void {
    this.bugService.patchBugAssignee(event.bugId, event.assignedTo).subscribe({
      next: () => {
        this.showToast('Defect assignee successfully updated!', 'success');
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        const dev = this.developers.find(d => d.userId === event.assignedTo);
        if (dev && this.activeDefectDetail) {
          this.activeDefectDetail.assignedTo = dev.fullName || dev.username;
          this.activeDefectDetail.assignedToId = dev.userId;
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error changing assignee:', err);
        this.showToast('Failed to update assignee', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // Export Bug Endpoint Calling (Summary Export Service - Download Testcase and Bug Data)
  exportBugsSummary(): void {
    this.exportTestCasesSummary();
  }

  // ==========================================
  // ATTACHMENTS (PROJECT / FEATURE / DEFECT)
  // ==========================================
  openAttachments(targetType: 'project' | 'feature' | 'bug', id: number, title: string): void {
    this.attachmentsModalTargetType = targetType;
    this.attachmentsModalTargetId = id;
    this.attachmentsModalTitle = title;
    this.attachmentsModalSubtitle = `Target ID: ${id}`;
    this.attachmentsList = [];
    this.isAttachmentsModalOpen = true;
    this.loadAttachmentsList();
    this.cdr.markForCheck();
  }

  loadAttachmentsList(): void {
    if (!this.attachmentsModalTargetId) return;
    const id = this.attachmentsModalTargetId;

    let obs;
    if (this.attachmentsModalTargetType === 'project') {
      obs = this.projectService.getProjectAttachments(id);
    } else if (this.attachmentsModalTargetType === 'feature') {
      obs = this.featureService.getFeatureAttachments(id);
    } else {
      obs = this.bugService.getBugAttachments(id);
    }

    obs.subscribe({
      next: (files) => {
        this.attachmentsList = files;
        this.cdr.markForCheck();
      },
      error: () => {
        this.attachmentsList = [];
        this.cdr.markForCheck();
      }
    });
  }

  handleUploadFiles(files: File[]): void {
    if (!this.attachmentsModalTargetId || !files.length) return;
    this.isUploadingAttachments = true;
    this.showToast('Uploading file attachments...', 'info');

    let uploadObs;
    if (this.attachmentsModalTargetType === 'project') {
      uploadObs = this.projectService.uploadProjectAttachments(this.attachmentsModalTargetId, files);
    } else if (this.attachmentsModalTargetType === 'feature') {
      uploadObs = this.featureService.uploadFeatureAttachments(this.attachmentsModalTargetId, files);
    } else {
      uploadObs = this.bugService.uploadBugAttachments(this.attachmentsModalTargetId, files);
    }

    uploadObs.subscribe({
      next: () => {
        this.isUploadingAttachments = false;
        this.showToast('Attachments uploaded successfully!', 'success');
        this.loadAttachmentsList();
        if (this.attachmentsModalTargetType === 'project' && this.activeProject) {
          this.loadProjectAttachmentCount(this.activeProject.projectId);
        } else if (this.attachmentsModalTargetType === 'feature' && this.activeFeature) {
          this.loadFeatureAttachmentCount(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isUploadingAttachments = false;
        console.error('Upload error:', err);
        this.showToast(err?.error?.message || 'File upload failed', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  handleDownloadAllAttachments(): void {
    if (!this.attachmentsModalTargetId) return;
    const defaultName = `${this.attachmentsModalTargetType}_${this.attachmentsModalTargetId}_attachments.zip`;

    let downloadObs;
    if (this.attachmentsModalTargetType === 'project') {
      downloadObs = this.projectService.downloadProjectAttachments(this.attachmentsModalTargetId);
    } else if (this.attachmentsModalTargetType === 'feature') {
      downloadObs = this.featureService.downloadFeatureAttachments(this.attachmentsModalTargetId);
    } else {
      downloadObs = this.bugService.downloadBugAttachments(this.attachmentsModalTargetId);
    }

    downloadObs.subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, defaultName);
        this.showToast('Attachments archive downloaded', 'success');
      },
      error: () => {
        this.showToast('Failed to download attachments archive', 'error');
      }
    });
  }

  handleDownloadSingleAttachment(file: AttachmentItem): void {
    if (!file || !this.attachmentsModalTargetId) return;
    const attachmentId = file.attachmentId || file.id;
    const fileName = file.fileName || `attachment_${attachmentId || 'file'}.dat`;

    // Direct single file download via endpoint
    if (attachmentId) {
      this.projectService.downloadSingleAttachment(attachmentId).subscribe({
        next: (blob) => {
          this.triggerBlobDownload(blob, fileName);
          this.showToast(`Downloaded "${fileName}"`, 'success');
        },
        error: () => {
          // Fallback: download entity archive and extract target file
          this.downloadAndExtractSingleFile(fileName);
        }
      });
    } else {
      this.downloadAndExtractSingleFile(fileName);
    }
  }

  private downloadAndExtractSingleFile(targetFileName: string): void {
    if (!this.attachmentsModalTargetId) return;

    let downloadObs;
    if (this.attachmentsModalTargetType === 'project') {
      downloadObs = this.projectService.downloadProjectAttachments(this.attachmentsModalTargetId);
    } else if (this.attachmentsModalTargetType === 'feature') {
      downloadObs = this.featureService.downloadFeatureAttachments(this.attachmentsModalTargetId);
    } else {
      downloadObs = this.bugService.downloadBugAttachments(this.attachmentsModalTargetId);
    }

    downloadObs.subscribe({
      next: async (blob) => {
        // If the returned blob is already a single file (not a zip)
        if (blob.type !== 'application/zip' && !blob.type.includes('zip') && this.attachmentsList.length === 1) {
          this.triggerBlobDownload(blob, targetFileName);
          this.showToast(`Downloaded "${targetFileName}"`, 'success');
          return;
        }

        // Extract the file from ZIP archive
        try {
          const singleBlob = await this.extractFileFromZip(blob, targetFileName);
          if (singleBlob) {
            this.triggerBlobDownload(singleBlob, targetFileName);
            this.showToast(`Downloaded "${targetFileName}"`, 'success');
            return;
          }
        } catch (e) {
          console.warn('ZIP extraction fallback:', e);
        }

        // Final fallback: download blob
        this.triggerBlobDownload(blob, targetFileName);
        this.showToast(`Downloaded "${targetFileName}"`, 'success');
      },
      error: () => {
        this.showToast(`Failed to download "${targetFileName}"`, 'error');
      }
    });
  }

  private async extractFileFromZip(zipBlob: Blob, targetFileName: string): Promise<Blob | null> {
    const arrayBuffer = await zipBlob.arrayBuffer();
    const bytes = new Uint8Array(arrayBuffer);
    const view = new DataView(arrayBuffer);
    let offset = 0;

    while (offset + 30 <= bytes.length) {
      if (view.getUint32(offset, true) !== 0x04034b50) {
        break;
      }

      const method = view.getUint16(offset + 8, true);
      const compSize = view.getUint32(offset + 18, true);
      const nameLen = view.getUint16(offset + 26, true);
      const extraLen = view.getUint16(offset + 28, true);

      const nameBytes = bytes.subarray(offset + 30, offset + 30 + nameLen);
      const entryName = new TextDecoder().decode(nameBytes);
      const dataStart = offset + 30 + nameLen + extraLen;
      const dataEnd = dataStart + compSize;

      if (entryName === targetFileName || entryName.endsWith('/' + targetFileName) || targetFileName.includes(entryName)) {
        const compressedData = bytes.subarray(dataStart, dataEnd);
        if (method === 0) {
          return new Blob([compressedData]);
        } else if (method === 8) {
          if (typeof DecompressionStream !== 'undefined') {
            const ds = new DecompressionStream('deflate-raw');
            const stream = new Response(compressedData).body!.pipeThrough(ds);
            const decompressedBuf = await new Response(stream).arrayBuffer();
            return new Blob([decompressedBuf]);
          }
        }
        return new Blob([compressedData]);
      }

      offset = dataEnd;
    }
    return null;
  }

  handleDeleteAttachment(attachmentId: number): void {
    this.projectService.deleteAttachment(attachmentId).subscribe({
      next: () => {
        this.showToast('Attachment deleted successfully', 'success');
        this.loadAttachmentsList();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Delete attachment error:', err);
        this.showToast('Failed to delete attachment', 'error');
      }
    });
  }

  // ==========================================
  // BUG CHAT / COMMENTS
  // ==========================================
  openBugChat(b: Bug): void {
    this.activeChatBug = b;
    this.chatComments = [];
    this.isChatLoading = true;
    this.isChatModalOpen = true;
    this.cdr.markForCheck();

    this.bugService.getBugComments(b.bugId).subscribe({
      next: (comments) => {
        this.chatComments = comments;
        this.isChatLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.chatComments = [];
        this.isChatLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  handleSendChatMessage(msg: string): void {
    if (!this.activeChatBug) return;
    this.bugService.createBugComment(this.activeChatBug.bugId, msg).subscribe({
      next: () => {
        this.showToast('Comment posted to discussion', 'success');
        if (this.activeChatBug) {
          this.bugService.getBugComments(this.activeChatBug.bugId).subscribe({
            next: (comments) => {
              this.chatComments = comments;
              this.cdr.markForCheck();
            }
          });
        }
      },
      error: (err) => {
        console.error('Comment error:', err);
        this.showToast(err?.error?.message || 'Failed to post comment', 'error');
      }
    });
  }

  // ==========================================
  // IN-APP NOTIFICATIONS
  // ==========================================
  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (notifs) => {
        this.notifications = notifs;
        this.unreadNotificationCount = notifs.filter(n => n.notificationStatus === 'UNREAD').length;
        this.cdr.markForCheck();
      },
      error: () => {
        this.notifications = [];
        this.unreadNotificationCount = 0;
        this.cdr.markForCheck();
      }
    });
  }

  openNotificationsModal(): void {
    this.isNotificationsModalOpen = true;
    this.cdr.markForCheck();
  }

  handleNotificationClick(n: InAppNotification): void {
    if (!n) return;

    // 1. Mark as read immediately if it's currently UNREAD
    if (n.notificationStatus === 'UNREAD') {
      n.notificationStatus = 'READ';
      this.unreadNotificationCount = this.notifications.filter(item => item.notificationStatus === 'UNREAD').length;
      this.notificationService.markAsRead(n.notificationId).subscribe();
    }

    // 2. Close notifications modal
    this.isNotificationsModalOpen = false;
    this.cdr.markForCheck();

    // 3. Navigate directly to the specific defect / bug
    if (n.bugId) {
      const bugIdNum = Number(n.bugId);
      // Check if the bug is already in our loaded bugs list or overview bugs list
      const existingBug = this.bugs.find(b => Number(b.bugId) === bugIdNum || (n.bugFormatId && b.bugFormatId === n.bugFormatId))
        || this.allBugsList.find(b => Number(b.bugId) === bugIdNum || (n.bugFormatId && b.bugFormatId === n.bugFormatId));

      if (existingBug) {
        this.navigateToBug(existingBug);
      } else {
        // Fetch bug by ID and navigate to the defect
        this.bugService.getBugById(bugIdNum).subscribe({
          next: (bug) => {
            if (bug) {
              this.navigateToBug(bug);
            }
          },
          error: (err) => {
            console.error('Failed to load defect for notification:', err);
            this.showToast('Could not load defect details', 'error');
          }
        });
      }
    }
  }

  navigateToBug(bug: Bug): void {
    if (!bug) return;

    // Switch context to feature & defects tab if featureId is known
    if (bug.featureId) {
      if (this.allFeaturesList && this.allFeaturesList.length > 0) {
        const foundFeat = this.allFeaturesList.find(f => f.featureId === bug.featureId);
        if (foundFeat) {
          const foundProj = this.projects.find(p => p.projectId === foundFeat.projectId);
          if (foundProj) {
            this.activeProject = foundProj;
          }
          if (!this.activeFeature || this.activeFeature.featureId !== foundFeat.featureId) {
            this.selectFeature(foundFeat);
          }
        }
      } else if (this.features && this.features.length > 0) {
        const foundFeat = this.features.find(f => f.featureId === bug.featureId);
        if (foundFeat) {
          if (!this.activeFeature || this.activeFeature.featureId !== foundFeat.featureId) {
            this.selectFeature(foundFeat);
          }
        }
      }
      this.isOverviewMode = false;
      this.activeSubTab = 'bugs';
    }

    // Open the defect detail card modal
    this.viewDefectDetail(bug);
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
    this.showToast('All notifications marked as read', 'info');
    this.cdr.markForCheck();
  }

  // ==========================================
  // LOGOUT
  // ==========================================
  performLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.showToast('Signed out successfully', 'info');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.authService.clearSession();
        this.showToast('Signed out successfully', 'info');
        this.router.navigate(['/login']);
      }
    });
  }

  // ==========================================
  // METRICS & HELPERS
  // ==========================================
  private updateMetrics(): void {
    if (this.tcTotalRecords > 0) {
      this.totalTestCasesCount = this.tcTotalRecords;
    }
    this.totalDefectsCount = this.bugs.length;
    this.openDefectsCount = this.bugs.filter(b => b.status === 'OPEN' || b.status === 'IN_PROGRESS' || b.status === 'REOPENED').length;
  }

  showToast(message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info'): void {
    const id = ++this.toastCounter;
    this.toasts.push({ id, message, type });
    setTimeout(() => {
      this.toasts = this.toasts.filter(t => t.id !== id);
      this.cdr.markForCheck();
    }, 4500);
    this.cdr.markForCheck();
  }

  removeToast(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.cdr.markForCheck();
  }

  private triggerBlobDownload(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }

  trackByTestCaseId(_index: number, tc: TestCase): number {
    return tc.testcaseId;
  }

  trackByBugId(_index: number, bug: Bug): number {
    return bug.bugId;
  }

  trackByFeatureId(_index: number, f: Feature): number {
    return f.featureId;
  }

  trackByProjectId(_index: number, p: Project): number {
    return p.projectId;
  }

  // ==========================================
  // ALL-ENTITIES OVERVIEW METHODS & GETTERS
  // ==========================================
  clearActiveFeatureToOverview(tab: 'projects' | 'features' | 'testcases' | 'bugs' = 'projects'): void {
    this.isOverviewMode = true;
    this.activeProject = null;
    this.activeFeature = null;
    this.activeOverviewTab = tab;
    this.loadAllOverviewData();
    this.cdr.markForCheck();
  }

  loadAllOverviewData(): void {
    this.isOverviewLoading = true;
    this.cdr.markForCheck();

    this.projectService.getProjects().subscribe({
      next: (projs) => {
        this.allProjectsList = projs || [];

        // Load all bugs globally
        this.bugService.getAllBugs().subscribe({
          next: (allBugs) => {
            this.allBugsList = allBugs || [];
            this.cdr.markForCheck();
          },
          error: () => {
            this.allBugsList = [];
            this.cdr.markForCheck();
          }
        });

        // Load features and test cases across projects
        const featurePromises = this.allProjectsList.map(p =>
          new Promise<{ project: Project; features: Feature[] }>((resolve) => {
            this.featureService.getFeaturesByProject(p.projectId).subscribe({
              next: (feats) => resolve({ project: p, features: feats || [] }),
              error: () => resolve({ project: p, features: [] })
            });
          })
        );

        Promise.all(featurePromises).then(projectFeaturePairs => {
          const flatFeatures: (Feature & { projectName?: string; region?: string })[] = [];
          projectFeaturePairs.forEach(pair => {
            pair.features.forEach(f => {
              flatFeatures.push({
                ...f,
                projectName: pair.project.projectName,
                region: pair.project.region
              });
            });
          });
          this.allFeaturesList = flatFeatures;

          // Fetch test cases for these features
          const testCasePromises = flatFeatures.map(f =>
            new Promise<(TestCase & { featureName?: string; projectName?: string })[]>((resolve) => {
              this.testCaseService.getTestCases(f.featureId, 0, 1000).subscribe({
                next: (pageRes) => {
                  const cases = pageRes.content || [];
                  const mapped = cases.map(c => ({
                    ...c,
                    featureName: f.featureName,
                    projectName: f.projectName
                  }));
                  resolve(mapped);
                },
                error: () => resolve([])
              });
            })
          );

          Promise.all(testCasePromises).then(tcGroups => {
            this.allTestCasesList = tcGroups.flat();
            this.isOverviewLoading = false;
            this.cdr.markForCheck();
          });
        });
      },
      error: () => {
        this.isOverviewLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  // Filtered All Projects
  get filteredAllProjects(): Project[] {
    const s = this.allProjSearch.toLowerCase().trim();
    return this.allProjectsList.filter(p => {
      const matchSearch = !s || p.projectName.toLowerCase().includes(s) || (p.region && p.region.toLowerCase().includes(s)) || (p.description && p.description.toLowerCase().includes(s));
      const matchStatus = this.allProjStatusFilter === 'ALL' || (p.status || 'ACTIVE') === this.allProjStatusFilter;
      const matchRegion = this.selectedRegion === 'ALL' || (p.region || '').trim().toLowerCase() === this.selectedRegion.trim().toLowerCase();
      return matchSearch && matchStatus && matchRegion;
    });
  }

  get paginatedAllProjects(): Project[] {
    const start = (this.overviewProjPage - 1) * this.overviewProjPageSize;
    return this.filteredAllProjects.slice(start, start + this.overviewProjPageSize);
  }

  get totalOverviewProjPages(): number {
    return Math.max(1, Math.ceil(this.filteredAllProjects.length / this.overviewProjPageSize));
  }

  // Filtered All Features
  get filteredAllFeatures(): (Feature & { projectName?: string; region?: string })[] {
    const s = this.allFeatSearch.toLowerCase().trim();
    return this.allFeaturesList.filter(f => {
      const sprintNum = f.sprint != null ? String(f.sprint) : '';
      const sprintFormatted = sprintNum ? `sprint ${sprintNum}` : '';
      const sprintCompact = sprintNum ? `sprint${sprintNum}` : '';
      const matchSprint = sprintNum.includes(s) || sprintFormatted.includes(s) || sprintCompact.includes(s);

      const matchSearch = !s || f.featureName.toLowerCase().includes(s) ||
        (f.projectName && f.projectName.toLowerCase().includes(s)) ||
        (f.description && f.description.toLowerCase().includes(s)) ||
        (f.featureVersion && f.featureVersion.toLowerCase().includes(s)) ||
        matchSprint;
      const matchProj = this.allFeatProjFilter === 'ALL' || (f.projectName && f.projectName === this.allFeatProjFilter);
      const matchRegion = this.selectedRegion === 'ALL' || (f.region || '').trim().toLowerCase() === this.selectedRegion.trim().toLowerCase();
      return matchSearch && matchProj && matchRegion;
    });
  }

  get paginatedAllFeatures(): (Feature & { projectName?: string; region?: string })[] {
    const start = (this.overviewFeatPage - 1) * this.overviewFeatPageSize;
    return this.filteredAllFeatures.slice(start, start + this.overviewFeatPageSize);
  }

  get totalOverviewFeatPages(): number {
    return Math.max(1, Math.ceil(this.filteredAllFeatures.length / this.overviewFeatPageSize));
  }

  // Filtered All Test Cases
  get filteredAllTestCases(): (TestCase & { featureName?: string; projectName?: string })[] {
    const s = this.allTcSearch.toLowerCase().trim();
    return this.allTestCasesList.filter(tc => {
      const matchSearch = !s || tc.title.toLowerCase().includes(s) || (tc.testcaseFormatId && tc.testcaseFormatId.toLowerCase().includes(s)) || (tc.featureName && tc.featureName.toLowerCase().includes(s)) || (tc.projectName && tc.projectName.toLowerCase().includes(s));
      const matchType = this.allTcTypeFilter === 'ALL' || tc.testType === this.allTcTypeFilter;
      const matchPrio = this.allTcPrioFilter === 'ALL' || tc.testPriority === this.allTcPrioFilter;
      const matchStatus = this.allTcStatusFilter === 'ALL' || tc.executionStatus === this.allTcStatusFilter || tc.testcaseStatus === this.allTcStatusFilter;
      return matchSearch && matchType && matchPrio && matchStatus;
    });
  }

  get paginatedAllTestCases(): (TestCase & { featureName?: string; projectName?: string })[] {
    const start = (this.overviewTcPage - 1) * this.overviewTcPageSize;
    return this.filteredAllTestCases.slice(start, start + this.overviewTcPageSize);
  }

  get totalOverviewTcPages(): number {
    return Math.max(1, Math.ceil(this.filteredAllTestCases.length / this.overviewTcPageSize));
  }

  // Filtered All Bugs
  get filteredAllBugs(): (Bug & { featureName?: string; projectName?: string })[] {
    const s = this.allBugSearch.toLowerCase().trim();
    return this.allBugsList.filter(b => {
      const matchSearch = !s || b.title.toLowerCase().includes(s) ||
        (b.bugFormatId && b.bugFormatId.toLowerCase().includes(s)) ||
        (b.bugId && (('bug_' + b.bugId).toLowerCase().includes(s) || String(b.bugId) === s)) ||
        (b.description && b.description.toLowerCase().includes(s)) ||
        (b.testcaseFormatId && b.testcaseFormatId.toLowerCase().includes(s)) ||
        (b.testcaseId && (('tc_' + b.testcaseId).toLowerCase().includes(s) || String(b.testcaseId) === s)) ||
        (b.featureName && b.featureName.toLowerCase().includes(s)) ||
        (b.projectName && b.projectName.toLowerCase().includes(s));
      const matchSev = this.allBugSevFilter === 'ALL' || b.severity === this.allBugSevFilter;
      const matchStatus = this.allBugStatusFilter === 'ALL' || b.status === this.allBugStatusFilter;
      return matchSearch && matchSev && matchStatus;
    });
  }

  get paginatedAllBugs(): (Bug & { featureName?: string; projectName?: string })[] {
    const start = (this.overviewBugPage - 1) * this.overviewBugPageSize;
    return this.filteredAllBugs.slice(start, start + this.overviewBugPageSize);
  }

  get totalOverviewBugPages(): number {
    return Math.max(1, Math.ceil(this.filteredAllBugs.length / this.overviewBugPageSize));
  }

  // Quick navigation from all-entities view to a feature
  navigateToFeature(featureId: number): void {
    this.isOverviewMode = false;
    const foundFeat = this.allFeaturesList.find(f => f.featureId === featureId);
    if (foundFeat) {
      const foundProj = this.projects.find(p => p.projectId === foundFeat.projectId);
      if (foundProj) {
        this.activeProject = foundProj;
      }
      this.selectFeature(foundFeat);
    }
  }

  // Quick navigation to project
  navigateToProject(p: Project): void {
    this.isOverviewMode = false;
    this.selectProject(p);
  }

  // =========================================================================
  // TEST CASE BULK SELECTION & MAP TO FEATURE
  // =========================================================================
  toggleTestCaseSelect(tcId: number, event?: Event): void {
    if (event) event.stopPropagation();
    if (this.selectedTestCaseIds.has(tcId)) {
      this.selectedTestCaseIds.delete(tcId);
    } else {
      this.selectedTestCaseIds.add(tcId);
    }
    this.cdr.markForCheck();
  }

  isTestCaseSelected(tcId: number): boolean {
    return this.selectedTestCaseIds.has(tcId);
  }

  isAllTestCasesSelected(): boolean {
    if (!this.paginatedTestCases || this.paginatedTestCases.length === 0) return false;
    return this.paginatedTestCases.every(tc => this.selectedTestCaseIds.has(tc.testcaseId));
  }

  isPartiallySelected(): boolean {
    if (!this.paginatedTestCases || this.paginatedTestCases.length === 0) return false;
    const count = this.paginatedTestCases.filter(tc => this.selectedTestCaseIds.has(tc.testcaseId)).length;
    return count > 0 && count < this.paginatedTestCases.length;
  }

  toggleSelectAllTestCases(event?: Event): void {
    if (event) event.stopPropagation();
    const allSelected = this.isAllTestCasesSelected();
    if (allSelected) {
      this.paginatedTestCases.forEach(tc => this.selectedTestCaseIds.delete(tc.testcaseId));
    } else {
      this.paginatedTestCases.forEach(tc => this.selectedTestCaseIds.add(tc.testcaseId));
    }
    this.cdr.markForCheck();
  }

  isAllOverviewTcSelected(): boolean {
    if (!this.paginatedAllTestCases || this.paginatedAllTestCases.length === 0) return false;
    return this.paginatedAllTestCases.every(tc => this.selectedTestCaseIds.has(tc.testcaseId));
  }

  isPartiallyOverviewTcSelected(): boolean {
    if (!this.paginatedAllTestCases || this.paginatedAllTestCases.length === 0) return false;
    const count = this.paginatedAllTestCases.filter(tc => this.selectedTestCaseIds.has(tc.testcaseId)).length;
    return count > 0 && count < this.paginatedAllTestCases.length;
  }

  toggleSelectAllOverviewTc(event?: Event): void {
    if (event) event.stopPropagation();
    const allSelected = this.isAllOverviewTcSelected();
    if (allSelected) {
      this.paginatedAllTestCases.forEach(tc => this.selectedTestCaseIds.delete(tc.testcaseId));
    } else {
      this.paginatedAllTestCases.forEach(tc => this.selectedTestCaseIds.add(tc.testcaseId));
    }
    this.cdr.markForCheck();
  }

  clearSelectedTestCases(): void {
    this.selectedTestCaseIds.clear();
    this.cdr.markForCheck();
  }

  getSelectedTestCaseDisplayNames(): string[] {
    const names: string[] = [];
    const allCases = [...this.testcases, ...(this.allTestCasesList || [])];
    const seen = new Set<number>();
    for (const tcId of this.selectedTestCaseIds) {
      if (seen.has(tcId)) continue;
      seen.add(tcId);
      const found = allCases.find(t => t.testcaseId === tcId);
      if (found) {
        names.push(found.testcaseFormatId || `TC_${found.testcaseId}`);
      } else {
        names.push(`TC_${tcId}`);
      }
      if (names.length >= 6) break;
    }
    return names;
  }

  openMoveTestCasesModal(): void {
    if (this.selectedTestCaseIds.size === 0) {
      this.showToast('Please select at least one test case to map.');
      return;
    }
    this.isMoveTestCasesModalOpen = true;
    this.moveErrorMessage = '';
    this.targetMoveProjectId = this.activeProject?.projectId || (this.projects && this.projects[0] ? this.projects[0].projectId : null);
    this.targetMoveFeatureId = null;
    this.loadFeaturesForMove(this.targetMoveProjectId);
    this.cdr.markForCheck();
  }

  closeMoveTestCasesModal(): void {
    if (this.isMovingTestCases) return;
    this.isMoveTestCasesModalOpen = false;
    this.cdr.markForCheck();
  }

  onMoveProjectChange(projectId: number): void {
    this.targetMoveProjectId = projectId;
    this.targetMoveFeatureId = null;
    this.loadFeaturesForMove(projectId);
  }

  loadFeaturesForMove(projectId: number | null): void {
    if (!projectId) {
      this.availableMoveFeatures = [];
      return;
    }
    this.isLoadingMoveFeatures = true;
    this.featureService.getFeaturesByProject(projectId).subscribe({
      next: (feats) => {
        const list = feats || [];
        if (this.activeFeature && projectId === this.activeProject?.projectId) {
          this.availableMoveFeatures = list.filter(f => f.featureId !== this.activeFeature?.featureId);
        } else {
          this.availableMoveFeatures = list;
        }
        if (this.availableMoveFeatures.length > 0) {
          this.targetMoveFeatureId = this.availableMoveFeatures[0].featureId;
        } else {
          this.targetMoveFeatureId = null;
        }
        this.isLoadingMoveFeatures = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load features for project:', err);
        this.availableMoveFeatures = [];
        this.isLoadingMoveFeatures = false;
        this.cdr.markForCheck();
      }
    });
  }

  confirmMoveTestCases(): void {
    if (!this.targetMoveFeatureId) {
      this.moveErrorMessage = 'Please select a destination feature.';
      return;
    }
    if (this.selectedTestCaseIds.size === 0) {
      this.moveErrorMessage = 'No test cases selected.';
      return;
    }

    this.isMovingTestCases = true;
    this.moveErrorMessage = '';

    const testCaseIds = Array.from(this.selectedTestCaseIds);
    const targetFeat = this.availableMoveFeatures.find(f => f.featureId === this.targetMoveFeatureId);
    const targetName = targetFeat?.featureName || `Feature #${this.targetMoveFeatureId}`;

    this.featureService.patchFeature(this.targetMoveFeatureId, { testCaseId: testCaseIds }).subscribe({
      next: (res) => {
        this.isMovingTestCases = false;
        this.isMoveTestCasesModalOpen = false;
        const count = testCaseIds.length;
        this.showToast(`Successfully mapped ${count} test case${count > 1 ? 's' : ''} to ${targetName}!`);

        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
        }
        this.testcaseCache.delete(this.targetMoveFeatureId!);

        if (this.activeFeature) {
          this.loadFeatureMetrics(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }

        if (this.allFeaturesList.length > 0) {
          this.loadAllOverviewData();
        }

        this.selectedTestCaseIds.clear();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to move test cases:', err);
        this.isMovingTestCases = false;
        const errorDetail = err?.error?.message || err?.message || 'Failed to map test cases to the selected feature.';
        this.moveErrorMessage = errorDetail;
        this.showToast('Error mapping test cases: ' + errorDetail);
        this.cdr.markForCheck();
      }
    });
  }

  // =========================================================================
  // BULK DELETE SELECTED TEST CASES
  // =========================================================================
  openBulkDeleteModal(): void {
    if (this.selectedTestCaseIds.size === 0) {
      this.showToast('Please select at least one test case to delete.', 'warning');
      return;
    }
    this.isBulkDeleteModalOpen = true;
    this.bulkDeleteErrorMessage = '';
    this.cdr.markForCheck();
  }

  closeBulkDeleteModal(): void {
    if (this.isBulkDeletingTestCases) return;
    this.isBulkDeleteModalOpen = false;
    this.bulkDeleteErrorMessage = '';
    this.cdr.markForCheck();
  }

  confirmBulkDeleteTestCases(): void {
    if (this.selectedTestCaseIds.size === 0) {
      this.bulkDeleteErrorMessage = 'No test cases selected for deletion.';
      return;
    }

    this.isBulkDeletingTestCases = true;
    this.bulkDeleteErrorMessage = '';

    const testCaseIds = Array.from(this.selectedTestCaseIds);

    this.testCaseService.bulkDeleteTestCases(testCaseIds).subscribe({
      next: (deletedIds) => {
        this.isBulkDeletingTestCases = false;
        this.isBulkDeleteModalOpen = false;

        const count = Array.isArray(deletedIds) ? deletedIds.length : testCaseIds.length;

        this.showToast(
          `Successfully deleted ${count} test case${count > 1 ? 's' : ''}!`,
          'success'
        );

        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadFeatureMetrics(this.activeFeature.featureId);
          if (this.filteredTestCases.length <= count && this.tcCurrentPage > 1) {
            this.tcCurrentPage = Math.max(1, this.tcCurrentPage - 1);
          }
          this.loadTestCases(this.activeFeature.featureId, false);
        }

        if (this.allFeaturesList.length > 0) {
          this.loadAllOverviewData();
        }

        this.selectedTestCaseIds.clear();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to bulk delete test cases:', err);
        this.isBulkDeletingTestCases = false;
        const errorDetail = err?.error?.message || err?.message || 'Failed to delete selected test cases.';
        this.bulkDeleteErrorMessage = errorDetail;
        this.showToast('Error deleting test cases: ' + errorDetail, 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // =========================================================================
  // DEFECT / BUG BULK SELECTION & DELETE
  // =========================================================================
  toggleBugSelect(bugId: number, event?: Event): void {
    if (event) event.stopPropagation();
    if (this.selectedBugIds.has(bugId)) {
      this.selectedBugIds.delete(bugId);
    } else {
      this.selectedBugIds.add(bugId);
    }
    this.cdr.markForCheck();
  }

  isBugSelected(bugId: number): boolean {
    return this.selectedBugIds.has(bugId);
  }

  isAllBugsSelected(): boolean {
    if (!this.paginatedBugs || this.paginatedBugs.length === 0) return false;
    return this.paginatedBugs.every(b => this.selectedBugIds.has(b.bugId));
  }

  isPartiallyBugsSelected(): boolean {
    if (!this.paginatedBugs || this.paginatedBugs.length === 0) return false;
    const count = this.paginatedBugs.filter(b => this.selectedBugIds.has(b.bugId)).length;
    return count > 0 && count < this.paginatedBugs.length;
  }

  toggleSelectAllBugs(event?: Event): void {
    if (event) event.stopPropagation();
    const allSelected = this.isAllBugsSelected();
    if (allSelected) {
      this.paginatedBugs.forEach(b => this.selectedBugIds.delete(b.bugId));
    } else {
      this.paginatedBugs.forEach(b => this.selectedBugIds.add(b.bugId));
    }
    this.cdr.markForCheck();
  }

  clearSelectedBugs(): void {
    this.selectedBugIds.clear();
    this.cdr.markForCheck();
  }

  getSelectedBugDisplayNames(): string[] {
    const names: string[] = [];
    const allBugs = [...this.bugs, ...(this.allBugsList || [])];
    const seen = new Set<number>();
    for (const bugId of this.selectedBugIds) {
      if (seen.has(bugId)) continue;
      seen.add(bugId);
      const found = allBugs.find(b => b.bugId === bugId);
      if (found) {
        names.push(found.bugFormatId || `BUG_${found.bugId}`);
      } else {
        names.push(`BUG_${bugId}`);
      }
      if (names.length >= 6) break;
    }
    return names;
  }

  openBulkDeleteBugsModal(): void {
    if (this.selectedBugIds.size === 0) {
      this.showToast('Please select at least one defect to delete.', 'warning');
      return;
    }
    this.isBulkDeleteBugsModalOpen = true;
    this.bulkDeleteBugsErrorMessage = '';
    this.cdr.markForCheck();
  }

  closeBulkDeleteBugsModal(): void {
    if (this.isBulkDeletingBugs) return;
    this.isBulkDeleteBugsModalOpen = false;
    this.bulkDeleteBugsErrorMessage = '';
    this.cdr.markForCheck();
  }

  confirmBulkDeleteBugs(): void {
    if (this.selectedBugIds.size === 0) {
      this.bulkDeleteBugsErrorMessage = 'No defects selected for deletion.';
      return;
    }

    this.isBulkDeletingBugs = true;
    this.bulkDeleteBugsErrorMessage = '';

    const bugIds = Array.from(this.selectedBugIds);

    this.bugService.bulkDeleteBugs(bugIds).subscribe({
      next: (deletedIds) => {
        this.isBulkDeletingBugs = false;
        this.isBulkDeleteBugsModalOpen = false;

        const count = Array.isArray(deletedIds) ? deletedIds.length : bugIds.length;
        this.showToast(
          `Successfully deleted ${count} defect${count > 1 ? 's' : ''}!`,
          'success'
        );

        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }

        if (this.allFeaturesList.length > 0) {
          this.loadAllOverviewData();
        }

        this.selectedBugIds.clear();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to bulk delete defects:', err);
        this.isBulkDeletingBugs = false;
        const errorDetail = err?.error?.message || err?.message || 'Failed to delete selected defects.';
        this.bulkDeleteBugsErrorMessage = errorDetail;
        this.showToast('Error deleting defects: ' + errorDetail, 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // Feature Delete
  isDeleteFeatureModalOpen = false;
  featureToDelete: Feature | null = null;
  isDeletingFeature = false;
  deleteFeatureErrorMessage = '';

  openDeleteFeatureModal(feature: Feature): void {
    if (!this.isManager) {
      this.showToast('Only managers can delete features.', 'warning');
      return;
    }
    if (this.isDeletingFeature) return;
    this.featureToDelete = feature;
    this.deleteFeatureErrorMessage = '';
    this.isDeleteFeatureModalOpen = true;
    this.cdr.markForCheck();
  }

  closeDeleteFeatureModal(): void {
    if (this.isDeletingFeature) return;
    this.isDeleteFeatureModalOpen = false;
    this.featureToDelete = null;
    this.deleteFeatureErrorMessage = '';
    this.cdr.markForCheck();
  }

  confirmDeleteFeature(): void {
    if (!this.isManager) return;
    if (!this.featureToDelete || this.isDeletingFeature) return;

    const feature = this.featureToDelete;
    const featId = feature.featureId;
    this.isDeletingFeature = true;
    this.deleteFeatureErrorMessage = '';

    this.featureService.deleteFeature(featId).subscribe({
      next: () => {
        this.showToast(`Feature "${feature.featureName}" deleted successfully.`, 'success');
        this.isDeletingFeature = false;
        this.isDeleteFeatureModalOpen = false;
        this.isFeatureEditModalOpen = false;
        this.featureToDelete = null;

        // Remove from local lists
        this.features = this.features.filter(f => f.featureId !== featId);
        this.allFeaturesList = this.allFeaturesList.filter(f => f.featureId !== featId);

        // If the activeFeature was the deleted one, clear or switch
        if (this.activeFeature?.featureId === featId) {
          if (this.features.length > 0) {
            this.selectFeature(this.features[0]);
          } else {
            this.activeFeature = null;
            this.clearActiveFeatureToOverview('projects');
          }
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to delete feature:', err);
        this.isDeletingFeature = false;
        const msg = err?.error?.message || err?.message || 'Failed to delete feature.';
        this.deleteFeatureErrorMessage = msg;
        this.showToast(msg, 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // ==========================================
  // FEATURE WORK DURATION & LIVE TIMER
  // ==========================================
  timerInterval: any = null;
  currentLiveElapsed = 0;
  isUpdatingTimer = false;

  // Manual Duration Modal State
  isManualDurationModalOpen = false;
  isSavingDuration = false;
  manualDurationDays = 0;
  manualDurationHours = 0;
  manualDurationMinutes = 0;
  manualDurationError = '';

  // Feature Edit Duration Inputs
  editFeatureDays = 0;
  editFeatureHours = 0;
  editFeatureMinutes = 0;

  get isFeatureTimerRunning(): boolean {
    return !!this.activeFeature?.startTime;
  }

  get displayFeatureDuration(): string {
    const base = this.activeFeature?.duration || 0;
    const total = this.isFeatureTimerRunning ? (base + this.currentLiveElapsed) : base;
    return this.formatDurationDisplay(total);
  }

  formatDurationDisplay(totalSeconds: number): string {
    if (!totalSeconds || totalSeconds < 0) return '0m 00s';
    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    const pad = (n: number) => n < 10 ? '0' + n : String(n);

    if (days > 0) {
      return `${days}d ${pad(hours)}h ${pad(minutes)}m ${pad(seconds)}s`;
    }
    if (hours > 0) {
      return `${pad(hours)}h ${pad(minutes)}m ${pad(seconds)}s`;
    }
    return `${pad(minutes)}m ${pad(seconds)}s`;
  }

  formatDurationShort(totalSeconds: number): string {
    if (!totalSeconds || totalSeconds < 0) return '0s';
    const days = Math.floor(totalSeconds / 86400);
    const hours = Math.floor((totalSeconds % 86400) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    if (days > 0) return `${days}d ${hours}h`;
    if (hours > 0) return `${hours}h ${minutes}m`;
    if (minutes > 0) return `${minutes}m`;
    return `${totalSeconds}s`;
  }

  syncFeatureTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    this.currentLiveElapsed = 0;

    if (this.activeFeature?.startTime) {
      const startMs = new Date(this.activeFeature.startTime).getTime();
      const calcElapsed = () => Math.max(0, Math.floor((Date.now() - startMs) / 1000));
      this.currentLiveElapsed = calcElapsed();

      this.timerInterval = setInterval(() => {
        this.currentLiveElapsed = calcElapsed();
        this.cdr.markForCheck();
      }, 1000);
    }
  }

  startFeatureTimer(): void {
    if (!this.activeFeature || this.isUpdatingTimer) return;
    const feat = this.activeFeature;
    this.isUpdatingTimer = true;

    // OPTIMISTIC ZERO-DELAY UPDATE: Start timer immediately in 0ms
    const prevStartTime = feat.startTime;
    const nowIso = new Date().toISOString();
    feat.startTime = nowIso;
    this.syncFeatureTimer();

    // Sync in features array and overview list immediately
    const sidebarMatch = this.features.find(f => f.featureId === feat.featureId);
    if (sidebarMatch) {
      sidebarMatch.startTime = nowIso;
    }
    const overviewMatch = this.allFeaturesList.find(f => f.featureId === feat.featureId);
    if (overviewMatch) {
      overviewMatch.startTime = nowIso;
    }
    this.cdr.markForCheck();

    this.featureService.startTimer(feat.featureId).subscribe({
      next: () => {
        this.isUpdatingTimer = false;
        this.showToast(`Timer started for "${feat.featureName}".`, 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to start timer:', err);
        // Rollback on network error
        feat.startTime = prevStartTime;
        if (sidebarMatch) sidebarMatch.startTime = prevStartTime;
        if (overviewMatch) overviewMatch.startTime = prevStartTime;
        this.syncFeatureTimer();
        this.isUpdatingTimer = false;
        this.showToast(err?.error?.message || 'Failed to start timer', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  stopFeatureTimer(): void {
    if (!this.activeFeature || this.isUpdatingTimer) return;
    const feat = this.activeFeature;
    this.isUpdatingTimer = true;

    // OPTIMISTIC ZERO-DELAY UPDATE: Stop timer immediately in 0ms without waiting for HTTP response
    const prevStartTime = feat.startTime;
    const prevDuration = feat.duration || 0;
    const addedSecs = this.currentLiveElapsed;
    const newTotalSecs = prevDuration + addedSecs;

    feat.duration = newTotalSecs;
    feat.startTime = null;
    this.syncFeatureTimer();

    // Sync in features array and overview list immediately for sidebar & table
    const sidebarMatch = this.features.find(f => f.featureId === feat.featureId);
    if (sidebarMatch) {
      sidebarMatch.duration = newTotalSecs;
      sidebarMatch.startTime = null;
    }
    const overviewMatch = this.allFeaturesList.find(f => f.featureId === feat.featureId);
    if (overviewMatch) {
      overviewMatch.duration = newTotalSecs;
      overviewMatch.startTime = null;
    }
    this.cdr.markForCheck();

    this.featureService.stopTimer(feat.featureId).subscribe({
      next: () => {
        this.isUpdatingTimer = false;
        this.showToast(`Timer paused. Logged: ${this.formatDurationDisplay(newTotalSecs)}`, 'info');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to stop timer:', err);
        // Rollback on network error
        feat.startTime = prevStartTime;
        feat.duration = prevDuration;
        if (sidebarMatch) {
          sidebarMatch.startTime = prevStartTime;
          sidebarMatch.duration = prevDuration;
        }
        if (overviewMatch) {
          overviewMatch.startTime = prevStartTime;
          overviewMatch.duration = prevDuration;
        }
        this.syncFeatureTimer();
        this.isUpdatingTimer = false;
        this.showToast(err?.error?.message || 'Failed to stop timer', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  openManualDurationModal(): void {
    if (!this.activeFeature) return;
    const dur = this.activeFeature.duration || 0;
    this.manualDurationDays = Math.floor(dur / 86400);
    this.manualDurationHours = Math.floor((dur % 86400) / 3600);
    this.manualDurationMinutes = Math.floor((dur % 3600) / 60);
    this.manualDurationError = '';
    this.isManualDurationModalOpen = true;
    this.cdr.markForCheck();
  }

  closeManualDurationModal(): void {
    if (this.isSavingDuration) return;
    this.isManualDurationModalOpen = false;
    this.manualDurationError = '';
    this.cdr.markForCheck();
  }

  calculateDurationTotalPreview(): string {
    const days = Math.max(0, Number(this.manualDurationDays) || 0);
    const hours = Math.max(0, Number(this.manualDurationHours) || 0);
    const minutes = Math.max(0, Number(this.manualDurationMinutes) || 0);
    const totalSecs = (days * 86400) + (hours * 3600) + (minutes * 60);
    return this.formatDurationDisplay(totalSecs);
  }

  saveManualDuration(): void {
    if (!this.activeFeature || this.isSavingDuration) return;
    const days = Number(this.manualDurationDays) || 0;
    const hours = Number(this.manualDurationHours) || 0;
    const minutes = Number(this.manualDurationMinutes) || 0;

    if (days < 0 || hours < 0 || minutes < 0) {
      this.manualDurationError = 'Values cannot be negative.';
      return;
    }
    if (hours > 23) {
      this.manualDurationError = 'Hours must be between 0 and 23.';
      return;
    }
    if (minutes > 59) {
      this.manualDurationError = 'Minutes must be between 0 and 59.';
      return;
    }

    this.isSavingDuration = true;
    this.manualDurationError = '';
    const feat = this.activeFeature;
    const totalSecs = (days * 86400) + (hours * 3600) + (minutes * 60);

    // Optimistic instant update
    feat.duration = totalSecs;
    if (feat.startTime) {
      feat.startTime = null;
      this.syncFeatureTimer();
    }
    const sidebarMatch = this.features.find(f => f.featureId === feat.featureId);
    if (sidebarMatch) {
      sidebarMatch.duration = totalSecs;
      sidebarMatch.startTime = null;
    }
    const overviewMatch = this.allFeaturesList.find(f => f.featureId === feat.featureId);
    if (overviewMatch) {
      overviewMatch.duration = totalSecs;
      overviewMatch.startTime = null;
    }
    this.cdr.markForCheck();

    this.featureService.setManualDuration(feat.featureId, days, hours, minutes).subscribe({
      next: () => {
        this.isSavingDuration = false;
        this.isManualDurationModalOpen = false;
        this.showToast(`Duration updated to ${this.formatDurationDisplay(totalSecs)}.`, 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to set duration:', err);
        this.isSavingDuration = false;
        this.manualDurationError = err?.error?.message || 'Failed to update duration.';
        this.cdr.markForCheck();
      }
    });
  }
}
