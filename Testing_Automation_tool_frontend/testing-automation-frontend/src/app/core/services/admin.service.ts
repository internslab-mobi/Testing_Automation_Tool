import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateManagerRequest, ManagerResponse } from '../../model/admin/admin.model';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  timestamp?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  createManager(request: CreateManagerRequest): Observable<ApiResponse<ManagerResponse>> {
    return this.http.post<ApiResponse<ManagerResponse>>(`${this.baseUrl}/admin/managers`, request);
  }

  updateManager(userId: number, request: CreateManagerRequest): Observable<ApiResponse<ManagerResponse>> {
    return this.http.put<ApiResponse<ManagerResponse>>(`${this.baseUrl}/admin/managers/${userId}`, request);
  }

  updateUser(userId: number, request: CreateManagerRequest): Observable<ApiResponse<ManagerResponse>> {
    return this.http.put<ApiResponse<ManagerResponse>>(`${this.baseUrl}/admin/users/${userId}`, request);
  }

  patchManager(userId: number, request: Partial<CreateManagerRequest>): Observable<ApiResponse<string>> {
    return this.http.patch<ApiResponse<string>>(`${this.baseUrl}/admin/managers/${userId}`, request);
  }

  getUserById(userId: number): Observable<ApiResponse<ManagerResponse>> {
    return this.http.get<ApiResponse<ManagerResponse>>(`${this.baseUrl}/admin/users/${userId}`);
  }

  getAllUsers(): Observable<ApiResponse<ManagerResponse[]>> {
    return this.http.get<ApiResponse<ManagerResponse[]>>(`${this.baseUrl}/admin/users`);
  }

  getUsersByRole(role: string): Observable<ApiResponse<ManagerResponse[]>> {
    return this.http.get<ApiResponse<ManagerResponse[]>>(`${this.baseUrl}/admin/users/role/${role}`);
  }

  softDeleteUser(userId: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.baseUrl}/admin/users/${userId}`);
  }

  hardDeleteUser(userId: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.baseUrl}/admin/users/${userId}/permanent`);
  }
}
