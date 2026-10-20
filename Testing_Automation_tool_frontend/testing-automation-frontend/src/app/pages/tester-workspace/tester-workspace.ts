import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

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
  FeatureCreateRequest,
  TestCaseCreatePayload,
  TestCasePutRequest,
  BugCreateRequest,
  BugPutRequest,
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
export class TesterWorkspace implements OnInit {
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

  // Responsive Sidebar (open by default on desktop)
  isSidebarOpen = typeof window !== 'undefined' ? window.innerWidth > 1024 : true;

  // Projects & Dynamic Regions
  projects: Project[] = [];
  availableRegions: string[] = [];
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

  // Inline Creation Triggers & Inputs
  isAddingNewModule = false;
  newModuleNameInput = '';
  isAddingNewSubModule = false;
  newSubModuleNameInput = '';
  isAddingNewRegion = false;
  newRegionNameInput = '';

  // Features
  features: Feature[] = [];
  activeFeature: Feature | null = null;
  featureAttachmentCount = 0;

  // Active Workspace Sub-Tab ('testcases' | 'bugs')
  activeSubTab: 'testcases' | 'bugs' = 'testcases';

  // Global All-Entity Overview State (Active when no feature is selected or in All-Entities view)
  activeOverviewTab: 'projects' | 'features' | 'testcases' | 'bugs' = 'projects';
  allProjectsList: Project[] = [];
  allFeaturesList: (Feature & { projectName?: string; region?: string })[] = [];
  allTestCasesList: (TestCase & { featureName?: string; projectName?: string })[] = [];
  allBugsList: (Bug & { featureName?: string; projectName?: string })[] = [];
  isOverviewLoading = false;

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
  readonly overviewPageSizeOptions = [5, 10, 20, 50];
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

  // Defects / Bugs
  bugs: Bug[] = [];
  filteredBugs: Bug[] = [];
  bugSearch = '';
  bugFilterSeverity = 'ALL';
  bugFilterStatus = 'ALL';
  developers: DeveloperUser[] = [];

  // Pagination: Test Cases
  tcCurrentPage = 1;
  tcPageSize = 5;
  readonly tcPageSizeOptions = [5, 10, 20, 50];

