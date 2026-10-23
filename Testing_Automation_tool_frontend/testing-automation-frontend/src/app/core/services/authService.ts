import { Injectable, inject, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable, tap, map, catchError, finalize, shareReplay, throwError, timer, Subscription, switchMap } from 'rxjs';
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

  // Background proactive 14th-minute auto-refresh subscription
  private refreshTimerSub: Subscription | null = null;

  // In-Memory Signal for logged-in user profile
  private readonly currentUserSignal = signal<UserProfile | null>(null);
  public readonly currentUser = this.currentUserSignal.asReadonly();

  constructor() {
    this.restoreSessionFromToken();
  }

  private restoreSessionFromToken(): void {
    const token = this.getAccessToken();
    if (token) {
      try {
        const payloadBase64 = token.split('.')[1];
        if (payloadBase64) {
          const decoded = JSON.parse(atob(payloadBase64));
          const profile: UserProfile = {
            userId: decoded.userId ? Number(decoded.userId) : undefined,
            username: decoded.sub || decoded.username || '',
            email: decoded.email,
            role: decoded.role,
            fullName: decoded.fullName
          };
          this.currentUserSignal.set(profile);
          if (typeof localStorage !== 'undefined' && profile.userId) {
            localStorage.setItem('tat_user_id', String(profile.userId));
            if (profile.role) localStorage.setItem('tat_user_role', profile.role);
            if (profile.username) localStorage.setItem('tat_user_name', profile.username);
            if (profile.fullName) localStorage.setItem('tat_user_fullname', profile.fullName);
          }

          // If JWT has expiration claim, schedule proactive refresh 60s before expiry
          if (decoded.exp) {
            const nowSec = Math.floor(Date.now() / 1000);
            const remainingSec = decoded.exp - nowSec;
            if (remainingSec > 60) {
              this.scheduleTokenRefresh(remainingSec);
            } else {
              this.scheduleTokenRefresh(900);
            }
          } else {
            this.scheduleTokenRefresh(900);
          }
        }
      } catch (e) {
        // ignore decoding errors
      }
    }
  }

  getCurrentUser(): UserProfile | null {
    const mem = this.currentUserSignal();
    if (mem && mem.userId) return mem;

    const token = this.getAccessToken();
    if (token) {
      try {
        const payloadBase64 = token.split('.')[1];
        if (payloadBase64) {
          const decoded = JSON.parse(atob(payloadBase64));
          const profile: UserProfile = {
            userId: decoded.userId ? Number(decoded.userId) : undefined,
            username: decoded.sub || decoded.username || '',
            email: decoded.email,
            role: decoded.role,
            fullName: decoded.fullName
          };
          if (profile.userId) {
            this.currentUserSignal.set(profile);
            if (typeof localStorage !== 'undefined') {
              localStorage.setItem('tat_user_id', String(profile.userId));
              if (profile.role) localStorage.setItem('tat_user_role', profile.role);
              if (profile.username) localStorage.setItem('tat_user_name', profile.username);
              if (profile.fullName) localStorage.setItem('tat_user_fullname', profile.fullName);
            }
            return profile;
          }
        }
      } catch (e) {}
    }
    return mem;
  }

  getUserId(): number | null {
    const profile = this.getCurrentUser();
    if (profile?.userId) return Number(profile.userId);
    if (typeof localStorage !== 'undefined') {
      const stored = localStorage.getItem('tat_user_id');
      if (stored) return Number(stored);
    }
    return null;
  }

  /**
   * Retrieves the current access token from memory only (avoiding localStorage to prevent XSS leakage).
   */
  getAccessToken(): string | null {
    return this.inMemoryAccessToken;
  }

  /**
   * Sets or clears the current access token in memory only.
   */
  setAccessToken(token: string | null): void {
    this.inMemoryAccessToken = token;
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('jwt');
    }
  }

  /**
   * Retrieves the current refresh token from memory or localStorage fallback.
   */
  getRefreshToken(): string | null {
    if (this.inMemoryRefreshToken) return this.inMemoryRefreshToken;
    if (typeof localStorage !== 'undefined') {
      return localStorage.getItem('tat_refresh_token');
    }
    return null;
  }

  /**
   * Sets or clears the current refresh token in memory and localStorage.
   */
  setRefreshToken(refreshToken: string | null): void {
    this.inMemoryRefreshToken = refreshToken;
    if (typeof localStorage !== 'undefined') {
      if (refreshToken) {
        localStorage.setItem('tat_refresh_token', refreshToken);
      } else {
        localStorage.removeItem('tat_refresh_token');
        localStorage.removeItem('refresh_token');
      }
    }
  }

  /**
   * Proactive 14th-Minute Auto-Refresh Timer (Heartbeat).
   * Automatically refreshes the access token 1 minute before expiry (at minute 14 of 15).
   * Runs seamlessly in the background so the user NEVER encounters a 401 error.
   */
  public scheduleTokenRefresh(expiresInSeconds: number = 900): void {
    this.stopTokenRefreshTimer();

    // 15 minutes = 900 seconds. We trigger 1 minute before expiry -> 14 minutes = 840 seconds (840,000 ms)
    const refreshDelayMs = Math.max((expiresInSeconds - 60) * 1000, 30000);

    console.info(`[AuthService] Proactive auto-refresh scheduled in ${refreshDelayMs / 1000}s (14th-minute heartbeat)`);

    this.refreshTimerSub = timer(refreshDelayMs).pipe(
      switchMap(() => this.refreshToken())
    ).subscribe({
      next: (newToken) => {
        if (newToken) {
          console.info('[AuthService] 14th-minute proactive token refresh successful. Next cycle scheduled.');
        }
      },
      error: (err) => {
        console.warn('[AuthService] 14th-minute proactive token refresh failed:', err);
      }
    });
  }

  /**
   * Cancels the proactive refresh timer (e.g. on logout or session clear).
   */
  public stopTokenRefreshTimer(): void {
    if (this.refreshTimerSub) {
      this.refreshTimerSub.unsubscribe();
      this.refreshTimerSub = null;
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
          // Re-schedule proactive refresh for the new 15-minute access token (runs at 14m)
          const expiresIn = res?.data?.expiresIn || 900;
          this.scheduleTokenRefresh(expiresIn);
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
          if (res.data.userId && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_id', String(res.data.userId));
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

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.authUrl}/login`, request, { withCredentials: true }).pipe(
      tap((res) => {
        if (res?.data) {
          if (res.data.token) {
            this.setAccessToken(res.data.token);
            // Start proactive 14-minute refresh timer on login!
            const expiresIn = res.data.expiresIn || 900;
            this.scheduleTokenRefresh(expiresIn);
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
          if (res.data.userId && typeof localStorage !== 'undefined') {
            localStorage.setItem('tat_user_id', String(res.data.userId));
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
            // Restore proactive 14-minute refresh timer on profile recovery!
            this.scheduleTokenRefresh(900);
            if (typeof localStorage !== 'undefined') {
              if (res.data.userId) localStorage.setItem('tat_user_id', String(res.data.userId));
              if (res.data.username) localStorage.setItem('tat_user_name', res.data.username);
              if (res.data.fullName) localStorage.setItem('tat_user_fullname', res.data.fullName);
              if (res.data.role) localStorage.setItem('tat_user_role', res.data.role);
            }
          }
        },
        error: () => {
          this.currentUserSignal.set(null);
        }
      })
    );
  }

  updateProfile(payload: { fullName?: string; designation?: string; skills?: string }): Observable<ApiResponse<UserProfile>> {
    return this.http.put<ApiResponse<UserProfile>>(`${this.authUrl}/profile`, payload, { withCredentials: true }).pipe(
      tap({
        next: (res) => {
          if (res?.data) {
            this.currentUserSignal.set(res.data);
            if (typeof localStorage !== 'undefined') {
              if (res.data.userId) localStorage.setItem('tat_user_id', String(res.data.userId));
              if (res.data.username) localStorage.setItem('tat_user_name', res.data.username);
              if (res.data.fullName) localStorage.setItem('tat_user_fullname', res.data.fullName);
              if (res.data.role) localStorage.setItem('tat_user_role', res.data.role);
            }
          }
        }
      })
    );
  }

  changePassword(payload: { currentPassword: string; newPassword: string; confirmPassword?: string }): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.authUrl}/change-password`, payload, { withCredentials: true });
  }

  /**
   * Initializes the CSRF cookie token from the backend.
   * Proactively hits the main-service CSRF endpoint (and falls back to auth-service)
   * to ensure XSRF-TOKEN is placed in cookies before any state-changing action.
   */
  initCsrfToken(): Observable<any> {
    const csrfUrl = `${environment.apiGatewayUrl}/csrf`;
    return this.http.get<ApiResponse<string>>(csrfUrl, { withCredentials: true }).pipe(
      catchError((err) => {
        console.warn('Could not initialize CSRF token from API Gateway:', err);
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
    this.stopTokenRefreshTimer();
    this.inMemoryAccessToken = null;
    this.inMemoryRefreshToken = null;
    this.currentUserSignal.set(null);
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('jwt');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('tat_refresh_token');
      localStorage.removeItem('tat_user_id');
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
