import { Component, OnInit, ChangeDetectorRef, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { BugService } from '../../core/services/bug.service';
import { TestCaseService } from '../../core/services/testcase.service';
import { ProjectService } from '../../core/services/project.service';
import { FeatureService } from '../../core/services/feature.service';
import { AuthService } from '../../core/services/authService';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationsModalComponent } from '../tester-workspace/components/notifications-modal/notifications-modal.component';
import {
  Bug,
  BugComment,
  AttachmentItem,
  TestCase,
  Project,
  Feature,
  InAppNotification,
  TestCaseAuditLog,
  BugAuditHistory
} from '../../model/workspace/workspace.model';
import { UserProfile } from '../../model/auth/userProfile';

interface FeatureLookup {
  featureId: number;
  featureName: string;
  sprint: number;
  projectId: number;
  projectName: string;
}

@Component({
  selector: 'app-developer-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NotificationsModalComponent],
  templateUrl: './developer-workspace.html',
  styleUrl: './developer-workspace.css'
})
export class DeveloperWorkspace implements OnInit, OnDestroy {
  private readonly bugService = inject(BugService);
  private readonly testCaseService = inject(TestCaseService);
  private readonly projectService = inject(ProjectService);
  private readonly featureService = inject(FeatureService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);

  // In-App Notifications State
  notifications: InAppNotification[] = [];
  unreadNotificationCount = 0;
  isNotificationsModalOpen = false;

  // User Profile & Assigned ID
  currentUser: UserProfile | null = null;
  currentUserId: number | string | null = null;
  userName = 'Developer';
  userRole = 'DEVELOPER';
  userInitials = 'DV';

  // Projects & Features Lookup (for displaying Project/Feature/Sprint in Bug Details)
  projects: Project[] = [];
  featureLookupMap = new Map<number, FeatureLookup>();

  // Bugs State
  bugs: Bug[] = [];
  isLoading = false;

  // Workflow Categorization Tabs: ACTIVE vs (FIXED, NOT_A_BUG, CLOSED) vs ALL
  defectCategoryTab: 'ACTIVE' | 'RESOLVED' | 'ALL' = 'ACTIVE';

  // Search Filter & Severity Filter
  bugSearch = '';
  bugFilterSeverity = 'ALL';
  readonly developerStatuses = ['IN_PROGRESS', 'FIXED', 'NOT_A_BUG'];

  isBugFrozen(bug: Bug | null): boolean {
    if (!bug || !bug.status) return false;
    const s = bug.status.toUpperCase().trim();
    return s === 'FIXED' || s === 'CLOSED' || s === 'RESOLVED' || s === 'NOT_A_BUG';
  }

  // Pagination
  bugCurrentPage = 1;
  bugPageSize = 10;
  readonly bugPageSizeOptions = [5, 10, 20, 50];

  // Defect & Linked TestCase Details Inspector (GET-Only + Attachments + Project/Feature info)
  isDetailModalOpen = false;
  activeBugForDetail: Bug | null = null;
  selectedTestCase: TestCase | null = null;
  isTestCaseLoading = false;
  testCaseLoadError = '';

  // Defect & TestCase Audit History State
  bugAuditHistory: BugAuditHistory[] = [];
  isLoadingBugHistory = false;
  testCaseAuditLogs: TestCaseAuditLog[] = [];
  isLoadingTcHistory = false;

  // Attachments State (for Developer File Evidence & Logs)
  activeBugAttachments: AttachmentItem[] = [];
  isAttachmentsLoading = false;
  isUploadingAttachment = false;

  // Standalone Attachments Modal
  isAttachmentsModalOpen = false;
  activeBugForAttachments: Bug | null = null;

  // In-Browser File Preview Lightbox State
  isPreviewOpen = false;
  isLoadingPreview = false;
  previewError: string | null = null;
  previewFile: AttachmentItem | null = null;
  previewFileType: 'image' | 'pdf' | 'text' | 'audio' | 'video' | 'unsupported' = 'unsupported';
  previewTextContent = '';
  previewBlobUrl: SafeResourceUrl | SafeUrl | null = null;
  private rawPreviewBlobUrl: string | null = null;
  previewImageZoom = 1;
  previewCopySuccess = false;

  // Chat / Discussion with QA Reporter
  isBugChatOpen = false;
  activeBugForChat: Bug | null = null;
  bugComments: BugComment[] = [];
  isBugChatLoading = false;
  isSendingComment = false;
  newChatMessage = '';

