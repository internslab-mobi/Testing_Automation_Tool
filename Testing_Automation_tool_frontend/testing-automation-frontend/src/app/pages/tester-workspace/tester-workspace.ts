import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TesterService } from '../../core/services/testerService';

export interface FeatureItem {
  id: string;
  rawId?: number;
  name: string;
  sprint: string;
  sprintNum: number;
  bugs: number;
  tcCount: number;
  status: 'BUGS' | 'PASSED';
}

export interface ProjectItem {
  id: string;
  rawId?: number;
  name: string;
  version: string;
  status: 'ACTIVE' | 'INACTIVE';
  region: string;
  features: FeatureItem[];
}

export interface TestCaseItem {
  id: string;
  rawId?: number;
  title: string;
  featureName: string;
  featureId?: number;
  precondition: string;
  type: 'FUNCTIONAL' | 'SECURITY' | 'REGRESSION';
  priority: 'CRITICAL' | 'HIGH' | 'MEDIUM';
  uiValidation: string;
  dbValidation: string;
  status: 'PASSED' | 'FAILED';
}

export interface DefectItem {
  id: string;
  rawId?: number;
  title: string;
  featureName: string;
  originTestCaseId: string;
  originTestCaseRawId?: number;
  precondition: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM';
  assignee: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'FIXED' | 'CLOSED';
}

export interface NotificationItem {
  id: string;
  rawId?: number;
  type: 'FIXED' | 'COMMENT' | 'TC';
  title: string;
  highlightId?: string;
  isNew: boolean;
  read: boolean;
  message: string;
  featureName: string;
  timeAgo: string;
  originTestCaseId?: string;
}

export interface ChatMessage {
  sender: 'tester' | 'dev';
  senderName: string;
  text: string;
  timestamp: string;
}

export interface FileAttachment {
  name: string;
  size: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-tester-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './tester-workspace.html',
  styleUrl: './tester-workspace.css'
})
export class TesterWorkspace implements OnInit {
  private readonly router = inject(Router);
  private readonly testerService = inject(TesterService);

  // User Profile
  userName = 'Tester';
  userRole = 'QA Tester';
  userInitials = 'QT';

  // Navigation State
  activeRegion = 'ALL';
  availableRegions: string[] = [];
  allProjects: ProjectItem[] = [];
  activeProject: ProjectItem | null = null;
  activeFeature: FeatureItem | null = null;
  activeTab: 'tc' | 'bugs' = 'tc';
  featureFilterCategory: 'ALL' | 'BUGS' | 'PASSED' = 'ALL';

  // Search & Filter Inputs
  projectSearchQuery = '';
  featureSearchQuery = '';
  tcSearchQuery = '';
  tcTypeFilter = 'ALL';
  tcStatusFilter = 'ALL';

  bugSearchQuery = '';
  bugSevFilter = 'ALL';
  bugStatusFilter = 'ALL';

  notifFilterCategory: 'ALL' | 'UNREAD' | 'FIXED' | 'COMMENT' = 'ALL';

  // Pagination State for Test Cases & Defects
  tcCurrentPage = 1;
  tcPageSize = 5;

  bugCurrentPage = 1;
  bugPageSize = 5;

  // Modals state
  activeModal: string | null = null;

  // Toast feedback
  toastMessage = '';
  toastType: 'success' | 'info' | 'error' = 'info';
  private toastTimer: any = null;

  // Loading indicator for API calls
  isApiLoading = false;

  // Project data per region (populated dynamically from project regions)
  projectData: Record<string, ProjectItem[]> = {};

  // Test cases repository (populated from backend API)
  testCases: TestCaseItem[] = [];

  // Defects repository (populated from backend API)
  defects: DefectItem[] = [];

  // Notifications repository (populated from backend API)
  notifications: NotificationItem[] = [];

  // Dedicated Chat state
  activeChatBug: DefectItem | null = null;
  chatMessages: ChatMessage[] = [];
  newChatMessage = '';

  // Project Attachments
  projectAttachments: FileAttachment[] = [];

  // Feature Attachments
  featureAttachments: FileAttachment[] = [];

  // Form Models for Modals
  newTc = {
    title: '',
    type: 'FUNCTIONAL' as 'FUNCTIONAL' | 'SECURITY' | 'REGRESSION',
    priority: 'CRITICAL' as 'CRITICAL' | 'HIGH' | 'MEDIUM',
    uiValidation: '',
    dbValidation: ''
  };

  bugForm = {
    featureName: '',
    originTestCaseId: '',
    originTestCaseRawId: 1,
    id: '',
    title: '',
    severity: 'CRITICAL' as 'CRITICAL' | 'HIGH' | 'MEDIUM',
    assignee: 'Lead Developer'
  };

  editProjectForm = {
    name: '',
    region: 'US-EAST',
    status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE'
  };

  createProjectForm = {
    region: 'US-EAST',
    name: ''
  };

  editFeatureForm = {
    name: '',
    sprint: 1,
    version: 'v1.0.0',
    status: 'ACTIVE'
  };

  createFeatureForm = {
    name: '',
    sprint: 1,
    version: 'v1.0.0'
  };

  ngOnInit(): void {
    this.loadUserSession();
    this.initDefaultSelection();
    this.fetchBackendData();
  }

