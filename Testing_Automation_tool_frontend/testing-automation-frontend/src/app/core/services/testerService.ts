import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../model/auth/commonApiResponse/apiResponse';

@Injectable({
  providedIn: 'root'
})
export class TesterService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8082';

  // 1. Projects API
  getProjects(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/project`);
  }

  createProject(request: { projectName: string; description?: string; region: string; comments?: string }): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/project`, request);
  }

  uploadProjectAttachments(projectId: number, files: File[]): Observable<ApiResponse<any[]>> {
    const formData = new FormData();
    files.forEach(file => formData.append('file', file));
    return this.http.post<ApiResponse<any[]>>(`${this.baseUrl}/project/attachment/${projectId}`, formData);
  }

  // 2. Features API
  getFeaturesByProject(projectId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/feature/project/${projectId}`);
  }

  createFeature(request: {
    projectId: number;
    featureName: string;
    description?: string;
    comments?: string;
    sprint: number;
    featureVersion: string;
  }): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/feature`, request);
  }

  updateFeature(featureId: number, request: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.baseUrl}/feature/${featureId}`, request);
  }

  exportFeatureSummary(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/feature/${featureId}/export`, { responseType: 'blob' });
  }

  uploadFeatureAttachments(featureId: number, files: File[]): Observable<ApiResponse<any[]>> {
    const formData = new FormData();
    files.forEach(file => formData.append('file', file));
    return this.http.post<ApiResponse<any[]>>(`${this.baseUrl}/feature/attachment/${featureId}`, formData);
  }

  // 3. Test Cases API
  getTestCases(params?: {
    featureId?: number;
    status?: string;
    type?: string;
    priority?: string;
    page?: number;
    size?: number;
  }): Observable<ApiResponse<any>> {
    let httpParams = new HttpParams();
    if (params?.featureId !== undefined) httpParams = httpParams.set('featureId', params.featureId.toString());
    if (params?.status && params.status !== 'ALL') httpParams = httpParams.set('status', params.status);
    if (params?.type && params.type !== 'ALL') httpParams = httpParams.set('type', params.type);
    if (params?.priority && params.priority !== 'ALL') httpParams = httpParams.set('priority', params.priority);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/testcases`, { params: httpParams });
  }

  createTestCase(request: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/testcases`, request);
  }

  uploadExcelTestCases(featureId: number, file: File): Observable<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('featureId', featureId.toString());
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/testcases/upload`, formData);
  }

  downloadTestCaseTemplate(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/testcases/${featureId}/template`, { responseType: 'blob' });
  }

  // 4. Bugs API
  getBugs(page = 0, size = 50): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<ApiResponse<any>>(`${this.baseUrl}/bugs`, { params });
  }

  createBug(testcaseId: number, request: any): Observable<ApiResponse<any>> {
    const params = new HttpParams().set('testcaseId', testcaseId.toString());
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/bugs`, request, { params });
  }

  patchBug(bugId: number, request: any): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.baseUrl}/bugs/${bugId}`, request);
  }

  // 5. Bug Comments / Chat API
  getBugComments(bugId: number): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/bugs/${bugId}/comments`);
  }

  addBugComment(bugId: number, comment: string): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.baseUrl}/bugs/${bugId}/comments`, { comment });
  }

  // 6. In-App Notifications API
  getNotifications(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.baseUrl}/in-app-notifications`);
  }

  patchNotification(notificationId: number, status?: string): Observable<ApiResponse<string>> {
    return this.http.patch<ApiResponse<string>>(`${this.baseUrl}/in-app-notifications/${notificationId}`, status ? { status } : {});
  }

  deleteNotification(notificationId: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.baseUrl}/in-app-notifications/${notificationId}`);
  }
}
