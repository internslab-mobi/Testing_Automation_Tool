import {
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/authService';

function getCookie(name: string): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const escapedName = name.replace(
    /[.*+?^${}()|[\]\\]/g,
    '\\$&'
  );

  const match = document.cookie.match(
    new RegExp('(?:^|;\\s*)' + escapedName + '=([^;]*)')
  );

  return match ? decodeURIComponent(match[1]) : null;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isApiRequest =
    req.url.startsWith('/api') ||
    req.url.includes(':8082');

  if (!isApiRequest) {
    return next(req);
  }

  const csrfToken = getCookie('XSRF-TOKEN');
  const accessToken = authService.getAccessToken();

  const headersConfig: Record<string, string> = {};
  if (csrfToken) {
    headersConfig['X-XSRF-TOKEN'] = csrfToken;
  }
  if (accessToken && !req.headers.has('Authorization')) {
    headersConfig['Authorization'] = `Bearer ${accessToken}`;
  }

  const modifiedReq = req.clone({
    withCredentials: true,
    setHeaders: headersConfig
  });

  return next(modifiedReq).pipe(
    catchError((err: HttpErrorResponse) => {
      // Avoid refreshing on auth endpoints (login, refresh, register, reset) to prevent loops
      const isAuthEndpoint =
        req.url.includes('/auth/login') ||
        req.url.includes('/auth/refresh') ||
        req.url.includes('/auth/register') ||
        req.url.includes('/auth/forgot-password') ||
        req.url.includes('/auth/reset-password') ||
        req.url.includes('/auth/verify-otp');

      if (err.status === 401 && !isAuthEndpoint) {
        return authService.refreshToken().pipe(
          switchMap((newAccessToken) => {
            if (!newAccessToken) {
              authService.clearSession();
              router.navigate(['/login']);
              return throwError(() => err);
            }

            // Retry the original request with the refreshed access token
            const retryHeaders = { ...headersConfig };
            retryHeaders['Authorization'] = `Bearer ${newAccessToken}`;

            const retriedReq = req.clone({
              withCredentials: true,
              setHeaders: retryHeaders
            });

            return next(retriedReq);
          }),
          catchError((refreshErr) => {
            authService.clearSession();
            router.navigate(['/login']);
            return throwError(() => refreshErr);
          })
        );
      }

      if (err.status === 403) {
        console.error(
          '403 Forbidden: inspect CSRF validation and user permissions.'
        );
      }

      return throwError(() => err);
    })
  );
};