  loadUserSession(): void {
    const fullName = localStorage.getItem('fullName');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    const designation = localStorage.getItem('designation');

    if (fullName) {
      this.userName = fullName;
    } else if (username) {
      this.userName = username;
    }

    if (role) {
      this.userRole = designation ? `${designation} (${role})` : `${role}`;
    }

    if (this.userName) {
      const parts = this.userName.trim().split(/\s+/);
      if (parts.length >= 2) {
        this.userInitials = (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
      } else if (parts.length === 1 && parts[0].length > 0) {
        this.userInitials = parts[0].substring(0, 2).toUpperCase();
      }
    }
  }

  initDefaultSelection(): void {
    const projects = this.filteredProjects;
    if (projects.length > 0) {
      this.selectProject(projects[0]);
    }
  }

  fetchBackendData(): void {
    this.isApiLoading = true;
    this.testerService.getProjects().subscribe({
      next: (response) => {
        console.log('projects response',response);
        this.isApiLoading = false;
        const projectList = response?.data;
        if (Array.isArray(projectList) && projectList.length > 0) {
          this.mapBackendProjects(projectList);
        } else {
          this.allProjects = [];
          this.availableRegions = [];
          this.projectData = {};
          this.activeProject = null;
          this.activeFeature = null;
          this.showToast('No projects found in database. Use "+ New" to create one.', 'info');
        }
      },
      error: (err) => {
        console.log(err)
        this.isApiLoading = false;
        console.warn('Backend API /project error:', err);
        const status = err?.status;
        const msg = err?.error?.message;
        if (status === 401) {
          this.showToast('Authentication required (401). Retrying with active tester session...', 'info');
        } else if (status === 403) {
          this.showToast('Access Denied (403): Your account requires TESTER or MANAGER privileges.', 'error');
        } else if (status === 0) {
          this.showToast('Cannot connect to API Gateway on port 8082. Please check backend services.', 'error');
        } else {
          this.showToast(`Error loading projects: ${msg || err?.message || 'Server error'}`, 'error');
        }
      }
    });

    this.fetchBackendNotifications();
  }

  mapBackendProjects(list: any[]): void {
    const rawRegions = new Set<string>();

    this.allProjects = list.map((item, index) => {
      const proj = item.project || item;
      const rawId = proj.projectId || (index + 1);
      const name = proj.projectName || `Project ${rawId}`;
      const region = (proj.region || 'General').trim();
      if (region) {
        rawRegions.add(region);
      }

      return {
        id: `PROJ-${rawId}`,
        rawId: rawId,
        name: name,
        version: proj.version ? `v${proj.version}` : 'v1.0',
        status: proj.status || 'ACTIVE',
        region: region,
        features: []
      };
    });

    this.availableRegions = Array.from(rawRegions).sort();

    // Populate projectData by region for quick access
    const newProjectData: Record<string, ProjectItem[]> = {};
    this.availableRegions.forEach(reg => {
      newProjectData[reg] = this.allProjects.filter(p => (p.region || '').toLowerCase() === reg.toLowerCase());
    });
    this.projectData = newProjectData;

    // Automatically select the first project in current region filter
    const currentList = this.filteredProjects;
    if (currentList.length > 0) {
      this.selectProject(currentList[0]);
    } else if (this.allProjects.length > 0) {
      this.activeRegion = 'ALL';
      this.selectProject(this.allProjects[0]);
    } else {
      this.activeProject = null;
      this.activeFeature = null;
    }
  }

  fetchFeaturesForProject(project: ProjectItem): void {
    if (!project.rawId) return;

    this.testerService.getFeaturesByProject(project.rawId).subscribe({
      next: (response) => {
        const featureList = response?.data;
        if (Array.isArray(featureList)) {
          project.features = featureList.map((item, idx) => {
            const feat = item.feature || item;
            const featId = feat.featureId || (idx + 1);
            return {
              id: `FEAT-${featId}`,
              rawId: featId,
              name: feat.featureName || `Feature ${featId}`,
              sprint: `Sprint ${feat.sprint || 1} • ${feat.featureVersion || 'v1.0'}`,
              sprintNum: feat.sprint || 1,
              bugs: 0,
              tcCount: 0,
              status: 'PASSED'
            };
          });

          if (project.features.length > 0) {
            this.selectFeature(project.features[0]);
          } else {
            this.activeFeature = null;
          }
        } else {
          project.features = [];
          this.activeFeature = null;
        }
      },
      error: (err) => {
        console.warn('Backend /feature/project error:', err?.message || err);
        project.features = [];
        this.activeFeature = null;
      }
    });
  }

  fetchTestCasesForFeature(feature: FeatureItem): void {
    if (!feature.rawId) return;

    this.testerService.getTestCases({ featureId: feature.rawId }).subscribe({
      next: (response) => {
        const pageData = response?.data;
        const tcList = pageData?.content || (Array.isArray(pageData) ? pageData : null);

        if (Array.isArray(tcList)) {
          const mappedCases: TestCaseItem[] = tcList.map((item, idx) => {
            const tc = item.testCase || item;
            const exec = item.execution || {};
            const val = item.validations || {};
            const rawId = tc.testcaseId || (idx + 1);

            return {
              id: tc.testcaseFormatId || `TC_${rawId}`,
              rawId: rawId,
              title: tc.title || `Test Case ${rawId}`,
              featureName: feature.name,
              featureId: feature.rawId,
              precondition: exec.precondition || 'Precondition verified',
              type: tc.testType || 'FUNCTIONAL',
              priority: tc.testPriority || 'CRITICAL',
              uiValidation: val.uiValidations || 'Verify UI screen state',
              dbValidation: val.dbValidations || 'Verify DB table record state',
              status: tc.testcaseStatus || 'PASSED'
            };
          });

          this.testCases = [
            ...mappedCases,
            ...this.testCases.filter(t => t.featureName !== feature.name)
          ];
          feature.tcCount = mappedCases.length;
        } else {
          this.testCases = this.testCases.filter(t => t.featureName !== feature.name);
          feature.tcCount = 0;
        }
      },
      error: (err) => {
        console.warn('Backend /testcases error:', err?.message || err);
        this.testCases = this.testCases.filter(t => t.featureName !== feature.name);
        feature.tcCount = 0;
      }
    });

    this.fetchBugsForFeature(feature);
  }

  fetchBugsForFeature(feature: FeatureItem): void {

    this.testerService.getBugs(0, 50).subscribe({
      next: (response) => {
        console.log('bugs response',response  )
        const pageData = response?.data;
        const bugList = pageData?.content || (Array.isArray(pageData) ? pageData : null);

        if (Array.isArray(bugList)) {
          const mappedBugs: DefectItem[] = bugList.map((item, idx) => {
            const b = item.bug || item;
            const rawId = b.bugId || (idx + 1);
            return {
              id: b.bugFormatId || `BUG-${rawId}`,
              rawId: rawId,
              title: b.title || `Bug ${rawId}`,
              featureName: item.feature?.featureName || feature.name,
              originTestCaseId: item.testCase?.testcaseFormatId || `TC_ORIGIN`,
              originTestCaseRawId: item.testCase?.testcaseId || 1,
              precondition: `Precondition: Logged from test case execution`,
              severity: b.severity || 'CRITICAL',
              assignee: b.assignedTo ? `Dev #${b.assignedTo}` : 'Lead Developer',
              status: b.status || 'OPEN'
            };
          });

          this.defects = mappedBugs;
          const featBugs = this.defects.filter(b => b.featureName === feature.name);
          feature.bugs = featBugs.length;
          feature.status = featBugs.length > 0 ? 'BUGS' : 'PASSED';
        } else {
          this.defects = [];
          feature.bugs = 0;
          feature.status = 'PASSED';
        }
      },
      error: (err) => {
        console.warn('Backend /bugs error:', err?.message || err);
        this.defects = [];
        feature.bugs = 0;
        feature.status = 'PASSED';
      }
    });
  }

  fetchBackendNotifications(): void {
    this.testerService.getNotifications().subscribe({
      next: (response) => {
        const notifList = response?.data;
        if (Array.isArray(notifList)) {
          this.notifications = notifList.map((n, idx) => ({
            id: `notif-${n.notificationId || idx}`,
            rawId: n.notificationId,
            type: 'COMMENT',
            title: n.bugFormatId ? `Defect Update — ${n.bugFormatId}` : 'System Notification',
            highlightId: n.bugFormatId,
            isNew: n.notificationStatus !== 'READ',
            read: n.notificationStatus === 'READ',
            message: n.message || 'Notification received.',
            featureName: this.activeFeature?.name || 'General',
            timeAgo: 'Recently'
          }));
        } else {
          this.notifications = [];
        }
      },
      error: (err) => {
        console.warn('Backend /in-app-notifications error:', err?.message || err);
        this.notifications = [];
      }
    });
  }

  // Region Handlers
  onRegionChange(newRegion: string): void {
    this.activeRegion = newRegion;
    this.createProjectForm.region = (newRegion === 'ALL' ? (this.availableRegions[0] || 'General') : newRegion);
    this.projectSearchQuery = '';
    const projects = this.filteredProjects;
    if (projects.length > 0) {
      this.selectProject(projects[0]);
    } else {
      this.activeProject = null;
      this.activeFeature = null;
      this.testCases = [];
      this.defects = [];
    }
    this.showToast(newRegion === 'ALL' ? 'Showing all project regions' : `Region filtered: ${newRegion}`, 'info');
  }

  getRegionProjectCount(region: string): number {
    if (region === 'ALL') return this.allProjects.length;
    return this.allProjects.filter(p => (p.region || '').toLowerCase() === region.toLowerCase()).length;
  }

  // Project Filtering & Selection
  get filteredProjects(): ProjectItem[] {
    const query = this.projectSearchQuery.toLowerCase().trim();
    return this.allProjects.filter(p => {
      const matchesRegion = (this.activeRegion === 'ALL') || ((p.region || '').toLowerCase() === this.activeRegion.toLowerCase());
      const matchesQuery = !query || p.name.toLowerCase().includes(query) || p.id.toLowerCase().includes(query);
      return matchesRegion && matchesQuery;
    });
  }

  selectProject(project: ProjectItem): void {
    this.activeProject = project;
    this.featureSearchQuery = '';
    this.featureFilterCategory = 'ALL';
    this.fetchFeaturesForProject(project);

    if (project.features.length > 0) {
      this.selectFeature(project.features[0]);
    } else {
      this.activeFeature = null;
      this.testCases = [];
      this.defects = [];
    }
  }

  onProjectDropdownChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const projId = select.value;
    const found = this.filteredProjects.find(p => p.id === projId) || this.allProjects.find(p => p.id === projId);
    if (found) {
      this.selectProject(found);
    }
  }

