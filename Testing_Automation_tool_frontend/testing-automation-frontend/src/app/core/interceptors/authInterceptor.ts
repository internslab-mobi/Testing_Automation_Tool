import { HttpInterceptorFn } from '@angular/common/http';

function getCookie(name: string): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const match = document.cookie.match(
    new RegExp('(^|;\\s*)(' + name + ')=([^;]*)')
  );

  return match ? decodeURIComponent(match[3]) : null;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const headersConfig: Record<string, string> = {};

  // Add CSRF token for state-changing requests
  if (!['GET', 'HEAD', 'OPTIONS'].includes(req.method.toUpperCase())) {
    const xsrfToken = getCookie('XSRF-TOKEN');

    if (xsrfToken) {
      headersConfig['X-XSRF-TOKEN'] = xsrfToken;
    }
  }

  const modifiedReq = req.clone({
    withCredentials: true,
    setHeaders: headersConfig
  });

  return next(modifiedReq);
};
