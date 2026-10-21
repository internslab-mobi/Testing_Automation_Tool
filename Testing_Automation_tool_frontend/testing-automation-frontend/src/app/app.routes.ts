import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { ForgotPassword } from './pages/forgot-password/forgot-password';
import { VerifyOtp } from './pages/verify-otp/verify-otp';
import { ResetPassword } from './pages/reset-password/reset-password';
import { TesterWorkspace } from './pages/tester-workspace/tester-workspace';
import { ManagerDashboard } from './pages/manager-dashboard/manager-dashboard';
import { managerGuard } from './core/guards/manager.guard';
import { DeveloperWorkspace } from './pages/developer-workspace/developer-workspace';
import { AdminWorkspace } from './pages/admin-workspace/admin-workspace';
import { Profile } from './pages/profile/profile';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: Login
  },
  {
    path: 'profile',
    component: Profile
  },
  {
    path: 'manager-dashboard',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager',
    redirectTo: 'manager-dashboard',
    pathMatch: 'full'
  },
  {
    path: 'manager/system-dashboard',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager/project-dashboard',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager/qa-dashboard',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager/test-execution',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager/defect-management',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager/automation-reports',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'manager/create-dashboard',
    component: ManagerDashboard,
    canActivate: [managerGuard]
  },
  {
    path: 'tester-workspace',
    component: TesterWorkspace
  },
  {
    path: 'tester',
    redirectTo: 'tester-workspace',
    pathMatch: 'full'
  },
  {
    path: 'developer-workspace',
    component: DeveloperWorkspace
  },
  {
    path: 'developer',
    redirectTo: 'developer-workspace',
    pathMatch: 'full'
  },
  {
    path: 'admin-workspace',
    component: AdminWorkspace
  },
  {
    path: 'admin',
    redirectTo: 'admin-workspace',
    pathMatch: 'full'
  },
  {
    path: 'register',
    component: Register
  },
  {
    path: 'forgot-password',
    component: ForgotPassword
  },
  {
    path: 'verify-otp',
    component: VerifyOtp
  },
  {
    path: 'otp',
    redirectTo: 'verify-otp',
    pathMatch: 'full'
  },
  {
    path: 'reset-password',
    component: ResetPassword
  }
];
