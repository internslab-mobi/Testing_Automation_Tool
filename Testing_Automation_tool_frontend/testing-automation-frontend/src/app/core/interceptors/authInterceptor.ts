import { HttpInterceptorFn } from '@angular/common/http';

function getCookie(name: string): string | null {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'));
  return match ? decodeURIComponent(match[3]) : null;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const headersConfig: Record<string, string> = {};

  // For Spring Security double submit CSRF token with cookies
  if (!['GET', 'HEAD', 'OPTIONS'].includes(req.method.toUpperCase())) {
    const xsrfToken = getCookie('XSRF-TOKEN');
    if (xsrfToken) {
      headersConfig['X-XSRF-TOKEN'] = xsrfToken;
    }
  }

  // Pure cookie-based authentication:
  // Browser automatically sends the HttpOnly accessToken & refreshToken cookies with `withCredentials: true`.
  // No tokens are stored in or retrieved from localStorage.
  const modifiedReq = req.clone({
    withCredentials: true,
    setHeaders: headersConfig
  });

  return next(modifiedReq);
};
