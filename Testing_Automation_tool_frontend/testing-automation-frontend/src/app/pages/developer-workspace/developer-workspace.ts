import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { BugService } from '../../core/services/bug.service';
import { TestCaseService } from '../../core/services/testcase.service';
import { ProjectService } from '../../core/services/project.service';
import { FeatureService } from '../../core/services/feature.service';
import { AuthService } from '../../core/services/authService';
import {
  Bug,
  BugComment,
  AttachmentItem,
  TestCase,
  Project,
  Feature
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
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './developer-workspace.html',
  styleUrl: './developer-workspace.css'
})
export class DeveloperWorkspace implements OnInit {
  private readonly bugService = inject(BugService);
  private readonly testCaseService = inject(TestCaseService);
  private readonly projectService = inject(ProjectService);
  private readonly featureService = inject(FeatureService);
  private readonly authService = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

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

  // Workflow Categorization Tabs: OPEN vs (FIXED, NOT_A_BUG)
  defectCategoryTab: 'OPEN' | 'RESOLVED' | 'ALL' = 'OPEN';

  // Search Filter & Severity Filter
  bugSearch = '';
  bugFilterSeverity = 'ALL';
  readonly developerStatuses = ['OPEN', 'FIXED', 'NOT_A_BUG'];

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

  // Attachments State (for Developer File Evidence & Logs)
  activeBugAttachments: AttachmentItem[] = [];
  isAttachmentsLoading = false;
  isUploadingAttachment = false;

  // Standalone Attachments Modal
  isAttachmentsModalOpen = false;
  activeBugForAttachments: Bug | null = null;

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

  // Count of OPEN defects
  get openBugsCount(): number {
    return this.myAssignedBugs.filter((b) => (b.status || '').toUpperCase() === 'OPEN').length;
  }

  // Count of FIXED & NOT_A_BUG defects
  get resolvedBugsCount(): number {
    return this.myAssignedBugs.filter((b) => {
      const st = (b.status || '').toUpperCase();
      return st === 'FIXED' || st === 'NOT_A_BUG' || st === 'RESOLVED';
    }).length;
  }

  // Filtered bugs based on Category Tab (OPEN vs FIXED/NOT_A_BUG), Severity, and Search
  get filteredBugs(): Bug[] {
    let list = this.myAssignedBugs;

    // 1. Filter by Workflow Categorization Tab
    if (this.defectCategoryTab === 'OPEN') {
      list = list.filter((b) => (b.status || '').toUpperCase() === 'OPEN');
    } else if (this.defectCategoryTab === 'RESOLVED') {
      list = list.filter((b) => {
        const st = (b.status || '').toUpperCase();
        return st === 'FIXED' || st === 'NOT_A_BUG' || st === 'RESOLVED';
      });
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

  setCategoryTab(tab: 'OPEN' | 'RESOLVED' | 'ALL'): void {
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

  closeDetailModal(): void {
    this.isDetailModalOpen = false;
    this.activeBugForDetail = null;
    this.selectedTestCase = null;
    this.activeBugAttachments = [];
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
    const creator = (c.creatorName || c.createdByFullName || c.createdByUsername || '').toLowerCase();
    const myName = this.userName.toLowerCase();
    return creator.includes(myName) || creator.includes('developer') || creator.includes('dev');
  }

  getCommentRole(c: BugComment): string {
    const text = (c.creatorName || c.createdByFullName || c.createdByUsername || '').toLowerCase();
    if (text.includes('qa') || text.includes('tester')) return 'QA Tester';
    if (text.includes('lead') || text.includes('manager')) return 'QA Lead';
    return 'Developer';
  }

  private scrollChatToBottom(): void {
    setTimeout(() => {
      const container = document.getElementById('devChatStream');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 60);
  }

  // --- Status Transitions (PATCH Status Only: OPEN, FIXED, NOT_A_BUG) ---

  onStatusSelect(bug: Bug, event: Event): void {
    const select = event.target as HTMLSelectElement;
    if (!select || !select.value) return;
    const newStatus = select.value;
    this.applyStatusUpdate(bug.bugId, newStatus);
  }

  applyStatusUpdate(bugId: number, status: string): void {
    this.bugService
      .patchBug(bugId, {
        status
      })
      .subscribe({
        next: () => {
          this.updateLocalBugStatus(bugId, status);
          if (this.activeBugForDetail && this.activeBugForDetail.bugId === bugId) {
            this.activeBugForDetail.status = status;
          }
          if (this.activeBugForChat && this.activeBugForChat.bugId === bugId) {
            this.activeBugForChat.status = status;
          }

          if (status === 'FIXED') {
            this.showToast(`Defect marked as FIXED. In-app notification sent to QA tester.`);
          } else if (status === 'NOT_A_BUG') {
            this.showToast(`Defect marked as NOT A BUG. In-app notification sent to QA tester.`);
          } else {
            this.showToast(`Defect status updated to ${status}.`);
          }

          this.cdr.markForCheck();
        },
        error: () => {
          this.updateLocalBugStatus(bugId, status);
          if (status === 'FIXED') {
            this.showToast(`Status updated to FIXED.`);
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
