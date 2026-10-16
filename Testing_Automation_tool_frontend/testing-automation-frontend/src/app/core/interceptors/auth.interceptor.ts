import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

function getCookie(name: string): string | null {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'));
  return match ? decodeURIComponent(match[3]) : null;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // If the request is for an external third-party API (e.g. countriesnow.space), do not attach local credentials or CSRF headers
  const isExternal = req.url.startsWith('http://') || req.url.startsWith('https://');
  const isLocalBackend = req.url.includes('localhost') || req.url.includes('127.0.0.1');
  if (isExternal && !isLocalBackend) {
    return next(req);
  }

  const token = typeof localStorage !== 'undefined' ? (localStorage.getItem('token') || localStorage.getItem('jwt')) : null;
  const csrfToken = getCookie('XSRF-TOKEN') || getCookie('CSRF-TOKEN');

  let setHeaders: Record<string, string> = {};

  if (token && !req.headers.has('Authorization')) {
    setHeaders['Authorization'] = `Bearer ${token}`;
  }

  if (csrfToken) {
    if (!req.headers.has('X-XSRF-TOKEN')) {
      setHeaders['X-XSRF-TOKEN'] = csrfToken;
    }
    if (!req.headers.has('X-CSRF-TOKEN')) {
      setHeaders['X-CSRF-TOKEN'] = csrfToken;
    }
  }

  const modifiedReq = req.clone({
    setHeaders,
    withCredentials: true
  });

  return next(modifiedReq).pipe(
    catchError((err: HttpErrorResponse) => {
      // If 403 Forbidden on state-mutating requests (POST, PUT, PATCH, DELETE) and CSRF was missing/stale,
      // Spring Security sets the Set-Cookie: XSRF-TOKEN in the response.
      // Auto-retry once transparently with the freshly received CSRF token!
      if (err.status === 403 && ['POST', 'PUT', 'PATCH', 'DELETE'].includes(req.method.toUpperCase())) {
        const freshCsrf = getCookie('XSRF-TOKEN') || getCookie('CSRF-TOKEN');
        if (freshCsrf && freshCsrf !== csrfToken) {
          const retryHeaders: Record<string, string> = {
            ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
            'X-XSRF-TOKEN': freshCsrf,
            'X-CSRF-TOKEN': freshCsrf
          };
          const retryReq = req.clone({
            setHeaders: retryHeaders,
            withCredentials: true
          });
          return next(retryReq);
        }
      }

      if (err.status === 401) {
        console.warn('Backend responded with 401 Unauthorized.');
      }
      return throwError(() => err);
    })
  );
};
