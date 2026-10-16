import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, shareReplay } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Project, ProjectCreateRequest, AttachmentItem } from '../../model/workspace/workspace.model';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;
  private cachedCountries$?: Observable<string[]>;

  getProjects(): Observable<Project[]> {
    return this.http.get<any>(`${this.baseUrl}/project`).pipe(
      map(res => {
        const rawList = res?.data || res || [];
        return rawList.map((item: any) => {
          const p = item.project || item;
          return {
            projectId: p.projectId,
            projectName: p.projectName,
            region: (p.region && p.region.trim()) ? p.region.trim() : 'DEFAULT',
            description: p.description || '',
            comments: p.comments || '',
            status: p.status || 'ACTIVE',
            createdDate: p.createdDate
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

  getCountryList(): Observable<string[]> {
    if (this.cachedCountries$) {
      return this.cachedCountries$;
    }
    this.cachedCountries$ = this.http.get<any>('https://countriesnow.space/api/v0.1/countries').pipe(
      map(res => {
        const data = res?.data || [];
        if (Array.isArray(data)) {
          const list = data
            .map((item: any) => typeof item === 'string' ? item : item?.country)
            .filter((name: string) => !!name && name.trim().length > 0)
            .sort((a: string, b: string) => a.localeCompare(b));
          if (list.length > 0) return list;
        }
        return this.getDefaultCountryList();
      }),
      catchError(() => of(this.getDefaultCountryList())),
      shareReplay(1)
    );
    return this.cachedCountries$;
  }

  private getDefaultCountryList(): string[] {
    return [
      'Afghanistan', 'Albania', 'Algeria', 'Andorra', 'Angola', 'Argentina', 'Armenia', 'Australia', 'Austria',
      'Azerbaijan', 'Bahamas', 'Bahrain', 'Bangladesh', 'Barbados', 'Belarus', 'Belgium', 'Belize', 'Benin',
      'Bhutan', 'Bolivia', 'Bosnia and Herzegovina', 'Botswana', 'Brazil', 'Brunei', 'Bulgaria', 'Cambodia',
      'Cameroon', 'Canada', 'Chile', 'China', 'Colombia', 'Costa Rica', 'Croatia', 'Cuba', 'Cyprus',
      'Czech Republic', 'Denmark', 'Dominican Republic', 'Ecuador', 'Egypt', 'Estonia', 'Ethiopia', 'Fiji',
      'Finland', 'France', 'Georgia', 'Germany', 'Ghana', 'Greece', 'Guatemala', 'Honduras', 'Hong Kong',
      'Hungary', 'Iceland', 'India', 'Indonesia', 'Iran', 'Iraq', 'Ireland', 'Israel', 'Italy', 'Jamaica',
      'Japan', 'Jordan', 'Kazakhstan', 'Kenya', 'Kuwait', 'Latvia', 'Lebanon', 'Lithuania', 'Luxembourg',
      'Malaysia', 'Maldives', 'Malta', 'Mexico', 'Monaco', 'Morocco', 'Nepal', 'Netherlands', 'New Zealand',
      'Nigeria', 'Norway', 'Oman', 'Pakistan', 'Panama', 'Peru', 'Philippines', 'Poland', 'Portugal', 'Qatar',
      'Romania', 'Saudi Arabia', 'Singapore', 'South Africa', 'South Korea', 'Spain', 'Sri Lanka', 'Sweden',
      'Switzerland', 'Taiwan', 'Thailand', 'Turkey', 'Ukraine', 'United Arab Emirates', 'United Kingdom',
      'United States', 'Uruguay', 'Uzbekistan', 'Vietnam', 'Zimbabwe'
    ];
  }
}
