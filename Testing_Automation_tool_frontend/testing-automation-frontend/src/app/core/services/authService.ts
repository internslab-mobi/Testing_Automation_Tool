import { Injectable, inject } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { LoginRequest } from "../../model/auth/loginRequest";
import { AuthResponse } from "../../model/auth/authResponse";
import {Observable} from 'rxjs'

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly authUrl = 'http://localhost:8090/auth';

    login(request:LoginRequest):Observable<AuthResponse>{
        return this.http.post<AuthResponse>(
            this.authUrl+"/login",request
        );
    }
}
