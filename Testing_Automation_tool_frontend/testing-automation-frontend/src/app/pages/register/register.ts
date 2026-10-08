import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/authService';
import { RegisterRequest } from '../../model/auth/registerRequest';

@Component({
  standalone: true,
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  showPassword = false;
  showConfirmPassword = false;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  minLength = false;
  hasUppercase = false;
  hasLowercase = false;
  hasNumber = false;
  strengthLevel = 0;
  strengthText = '';

  readonly registerForm: FormGroup = this.fb.group({
    fullName: ['', [Validators.required]],
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
    acceptTerms: [false, [Validators.requiredTrue]]
  });

  checkPassword(password: string): void {
    this.minLength = password.length >= 8;
    this.hasUppercase = /[A-Z]/.test(password);
    this.hasLowercase = /[a-z]/.test(password);
    this.hasNumber = /[0-9]/.test(password);

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
      case 1: this.strengthText = 'Weak'; break;
      case 2: this.strengthText = 'Fair'; break;
      case 3: this.strengthText = 'Good'; break;
      case 4: this.strengthText = 'Strong'; break;
      default: this.strengthText = 'Very Weak';
    }
  }

  get passwordsMatch(): boolean {
    const pwd = this.registerForm.get('password')?.value;
    const confirm = this.registerForm.get('confirmPassword')?.value;
    return !!pwd && !!confirm && pwd === confirm;
  }

  get isPasswordValid(): boolean {
    return this.minLength && this.hasUppercase && this.hasLowercase && this.hasNumber;
  }

  onRegister(): void {
    if (this.registerForm.invalid || !this.passwordsMatch || !this.isPasswordValid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = true;

    const formValues = this.registerForm.value;
    const request: RegisterRequest = {
      username: formValues.username,
      email: formValues.email,
      password: formValues.password,
      fullName: formValues.fullName
    };

    this.authService.register(request).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response?.message ?? 'Account created successfully! Redirecting to sign in...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1200);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error?.error?.message ?? 'Registration failed. Please verify your details and try again.';
      }
    });
  }
}
