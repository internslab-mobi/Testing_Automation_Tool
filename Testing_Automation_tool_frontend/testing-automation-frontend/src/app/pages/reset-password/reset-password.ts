import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/authService';

@Component({
  standalone: true,
  imports: [CommonModule, RouterLink],
  selector: 'app-reset-password',
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css']
})
export class ResetPassword implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  resetToken = '';

  minLength = false;
  hasUppercase = false;
  hasLowercase = false;
  hasNumber = false;

  passwordsMatch = false;
  isPasswordValid = false;

  newPassword = '';
  confirmPassword = '';

  showNewPassword = false;
  showConfirmPassword = false;

  pasteAttempted = false;
  private pasteTimer: any;

  strengthLevel = 0;
  strengthText = '';

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    const navState = history.state;
    this.resetToken = navState?.resetToken || this.route.snapshot.queryParams['token'] || '';
  }

  checkPassword(password: string): void {
    this.newPassword = password;

    this.minLength = password.length >= 8;
    this.hasUppercase = /[A-Z]/.test(password);
    this.hasLowercase = /[a-z]/.test(password);
    this.hasNumber = /[0-9]/.test(password);

    this.isPasswordValid =
      this.minLength &&
      this.hasUppercase &&
      this.hasLowercase &&
      this.hasNumber;

    this.calculateStrength(password);
    this.checkMatch();
  }

  checkConfirmPassword(password: string): void {
    this.confirmPassword = password;
    this.checkMatch();
  }

  checkMatch(): void {
    this.passwordsMatch =
      this.newPassword.length > 0 &&
      this.confirmPassword.length > 0 &&
      this.newPassword === this.confirmPassword;
  }

  calculateStrength(password: string): void {
    if (!password) {
      this.strengthLevel = 0;
      this.strengthText = '';
      return;
    }

    let score = 0;
    if (this.minLength) score++;
    if (this.hasUppercase) score++;
    if (this.hasLowercase) score++;
    if (this.hasNumber) score++;

    this.strengthLevel = score;
    switch (score) {
      case 1:
        this.strengthText = 'Weak';
        break;
      case 2:
        this.strengthText = 'Fair';
        break;
      case 3:
        this.strengthText = 'Good';
        break;
      case 4:
        this.strengthText = 'Strong';
        break;
      default:
        this.strengthText = 'Very Weak';
    }
  }

  handlePasteBlocked(event: ClipboardEvent): void {
    event.preventDefault();
    this.pasteAttempted = true;

    if (this.pasteTimer) {
      clearTimeout(this.pasteTimer);
    }

    this.pasteTimer = setTimeout(() => {
      this.pasteAttempted = false;
    }, 3500);
  }

  onResetPassword(): void {
    if (!this.isPasswordValid || !this.passwordsMatch || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.resetPassword({
      resetToken: this.resetToken,
      newPassword: this.newPassword
    }).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response?.message ?? 'Password reset successfully! Redirecting to sign in...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1200);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error?.error?.message ?? 'Password reset failed. Please request a new OTP.';
      }
    });
  }
}
