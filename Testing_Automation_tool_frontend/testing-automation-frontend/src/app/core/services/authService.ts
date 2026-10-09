import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { LoginRequest } from "../../model/auth/loginRequest";
import { RegisterRequest } from "../../model/auth/registerRequest";
import { ForgotPasswordRequest } from "../../model/auth/forgotPasswordRequest";
import { VerifyOtpRequest } from "../../model/auth/verifyOtpRequest";
import { ResetPasswordRequest } from "../../model/auth/resetPasswordRequest";
import { AuthResponse } from "../../model/auth/authResponse";
import { Observable } from 'rxjs';
import { ApiResponse } from "../../model/auth/commonApiResponse/apiResponse";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authUrl = 'http://localhost:8090/auth';

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/login`, request);
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
