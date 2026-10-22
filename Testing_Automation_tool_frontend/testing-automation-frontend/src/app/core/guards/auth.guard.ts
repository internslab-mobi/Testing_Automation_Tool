import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from '../services/authService';

/**
 * Route guard restricting access exclusively to authenticated users.
 * 
 * - Unauthenticated users (or users with expired/invalid sessions) are redirected to /login.
 * - Authenticated users are permitted to activate the target route.
 */
export const authGuard: CanActivateFn = (): Observable<boolean | UrlTree> | boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Check in-memory cached user from active session
  const cachedUser = authService.currentUser();
  if (cachedUser) {
    return true;
  }

  // 2. If no user in memory, verify session with backend profile API
  return authService.getProfile().pipe(
    map((res) => {
      const user = res?.data;
      if (user) {
        return true;
      }
      return router.createUrlTree(['/login']);
    }),
    catchError(() => {
      // Unauthenticated (401, expired session, network failure) -> redirect to login
      return of(router.createUrlTree(['/login']));
    })
  );
};
