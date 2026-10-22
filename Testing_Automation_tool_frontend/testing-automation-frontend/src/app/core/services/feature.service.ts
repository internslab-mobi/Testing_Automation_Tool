import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Feature, FeatureCreateRequest, FeaturePatchRequest, AttachmentItem } from '../../model/workspace/workspace.model';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  getFeaturesByProject(projectId: number): Observable<Feature[]> {
    return this.http.get<any>(`${this.baseUrl}/feature/project/${projectId}`).pipe(
      map(res => {
        const rawList = res?.data || res || [];
        return rawList.map((item: any) => {
          const f = item.feature || item;
          return {
            featureId: f.featureId,
            projectId: f.projectId || projectId,
            featureName: f.featureName,
            sprint: f.sprint || 1,
            featureVersion: f.featureVersion || 'v1.0',
            description: f.description || '',
            comments: f.comments || '',
            status: f.status || 'ACTIVE',
            createdDate: f.createdDate
          } as Feature;
        });
      })
    );
  }

  createFeature(req: FeatureCreateRequest): Observable<Feature> {
    return this.http.post<any>(`${this.baseUrl}/feature`, req).pipe(
      map(res => res?.data || res)
    );
  }

  updateFeature(featureId: number, req: Partial<FeatureCreateRequest>): Observable<Feature> {
    return this.http.put<any>(`${this.baseUrl}/feature/${featureId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  patchFeature(featureId: number, req: FeaturePatchRequest): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/feature/${featureId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  getFeatureAttachments(featureId: number): Observable<AttachmentItem[]> {
    return this.http.get<any>(`${this.baseUrl}/feature/${featureId}/attachments`).pipe(
      map(res => {
        const list = Array.isArray(res) ? res : (res?.data || []);
        return list.map((item: any) => ({
          attachmentId: item.attachmentId || item.id,
          id: item.attachmentId || item.id,
          fileName: item.fileName,
          fileType: item.fileType || item.contentType,
          fileSize: item.fileSize,
          uploaderName: item.uploaderName,
          uploadedAt: item.createdAt || item.uploadedAt
        } as AttachmentItem));
      }),
      catchError(() => of([]))
    );
  }

  uploadFeatureAttachments(featureId: number, files: File[]): Observable<any> {
    const formData = new FormData();
    files.forEach(f => {
      formData.append('files', f);
      formData.append('file', f);
    });
    return this.http.post<any>(`${this.baseUrl}/feature/${featureId}/attachments`, formData);
  }

  downloadFeatureAttachments(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/feature/${featureId}/attachments/download`, {
      responseType: 'blob'
    });
  }
}

