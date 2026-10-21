import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { AuthService } from '../services/authService';

/**
 * Route guard restricting access exclusively to authenticated users with role MANAGER (or ADMIN).
 * 
 * - Unauthenticated users are redirected to the login page (/login).
 * - Authenticated users who are not Managers (e.g. Testers) are redirected to their
 *   role-appropriate workspace (/tester-workspace) without causing redirect loops.
 */
export const managerGuard: CanActivateFn = (): Observable<boolean | UrlTree> | boolean | UrlTree => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const cachedUser = authService.currentUser();
  if (cachedUser) {
    const role = (cachedUser.role || '').toUpperCase();
    if (role.includes('MANAGER') || role.includes('ADMIN')) {
      return true;
    }
    // Authenticated user is not a Manager -> redirect to Tester workspace
    return router.createUrlTree(['/tester-workspace']);
  }

  // If no user in memory, verify session with backend profile API
  return authService.getProfile().pipe(
    map((res) => {
      const user = res?.data;
      if (!user) {
        return router.createUrlTree(['/login']);
      }
      const role = (user.role || '').toUpperCase();
      if (role.includes('MANAGER') || role.includes('ADMIN')) {
        return true;
      }
      // Authenticated but not a Manager -> redirect to role workspace
      return router.createUrlTree(['/tester-workspace']);
    }),
    catchError(() => {
      // Unauthenticated (401, expired session, network failure) -> redirect to login
      return of(router.createUrlTree(['/login']));
    })
  );
};
