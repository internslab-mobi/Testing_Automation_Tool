import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Bug, BugRequest, BugCreateRequest, BugPutRequest, BugPatchRequest, AttachmentItem, BugComment, BugAuditHistory } from '../../model/workspace/workspace.model';

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

  getAllBugs(assignedTo?: number): Observable<Bug[]> {
    const url = assignedTo != null
      ? `${this.baseUrl}/bug?assignedTo=${assignedTo}&page=0&size=2000`
      : `${this.baseUrl}/bug?page=0&size=2000`;
    return this.http.get<any>(url).pipe(
      map(res => {
        const pageData = res?.data || res;
        const rawList = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        return rawList.map((item: any) => this.mapBugEntity(item));
      })
    );
  }

  private mapBugEntity(item: any): Bug {
    const b = item.bug || item;
    const tc = item.testCase || b.testCase || {};
    const feat = item.feature || b.feature || {};
    const wf = item.workflow || b.workflow || {};
    const assignedUser = b.assignedTo || {};
    const reporterUser = b.reportedBy || b.executedBy || {};
    const audit = item.audit || b.audit || {};

    const tcId = tc.testcaseId || b.testcaseId;
    const tcFormat = tc.testcaseFormatId || b.testcaseFormatId || (tcId ? `TC_${tcId}` : undefined);
    const featId = feat.featureId || b.featureId;
    const featName = feat.featureName || b.featureName || '';
    const projName = feat.projectName || b.projectName || '';
    const sprintNum = feat.sprint || b.sprint || 1;

    const assignedId = wf.assignedToId || (typeof assignedUser === 'object' ? assignedUser.userId : undefined) || b.assignedToId;
    const reportedId = wf.reportedById || (typeof reporterUser === 'object' ? reporterUser.userId : undefined) || b.reportedById;
    const assignedName = typeof assignedUser === 'string' ? assignedUser : (assignedUser.fullName || assignedUser.username || wf.assignedTo || '');
    const reportedName = typeof reporterUser === 'string' ? reporterUser : (reporterUser.fullName || reporterUser.username || wf.reportedBy || 'Tester');
    const updatedName = audit.updatedBy || (typeof b.updatedBy === 'string' ? b.updatedBy : (b.updatedBy?.fullName || b.updatedBy?.username)) || wf.updatedBy || reportedName || 'Tester';
    const updatedAtVal = audit.updatedAt || b.updatedAt || b.createdAt;

    return {
      bugId: b.bugId,
      bugFormatId: b.bugFormatId || `BUG_${b.bugId}`,
      testcaseId: tcId,
      testcaseFormatId: tcFormat,
      featureId: featId,
      featureName: featName,
      projectName: projName,
      sprint: sprintNum,
      title: b.title,
      description: b.description || '',
      severity: b.severity || 'HIGH',
      priority: b.priority || 'HIGH',
      status: b.status || 'OPEN',
      category: b.category || 'FUNCTIONAL',
      bugOccurrence: b.bugOccurrence || 1,
      comments: b.comments || '',
      assignedTo: assignedName,
      assignedToId: assignedId ? Number(assignedId) : undefined,
      reportedBy: reportedName,
      reportedById: reportedId ? Number(reportedId) : undefined,
      createdAt: b.createdAt || item.audit?.createdAt,
      updatedBy: updatedName,
      updatedAt: updatedAtVal,
      audit: {
        updatedBy: updatedName,
        createdAt: b.createdAt || item.audit?.createdAt,
        updatedAt: updatedAtVal
      },
      workflow: {
        workflowId: wf.workflowId,
        assignedTo: assignedName || 'Unassigned',
        assignedToId: assignedId ? Number(assignedId) : undefined,
        reportedBy: reportedName || 'QA',
        reportedById: reportedId ? Number(reportedId) : undefined,
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

  viewSingleAttachment(attachmentId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/attachments/${attachmentId}/view`, {
      responseType: 'blob'
    });
  }

  createBug(testcaseId: number, req: BugRequest): Observable<Bug> {
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

  bulkDeleteBugs(bugIds: number[]): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/bug/bulk`, { body: bugIds }).pipe(
      map(res => res?.data || res)
    );
  }

  patchBugStatus(bugId: number, status: string): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/bug/${bugId}`, { status });
  }

  patchBugAssignee(bugId: number, assignedTo: number): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/bug/${bugId}`, { assignedTo });
  }

  patchBug(bugId: number, req: BugPatchRequest): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/bug/${bugId}`, req).pipe(
      map(res => res?.data || res)
    );
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

  exportBugs(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/feature/${featureId}/export`, {
      responseType: 'blob'
    });
  }

  getBugById(bugId: number): Observable<Bug> {
    return this.http.get<any>(`${this.baseUrl}/bug/${bugId}`).pipe(
      map(res => {
        const item = res?.data || res;
        return this.mapBugEntity(item);
      })
    );
  }


  getBugComments(bugId: number): Observable<BugComment[]> {
    return this.http.get<any>(`${this.baseUrl}/bugs/${bugId}/comments`).pipe(
      map(res => {
        const raw = res?.data || res || [];
        return Array.isArray(raw) ? raw : [];
      }),
      catchError(() => {
        return this.http.get<any>(`${this.baseUrl}/bug/${bugId}/comments`).pipe(
          map(res => {
            const raw = res?.data || res || [];
            return Array.isArray(raw) ? raw : [];
          }),
          catchError(() => of([]))
        );
      })
    );
  }

  createBugComment(bugId: number, comment: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/bugs/${bugId}/comments`, { comment }).pipe(
      map(res => res?.data || res),
      catchError(() => {
        return this.http.post<any>(`${this.baseUrl}/bug/${bugId}/comments`, { comment }).pipe(
          map(res => res?.data || res)
        );
      })
    );
  }

  getBugHistory(bugId: number): Observable<BugAuditHistory[]> {
    return this.http.get<any>(`${this.baseUrl}/bug/${bugId}/history`).pipe(
      map(res => res?.data || res || []),
      catchError(() => of([]))
    );
  }
}
