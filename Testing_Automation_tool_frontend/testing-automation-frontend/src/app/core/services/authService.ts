import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { LoginRequest } from "../../model/auth/loginRequest";
import { RegisterRequest } from "../../model/auth/registerRequest";
import { ForgotPasswordRequest } from "../../model/auth/forgotPasswordRequest";
import { VerifyOtpRequest } from "../../model/auth/verifyOtpRequest";
import { ResetPasswordRequest } from "../../model/auth/resetPasswordRequest";
import { AuthResponse } from "../../model/auth/authResponse";
import { UserProfile } from "../../model/auth/userProfile";
import { Observable, tap } from 'rxjs';
import { ApiResponse } from "../../model/auth/commonApiResponse/apiResponse";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authUrl = 'http://localhost:8082/auth';

  // In-Memory Signal for logged-in user profile (Zero LocalStorage)
  private readonly currentUserSignal = signal<UserProfile | null>(null);
  public readonly currentUser = this.currentUserSignal.asReadonly();

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/login`, request).pipe(
      tap((res) => {
        if (res?.data) {
          this.currentUserSignal.set({
            userId: res.data.userId,
            username: res.data.username,
            fullName: res.data.fullName,
            role: res.data.role,
            designation: res.data.designation
          });
        }
      })
    );
  }

  getProfile(): Observable<ApiResponse<UserProfile>> {
    return this.http.get<ApiResponse<UserProfile>>(`${this.authUrl}/profile`).pipe(
      tap({
        next: (res) => {
          if (res?.data) {
            this.currentUserSignal.set(res.data);
          }
        },
        error: () => {
          this.currentUserSignal.set(null);
        }
      })
    );
  }

  logout(): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/logout`, {}).pipe(
      tap({
        next: () => this.currentUserSignal.set(null),
        error: () => this.currentUserSignal.set(null)
      })
    );
  }

  clearSession(): void {
    this.currentUserSignal.set(null);
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/register`, request);
  }

  sendPasswordResetOtp(request: ForgotPasswordRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/forgot-password`, request);
  }

  verifyOtp(request: VerifyOtpRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/verify-otp`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/reset-password`, request);
  }
}
  