import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, tap, map, catchError, finalize, shareReplay, throwError } from 'rxjs';
import { LoginRequest } from "../../model/auth/loginRequest";
import { RegisterRequest } from "../../model/auth/registerRequest";
import { ForgotPasswordRequest } from "../../model/auth/forgotPasswordRequest";
import { VerifyOtpRequest } from "../../model/auth/verifyOtpRequest";
import { ResetPasswordRequest } from "../../model/auth/resetPasswordRequest";
import { RefreshTokenRequest } from "../../model/auth/refreshTokenRequest";
import { AuthResponse } from "../../model/auth/authResponse";
import { UserProfile } from "../../model/auth/userProfile";
import { ApiResponse } from "../../model/auth/commonApiResponse/apiResponse";
import { environment } from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authUrl = `${environment.apiGatewayUrl}/auth`;

  // In-memory token storage (synced with localStorage for cross-refresh persistence)
  private inMemoryAccessToken: string | null = null;
  private inMemoryRefreshToken: string | null = null;

  // Single in-flight refresh observable to deduplicate concurrent 401s
  private refreshInProgress$: Observable<string | null> | null = null;

  // In-Memory Signal for logged-in user profile
  private readonly currentUserSignal = signal<UserProfile | null>(null);
  public readonly currentUser = this.currentUserSignal.asReadonly();

  /**
   * Retrieves the current access token from memory or localStorage.
   */
  getAccessToken(): string | null {
    if (this.inMemoryAccessToken) {
      return this.inMemoryAccessToken;
    }
    if (typeof localStorage !== 'undefined') {
      const stored = localStorage.getItem('token') || localStorage.getItem('jwt');
      if (stored) {
        this.inMemoryAccessToken = stored;
        return stored;
      }
    }
    return null;
  }

  /**
   * Sets or clears the current access token.
   */
  setAccessToken(token: string | null): void {
    this.inMemoryAccessToken = token;
    if (typeof localStorage !== 'undefined') {
      if (token) {
        localStorage.setItem('token', token);
        localStorage.setItem('jwt', token);
      } else {
        localStorage.removeItem('token');
        localStorage.removeItem('jwt');
      }
    }
  }

  /**
   * Retrieves the current refresh token from memory or localStorage.
   */
  getRefreshToken(): string | null {
    if (this.inMemoryRefreshToken) {
      return this.inMemoryRefreshToken;
    }
    if (typeof localStorage !== 'undefined') {
      const stored = localStorage.getItem('refresh_token');
      if (stored) {
        this.inMemoryRefreshToken = stored;
        return stored;
      }
    }
    return null;
  }

  /**
   * Sets or clears the current refresh token.
   */
  setRefreshToken(refreshToken: string | null): void {
    this.inMemoryRefreshToken = refreshToken;
    if (typeof localStorage !== 'undefined') {
      if (refreshToken) {
        localStorage.setItem('refresh_token', refreshToken);
      } else {
        localStorage.removeItem('refresh_token');
      }
    }
  }

  /**
   * Refreshes the access token using the stored 24-hour refresh token or HttpOnly cookie.
   * Returns an Observable emitting the new access token string (or null).
   * Concurrent calls share the same in-flight request.
   */
  refreshToken(explicitToken?: string): Observable<string | null> {
    if (this.refreshInProgress$) {
      return this.refreshInProgress$;
    }

    const currentRefreshToken = this.getRefreshToken()|| explicitToken;
    const payload: RefreshTokenRequest = currentRefreshToken ? { refreshToken: currentRefreshToken } : {};

    this.refreshInProgress$ = this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/refresh`, payload, {
      withCredentials: true
    }).pipe(
      map((res) => {
        const newAccessToken = res?.data?.token || null;
        const newRefreshToken = res?.data?.refreshToken || null;

        if (newAccessToken) {
          this.setAccessToken(newAccessToken);
        }
        if (newRefreshToken) {
          this.setRefreshToken(newRefreshToken);
        }

        if (res?.data) {
          if (res.data.username && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_name', res.data.username);
          }
          if (res.data.fullName && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_fullname', res.data.fullName);
          }
          if (res.data.role && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_role', res.data.role);
          }
          this.currentUserSignal.set({
            userId: res.data.userId,
            username: res.data.username,
            fullName: res.data.fullName,
            role: res.data.role,
            designation: res.data.designation
          });
        }

        return newAccessToken;
      }),
      catchError((err) => {
        this.clearSession();
        return throwError(() => err);
      }),
      finalize(() => {
        this.refreshInProgress$ = null;
      }),
      shareReplay(1)
    );

    return this.refreshInProgress$;
  }

  /**
   * Calls /auth/refresh and returns the full ApiResponse<AuthResponse> Observable.
   */
  refreshAccessToken(explicitToken?: string): Observable<ApiResponse<AuthResponse>> {
    const currentRefreshToken = explicitToken || this.getRefreshToken();
    const payload: RefreshTokenRequest = currentRefreshToken ? { refreshToken: currentRefreshToken } : {};

    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/refresh`, payload, {
      withCredentials: true
    }).pipe(
      tap((res) => {
        if (res?.data?.token) {
          this.setAccessToken(res.data.token);
        }
        if (res?.data?.refreshToken) {
          this.setRefreshToken(res.data.refreshToken);
        }
        if (res?.data) {
          if (res.data.username && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_name', res.data.username);
          }
          if (res.data.fullName && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_fullname', res.data.fullName);
          }
          if (res.data.role && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_role', res.data.role);
          }
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

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/login`, request, { withCredentials: true }).pipe(
      tap((res) => {
        if (res?.data) {
          if (res.data.token) {
            this.setAccessToken(res.data.token);
          }
          if (res.data.refreshToken) {
            this.setRefreshToken(res.data.refreshToken);
          }
          if (res.data.username && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_name', res.data.username);
          }
          if (res.data.fullName && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_fullname', res.data.fullName);
          }
          if (res.data.role && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_role', res.data.role);
          }
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
    return this.http.get<ApiResponse<UserProfile>>(`${this.authUrl}/profile`, { withCredentials: true }).pipe(
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

  /**
   * Initializes the CSRF cookie token from the backend.
   */
  initCsrfToken(): Observable<any> {
    return this.http.get<ApiResponse<string>>(`${this.authUrl}/csrf`, { withCredentials: true }).pipe(
      catchError((err) => {
        console.warn('Could not initialize CSRF token:', err);
        return throwError(() => err);
      })
    );
  }

  logout(): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap({
        next: () => this.clearSession(),
        error: () => this.clearSession()
      })
    );
  }

  clearSession(): void {
    this.inMemoryAccessToken = null;
    this.inMemoryRefreshToken = null;
    this.currentUserSignal.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('jwt');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('tat_user_name');
      localStorage.removeItem('tat_user_fullname');
      localStorage.removeItem('tat_user_role');
    }
  }

  register(request: RegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/register`, request, { withCredentials: true });
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