  // Quick Chat Replies for fast developer collaboration
  readonly quickReplies: string[] = [
    '🔍 Issue reproduced locally. Investigating cause.',
    '🚀 Fix committed to branch. Verified locally.',
    '❓ Could not reproduce with given test data. Please provide extra steps.',
    '✅ Defect fixed and verified. Ready for QA retest.',
    '⚡ Database/API constraint adjusted.'
  ];

  // Toast Banner for In-App Notification Confirmation
  statusToastMessage = '';
  toastTimeoutId: any = null;

  ngOnInit(): void {
    this.initUserProfile();
    this.loadProjectAndFeatureLookup();
    this.loadBugs();
    this.loadNotifications();
  }

  private initUserProfile(): void {
    const profile = this.authService.getCurrentUser();
    if (profile && profile.userId) {
      this.currentUser = profile;
      this.currentUserId = Number(profile.userId);
      this.userName = profile.fullName || profile.username || 'Developer';
      this.userRole = profile.role || 'DEVELOPER';
      this.userInitials = this.calculateInitials(this.userName);
    } else if (typeof localStorage !== 'undefined') {
      const storedId = localStorage.getItem('tat_user_id');
      const storedName = localStorage.getItem('tat_user_fullname') || localStorage.getItem('tat_user_name');
      const storedRole = localStorage.getItem('tat_user_role');
      if (storedId) {
        this.currentUserId = Number(storedId);
      }
      if (storedName) {
        this.userName = storedName;
        this.userInitials = this.calculateInitials(storedName);
      }
      if (storedRole) {
        this.userRole = storedRole;
      }
    }

    if (!this.currentUserId) {
      const uid = this.authService.getUserId();
      if (uid) {
        this.currentUserId = Number(uid);
      }
    }

    // If profile is not in memory or userId is pending, fetch profile to ensure fresh user id & reload
    if (!this.currentUser || !this.currentUserId) {
      this.authService.getProfile().subscribe({
        next: (res) => {
          if (res?.data) {
            this.currentUser = res.data;
            if (res.data.userId) {
              this.currentUserId = Number(res.data.userId);
            }
            if (res.data.fullName || res.data.username) {
              this.userName = res.data.fullName || res.data.username || this.userName;
              this.userInitials = this.calculateInitials(this.userName);
            }
            if (res.data.role) {
              this.userRole = res.data.role;
            }
            this.loadBugs();
            this.cdr.markForCheck();
          }
        },
        error: () => {}
      });
    }
  }

