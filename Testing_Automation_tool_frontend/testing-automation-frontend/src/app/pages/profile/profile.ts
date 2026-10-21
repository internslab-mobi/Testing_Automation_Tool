import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/authService';
import { UserProfile } from '../../model/auth/userProfile';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly cdr = inject(ChangeDetectorRef);

  profile: UserProfile | null = null;
  isLoading = false;
  isSaving = false;
  isUpdatingPassword = false;

  // Editable Form Model
  editFullName = '';
  editDesignation = '';
  editSkills = '';
  newSkillInput = '';

  // Password Update Model
  showPasswordSection = false;
  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  passwordError = '';

  // Toast Notification
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  private toastTimer: any = null;

  ngOnInit(): void {
    this.loadUserProfile();
  }

  loadUserProfile(): void {
    this.isLoading = true;
    // 1. Immediately hydrate from in-memory / decoded token
    const cached = this.authService.getCurrentUser();
    if (cached) {
      this.populateData(cached);
    }

    // 2. Fetch fresh profile from backend API
    this.authService.getProfile().subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res?.data) {
          this.populateData(res.data);
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Failed to fetch full profile from API:', err);
        if (!this.profile && cached) {
          this.populateData(cached);
        }
        this.cdr.markForCheck();
      }
    });
  }

  private populateData(data: UserProfile): void {
    this.profile = data;
    this.editFullName = data.fullName || '';
    this.editDesignation = data.designation || '';
    this.editSkills = data.skills || '';
  }

  get userInitials(): string {
    const name = this.editFullName || this.profile?.username || 'User';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  get skillsList(): string[] {
    if (!this.editSkills) return [];
    return this.editSkills
      .split(',')
      .map((s) => s.trim())
      .filter((s) => s.length > 0);
  }

  addSkill(): void {
    const skill = this.newSkillInput.trim();
    if (!skill) return;

    const current = this.skillsList;
    if (!current.some((s) => s.toLowerCase() === skill.toLowerCase())) {
      current.push(skill);
      this.editSkills = current.join(', ');
    }
    this.newSkillInput = '';
    this.cdr.markForCheck();
  }

  removeSkill(skillToRemove: string): void {
    const filtered = this.skillsList.filter(
      (s) => s.toLowerCase() !== skillToRemove.toLowerCase()
    );
    this.editSkills = filtered.join(', ');
    this.cdr.markForCheck();
  }

  onSkillKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.addSkill();
    }
  }

  saveProfile(): void {
    if (!this.editFullName.trim()) {
      this.showToast('Full name cannot be blank', 'error');
      return;
    }

    this.isSaving = true;
    const payload = {
      fullName: this.editFullName.trim(),
      designation: this.editDesignation.trim(),
      skills: this.editSkills.trim()
    };

    this.authService.updateProfile(payload).subscribe({
      next: (res) => {
        this.isSaving = false;
        if (res?.data) {
          this.populateData(res.data);
        }
        this.showToast('Profile updated successfully!', 'success');
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.isSaving = false;
        console.error('Failed to update profile:', err);
        const errMsg = err?.error?.message || 'Failed to update profile. Please try again.';
        this.showToast(errMsg, 'error');
        this.cdr.markForCheck();
      }
    });
  }

  updatePassword(): void {
    this.passwordError = '';
    if (!this.currentPassword) {
      this.passwordError = 'Please provide your current password.';
      return;
    }
    if (!this.newPassword || this.newPassword.length < 6) {
      this.passwordError = 'New password must be at least 6 characters long.';
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.passwordError = 'New passwords do not match.';
      return;
    }

    this.isUpdatingPassword = true;
    this.authService
      .changePassword({
        currentPassword: this.currentPassword,
        newPassword: this.newPassword,
        confirmPassword: this.confirmPassword
      })
      .subscribe({
        next: () => {
          this.isUpdatingPassword = false;
          this.currentPassword = '';
          this.newPassword = '';
          this.confirmPassword = '';
          this.showPasswordSection = false;
          this.showToast('Password changed successfully!', 'success');
          this.cdr.markForCheck();
        },
        error: (err) => {
          this.isUpdatingPassword = false;
          this.passwordError =
            err?.error?.message || 'Password update failed. Verify your current password.';
          this.cdr.markForCheck();
        }
      });
  }

  goBack(): void {
    const role = (this.profile?.role || '').toUpperCase();
    if (role.includes('ADMIN')) {
      this.router.navigate(['/admin-workspace']);
    } else if (role.includes('DEV')) {
      this.router.navigate(['/developer-workspace']);
    } else if (role.includes('TESTER')) {
      this.router.navigate(['/tester-workspace']);
    } else {
      this.location.back();
    }
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => {
      this.toastMessage = '';
      this.cdr.markForCheck();
    }, 4000);
  }
}
