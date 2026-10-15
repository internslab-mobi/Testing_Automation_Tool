import { Component, inject } from '@angular/core';
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
export class Login {

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly cd = inject(ChangeDetectorRef);


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
      next: (response) => {
        this.isLoading = false;
        console.log('Login successful:', response);
            const role = (response?.data.role || '').toUpperCase();
        if (role.includes('TESTER')) {
          this.router.navigate(['/tester-workspace']);
        } else {
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