  // Pagination: Defects
  bugCurrentPage = 1;
  bugPageSize = 5;
  readonly bugPageSizeOptions = [5, 10, 20, 50];

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
  isDefectDetailModalOpen = false;
  isAttachmentsModalOpen = false;
  isChatModalOpen = false;
  isNotificationsModalOpen = false;

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
    title: '',
    testType: 'UI',
    testPriority: 'MEDIUM',
    testcaseStatus: 'PASSED',
    precondition: 'Verified environment state',
    executionSteps: '',
    uiValidations: '',
    dbValidations: '',
    comments: ''
  };

  newBugForm: BugCreateRequest = {
    title: '',
    description: '',
    severity: 'HIGH',
    priority: 'HIGH',
    category: 'FUNCTIONAL',
    status: 'OPEN',
    comments: ''
  };
  newBugAssignedToUserId: number | null = null;
  bugOriginTcId = 0;
  bugOriginTcFormatId = '';
  bugMappedFormatId = '';

  // Toast Alerts
  toasts: ToastAlert[] = [];
  private toastCounter = 0;

  ngOnInit(): void {
    this.initUserProfile();
    this.ensureAuthSession(() => {
      this.loadModules();
      this.loadRegions();
      this.loadDevelopers();
      this.loadProjects();
      this.loadNotifications();
    });
    this.warmupCsrf();
  }

  // ==========================================
  // INITIALIZATION & CSRF WARMUP (PREVENTS ACCESS DENIED FIRST CLICK)
  // ==========================================
  private initUserProfile(): void {
    if (typeof localStorage !== 'undefined') {
      const token = localStorage.getItem('token') || localStorage.getItem('jwt');
      this.isAuthenticated = !!token;
      this.userName = localStorage.getItem('tat_user_fullname') || localStorage.getItem('tat_user_name') || 'Arjun Kumar';
      this.userRole = localStorage.getItem('tat_user_role') || 'TESTER';
      this.userInitials = this.computeInitials(this.userName);
    }
  }

  private ensureAuthSession(onReady?: () => void): void {
    if (typeof localStorage === 'undefined') {
      if (onReady) onReady();
      return;
    }
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');
    if (token) {
      if (onReady) onReady();
      return;
    }
    // Auto-authenticate with tester account if token is missing
    this.authService.login({
      usernameOrEmail: 'Vikramraajak@gmail.com',
      password: 'Vikram@123'
    }).subscribe({
      next: () => {
        this.initUserProfile();
        if (onReady) onReady();
        this.cdr.markForCheck();
      },
      error: (e) => {
        console.warn('Auto auth check failed:', e);
        if (onReady) onReady();
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

  loadRegions(): void {
    this.projectService.getRegions().subscribe({
      next: (regs) => {
        this.dbRegions = regs && Array.isArray(regs) ? regs : [];
        this.availableRegions = this.dbRegions.map(r => r.countryName);

        // Default region is India from DB
        const india = this.dbRegions.find(r => r.countryName.toLowerCase() === 'india');
        if (india) {
          this.selectedRegion = 'India';
          this.selectedRegionId = india.countryId;
        } else if (this.availableRegions.length > 0) {
          this.selectedRegion = this.availableRegions[0];
          this.selectedRegionId = this.dbRegions[0].countryId;
        } else {
          this.selectedRegion = 'India';
        }

        // Auto-select project matching default region India
        if (this.projects && this.projects.length > 0 && !this.activeProject) {
          const indiaProj = this.projects.find(p => (p.region || '').toLowerCase() === this.selectedRegion.toLowerCase()) || this.projects[0];
          this.selectProject(indiaProj, false);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load regions from db:', err);
        this.dbRegions = [];
        this.availableRegions = [];
        this.selectedRegion = 'India';
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
        if (!this.availableRegions.includes(created.countryName)) {
          this.availableRegions.push(created.countryName);
        }
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
    const startIndex = (this.tcCurrentPage - 1) * this.tcPageSize;
    return this.filteredTestCases.slice(startIndex, startIndex + this.tcPageSize);
  }

  get totalTcPages(): number {
    return Math.max(1, Math.ceil(this.filteredTestCases.length / this.tcPageSize));
  }

  get tcStartIndex(): number {
    if (this.filteredTestCases.length === 0) return 0;
    return (this.tcCurrentPage - 1) * this.tcPageSize + 1;
  }

  get tcEndIndex(): number {
    return Math.min(this.tcCurrentPage * this.tcPageSize, this.filteredTestCases.length);
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
    if (page >= 1 && page <= this.totalTcPages) {
      this.tcCurrentPage = page;
      this.cdr.markForCheck();
    }
  }

  onTcPageSizeChange(): void {
    this.tcCurrentPage = 1;
    this.cdr.markForCheck();
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
        if (this.projects.length > 0 && !this.activeProject) {
          const indiaProj = this.projects.find(p => (p.region || '').toLowerCase() === this.selectedRegion.toLowerCase()) || this.projects[0];
          this.selectProject(indiaProj, false);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load projects:', err);
        this.projects = [];
        this.cdr.markForCheck();
      }
    });
  }

  onRegionChange(region: string): void {
    this.selectedRegion = region;
    const matching = this.projects.filter(p => region === 'ALL' || p.region === region);
    if (matching.length > 0) {
      this.selectProject(matching[0], false);
    } else {
      this.activeProject = null;
      this.features = [];
      this.activeFeature = null;
      this.testcases = [];
      this.filteredTestCases = [];
      this.bugs = [];
      this.filteredBugs = [];
      this.updateMetrics();
    }
    this.cdr.markForCheck();
  }

  selectProject(p: Project, openDetailsModal = false): void {
    this.activeProject = p;
    this.loadProjectAttachmentCount(p.projectId);
    this.loadFeatures(p.projectId);
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

    // Default region is India from DB
    const india = this.dbRegions.find(r => r.countryName.toLowerCase() === 'india');
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
        this.selectProject(created, true);
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
    // Find matching region id
    const reg = this.dbRegions.find(r => r.countryName.toLowerCase() === (p.region || '').toLowerCase());
    this.editSelectedRegionId = reg ? reg.countryId : (this.dbRegions.length > 0 ? this.dbRegions[0].countryId : null);

    // Find matching module id
    const mod = this.dbModules.find(m => m.moduleName.toLowerCase() === (p.module || '').toLowerCase());
    this.editSelectedModuleId = mod ? mod.moduleId : null;

    if (this.editSelectedModuleId) {
      this.loadSubModulesForModule(this.editSelectedModuleId);
    }

    this.editProjectForm = {
      projectName: p.projectName,
      region: this.editSelectedRegionId || p.region,
      module: this.editSelectedModuleId || p.module || '',
      subModule: p.subModule || '',
      description: p.description || '',
      comments: p.comments || '',
      status: p.status || 'ACTIVE'
    };
    this.isProjectEditModalOpen = true;
    this.cdr.markForCheck();
  }

  saveProjectEdit(): void {
    if (!this.activeProject) return;
    const req: any = {
      projectName: this.editProjectForm.projectName,
      region: this.editSelectedRegionId || this.editProjectForm.region,
      module: this.editSelectedModuleId || this.editProjectForm.module,
      subModule: this.editSelectedSubModuleId || this.editProjectForm.subModule,
      description: this.editProjectForm.description,
      comments: this.editProjectForm.comments,
      status: this.editProjectForm.status
    };
    this.projectService.updateProject(this.activeProject.projectId, req).subscribe({
      next: (updated) => {
        this.showToast(`Project "${updated.projectName}" updated!`, 'success');
        this.isProjectEditModalOpen = false;
        this.loadProjects();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error updating project:', err);
        this.showToast(err?.error?.message || 'Failed to update project', 'error');
        this.cdr.markForCheck();
      }
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
      this.cdr.markForCheck();
      return;
    }

    this.featureService.getFeaturesByProject(projectId).subscribe({
      next: (feats) => {
        const list = (feats && feats.length > 0) ? feats : [];
        this.features = list;
        this.featureCache.set(projectId, list);
        if (this.features.length > 0) {
          this.selectFeature(this.features[0]);
        }
        this.cdr.markForCheck();
      },
      error: () => {
        const list: Feature[] = [];
        this.features = list;
        this.featureCache.set(projectId, list);
        this.cdr.markForCheck();
      }
    });
  }

  selectFeature(f: Feature): void {
    this.activeFeature = f;
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
    if (!this.activeProject) return;
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
    this.isFeatureEditModalOpen = true;
    this.cdr.markForCheck();
  }

  saveFeatureEdit(): void {
    if (!this.activeFeature || !this.activeProject) return;
    this.featureService.updateFeature(this.activeFeature.featureId, this.editFeatureForm).subscribe({
      next: (updated) => {
        this.showToast(`Feature "${updated.featureName}" updated!`, 'success');
        this.isFeatureEditModalOpen = false;
        this.featureCache.delete(this.activeProject!.projectId);
        this.loadFeatures(this.activeProject!.projectId);
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error updating feature:', err);
        this.showToast(err?.error?.message || 'Failed to update feature', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // ==========================================
  // TEST CASES
  // ==========================================
  loadTestCases(featureId: number): void {
    if (this.testcaseCache.has(featureId)) {
      this.testcases = this.testcaseCache.get(featureId)!;
      this.applyTestCaseFilters();
      this.updateMetrics();
      this.cdr.markForCheck();
      return;
    }

    this.testCaseService.getTestCases(featureId).subscribe({
      next: (cases) => {
        const list = (cases && cases.length > 0) ? cases : [];
        this.testcases = list;
        this.testcaseCache.set(featureId, list);
        this.applyTestCaseFilters();
        this.updateMetrics();
        this.cdr.markForCheck();
      },
      error: () => {
        const list: TestCase[] = [];
        this.testcases = list;
        this.testcaseCache.set(featureId, list);
        this.applyTestCaseFilters();
        this.updateMetrics();
        this.cdr.markForCheck();
      }
    });
  }

  applyTestCaseFilters(): void {
    const s = this.tcSearch.toLowerCase().trim();
    this.filteredTestCases = this.testcases.filter(tc => {
      const matchSearch = !s || tc.title.toLowerCase().includes(s) || (tc.testcaseFormatId && tc.testcaseFormatId.toLowerCase().includes(s));
      const matchType = this.tcFilterType === 'ALL' || tc.testType === this.tcFilterType;
      const matchPrio = this.tcFilterPriority === 'ALL' || tc.testPriority === this.tcFilterPriority;
      const matchStatus = this.tcFilterStatus === 'ALL' || tc.executionStatus === this.tcFilterStatus || tc.testcaseStatus === this.tcFilterStatus;
      return matchSearch && matchType && matchPrio && matchStatus;
    });
  }

  onTestCaseFilterChange(): void {
    this.applyTestCaseFilters();
    this.cdr.markForCheck();
  }

  openCreateTestCaseModal(): void {
    if (!this.activeFeature) return;
    this.newTcForm = {
      title: '',
      testType: 'UI',
      testPriority: 'MEDIUM',
      testcaseStatus: 'PASSED',
      precondition: 'Verified environment state',
      executionSteps: '',
      uiValidations: '',
      dbValidations: '',
      comments: ''
    };
    this.isTestCaseCreateModalOpen = true;
    this.cdr.markForCheck();
  }

  saveNewTestCase(): void {
    if (!this.newTcForm.title.trim() || !this.activeFeature) {
      this.showToast('Please enter test case title', 'error');
      return;
    }

    const payload: TestCaseCreatePayload = {
      testCase: {
        featureId: this.activeFeature.featureId,
        title: this.newTcForm.title,
        testType: this.newTcForm.testType,
        testPriority: this.newTcForm.testPriority,
        testcaseStatus: this.newTcForm.testcaseStatus,
        comments: this.newTcForm.comments
      },
      testingExecutionRequest: {
        precondition: this.newTcForm.precondition,
        executionSteps: this.newTcForm.executionSteps,
        uiValidations: this.newTcForm.uiValidations,
        dbValidations: this.newTcForm.dbValidations
      }
    };

    this.testCaseService.createTestCase(payload).subscribe({
      next: (created) => {
        this.showToast(`Test case "${created.title}" created successfully!`, 'success');
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
  viewTestCaseDetail(tc: TestCase): void {
    this.activeTestCaseDetail = tc;
    this.isTestCaseDetailModalOpen = true;
    this.cdr.markForCheck();
  }

  // API 1 on Testcase Card: Edit Testcase (PUT /testcases/{id})
  handleUpdateTestCase(event: { testcaseId: number; data: TestCasePutRequest }): void {
    this.testCaseService.updateTestCase(event.testcaseId, event.data).subscribe({
      next: () => {
        this.showToast('Test case details updated successfully!', 'success');
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
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

  // API 2 on Testcase Card: Soft Delete (DELETE /testcases/soft/{id})
  handleSoftDeleteTestCase(testcaseId: number): void {
    this.testCaseService.softDeleteTestCase(testcaseId).subscribe({
      next: () => {
        this.showToast('Test case soft deleted successfully!', 'success');
        this.isTestCaseDetailModalOpen = false;
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error deleting test case:', err);
        this.showToast(err?.error?.message || 'Failed to soft delete test case', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  handleExecuteTestCase(event: { testcaseId: number; status: 'PASS' | 'FAIL' | 'DESCOPE' }): void {
    this.testCaseService.executeTestCase(event.testcaseId, event.status).subscribe({
      next: () => {
        this.showToast(`Test case marked as ${event.status}`, 'success');
        if (this.activeFeature) {
          this.testcaseCache.delete(this.activeFeature.featureId);
          this.loadTestCases(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Execution error:', err);
        this.showToast('Execution update failed', 'error');
        this.cdr.markForCheck();
      }
    });
  }

  // Export Test Cases via Summary Export Service
  exportTestCasesSummary(): void {
    if (!this.activeFeature) return;
    this.showToast('Generating test cases summary export...', 'info');
    this.testCaseService.exportTestCases(this.activeFeature.featureId).subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, `Feature_${this.activeFeature!.featureId}_TestCases_Export.xlsx`);
        this.showToast('Test cases Excel exported successfully!', 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Export error:', err);
        this.showToast('Failed to export test cases summary', 'error');
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
    if (!this.activeFeature) return;
    this.testCaseService.downloadTemplate(this.activeFeature.featureId).subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, `testcase_template_${this.activeFeature!.featureId}.xlsx`);
        this.showToast('Downloaded blank test case template', 'success');
      },
      error: () => {
        this.showToast('Could not download template', 'error');
      }
    });
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
      this.cdr.markForCheck();
      return;
    }

    this.bugService.getBugsByFeature(featureId).subscribe({
      next: (bList) => {
        const list = (bList && bList.length > 0) ? bList : [];
        this.bugs = list;
        this.bugCache.set(featureId, list);
        this.applyBugFilters();
        this.updateMetrics();
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.warn('Error loading defects:', err);
        const list: Bug[] = [];
        this.bugs = list;
        this.bugCache.set(featureId, list);
        this.applyBugFilters();
        this.updateMetrics();
        this.cdr.markForCheck();
      }
    });
  }

  applyBugFilters(): void {
    const s = this.bugSearch.toLowerCase().trim();
    this.filteredBugs = this.bugs.filter(b => {
      const matchSearch = !s || b.title.toLowerCase().includes(s) || (b.bugFormatId && b.bugFormatId.toLowerCase().includes(s));
      const matchSev = this.bugFilterSeverity === 'ALL' || b.severity === this.bugFilterSeverity;
      const matchStatus = this.bugFilterStatus === 'ALL' || b.status === this.bugFilterStatus;
      return matchSearch && matchSev && matchStatus;
    });
  }

  onBugFilterChange(): void {
    this.applyBugFilters();
    this.cdr.markForCheck();
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
      severity: 'HIGH',
      priority: 'HIGH',
      category: 'FUNCTIONAL',
      status: 'OPEN',
      comments: ''
    };
    this.newBugAssignedToUserId = null;
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

    const payload: BugCreateRequest = {
      title: this.newBugForm.title,
      description: this.newBugForm.description,
      severity: this.newBugForm.severity,
      priority: this.newBugForm.priority,
      category: this.newBugForm.category,
      status: this.newBugForm.status,
      comments: this.newBugForm.comments
    };

    if (this.newBugAssignedToUserId) {
      payload.assignedTo = this.newBugAssignedToUserId;
    }

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

  // API 1 on Defect Card: Edit Defect (PUT /bugs/{bugId})
  handleUpdateBug(event: { bugId: number; data: BugPutRequest }): void {
    this.bugService.updateBug(event.bugId, event.data).subscribe({
      next: () => {
        this.showToast('Defect details updated successfully!', 'success');
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
        this.showToast('Defect soft deleted successfully!', 'success');
        this.isDefectDetailModalOpen = false;
        if (this.activeFeature) {
          this.bugCache.delete(this.activeFeature.featureId);
          this.loadBugs(this.activeFeature.featureId);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Error deleting bug:', err);
        this.showToast(err?.error?.message || 'Failed to soft delete defect', 'error');
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

  // Export Bug Endpoint Calling (Summary Export Service)
  exportBugsSummary(): void {
    if (!this.activeFeature) return;
    this.showToast('Invoking defect summary export endpoint...', 'info');
    this.bugService.exportBugs(this.activeFeature.featureId).subscribe({
      next: (blob) => {
        this.triggerBlobDownload(blob, `Feature_${this.activeFeature!.featureId}_Defects_Export.xlsx`);
        this.showToast('Defect summary exported successfully!', 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Export defect error:', err);
        this.showToast('Failed to export defects summary', 'error');
        this.cdr.markForCheck();
      }
    });
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
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('jwt');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('tat_user_fullname');
      localStorage.removeItem('tat_user_role');
    }
    this.showToast('Signed out successfully', 'info');
    this.router.navigate(['/login']);
  }

  // ==========================================
  // METRICS & HELPERS
  // ==========================================
  private updateMetrics(): void {
    this.totalTestCasesCount = this.testcases.length;
    this.passedCount = this.testcases.filter(tc => tc.executionStatus === 'PASS' || tc.testcaseStatus === 'PASSED').length;
    this.failedCount = this.testcases.filter(tc => tc.executionStatus === 'FAIL' || tc.testcaseStatus === 'FAILED').length;
    this.descopeCount = this.testcases.filter(tc => tc.executionStatus === 'DESCOPE' || tc.testcaseStatus === 'DESCOPE').length;
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
              this.testCaseService.getTestCases(f.featureId).subscribe({
                next: (cases) => {
                  const mapped = (cases || []).map(c => ({
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
      const matchRegion = this.selectedRegion === 'ALL' || p.region === this.selectedRegion;
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
      const matchSearch = !s || f.featureName.toLowerCase().includes(s) || (f.projectName && f.projectName.toLowerCase().includes(s)) || (f.description && f.description.toLowerCase().includes(s));
      const matchProj = this.allFeatProjFilter === 'ALL' || (f.projectName && f.projectName === this.allFeatProjFilter);
      const matchRegion = this.selectedRegion === 'ALL' || f.region === this.selectedRegion;
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
      const matchSearch = !s || b.title.toLowerCase().includes(s) || (b.bugFormatId && b.bugFormatId.toLowerCase().includes(s)) || (b.description && b.description.toLowerCase().includes(s)) || (b.testcaseFormatId && b.testcaseFormatId.toLowerCase().includes(s));
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
    this.selectProject(p);
  }
}