  // Feature Filtering & Selection
  get filteredFeatures(): FeatureItem[] {
    if (!this.activeProject) return [];
    const query = this.featureSearchQuery.toLowerCase().trim();
    return this.activeProject.features.filter(f => {
      const matchesSearch = !query ||
        f.name.toLowerCase().includes(query) ||
        f.sprint.toLowerCase().includes(query);

      let matchesCategory = true;
      if (this.featureFilterCategory === 'BUGS') {
        matchesCategory = f.bugs > 0;
      } else if (this.featureFilterCategory === 'PASSED') {
        matchesCategory = f.bugs === 0;
      }

      return matchesSearch && matchesCategory;
    });
  }

  selectFeature(feature: FeatureItem): void {
    this.activeFeature = feature;
    this.activeTab = 'tc';
    this.tcCurrentPage = 1;
    this.bugCurrentPage = 1;
    this.fetchTestCasesForFeature(feature);
  }

  setFeatureCategoryFilter(cat: 'ALL' | 'BUGS' | 'PASSED'): void {
    this.featureFilterCategory = cat;
  }

  // Sub-Tab Switching
  switchTab(tab: 'tc' | 'bugs'): void {
    this.activeTab = tab;
  }

  // Test Case Filtering & Pagination
  get currentFeatureTestCases(): TestCaseItem[] {
    if (!this.activeFeature) return [];
    return this.testCases.filter(tc => 
      tc.featureId === this.activeFeature?.rawId ||
      tc.featureName.toLowerCase() === this.activeFeature?.name.toLowerCase()
    );
  }

