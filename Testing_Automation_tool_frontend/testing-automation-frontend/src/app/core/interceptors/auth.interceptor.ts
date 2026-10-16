
import {
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

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
  const isApiRequest =
    req.url.startsWith('/api') ||
    req.url.includes(':8082');

  if (!isApiRequest) {
    return next(req);
  }

  const csrfToken = getCookie('XSRF-TOKEN');

  const modifiedReq = req.clone({
    withCredentials: true,
    ...(csrfToken
      ? {
          setHeaders: {
            'X-XSRF-TOKEN': csrfToken
          }
        }
      : {})
  });

  return next(modifiedReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        console.error(
          '401 Unauthorized: inspect the request Cookies tab and backend JWT filter.'
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
