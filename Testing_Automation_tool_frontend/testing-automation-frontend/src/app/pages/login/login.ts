import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { AuthService } from '../../core/services/authService';
import { LoginRequest } from '../../model/auth/loginRequest';
import { Router, RouterLink } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cd = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.authService.initCsrfToken().subscribe({
      next: () => {},
      error: (e) => console.warn('CSRF init failed (will retry on demand)', e)
    });
  }


  readonly loginForm: FormGroup = this.fb.group({
    usernameOrEmail: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  isLoading = false;
  loginError = '';

  login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loginError = '';
    this.isLoading = true;

    const request: LoginRequest = {
      usernameOrEmail: this.loginForm.value.usernameOrEmail,
      password: this.loginForm.value.password
    };

    this.authService.login(request).subscribe({
      next: () => {
        this.isLoading = false;
        // Warm up CSRF token for downstream services so initial actions are immediately permitted
        this.authService.initCsrfToken().subscribe({ error: () => {} });
        const cachedUser = this.authService.currentUser();
        const role = (cachedUser?.role || "").toUpperCase();
        if (role.includes('ADMIN')) {
          this.router.navigate(['/admin-workspace']);
        } else if (role.includes('DEV')) {
          this.router.navigate(['/developer-workspace']);
        } else if (role.includes('TESTER')) {
          this.router.navigate(['/tester-workspace']);
        }
        else if (role.includes('MANAGER')) {
          this.router.navigate(['/manager-dashboard']);
        }
        else {
          this.router.navigate(['/tester-workspace']);
        }
      },

      error: (error) => {
        this.cd.markForCheck()
        this.isLoading = false;
        console.error('Login failed:', error);
        this.loginError =
          error?.error?.message ?? 'Invalid username or password.';
      }
    });
  }
}
