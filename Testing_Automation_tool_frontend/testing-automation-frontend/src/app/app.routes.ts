import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { ResetPassword} from './pages/reset-password/reset-password';
import { ForgotPassword } from './pages/forgot-password/forgot-password';
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
        path: 'reset-password',
        component: ResetPassword
    },
    {
        path: 'forgot-password',
        component: ForgotPassword
    }

];