  private calculateInitials(name: string): string {
    if (!name) return 'DV';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  // --- Load Projects & Features for Metadata Lookup in Bug Details ---

  private loadProjectAndFeatureLookup(): void {
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects = projects || [];
        this.projects.forEach((p) => {
          this.featureService.getFeaturesByProject(p.projectId).subscribe({
            next: (feats) => {
              (feats || []).forEach((f) => {
                this.featureLookupMap.set(f.featureId, {
                  featureId: f.featureId,
                  featureName: f.featureName,
                  sprint: f.sprint,
                  projectId: p.projectId,
                  projectName: p.projectName
                });
              });
              this.cdr.markForCheck();
            },
            error: () => {}
          });
        });
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load project hierarchy:', err);
      }
    });
  }

  // --- Helper to get Project / Feature / Sprint for any Bug ---

  getBugProjectName(bug: Bug | null): string {
    if (!bug) return 'Project Workspace';
    if (bug.projectName) return bug.projectName;
    if (bug.featureId && this.featureLookupMap.has(bug.featureId)) {
      return this.featureLookupMap.get(bug.featureId)!.projectName;
    }
    if (this.projects.length > 0) {
      return this.projects[0].projectName;
    }
    return 'Project Workspace';
  }

  getBugFeatureName(bug: Bug | null): string {
    if (!bug) return 'Defect Resolution';
    if (bug.featureName) return bug.featureName;
    if (bug.featureId && this.featureLookupMap.has(bug.featureId)) {
      return this.featureLookupMap.get(bug.featureId)!.featureName;
    }
    return 'Defect Resolution';
  }

  getBugSprint(bug: Bug | null): number {
    if (!bug) return 1;
    if (bug.sprint) return bug.sprint;
    if (bug.featureId && this.featureLookupMap.has(bug.featureId)) {
      return this.featureLookupMap.get(bug.featureId)!.sprint;
    }
    return 1;
  }

  // --- Data Loading ---

  loadBugs(): void {
    this.isLoading = true;
    const devId = this.currentUserId ? Number(this.currentUserId) : undefined;
    this.bugService.getAllBugs(devId).subscribe({
      next: (list) => {
        this.isLoading = false;
        // Strictly filter to ensure only bugs assigned to this developer are set
        this.bugs = (list || []).filter((b) => this.isAssignedToMe(b));
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Failed to load bugs:', err);
        this.bugs = [];
        this.cdr.markForCheck();
      }
    });
  }

  // --- Strict Assignment Filtering (Based on Developer Assigned ID) ---

  isAssignedToMe(b: Bug): boolean {
    if (!b) return false;

    const myId = this.currentUserId ? Number(this.currentUserId) : null;
    const myName = (this.userName || '').toLowerCase().trim();
    const myUser = (this.currentUser?.username || '').toLowerCase().trim();

    // 1. Primary check: Strict match on assignedToId
    const bugAssignedId = b.assignedToId != null ? Number(b.assignedToId) : (b.workflow?.assignedToId != null ? Number(b.workflow.assignedToId) : null);
    if (bugAssignedId != null && myId != null) {
      return bugAssignedId === myId;
    }

    // If bug has an assigned ID but our user ID is missing or doesn't match, strictly reject
    if (bugAssignedId != null && myId == null) {
      return false;
    }

    // 2. Secondary check: If assignedToId is not provided in legacy payload,
    // match strictly with logged-in developer's username or full name
    const assignee = (b.assignedTo || b.workflow?.assignedTo || '').toLowerCase().trim();
    if (assignee && assignee !== 'unassigned') {
      if (myUser && assignee === myUser) {
        return true;
      }
      if (myName && assignee === myName) {
        return true;
      }
    }

    return false;
  }

  // All bugs assigned strictly to this developer
  get myAssignedBugs(): Bug[] {
    return this.bugs.filter((b) => this.isAssignedToMe(b));
  }

  // Count of active defects (OPEN, IN_PROGRESS, ASSIGNED, REOPENED)
  get activeBugsCount(): number {
    return this.myAssignedBugs.filter((b) => !this.isBugFrozen(b)).length;
  }

  get openBugsCount(): number {
    return this.activeBugsCount;
  }

  // Count of FIXED, CLOSED, NOT_A_BUG & RESOLVED defects (Frozen)
  get resolvedBugsCount(): number {
    return this.myAssignedBugs.filter((b) => this.isBugFrozen(b)).length;
  }

  // Filtered bugs based on Category Tab (ACTIVE vs RESOLVED/FROZEN), Severity, and Search
  get filteredBugs(): Bug[] {
    let list = this.myAssignedBugs;

    // 1. Filter by Workflow Categorization Tab
    if (this.defectCategoryTab === 'ACTIVE') {
      list = list.filter((b) => !this.isBugFrozen(b));
    } else if (this.defectCategoryTab === 'RESOLVED') {
      list = list.filter((b) => this.isBugFrozen(b));
    }

    // 2. Filter by Severity
    if (this.bugFilterSeverity !== 'ALL') {
      list = list.filter(
        (b) => (b.severity || '').toUpperCase() === this.bugFilterSeverity.toUpperCase()
      );
    }

    // 3. Search Query (Search by Bug ID, Title, Description, TestCase, or Reporter)
    if (this.bugSearch.trim()) {
      const q = this.bugSearch.toLowerCase().trim();
      list = list.filter(
        (b) =>
          (b.bugFormatId || '').toLowerCase().includes(q) ||
          (b.title || '').toLowerCase().includes(q) ||
          (b.description || '').toLowerCase().includes(q) ||
          (b.testcaseFormatId || '').toLowerCase().includes(q) ||
          (b.reportedBy || '').toLowerCase().includes(q) ||
          (b.featureName || '').toLowerCase().includes(q) ||
          (b.projectName || '').toLowerCase().includes(q)
      );
    }

    return list;
  }

  setCategoryTab(tab: 'ACTIVE' | 'RESOLVED' | 'ALL'): void {
    this.defectCategoryTab = tab;
    this.bugCurrentPage = 1;
    this.cdr.markForCheck();
  }

  // --- Pagination ---

  get totalPages(): number {
    const total = this.filteredBugs.length;
    return Math.max(1, Math.ceil(total / this.bugPageSize));
  }

  get paginatedBugs(): Bug[] {
    const start = (this.bugCurrentPage - 1) * this.bugPageSize;
    return this.filteredBugs.slice(start, start + this.bugPageSize);
  }

  get bugStartIndex(): number {
    if (this.filteredBugs.length === 0) return 0;
    return (this.bugCurrentPage - 1) * this.bugPageSize + 1;
  }

  get bugEndIndex(): number {
    return Math.min(this.bugCurrentPage * this.bugPageSize, this.filteredBugs.length);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.bugCurrentPage = page;
      this.cdr.markForCheck();
    }
  }

  onPageSizeChange(): void {
    this.bugCurrentPage = 1;
    this.cdr.markForCheck();
  }

  onFilterChange(): void {
    this.bugCurrentPage = 1;
    this.cdr.markForCheck();
  }

  trackByBugId(index: number, item: Bug): number {
    return item.bugId;
  }

  // --- Bug & TestCase Details Inspector (GET Access Only + Attachments) ---

  viewDefectDetail(bug: Bug): void {
    this.activeBugForDetail = bug;
    this.isDetailModalOpen = true;
    this.selectedTestCase = null;
    this.testCaseLoadError = '';
    this.activeBugAttachments = [];
    this.bugAuditHistory = [];
    this.testCaseAuditLogs = [];

    // Fetch Bug Status Change History
    this.loadBugAuditHistory(bug.bugId);

    // Fetch Linked Test Case Details
    let tcId = bug.testcaseId;
    if (!tcId && bug.testcaseFormatId) {
      const match = bug.testcaseFormatId.match(/\d+/);
      if (match) {
        tcId = parseInt(match[0], 10);
      }
    }

    if (tcId) {
      this.isTestCaseLoading = true;
      this.testCaseService.getTestCaseById(tcId).subscribe({
        next: (tc) => {
          this.isTestCaseLoading = false;
          this.selectedTestCase = tc;
          this.loadTestCaseAuditHistory(tc.testcaseId);
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.isTestCaseLoading = false;
          this.testCaseLoadError = 'Could not load execution steps for testcase #' + tcId;
          console.error('Failed to load linked test case details:', err);
          this.cdr.markForCheck();
        }
      });
    } else {
      this.isTestCaseLoading = false;
    }

    // Load Attachments for this Bug
    this.loadBugAttachments(bug.bugId);

    // Also load comments for this Bug
    this.loadCommentsForBug(bug.bugId);

    this.cdr.markForCheck();
  }

  loadBugAuditHistory(bugId: number): void {
    if (!bugId) {
      this.bugAuditHistory = [];
      return;
    }
    this.isLoadingBugHistory = true;
    this.bugService.getBugHistory(bugId).subscribe({
      next: (history) => {
        this.bugAuditHistory = history || [];
        this.isLoadingBugHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.bugAuditHistory = [];
        this.isLoadingBugHistory = false;
        this.cdr.markForCheck();
      }
    });
  }

  loadTestCaseAuditHistory(testcaseId: number): void {
    if (!testcaseId) {
      this.testCaseAuditLogs = [];
      return;
    }
    this.isLoadingTcHistory = true;
    this.testCaseService.getTestCaseHistory(testcaseId).subscribe({
      next: (logs) => {
        this.testCaseAuditLogs = logs || [];
        this.isLoadingTcHistory = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.testCaseAuditLogs = [];
        this.isLoadingTcHistory = false;
        this.cdr.markForCheck();
      }
    });
  }

  closeDetailModal(): void {
    this.isDetailModalOpen = false;
    this.activeBugForDetail = null;
    this.selectedTestCase = null;
    this.activeBugAttachments = [];
    this.bugAuditHistory = [];
    this.testCaseAuditLogs = [];
    this.cdr.markForCheck();
  }

  // --- Attachments Handling ---

  loadBugAttachments(bugId: number): void {
    this.isAttachmentsLoading = true;
    this.bugService.getBugAttachments(bugId).subscribe({
      next: (items) => {
        this.isAttachmentsLoading = false;
        this.activeBugAttachments = items || [];
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isAttachmentsLoading = false;
        console.error('Failed to load bug attachments for bugId ' + bugId, err);
        this.activeBugAttachments = [];
        this.cdr.markForCheck();
      }
    });
  }

  onFileUpload(event: Event, bugId?: number): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const targetBugId = bugId || this.activeBugForDetail?.bugId || this.activeBugForAttachments?.bugId;
    if (!targetBugId) return;

    const targetBug = this.bugs.find((b) => b.bugId === targetBugId);
    if (targetBug && this.isBugFrozen(targetBug)) {
      this.showToast('Defect is frozen. Files cannot be uploaded to a frozen defect.');
      input.value = '';
      return;
    }

    const filesArray = Array.from(input.files);
    this.isUploadingAttachment = true;

    this.bugService.uploadBugAttachments(targetBugId, filesArray).subscribe({
      next: () => {
        this.isUploadingAttachment = false;
        input.value = '';
        this.loadBugAttachments(targetBugId);
        this.showToast('Files attached successfully to defect.');
      },
      error: (err) => {
        this.isUploadingAttachment = false;
        input.value = '';
        console.error('Failed to upload attachments:', err);
        this.showToast('Failed to upload files.');
        this.cdr.markForCheck();
      }
    });
  }

  downloadAttachment(att: AttachmentItem): void {
    if (!att.attachmentId && !att.id) return;
    const id = (att.attachmentId || att.id)!;
    this.bugService.downloadSingleAttachment(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = att.fileName || 'attachment';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        console.error('Failed to download attachment:', err);
      }
    });
  }

  viewAttachment(att: AttachmentItem): void {
    if (!att) return;
    const attachmentId = att.attachmentId || att.id;
    this.previewFile = att;
    this.isPreviewOpen = true;
    this.previewError = null;
    this.previewTextContent = '';
    this.previewBlobUrl = null;
    this.revokePreviewBlobUrl();
    this.previewImageZoom = 1;
    this.previewCopySuccess = false;

    const ext = this.getAttachmentExtension(att.fileName);
    this.previewFileType = this.detectAttachmentCategory(ext, att.fileType || att.contentType);

    if (!attachmentId) {
      this.previewError = 'Attachment ID is missing. Unable to preview file.';
      this.isLoadingPreview = false;
      this.cdr.markForCheck();
      return;
    }

    this.isLoadingPreview = true;
    this.cdr.markForCheck();

    this.bugService.viewSingleAttachment(attachmentId).subscribe({
      next: async (blob) => {
        try {
          if (blob.type) {
            const detectedFromMime = this.detectCategoryFromMime(blob.type);
            if (detectedFromMime !== 'unsupported') {
              this.previewFileType = detectedFromMime;
            }
          }

          if (this.previewFileType === 'text') {
            this.previewTextContent = await blob.text();
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          } else if (this.previewFileType === 'image') {
            this.rawPreviewBlobUrl = URL.createObjectURL(blob);
            this.previewBlobUrl = this.sanitizer.bypassSecurityTrustUrl(this.rawPreviewBlobUrl);
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          } else if (this.previewFileType === 'pdf' || this.previewFileType === 'audio' || this.previewFileType === 'video') {
            this.rawPreviewBlobUrl = URL.createObjectURL(blob);
            this.previewBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.rawPreviewBlobUrl);
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          } else {
            this.previewFileType = 'unsupported';
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          }
        } catch {
          this.previewError = 'Unable to parse file for display. You can download the file directly.';
          this.isLoadingPreview = false;
          this.cdr.markForCheck();
        }
      },
      error: () => {
        // Fallback to downloadSingleAttachment
        this.bugService.downloadSingleAttachment(attachmentId).subscribe({
          next: async (blob) => {
            try {
              if (this.previewFileType === 'text') {
                this.previewTextContent = await blob.text();
              } else if (this.previewFileType === 'image') {
                this.rawPreviewBlobUrl = URL.createObjectURL(blob);
                this.previewBlobUrl = this.sanitizer.bypassSecurityTrustUrl(this.rawPreviewBlobUrl);
              } else if (this.previewFileType === 'pdf' || this.previewFileType === 'audio' || this.previewFileType === 'video') {
                this.rawPreviewBlobUrl = URL.createObjectURL(blob);
                this.previewBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.rawPreviewBlobUrl);
              }
              this.isLoadingPreview = false;
              this.cdr.markForCheck();
            } catch {
              this.previewError = 'Failed to load file preview. Please download the file.';
              this.isLoadingPreview = false;
              this.cdr.markForCheck();
            }
          },
          error: () => {
            this.previewError = 'Failed to fetch attachment from server.';
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          }
        });
      }
    });
  }

  closePreview(): void {
    this.isPreviewOpen = false;
    this.isLoadingPreview = false;
    this.previewError = null;
    this.previewFile = null;
    this.previewTextContent = '';
    this.previewBlobUrl = null;
    this.revokePreviewBlobUrl();
    this.cdr.markForCheck();
  }

  copyPreviewText(): void {
    if (!this.previewTextContent) return;
    navigator.clipboard.writeText(this.previewTextContent).then(() => {
      this.previewCopySuccess = true;
      this.cdr.markForCheck();
      setTimeout(() => {
        this.previewCopySuccess = false;
        this.cdr.markForCheck();
      }, 2000);
    }).catch(() => {
      this.previewCopySuccess = false;
    });
  }

  zoomInPreview(): void {
    if (this.previewImageZoom < 3) {
      this.previewImageZoom = Math.min(3, +(this.previewImageZoom + 0.25).toFixed(2));
      this.cdr.markForCheck();
    }
  }

  zoomOutPreview(): void {
    if (this.previewImageZoom > 0.4) {
      this.previewImageZoom = Math.max(0.4, +(this.previewImageZoom - 0.25).toFixed(2));
      this.cdr.markForCheck();
    }
  }

  resetPreviewZoom(): void {
    this.previewImageZoom = 1;
    this.cdr.markForCheck();
  }

  getPreviewIconClass(): string {
    switch (this.previewFileType) {
      case 'image': return 'fa-file-image text-emerald';
      case 'pdf': return 'fa-file-pdf text-rose';
      case 'text': return 'fa-file-code text-indigo';
      case 'audio': return 'fa-file-audio text-amber';
      case 'video': return 'fa-file-video text-purple';
      default: return 'fa-file-lines text-slate';
    }
  }

  private detectAttachmentCategory(ext: string, mime?: string): 'image' | 'pdf' | 'text' | 'audio' | 'video' | 'unsupported' {
    const imgExts = ['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp', 'bmp', 'ico'];
    const textExts = ['txt', 'log', 'json', 'csv', 'xml', 'md', 'yaml', 'yml', 'html', 'css', 'js', 'ts', 'java', 'py', 'sql', 'sh', 'bat', 'properties', 'env', 'ini', 'conf'];
    const audioExts = ['mp3', 'wav', 'ogg'];
    const videoExts = ['mp4', 'webm'];

    if (imgExts.includes(ext)) return 'image';
    if (ext === 'pdf') return 'pdf';
    if (textExts.includes(ext)) return 'text';
    if (audioExts.includes(ext)) return 'audio';
    if (videoExts.includes(ext)) return 'video';

    if (mime) {
      return this.detectCategoryFromMime(mime);
    }
    return 'unsupported';
  }

  private detectCategoryFromMime(mime: string): 'image' | 'pdf' | 'text' | 'audio' | 'video' | 'unsupported' {
    const m = mime.toLowerCase();
    if (m.startsWith('image/')) return 'image';
    if (m.includes('pdf')) return 'pdf';
    if (m.startsWith('text/') || m.includes('json') || m.includes('xml') || m.includes('javascript') || m.includes('csv')) return 'text';
    if (m.startsWith('audio/')) return 'audio';
    if (m.startsWith('video/')) return 'video';
    return 'unsupported';
  }

  private getAttachmentExtension(filename?: string): string {
    if (!filename) return '';
    const parts = filename.split('.');
    return parts.length > 1 ? parts.pop()!.toLowerCase().trim() : '';
  }

  private revokePreviewBlobUrl(): void {
    if (this.rawPreviewBlobUrl) {
      try {
        URL.revokeObjectURL(this.rawPreviewBlobUrl);
      } catch { }
      this.rawPreviewBlobUrl = null;
    }
  }

  ngOnDestroy(): void {
    this.revokePreviewBlobUrl();
  }

  openAttachmentsModal(bug: Bug): void {
    this.activeBugForAttachments = bug;
    this.isAttachmentsModalOpen = true;
    this.loadBugAttachments(bug.bugId);
  }

  closeAttachmentsModal(): void {
    this.isAttachmentsModalOpen = false;
    this.activeBugForAttachments = null;
    this.cdr.markForCheck();
  }

  // --- Chat & Discussion with QA Reporter ---

  openBugChat(bug: Bug): void {
    this.activeBugForChat = bug;
    this.isBugChatOpen = true;
    this.isBugChatLoading = true;
    this.bugComments = [];
    this.newChatMessage = '';

    this.loadCommentsForBug(bug.bugId);
  }

  closeBugChat(): void {
    this.isBugChatOpen = false;
    this.activeBugForChat = null;
    this.newChatMessage = '';
    this.cdr.markForCheck();
  }

  private loadCommentsForBug(bugId: number): void {
    this.bugService.getBugComments(bugId).subscribe({
      next: (comments) => {
        this.isBugChatLoading = false;
        this.bugComments = comments || [];
        this.cdr.markForCheck();
        this.scrollChatToBottom();
      },
      error: (err) => {
        this.isBugChatLoading = false;
        console.error('Failed to load comments for bug ' + bugId, err);
        this.bugComments = [];
        this.cdr.markForCheck();
      }
    });
  }

  sendChatMessage(): void {
    if (!this.newChatMessage.trim() || !this.activeBugForChat) return;

    const msg = this.newChatMessage.trim();
    this.newChatMessage = '';
    this.isSendingComment = true;

    const tempComment: BugComment = {
      commentId: Date.now(),
      comment: msg,
      creatorName: this.userName,
      createdByFullName: this.userName,
      createdByUsername: this.currentUser?.username || 'developer',
      createdByUserId: this.currentUserId ? Number(this.currentUserId) : undefined,
      createdAt: new Date().toISOString()
    };
    this.bugComments.push(tempComment);
    this.scrollChatToBottom();

    this.bugService.createBugComment(this.activeBugForChat.bugId, msg).subscribe({
      next: (res) => {
        this.isSendingComment = false;
        if (res && res.commentId) {
          tempComment.commentId = res.commentId;
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.isSendingComment = false;
        this.cdr.markForCheck();
      }
    });
  }

  useQuickReply(text: string): void {
    this.newChatMessage = text;
    this.sendChatMessage();
  }

  isMyComment(c: BugComment): boolean {
    if (!c) return false;

    // Check by user ID
    const commentUserId = c.createdByUserId || (typeof c.createdBy === 'number' ? c.createdBy : null);
    if (commentUserId && this.currentUserId) {
      return Number(commentUserId) === Number(this.currentUserId);
    }

    // Check by exact username match
    const myUsername = (this.currentUser?.username || '').trim().toLowerCase();
    const commentUsername = (c.createdByUsername || '').trim().toLowerCase();
    if (myUsername && commentUsername) {
      return myUsername === commentUsername;
    }

    // Check by exact full name match (excluding generic defaults)
    const myFullName = (this.userName || '').trim().toLowerCase();
    const commentFullName = (c.creatorName || c.createdByFullName || '').trim().toLowerCase();
    if (myFullName && commentFullName && myFullName !== 'developer' && myFullName !== 'team member') {
      return myFullName === commentFullName;
    }

    return false;
  }

  getCommentRole(c: BugComment): string {
    if (this.isMyComment(c)) {
      return 'Developer';
    }
    return 'QA';
  }

  private scrollChatToBottom(): void {
    setTimeout(() => {
      const container = document.getElementById('devChatStream');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 60);
  }

  // --- Status Transitions (PATCH Status: IN_PROGRESS, FIXED, NOT_A_BUG) ---

  onStatusSelect(bug: Bug, event: Event): void {
    const select = event.target as HTMLSelectElement;
    if (!select || !select.value) return;
    const newStatus = select.value;
    if (this.isBugFrozen(bug)) {
      this.showToast('Defect is frozen. Only a tester can re-open or change its status.');
      select.value = bug.status || 'FIXED';
      return;
    }
    this.applyStatusUpdate(bug.bugId, newStatus);
  }

  applyStatusUpdate(bugId: number, status: string): void {
    const target = this.bugs.find((b) => b.bugId === bugId);
    if (target && this.isBugFrozen(target)) {
      this.showToast('Defect is frozen. Only a tester can re-open or reassign it.');
      return;
    }

    if (status === 'OPEN') {
      this.showToast('Developers cannot reopen defect to OPEN. Retest is handled by tester.');
      return;
    }

    this.bugService
      .patchBug(bugId, {
        status
      })
      .subscribe({
        next: () => {
          this.updateLocalBugStatus(bugId, status);
          if (this.activeBugForDetail && this.activeBugForDetail.bugId === bugId) {
            this.activeBugForDetail.status = status;
            this.loadBugAuditHistory(bugId);
          }
          if (this.activeBugForChat && this.activeBugForChat.bugId === bugId) {
            this.activeBugForChat.status = status;
          }

          if (status === 'FIXED') {
            this.showToast(`Defect marked as FIXED and frozen. QA tester notified for retest.`);
          } else if (status === 'NOT_A_BUG') {
            this.showToast(`Defect marked as NOT A BUG and frozen. QA tester notified.`);
          } else if (status === 'IN_PROGRESS') {
            this.showToast(`Defect status updated to IN_PROGRESS.`);
          } else {
            this.showToast(`Defect status updated to ${status}.`);
          }

          this.cdr.markForCheck();
        },
        error: () => {
          this.updateLocalBugStatus(bugId, status);
          if (status === 'FIXED') {
            this.showToast(`Status updated to FIXED.`);
          } else {
            this.showToast(`Defect status updated to ${status}.`);
          }
          this.cdr.markForCheck();
        }
      });
  }

  private updateLocalBugStatus(bugId: number, status: string): void {
    const target = this.bugs.find((b) => b.bugId === bugId);
    if (target) {
      target.status = status;
      if (target.workflow) target.workflow.status = status;
    }
  }

  private showToast(msg: string): void {
    this.statusToastMessage = msg;
    if (this.toastTimeoutId) {
      clearTimeout(this.toastTimeoutId);
    }
    this.toastTimeoutId = setTimeout(() => {
      this.statusToastMessage = '';
      this.cdr.markForCheck();
    }, 4500);
    this.cdr.markForCheck();
  }

  // --- In-App Notifications Management ---

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (list) => {
        this.notifications = list || [];
        this.unreadNotificationCount = this.notifications.filter(
          (n) => n.notificationStatus === 'UNREAD'
        ).length;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load in-app notifications:', err);
      }
    });
  }

  openNotificationsModal(): void {
    this.isNotificationsModalOpen = true;
    this.loadNotifications();
    this.cdr.markForCheck();
  }

  closeNotificationsModal(): void {
    this.isNotificationsModalOpen = false;
    this.cdr.markForCheck();
  }

  onSelectNotification(n: InAppNotification): void {
    // 1. Mark as read
    if (n.notificationStatus === 'UNREAD') {
      n.notificationStatus = 'READ';
      this.unreadNotificationCount = Math.max(0, this.unreadNotificationCount - 1);
      this.notificationService.markAsRead(n.notificationId).subscribe();
    }

    // 2. Close notifications modal
    this.isNotificationsModalOpen = false;
    this.cdr.markForCheck();

    // 3. Map to the particular defect/bug
    if (n.bugId || n.bugFormatId) {
      const bugIdNum = n.bugId ? Number(n.bugId) : null;
      const bugFormat = n.bugFormatId;

      const targetBug = this.bugs.find(
        (b) => (bugIdNum && Number(b.bugId) === bugIdNum) || (bugFormat && b.bugFormatId === bugFormat)
      );

      if (targetBug) {
        this.navigateToTargetBug(targetBug, n);
      } else {
        // If bug is not yet loaded in active list (e.g. freshly assigned), fetch it from backend
        if (bugIdNum) {
          this.bugService.getBugById(bugIdNum).subscribe({
            next: (fetchedBug) => {
              if (fetchedBug) {
                const idx = this.bugs.findIndex(b => b.bugId === fetchedBug.bugId);
                if (idx === -1) {
                  this.bugs.unshift(fetchedBug);
                } else {
                  this.bugs[idx] = fetchedBug;
                }
                this.navigateToTargetBug(fetchedBug, n);
              }
            },
            error: (err) => {
              console.error('Failed to load defect for notification:', err);
              this.showToast('Could not load defect details.');
            }
          });
        }
      }
    }
  }

  private navigateToTargetBug(bug: Bug, n?: InAppNotification): void {
    // Ensure the category tab displays this bug
    if (this.isBugFrozen(bug)) {
      if (this.defectCategoryTab !== 'RESOLVED' && this.defectCategoryTab !== 'ALL') {
        this.defectCategoryTab = 'RESOLVED';
      }
    } else {
      if (this.defectCategoryTab !== 'ACTIVE' && this.defectCategoryTab !== 'ALL') {
        this.defectCategoryTab = 'ACTIVE';
      }
    }

    // Reset filters so the defect is visible in the table
    this.bugSearch = '';
    this.bugFilterSeverity = 'ALL';

    // If notification is about a comment, open the discussion chat directly
    if (n && (n.type === 'COMMENT' || (n.message && n.message.toLowerCase().includes('comment')))) {
      this.openBugChat(bug);
    } else {
      this.viewDefectDetail(bug);
    }

    this.showToast(`Opened defect ${bug.bugFormatId || 'Bug #' + bug.bugId}`);

    // Highlight row in table
    setTimeout(() => {
      const row = document.getElementById(`dev-bug-row-${bug.bugId}`);
      if (row) {
        row.scrollIntoView({ behavior: 'smooth', block: 'center' });
        row.classList.add('highlight-pulse');
        setTimeout(() => row.classList.remove('highlight-pulse'), 3000);
      }
    }, 350);

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
    this.showToast('All notifications marked as read.');
    this.cdr.markForCheck();
  }

  goToProfile(event?: Event): void {
    if (event) {
      event.preventDefault();
    }
    this.router.navigate(['/profile']);
  }

  // --- Logout ---

  performLogout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }
}
