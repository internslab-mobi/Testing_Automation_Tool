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
  // Pure HttpOnly Cookie authentication: credentials sent automatically via withCredentials: true
  // headersConfig['Authorization'] = `Bearer ${accessToken}`;

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

            // Retry the original request with the refreshed access token (HttpOnly cookie sent automatically via withCredentials)
            const retryHeaders = { ...headersConfig };
            // Re-read fresh CSRF cookie in case the refresh request rotated or renewed the XSRF-TOKEN
            const freshCsrf = getCookie('XSRF-TOKEN');
            if (freshCsrf) {
              retryHeaders['X-XSRF-TOKEN'] = freshCsrf;
            }
            // retryHeaders['Authorization'] = `Bearer ${newAccessToken}`;

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

      if (err.status === 403 && !isAuthEndpoint) {
        // CSRF token mismatch or missing token causes 403 Forbidden on initial action.
        // Proactively refresh the CSRF cookie and seamlessly retry the request once.
        return authService.initCsrfToken().pipe(
          switchMap(() => {
            const freshCsrf = getCookie('XSRF-TOKEN');
            const retryHeaders = { ...headersConfig };
            if (freshCsrf) {
              retryHeaders['X-XSRF-TOKEN'] = freshCsrf;
            }
            const retriedReq = req.clone({
              withCredentials: true,
              setHeaders: retryHeaders
            });
            return next(retriedReq);
          }),
          catchError(() => throwError(() => err))
        );
      }

      return throwError(() => err);
    })
  );
};
