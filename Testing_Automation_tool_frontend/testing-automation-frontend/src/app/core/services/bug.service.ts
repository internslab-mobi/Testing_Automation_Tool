import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Bug, BugCreateRequest, BugPutRequest, AttachmentItem, BugComment } from '../../model/workspace/workspace.model';

export interface DeveloperUser {
  userId: number;
  username: string;
  fullName?: string;
  email?: string;
  role?: string;
}

@Injectable({
  providedIn: 'root'
})
export class BugService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  getBugsByFeature(featureId: number): Observable<Bug[]> {
    return this.http.get<any>(`${this.baseUrl}/bug?featureId=${featureId}&page=0&size=100`).pipe(
      map(res => {
        const pageData = res?.data || res;
        const rawList = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        return rawList.map((item: any) => this.mapBugEntity(item));
      })
    );
  }

  getAllBugs(): Observable<Bug[]> {
    return this.http.get<any>(`${this.baseUrl}/bug?page=0&size=100`).pipe(
      map(res => {
        const pageData = res?.data || res;
        const rawList = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        return rawList.map((item: any) => this.mapBugEntity(item));
      })
    );
  }

  private mapBugEntity(item: any): Bug {
    const b = item.bug || item;
    const wf = item.workflow || b.workflow || {};
    const assignedUser = b.assignedTo || {};
    const reporterUser = b.reportedBy || b.executedBy || {};

    return {
      bugId: b.bugId,
      bugFormatId: b.bugFormatId || `BUG_${b.bugId}`,
      testcaseId: b.testcaseId || (b.testCase ? b.testCase.testcaseId : undefined),
      testcaseFormatId: b.testcaseFormatId || (b.testCase ? (b.testCase.testcaseFormatId || `TC_${b.testCase.testcaseId}`) : 'TC_GENERAL'),
      featureId: b.featureId || (b.testCase ? b.testCase.featureId : undefined),
      title: b.title,
      description: b.description || '',
      severity: b.severity || 'HIGH',
      priority: b.priority || 'HIGH',
      status: b.status || 'OPEN',
      category: b.category || 'FUNCTIONAL',
      bugOccurrence: b.bugOccurrence || 1,
      comments: b.comments || '',
      assignedTo: assignedUser.fullName || assignedUser.username || wf.assignedTo || '',
      assignedToId: assignedUser.userId || undefined,
      reportedBy: reporterUser.fullName || reporterUser.username || wf.reportedBy || 'Tester',
      createdAt: b.createdAt,
      workflow: {
        workflowId: wf.workflowId,
        assignedTo: assignedUser.fullName || assignedUser.username || wf.assignedTo || 'Unassigned',
        reportedBy: reporterUser.fullName || reporterUser.username || wf.reportedBy || 'QA',
        status: b.status || 'OPEN'
      }
    };
  }

  getDevelopers(): Observable<DeveloperUser[]> {
    return this.http.get<any>(`${this.baseUrl}/bug/developers`).pipe(
      map(res => {
        const raw = res?.data || res || [];
        const list = Array.isArray(raw) ? raw : [];
        return list.map((item: any) => this.mapDeveloper(item));
      }),
      catchError(err => {
        console.error('Error fetching developers from backend API /bugs/developers:', err);
        return of([]);
      })
    );
  }

  private mapDeveloper(u: any): DeveloperUser {
    const roleStr = typeof u.role === 'object'
      ? (u.role?.role || u.role?.roleName || 'DEVELOPER')
      : (u.role || 'DEVELOPER');

    return {
      userId: u.userId || u.id,
      username: u.username || '',
      fullName: u.fullName || u.username || `Developer #${u.userId || u.id}`,
      email: u.email || '',
      role: roleStr
    };
  }

  downloadSingleAttachment(attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/attachments/${attachmentId}/download`, {
      responseType: 'blob'
    });
  }

  createBug(testcaseId: number, req: BugCreateRequest): Observable<Bug> {
    return this.http.post<any>(`${this.baseUrl}/bug?testcaseId=${encodeURIComponent(testcaseId)}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  updateBug(bugId: number, req: BugPutRequest): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/bug/${bugId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  softDeleteBug(bugId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/bug/delete/${bugId}`).pipe(
      map(res => res?.data || res)
    );
  }

  patchBugStatus(bugId: number, status: string): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/bug/${bugId}`, { status });
  }

  patchBugAssignee(bugId: number, assignedTo: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/bug/${bugId}`, { assignedTo });
  }

  getBugAttachments(bugId: number): Observable<AttachmentItem[]> {
    return this.http.get<any>(`${this.baseUrl}/bug/${bugId}/attachments`).pipe(
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

  uploadBugAttachments(bugId: number, files: File[]): Observable<any> {
    const formData = new FormData();
    files.forEach(f => {
      formData.append('files', f);
      formData.append('file', f);
    });
    return this.http.post<any>(`${this.baseUrl}/bug/${bugId}/attachments`, formData);
  }

  downloadBugAttachments(bugId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/bug/${bugId}/attachments/download`, {
      responseType: 'blob'
    });
  }

  deleteAttachment(attachmentId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/attachments/${attachmentId}`);
  }

  exportBugs(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/feature/${featureId}/export`, {
      responseType: 'blob'
    });
  }

  getBugComments(bugId: number): Observable<BugComment[]> {
    return this.http.get<any>(`${this.baseUrl}/bug/${bugId}/comments`).pipe(
      map(res => res?.data || res || [])
    );
  }

  createBugComment(bugId: number, comment: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/bug/${bugId}/comments`, { comment });
  }
}
