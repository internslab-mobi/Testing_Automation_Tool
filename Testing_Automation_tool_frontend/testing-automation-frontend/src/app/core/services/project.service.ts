import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, shareReplay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Project, ProjectCreateRequest, ProjectPatchRequest, AttachmentItem, ModuleItem, SubModuleItem, RegionItem } from '../../model/workspace/workspace.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  getProjects(): Observable<Project[]> {
    return this.http.get<any>(`${this.baseUrl}/project`).pipe(
      map(res => {
        const rawList = res?.data || res || [];
        return rawList.map((item: any) => {
          const p = item.project || item;
          const audit = item.audit || {};
          return {
            projectId: p.projectId,
            projectName: p.projectName,
            region: (p.region && p.region.trim()) ? p.region.trim() : 'DEFAULT',
            module: p.module || '',
            subModule: p.subModule || '',
            description: p.description || '',
            comments: p.comments || '',
            status: p.status || 'ACTIVE',
            isActive: p.isActive !== false,
            isDeleted: !!p.isDeleted,
            version: p.version || 0,
            createdBy: audit.createdByName || (audit.createdBy ? `User #${audit.createdBy}` : ''),
            createdByName: audit.createdByName || '',
            createdDate: audit.createdAt || p.createdDate || p.createdAt || '',
            createdAt: audit.createdAt || p.createdAt || '',
            updatedBy: audit.updatedByName || '',
            updatedByName: audit.updatedByName || '',
            updatedAt: audit.updatedAt || p.updatedAt || ''
          } as Project;
        });
      })
    );
  }

  createProject(req: ProjectCreateRequest): Observable<Project> {
    return this.http.post<any>(`${this.baseUrl}/project`, req).pipe(
      map(res => res?.data || res)
    );
  }

  updateProject(projectId: number, req: Partial<ProjectCreateRequest>): Observable<Project> {
    return this.http.put<any>(`${this.baseUrl}/project/${projectId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  patchProject(projectId: number, req: ProjectPatchRequest): Observable<any> {
    if (req.status) {
      return this.patchProjectAccess(projectId, req.status as 'ACTIVE' | 'INACTIVE');
    }
    return this.http.patch<any>(`${this.baseUrl}/project/${projectId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  patchProjectAccess(projectId: number, status: 'ACTIVE' | 'INACTIVE'): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/project/access/${projectId}`, null, {
      params: { status }
    }).pipe(
      map(res => res?.data || res)
    );
  }

  softDeleteProject(projectId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/project/delete/${projectId}`).pipe(
      map(res => res?.data || res)
    );
  }

  getModules(): Observable<ModuleItem[]> {
    return this.http.get<any>(`${this.baseUrl}/project/modules`).pipe(
      map(res => {
        const raw = res?.data || res || [];
        return Array.isArray(raw) ? raw : [];
      }),
      catchError(() => of([]))
    );
  }

  createModule(moduleName: string): Observable<ModuleItem> {
    return this.http.post<any>(`${this.baseUrl}/project/module`, { moduleName }).pipe(
      map(res => res?.data || res)
    );
  }

  getSubModules(moduleId: number): Observable<SubModuleItem[]> {
    return this.http.get<any>(`${this.baseUrl}/project/modules/${moduleId}/sub-modules`).pipe(
      map(res => {
        const raw = res?.data || res || [];
        return Array.isArray(raw) ? raw : [];
      }),
      catchError(() => of([]))
    );
  }

  createSubModule(moduleId: number, subModuleName: string): Observable<SubModuleItem> {
    return this.http.post<any>(`${this.baseUrl}/project/${moduleId}/sub-modules`, { subModuleName }).pipe(
      map(res => res?.data || res)
    );
  }

  getRegions(): Observable<RegionItem[]> {
    return this.http.get<any>(`${this.baseUrl}/project/regions`).pipe(
      map(res => {
        const raw = res?.data || res || [];
        const list = Array.isArray(raw) ? raw : [];
        return list.map((item: any) => ({
          countryId: item.countryId || item.id || item.regionId,
          countryName: (item.countryName || item.regionName || item.name || '').trim()
        } as RegionItem)).filter(r => !!r.countryName);
      }),
      catchError(() => of([]))
    );
  }

  createRegion(regionName: string): Observable<RegionItem> {
    return this.http.post<any>(`${this.baseUrl}/project/region`, { regionName }).pipe(
      map(res => res?.data || res)
    );
  }

  getProjectAttachments(projectId: number): Observable<AttachmentItem[]> {
    return this.http.get<any>(`${this.baseUrl}/project/${projectId}/attachments`).pipe(
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

  uploadProjectAttachments(projectId: number, files: File[]): Observable<any> {
    const formData = new FormData();
    files.forEach(f => {
      formData.append('files', f);
      formData.append('file', f);
    });
    return this.http.post<any>(`${this.baseUrl}/project/${projectId}/attachments`, formData);
  }

  downloadProjectAttachments(projectId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/project/${projectId}/attachments/download`, {
      responseType: 'blob'
    });
  }

  deleteAttachment(attachmentId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/attachments/${attachmentId}`);
  }

  downloadSingleAttachment(attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    });
  }

  viewSingleAttachment(attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/attachments/${attachmentId}/view`, {
      responseType: 'blob'
    });
  }
}