  get filteredTestCases(): TestCaseItem[] {
    const list = this.currentFeatureTestCases;
    const query = this.tcSearchQuery.toLowerCase().trim();

    return list.filter(tc => {
      const matchesSearch = !query ||
        tc.id.toLowerCase().includes(query) ||
        tc.title.toLowerCase().includes(query) ||
        tc.precondition.toLowerCase().includes(query);

      const matchesType = (this.tcTypeFilter === 'ALL') || (tc.type === this.tcTypeFilter);
      const matchesStatus = (this.tcStatusFilter === 'ALL') || (tc.status === this.tcStatusFilter);

      return matchesSearch && matchesType && matchesStatus;
    });
  }

  get tcTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredTestCases.length / this.tcPageSize));
  }

  get paginatedTestCases(): TestCaseItem[] {
    const startIndex = (this.tcCurrentPage - 1) * this.tcPageSize;
    return this.filteredTestCases.slice(startIndex, startIndex + this.tcPageSize);
  }

  setTcPage(page: number): void {
    if (page >= 1 && page <= this.tcTotalPages) {
      this.tcCurrentPage = page;
    }
  }

  nextTcPage(): void {
    if (this.tcCurrentPage < this.tcTotalPages) {
      this.tcCurrentPage++;
    }
  }

  prevTcPage(): void {
    if (this.tcCurrentPage > 1) {
      this.tcCurrentPage--;
    }
  }

  onTcPageSizeChange(newSize: number): void {
    this.tcPageSize = Number(newSize);
    this.tcCurrentPage = 1;
  }

  // Defect Filtering & Pagination
  get currentFeatureDefects(): DefectItem[] {
    if (!this.activeFeature) return [];
    return this.defects.filter(b => 
      b.featureName.toLowerCase() === this.activeFeature?.name.toLowerCase() ||
      (this.activeFeature?.rawId === 1 && b.originTestCaseId.includes('LOGIN'))
    );
  }

  get filteredDefects(): DefectItem[] {
    const list = this.currentFeatureDefects;
    const query = this.bugSearchQuery.toLowerCase().trim();

    return list.filter(b => {
      const matchesSearch = !query ||
        b.id.toLowerCase().includes(query) ||
        b.title.toLowerCase().includes(query) ||
        b.assignee.toLowerCase().includes(query);

      const matchesSev = (this.bugSevFilter === 'ALL') || (b.severity === this.bugSevFilter);
      const matchesStatus = (this.bugStatusFilter === 'ALL') || (b.status === this.bugStatusFilter);

      return matchesSearch && matchesSev && matchesStatus;
    });
  }

  get bugTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDefects.length / this.bugPageSize));
  }

  get paginatedDefects(): DefectItem[] {
    const startIndex = (this.bugCurrentPage - 1) * this.bugPageSize;
    return this.filteredDefects.slice(startIndex, startIndex + this.bugPageSize);
  }

  setBugPage(page: number): void {
    if (page >= 1 && page <= this.bugTotalPages) {
      this.bugCurrentPage = page;
    }
  }

  nextBugPage(): void {
    if (this.bugCurrentPage < this.bugTotalPages) {
      this.bugCurrentPage++;
    }
  }

  prevBugPage(): void {
    if (this.bugCurrentPage > 1) {
      this.bugCurrentPage--;
    }
  }

  onBugPageSizeChange(newSize: number): void {
    this.bugPageSize = Number(newSize);
    this.bugCurrentPage = 1;
  }

  // Defect State Transition
  advanceBugState(bug: DefectItem): void {
    const states: ('OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'FIXED' | 'CLOSED')[] = [
      'OPEN', 'IN_PROGRESS', 'RESOLVED', 'FIXED', 'CLOSED'
    ];
    const currentIndex = states.indexOf(bug.status);
    const nextIndex = (currentIndex + 1) % states.length;
    const nextState = states[nextIndex];
    bug.status = nextState;

    if (bug.rawId) {
      this.testerService.patchBug(bug.rawId, { status: nextState }).subscribe({
        next: () => this.showToast(`Defect [${bug.id}] status moved to: ${bug.status}`, 'info'),
        error: () => this.showToast(`Defect [${bug.id}] updated locally to: ${bug.status}`, 'info')
      });
    } else {
      this.showToast(`Defect [${bug.id}] status moved to: ${bug.status}`, 'info');
    }
  }

  // Defect Creation strictly from failed test case
  triggerCreateBugFromTest(tc: TestCaseItem): void {
    if (!this.activeFeature) return;
    const count = this.defects.length + 1;
    const cleanFeat = this.activeFeature.name.toUpperCase().replace(/[^A-Z0-9]+/g, '_');

    this.bugForm = {
      featureName: this.activeFeature.name,
      originTestCaseId: tc.id,
      originTestCaseRawId: tc.rawId || 1,
      id: `BUG-${cleanFeat}-00${count}`,
      title: `[${tc.id}] ${tc.title}`,
      severity: 'CRITICAL',
      assignee: 'Devon (Developer)'
    };

    this.openModal('modal-bug');
  }

  saveDefect(): void {
    if (!this.activeFeature) return;

    const newDefect: DefectItem = {
      id: this.bugForm.id,
      title: this.bugForm.title || 'New Defect',
      featureName: this.activeFeature.name,
      originTestCaseId: this.bugForm.originTestCaseId,
      precondition: `Precondition: Logged from test case execution ${this.bugForm.originTestCaseId}`,
      severity: this.bugForm.severity,
      assignee: this.bugForm.assignee,
      status: 'OPEN'
    };

    // Call Backend API
    const testcaseId = this.bugForm.originTestCaseRawId || 1;
    const backendBugPayload = {
      title: newDefect.title,
      description: `Logged from ${newDefect.originTestCaseId}`,
      severity: newDefect.severity,
      priority: 'CRITICAL',
      status: 'OPEN',
      comments: `Auto-mapped from testcase ${newDefect.originTestCaseId}`
    };

    this.testerService.createBug(testcaseId, backendBugPayload).subscribe({
      next: (res) => {
        const createdBug = res?.data?.bug || res?.data;
        if (createdBug?.bugId) {
          newDefect.rawId = createdBug.bugId;
          newDefect.id = createdBug.bugFormatId || newDefect.id;
        }
        this.finishDefectCreation(newDefect);
      },
      error: (err) => {
        console.warn('Backend createBug returned fallback:', err?.message || err);
        this.finishDefectCreation(newDefect);
      }
    });
  }

  private finishDefectCreation(newDefect: DefectItem): void {
    if (!this.activeFeature) return;
    this.defects.unshift(newDefect);
    this.activeFeature.bugs += 1;
    this.activeFeature.status = 'BUGS';

    this.notifications.unshift({
      id: `notif-${Date.now()}`,
      type: 'COMMENT',
      title: 'Defect Logged to Developer',
      highlightId: newDefect.id,
      isNew: true,
      read: false,
      message: `QA logged ${newDefect.id}: "${newDefect.title}" assigned to ${newDefect.assignee}.`,
      featureName: this.activeFeature.name,
      timeAgo: 'Just now',
      originTestCaseId: newDefect.originTestCaseId
    });

    this.closeModal('modal-bug');
    this.activeTab = 'bugs';
    this.showToast(`Defect ${newDefect.id} logged and developer notified!`, 'success');
  }

  // Add Manual Test Case
  saveNewTestCase(): void {
    if (!this.activeFeature) return;
    if (!this.newTc.title.trim()) {
      this.showToast('Please provide a Test Case title', 'error');
      return;
    }

    const randomSuffix = Math.floor(100 + Math.random() * 900);
    const tcFormatId = `TC_AUTH_${randomSuffix}`;

    const newCase: TestCaseItem = {
      id: tcFormatId,
      title: this.newTc.title.trim(),
      featureName: this.activeFeature.name,
      featureId: this.activeFeature.rawId,
      precondition: 'User session verified with valid test data',
      type: this.newTc.type,
      priority: this.newTc.priority,
      uiValidation: this.newTc.uiValidation.trim() || 'Verify UI screen element state and toast behavior.',
      dbValidation: this.newTc.dbValidation.trim() || 'Verify DB table record state and audit log.',
      status: 'PASSED'
    };

    // Call Backend API
    const payload = {
      testCase: {
        featureId: this.activeFeature.rawId || 1,
        testcaseFormatId: tcFormatId,
        title: newCase.title,
        testType: newCase.type,
        testPriority: newCase.priority,
        testcaseStatus: 'PASSED'
      },
      testingExecutionRequest: {
        precondition: newCase.precondition,
        uiValidations: newCase.uiValidation,
        dbValidations: newCase.dbValidation
      }
    };

    this.testerService.createTestCase(payload).subscribe({
      next: (res) => {
        const createdTc = res?.data?.testCase || res?.data;
        if (createdTc?.testcaseId) {
          newCase.rawId = createdTc.testcaseId;
          newCase.id = createdTc.testcaseFormatId || newCase.id;
        }
        this.finishTestCaseCreation(newCase);
      },
      error: (err) => {
        console.warn('Backend createTestCase returned fallback:', err?.message || err);
        this.finishTestCaseCreation(newCase);
      }
    });
  }

  private finishTestCaseCreation(newCase: TestCaseItem): void {
    if (!this.activeFeature) return;
    this.testCases.unshift(newCase);
    this.activeFeature.tcCount += 1;

    this.newTc = {
      title: '',
      type: 'FUNCTIONAL',
      priority: 'CRITICAL',
      uiValidation: '',
      dbValidation: ''
    };

    this.closeModal('modal-testcase');
    this.showToast(`Test Case [${newCase.id}] added to ${this.activeFeature.name}!`, 'success');
  }

  // Dedicated Bug Chat / Comments
  openSpecificBugChat(bug: DefectItem): void {
    this.activeChatBug = bug;
    this.newChatMessage = '';
    this.chatMessages = [];
    this.openModal('modal-bug-chat');

    if (bug.rawId) {
      this.testerService.getBugComments(bug.rawId).subscribe({
        next: (res) => {
          const commentList = res?.data;
          if (Array.isArray(commentList)) {
            this.chatMessages = commentList.map(c => ({
              sender: c.createdByName === this.userName ? 'tester' : 'dev',
              senderName: c.createdByName || 'Developer',
              text: c.comment || '',
              timestamp: 'Recently'
            }));
          }
        },
        error: (err) => {
          console.warn('Backend getBugComments error:', err?.message || err);
        }
      });
    }
  }

  sendChatMessage(): void {
    if (!this.newChatMessage.trim() || !this.activeChatBug) return;
    const text = this.newChatMessage.trim();

    this.chatMessages.push({
      sender: 'tester',
      senderName: `${this.userName} (Tester)`,
      text: text,
      timestamp: 'Just now'
    });
    this.newChatMessage = '';

    if (this.activeChatBug.rawId) {
      this.testerService.addBugComment(this.activeChatBug.rawId, text).subscribe({
        next: () => console.log('Comment saved to backend successfully'),
        error: (err) => console.warn('Comment saved locally:', err?.message || err)
      });
    }
  }

  // In-App Notifications
  get unreadNotificationCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  get filteredNotifications(): NotificationItem[] {
    return this.notifications.filter(n => {
      if (this.notifFilterCategory === 'ALL') return true;
      if (this.notifFilterCategory === 'UNREAD') return !n.read;
      return n.type === this.notifFilterCategory;
    });
  }

  setNotifFilterCategory(category: 'ALL' | 'UNREAD' | 'FIXED' | 'COMMENT'): void {
    this.notifFilterCategory = category;
  }

  toggleNotifRead(item: NotificationItem): void {
    item.read = !item.read;
    if (item.rawId) {
      this.testerService.patchNotification(item.rawId, item.read ? 'READ' : 'UNREAD').subscribe();
    }
  }

  markAllNotificationsAsRead(): void {
    this.notifications.forEach(n => {
      n.read = true;
      if (n.rawId) {
        this.testerService.patchNotification(n.rawId, 'READ').subscribe();
      }
    });
    this.showToast('All notifications marked as read', 'info');
  }

  dismissNotification(item: NotificationItem): void {
    this.notifications = this.notifications.filter(n => n.id !== item.id);
    if (item.rawId) {
      this.testerService.deleteNotification(item.rawId).subscribe();
    }
  }

  viewBugFromNotification(notif: NotificationItem): void {
    this.closeModal('modal-in-app-notifications');
    let bug = this.defects.find(b => b.id === notif.highlightId);
    if (!bug && this.defects.length > 0) {
      bug = this.defects[0];
    }
    if (bug) {
      this.openSpecificBugChat(bug);
    }
  }

  openFeatureFromNotification(featureName: string): void {
    this.closeModal('modal-in-app-notifications');
    for (const proj of this.allProjects) {
      const feat = proj.features.find(f => f.name === featureName);
      if (feat) {
        this.selectProject(proj);
        this.selectFeature(feat);
        return;
      }
    }
  }

  // Project Modals
  openEditProjectModal(): void {
    if (!this.activeProject) return;
    this.editProjectForm = {
      name: this.activeProject.name,
      region: this.activeProject.region || 'General',
      status: this.activeProject.status
    };
    this.openModal('modal-project-edit');
  }

  saveProjectDetails(): void {
    if (!this.activeProject) return;
    this.activeProject.name = this.editProjectForm.name;
    this.activeProject.status = this.editProjectForm.status;
    if (this.editProjectForm.region && this.editProjectForm.region.trim()) {
      const reg = this.editProjectForm.region.trim();
      this.activeProject.region = reg;
      if (!this.availableRegions.includes(reg)) {
        this.availableRegions.push(reg);
        this.availableRegions.sort();
      }
    }
    this.closeModal('modal-project-edit');
    this.showToast(`Project details updated successfully: "${this.activeProject.name}"`, 'success');
  }

  openCreateProjectModal(): void {
    this.createProjectForm = {
      region: this.activeRegion === 'ALL' ? (this.availableRegions[0] || 'General') : this.activeRegion,
      name: ''
    };
    this.openModal('modal-project-create');
  }

  createProjectAction(): void {
    if (!this.createProjectForm.name.trim()) {
      this.showToast('Please enter a project name', 'error');
      return;
    }

    const projectName = this.createProjectForm.name.trim();
    const region = this.createProjectForm.region.trim() || 'General';

    // Call Backend API
    this.testerService.createProject({ projectName, region }).subscribe({
      next: (res) => {
        const created = res?.data?.project || res?.data;
        const rawId = created?.projectId || Math.floor(100 + Math.random() * 900);
        this.addCreatedProjectLocally(projectName, region, rawId);
      },
      error: (err) => {
        console.warn('Backend createProject returned fallback:', err?.message || err);
        const rawId = Math.floor(100 + Math.random() * 900);
        this.addCreatedProjectLocally(projectName, region, rawId);
      }
    });
  }

  private addCreatedProjectLocally(projectName: string, region: string, rawId: number): void {
    const newProj: ProjectItem = {
      id: `PROJ-${rawId}`,
      rawId: rawId,
      name: projectName,
      version: 'v1.0',
      status: 'ACTIVE',
      region: region,
      features: []
    };

    this.allProjects.push(newProj);
    if (!this.availableRegions.includes(region)) {
      this.availableRegions.push(region);
      this.availableRegions.sort();
    }
    if (!this.projectData[region]) {
      this.projectData[region] = [];
    }
    this.projectData[region].push(newProj);

    this.selectProject(newProj);
    this.closeModal('modal-project-create');
    this.showToast(`New project created: "${newProj.name}" in region ${region}`, 'success');
  }

  // Feature Modals
  openEditFeatureModal(): void {
    if (!this.activeFeature) return;
    this.editFeatureForm = {
      name: this.activeFeature.name,
      sprint: this.activeFeature.sprintNum,
      version: this.activeFeature.sprint.split('•')[1]?.trim() || 'v2.4.0',
      status: 'ACTIVE'
    };
    this.openModal('modal-feature-edit');
  }

  saveFeatureDetails(): void {
    if (!this.activeFeature) return;
    this.activeFeature.name = this.editFeatureForm.name;
    this.activeFeature.sprintNum = this.editFeatureForm.sprint;
    this.activeFeature.sprint = `Sprint ${this.editFeatureForm.sprint} • ${this.editFeatureForm.version}`;

    if (this.activeFeature.rawId) {
      this.testerService.updateFeature(this.activeFeature.rawId, {
        featureName: this.activeFeature.name,
        sprint: this.activeFeature.sprintNum,
        featureVersion: this.editFeatureForm.version
      }).subscribe({
        next: () => this.showToast(`Feature updated: "${this.activeFeature?.name}"`, 'success'),
        error: () => this.showToast(`Feature updated locally: "${this.activeFeature?.name}"`, 'success')
      });
    } else {
      this.showToast(`Feature updated: "${this.activeFeature.name}"`, 'success');
    }

    this.closeModal('modal-feature-edit');
  }

  openCreateFeatureModal(): void {
    this.createFeatureForm = {
      name: '',
      sprint: 1,
      version: 'v1.0.0'
    };
    this.openModal('modal-feature-create');
  }

  createFeatureAction(): void {
    if (!this.activeProject) return;
    if (!this.createFeatureForm.name.trim()) {
      this.showToast('Please enter a feature name', 'error');
      return;
    }

    const featureName = this.createFeatureForm.name.trim();
    const sprint = this.createFeatureForm.sprint;
    const version = this.createFeatureForm.version;
    const projectId = this.activeProject.rawId || 1;

    this.testerService.createFeature({
      projectId,
      featureName,
      sprint,
      featureVersion: version
    }).subscribe({
      next: (res) => {
        const created = res?.data?.feature || res?.data;
        const rawId = created?.featureId || Math.floor(100 + Math.random() * 900);
        this.addCreatedFeatureLocally(featureName, sprint, version, rawId);
      },
      error: (err) => {
        console.warn('Backend createFeature returned fallback:', err?.message || err);
        const rawId = Math.floor(100 + Math.random() * 900);
        this.addCreatedFeatureLocally(featureName, sprint, version, rawId);
      }
    });
  }

  private addCreatedFeatureLocally(name: string, sprint: number, version: string, rawId: number): void {
    if (!this.activeProject) return;
    const newFeature: FeatureItem = {
      id: `FEAT-${rawId}`,
      rawId: rawId,
      name: name,
      sprint: `Sprint ${sprint} • ${version}`,
      sprintNum: sprint,
      bugs: 0,
      tcCount: 0,
      status: 'PASSED'
    };

    this.activeProject.features.push(newFeature);
    this.selectFeature(newFeature);
    this.closeModal('modal-feature-create');
    this.showToast(`Feature added: "${newFeature.name}"`, 'success');
  }

  // Attachments Actions
  uploadProjectFile(): void {
    if (!this.activeProject?.rawId) {
      this.showToast('Please select a project first', 'error');
      return;
    }
    const input = document.createElement('input');
    input.type = 'file';
    input.multiple = true;
    input.onchange = (e: any) => {
      const files: FileList = e.target?.files;
      if (files && files.length > 0 && this.activeProject?.rawId) {
        const fileArray = Array.from(files);
        this.testerService.uploadProjectAttachments(this.activeProject.rawId, fileArray).subscribe({
          next: () => {
            fileArray.forEach(f => {
              this.projectAttachments.push({
                name: f.name,
                size: `${(f.size / 1024).toFixed(0)} KB`,
                icon: f.name.endsWith('.pdf') ? 'fa-file-pdf' : f.name.endsWith('.zip') ? 'fa-file-zipper' : 'fa-file',
                color: 'var(--indigo-600)'
              });
            });
            this.showToast(`${fileArray.length} file(s) attached successfully!`, 'success');
          },
          error: (err) => {
            console.warn('Backend upload error:', err);
            fileArray.forEach(f => {
              this.projectAttachments.push({
                name: f.name,
                size: `${(f.size / 1024).toFixed(0)} KB`,
                icon: 'fa-file',
                color: 'var(--indigo-600)'
              });
            });
            this.showToast(`Attached ${fileArray.length} file(s)`, 'info');
          }
        });
      }
    };
    input.click();
  }

  uploadFeatureFile(): void {
    if (!this.activeFeature?.rawId) {
      this.showToast('Please select a feature first', 'error');
      return;
    }
    const input = document.createElement('input');
    input.type = 'file';
    input.multiple = true;
    input.onchange = (e: any) => {
      const files: FileList = e.target?.files;
      if (files && files.length > 0 && this.activeFeature?.rawId) {
        const fileArray = Array.from(files);
        this.testerService.uploadFeatureAttachments(this.activeFeature.rawId, fileArray).subscribe({
          next: () => {
            fileArray.forEach(f => {
              this.featureAttachments.push({
                name: f.name,
                size: `${(f.size / 1024).toFixed(0)} KB`,
                icon: f.name.endsWith('.pdf') ? 'fa-file-pdf' : 'fa-file-lines',
                color: 'var(--emerald-600)'
              });
            });
            this.showToast(`${fileArray.length} file(s) attached to feature!`, 'success');
          },
          error: (err) => {
            console.warn('Backend upload error:', err);
            fileArray.forEach(f => {
              this.featureAttachments.push({
                name: f.name,
                size: `${(f.size / 1024).toFixed(0)} KB`,
                icon: 'fa-file-lines',
                color: 'var(--emerald-600)'
              });
            });
            this.showToast(`Attached ${fileArray.length} file(s)`, 'info');
          }
        });
      }
    };
    input.click();
  }

  downloadMockFile(name: string): void {
    this.showToast(`Downloading "${name}"...`, 'info');
  }

  downloadExcelTemplate(): void {
    if (this.activeFeature?.rawId) {
      this.testerService.downloadTestCaseTemplate(this.activeFeature.rawId).subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `testcase_template_feature_${this.activeFeature?.rawId}.xlsx`;
          a.click();
          window.URL.revokeObjectURL(url);
          this.showToast(`Template downloaded successfully!`, 'success');
        },
        error: () => {
          this.showToast(`Downloading blank Excel template (.xlsx) for "${this.activeFeature?.name}"...`, 'info');
        }
      });
    } else {
      this.showToast(`Downloading dynamic blank Excel template (.xlsx) for "${this.activeFeature?.name}"...`, 'info');
    }
  }

  handleExcelImport(): void {
    if (!this.activeFeature?.rawId) {
      this.showToast('Please select a feature first', 'error');
      return;
    }
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.xlsx, .xls';
    input.onchange = (e: any) => {
      const file = e.target?.files?.[0];
      if (file && this.activeFeature?.rawId) {
        this.isApiLoading = true;
        this.testerService.uploadExcelTestCases(this.activeFeature.rawId, file).subscribe({
          next: () => {
            this.isApiLoading = false;
            this.closeModal('modal-excel');
            this.showToast(`Spreadsheet "${file.name}" uploaded successfully!`, 'success');
            if (this.activeFeature) {
              this.fetchTestCasesForFeature(this.activeFeature);
            }
          },
          error: (err) => {
            this.isApiLoading = false;
            console.error('Excel upload error:', err);
            this.showToast(err?.error?.message || 'Failed to upload test cases spreadsheet', 'error');
          }
        });
      }
    };
    input.click();
  }

  // Modal helpers
  openModal(modalId: string): void {
    this.activeModal = modalId;
  }

  closeModal(modalId?: string): void {
    if (!modalId || this.activeModal === modalId) {
      this.activeModal = null;
    }
  }

  // Toast feedback helper
  showToast(msg: string, type: 'success' | 'info' | 'error' = 'info'): void {
    this.toastMessage = msg;
    this.toastType = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
    }, 3200);
  }

  // Logout
  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
