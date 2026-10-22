import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AdminService } from '../../core/services/admin.service';
import { AuthService } from '../../core/services/authService';
import { CreateManagerRequest, ManagerResponse } from '../../model/admin/admin.model';

@Component({
  selector: 'app-admin-workspace',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-workspace.html',
  styleUrl: './admin-workspace.css'
})
export class AdminWorkspace implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cd = inject(ChangeDetectorRef);

  // Active Main Navigation Tab
  activeTab: 'directory' | 'create' = 'directory';

  // Role Filtering in User Directory
  roleFilter: 'ALL' | 'ADMIN' | 'MANAGER' | 'TESTER' | 'DEVELOPER' = 'ALL';
  searchQuery = '';

  // Data State
  users: ManagerResponse[] = [];
  pendingUsers: any[] = [];
  isLoading = false;
  isPendingLoading = false;
  pendingRoleSelections: { [userId: number]: string } = {};
  userSubTab: 'pending' | 'directory' = 'directory';
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';

  // Current Logged-in Admin Info
  adminName = 'Super Administrator';
  adminRole = 'ADMIN';
  adminInitials = 'AD';

  // Create Manager Form Model
  newManager: CreateManagerRequest = {
    username: '',
    email: '',
    fullName: '',
    designation: 'QA Lead / Manager',
    skills: 'QA Leadership, Defect Triage, API Automation'
  };
  isCreating = false;

  // Edit User / Manager Modal State
  isEditModalOpen = false;
  editingUserId: number | null = null;
  editingUserTarget: ManagerResponse | null = null;
  editManagerData: CreateManagerRequest = {
    username: '',
    email: '',
    fullName: '',
    designation: '',
    skills: ''
  };
  isUpdating = false;

  // Delete User Modal State
  isDeleteModalOpen = false;
  userToDelete: ManagerResponse | null = null;
  isDeleting = false;

  ngOnInit(): void {
    this.initAdminInfo();
    this.loadUsers();
    this.loadPendingUsers();
  }

  private initAdminInfo(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.adminName = user.fullName || user.username || 'Administrator';
      this.adminRole = user.role || 'ADMIN';
      const parts = this.adminName.trim().split(/\s+/);
      if (parts.length >= 2) {
        this.adminInitials = (parts[0][0] + parts[1][0]).toUpperCase();
      } else if (parts[0]) {
        this.adminInitials = parts[0].substring(0, 2).toUpperCase();
      }
    }
  }

  // Load All Users from Admin API (Excluding Admin Accounts)
  loadUsers(): void {
    this.isLoading = true;
    this.adminService.getAllUsers().subscribe({
      next: (res) => {
        this.isLoading = false;
        // Store all accounts including admins in user directory
        this.users = res?.data || [];
        this.cd.markForCheck();
      },
      error: (err) => {
        this.isLoading = false;
        this.showToast(err?.error?.message || 'Failed to load users directory', 'error');
        this.cd.markForCheck();
      }
    });
    this.loadPendingUsers();
  }

  // Load Pending Users Awaiting Registration Approval
  loadPendingUsers(): void {
    this.isPendingLoading = true;
    this.adminService.getPendingUsers().subscribe({
      next: (res) => {
        this.isPendingLoading = false;
        const raw = res?.data || res || [];
        this.pendingUsers = Array.isArray(raw) ? raw : [];
        this.pendingUsers.forEach(u => {
          if (!this.pendingRoleSelections[u.userId]) {
            this.pendingRoleSelections[u.userId] = 'TESTER';
          }
        });
        if (this.pendingUsers.length > 0 && this.userSubTab !== 'directory') {
          this.userSubTab = 'pending';
        }
        this.cd.markForCheck();
      },
      error: (err) => {
        this.isPendingLoading = false;
        console.warn('Failed to load pending users:', err);
        this.cd.markForCheck();
      }
    });
  }

  // Authorize & Confirm User Registration
  approvePendingUser(user: any): void {
    const role = this.pendingRoleSelections[user.userId] || 'TESTER';
    this.adminService.confirmUser(user.userId, role).subscribe({
      next: (res) => {
        this.showToast(res?.message || `User ${user.fullName || user.username} approved as ${role} successfully.`, 'success');
        this.loadPendingUsers();
        this.loadUsers();
      },
      error: (err) => {
        this.showToast(err?.error?.message || 'Failed to approve user registration.', 'error');
      }
    });
  }

  // Reject & Discard User Registration
  rejectPendingUser(user: any): void {
    if (!confirm(`Are you sure you want to reject registration for ${user.fullName || user.username}?`)) {
      return;
    }
    this.adminService.rejectUser(user.userId).subscribe({
      next: (res) => {
        this.showToast(res?.message || `User ${user.fullName || user.username} registration rejected.`, 'success');
        this.loadPendingUsers();
      },
      error: (err) => {
        this.showToast(err?.error?.message || 'Failed to reject user registration.', 'error');
      }
    });
  }

  // Subtab switching inside User Management
  setUserSubTab(subTab: 'pending' | 'directory'): void {
    this.userSubTab = subTab;
    this.cd.markForCheck();
  }

  // Filtered Users List
  get filteredUsers(): ManagerResponse[] {
    let list = [...this.users];

    // Filter by role
    if (this.roleFilter !== 'ALL') {
      list = list.filter(u => {
        const role = (u.role || '').toUpperCase();
        return role === this.roleFilter || role.includes(this.roleFilter);
      });
    }

    // Filter by search query
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase().trim();
      list = list.filter(u =>
        (u.fullName && u.fullName.toLowerCase().includes(q)) ||
        (u.username && u.username.toLowerCase().includes(q)) ||
        (u.email && u.email.toLowerCase().includes(q)) ||
        (u.designation && u.designation.toLowerCase().includes(q)) ||
        (u.skills && u.skills.toLowerCase().includes(q)) ||
        (u.role && u.role.toLowerCase().includes(q))
      );
    }

    return list;
  }

  // Statistics
  get totalUsersCount(): number {
    return this.users.length;
  }

  get adminsCount(): number {
    return this.users.filter(u => (u.role || '').toUpperCase().includes('ADMIN')).length;
  }

  get managersCount(): number {
    return this.users.filter(u => (u.role || '').toUpperCase().includes('MANAGER')).length;
  }

  get testersCount(): number {
    return this.users.filter(u => (u.role || '').toUpperCase().includes('TESTER')).length;
  }

  get developersCount(): number {
    return this.users.filter(u => (u.role || '').toUpperCase().includes('DEVELOPER') || (u.role || '').toUpperCase().includes('DEV')).length;
  }

  // Tab switching
  setTab(tab: 'directory' | 'create'): void {
    this.activeTab = tab;
    this.cd.markForCheck();
  }

  setRoleFilter(filter: 'ALL' | 'ADMIN' | 'MANAGER' | 'TESTER' | 'DEVELOPER'): void {
    this.roleFilter = filter;
    this.cd.markForCheck();
  }

  // Auto-fill username based on full name
  onFullNameInput(): void {
    if (this.newManager.fullName && !this.newManager.username) {
      this.newManager.username = this.newManager.fullName
        .toLowerCase()
        .replace(/[^a-z0-9]/g, '.')
        .replace(/\.+/g, '.')
        .replace(/^\.|\.$/g, '');
    }
  }

  // Create Manager Action
  submitCreateManager(): void {
    if (!this.newManager.fullName.trim() || !this.newManager.username.trim() || !this.newManager.email.trim()) {
      this.showToast('Please fill in all required fields marked with *', 'error');
      return;
    }

    this.isCreating = true;
    this.adminService.createManager(this.newManager).subscribe({
      next: (res) => {
        this.isCreating = false;
        this.showToast(res?.message || 'Manager created successfully!', 'success');
        this.resetCreateForm();
        this.loadUsers();
        this.activeTab = 'directory';
        this.cd.markForCheck();
      },
      error: (err) => {
        this.isCreating = false;
        this.showToast(err?.error?.message || 'Failed to create manager', 'error');
        this.cd.markForCheck();
      }
    });
  }

  resetCreateForm(): void {
    this.newManager = {
      username: '',
      email: '',
      fullName: '',
      designation: 'QA Lead / Manager',
      skills: 'QA Leadership, Defect Triage, API Automation'
    };
  }

  // Edit User / Manager Modal Flow (Profile Editing)
  openEditModal(user: ManagerResponse): void {
    if ((user.role || '').toUpperCase().includes('ADMIN')) {
      this.showToast('Administrator accounts cannot be edited', 'error');
      return;
    }
    this.editingUserId = user.userId;
    this.editingUserTarget = user;
    this.editManagerData = {
      username: user.username,
      email: user.email,
      fullName: user.fullName || user.username,
      designation: user.designation || '',
      skills: user.skills || ''
    };
    this.isEditModalOpen = true;
    this.cd.markForCheck();
  }

  closeEditModal(): void {
    this.isEditModalOpen = false;
    this.editingUserId = null;
    this.editingUserTarget = null;
    this.cd.markForCheck();
  }

  submitUpdateManager(): void {
    if (!this.editingUserId) return;
    if (!this.editManagerData.fullName?.trim() || !this.editManagerData.username?.trim() || !this.editManagerData.email?.trim()) {
      this.showToast('Full name, username, and email are required', 'error');
      return;
    }

    this.isUpdating = true;
    this.adminService.updateUser(this.editingUserId, this.editManagerData).subscribe({
      next: (res) => {
        this.isUpdating = false;
        this.showToast(res?.message || 'User profile updated successfully', 'success');
        this.closeEditModal();
        this.loadUsers();
      },
      error: (err) => {
        this.isUpdating = false;
        this.showToast(err?.error?.message || 'Failed to update user profile', 'error');
        this.cd.markForCheck();
      }
    });
  }

  // Delete User Modal Flow
  openDeleteModal(user: ManagerResponse): void {
    if ((user.role || '').toUpperCase().includes('ADMIN')) {
      this.showToast('Administrator accounts cannot be deleted', 'error');
      return;
    }
    this.userToDelete = user;
    this.isDeleteModalOpen = true;
    this.cd.markForCheck();
  }

  closeDeleteModal(): void {
    this.isDeleteModalOpen = false;
    this.userToDelete = null;
    this.cd.markForCheck();
  }

  executeSoftDelete(): void {
    if (!this.userToDelete) return;
    const userId = this.userToDelete.userId;

    this.isDeleting = true;
    this.adminService.softDeleteUser(userId).subscribe({
      next: (res) => {
        this.isDeleting = false;
        this.showToast(res?.message || `User #${userId} deactivated successfully`, 'success');
        this.closeDeleteModal();
        this.loadUsers();
      },
      error: (err) => {
        this.isDeleting = false;
        this.showToast(err?.error?.message || 'Failed to deactivate user', 'error');
        this.cd.markForCheck();
      }
    });
  }

  executeHardDelete(): void {
    if (!this.userToDelete) return;
    const userId = this.userToDelete.userId;

    this.isDeleting = true;
    this.adminService.hardDeleteUser(userId).subscribe({
      next: (res) => {
        this.isDeleting = false;
        this.showToast(res?.message || `User #${userId} permanently deleted`, 'success');
        this.closeDeleteModal();
        this.loadUsers();
      },
      error: (err) => {
        this.isDeleting = false;
        this.showToast(err?.error?.message || 'Failed to permanently delete user', 'error');
        this.cd.markForCheck();
      }
    });
  }

  // Toast helper
  showToast(msg: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = msg;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = '';
      this.cd.markForCheck();
    }, 4500);
  }

  // Navigation
  goToProfile(): void {
    this.router.navigate(['/profile']);
  }

  performLogout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => {
        this.authService.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }

  trackByUserId(index: number, item: ManagerResponse): number {
    return item.userId;
  }
}
