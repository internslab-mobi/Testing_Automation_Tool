import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TestCase, TestCaseExecutionRequest, TestCaseCreatePayload, TestCasePutRequest, TestCasePatchRequest, TestCaseAuditLog } from '../../model/workspace/workspace.model';

export interface TestCasePageResponse {
  content: TestCase[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface TestCaseFilterParams {
  keyword?: string;
  status?: string;
  type?: string;
  priority?: string;
  sort?: string;
}

@Injectable({
  providedIn: 'root'
})
export class TestCaseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  getTestCases(
    featureId?: number | null,
    page: number = 0,
    size: number = 10,
    filters?: TestCaseFilterParams
  ): Observable<TestCasePageResponse> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));

    if (featureId != null && featureId > 0) {
      params = params.set('featureId', String(featureId));
    }
    if (filters?.keyword && filters.keyword.trim()) {
      params = params.set('keyword', filters.keyword.trim());
    }
    if (filters?.status && filters.status !== 'ALL') {
      params = params.set('status', filters.status);
    }
    if (filters?.type && filters.type !== 'ALL') {
      params = params.set('type', filters.type);
    }
    if (filters?.priority && filters.priority !== 'ALL') {
      params = params.set('priority', filters.priority);
    }
    if (filters?.sort) {
      params = params.set('sort', filters.sort);
    }

    return this.http.get<any>(`${this.baseUrl}/testcase`, { params }).pipe(
      map(res => {
        const pageData = res?.data || res;
        const rawList = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        const content = rawList.map((item: any) => this.mapRawToTestCase(item, featureId || undefined));
        return {
          content,
          totalElements: typeof pageData?.totalElements === 'number' ? pageData.totalElements : content.length,
          totalPages: typeof pageData?.totalPages === 'number' ? pageData.totalPages : (content.length > 0 ? 1 : 0),
          size: typeof pageData?.size === 'number' ? pageData.size : size,
          number: typeof pageData?.number === 'number' ? pageData.number : page,
          first: pageData?.first ?? (page === 0),
          last: pageData?.last ?? true,
          empty: pageData?.empty ?? (content.length === 0)
        } as TestCasePageResponse;
      })
    );
  }

  private mapRawToTestCase(item: any, defaultFeatureId?: number): TestCase {
    const tc = item.testCase || item;
    const ex = item.execution || item.testingExecution || {};
    const val = item.validations || {};
    const auto = item.automation || {};
    const bugSum = item.bugSummary || {};
    const audit = item.audit || {};

    const tcId = tc.testcaseId || item.testcaseId || tc.id || item.id;
    const tcFormat = tc.testcaseFormatId || item.testcaseFormatId || (tcId ? `TC_${tcId}` : 'TC');

    return {
      testcaseId: tcId,
      testcaseFormatId: tcFormat,
      featureId: tc.featureId || item.featureId || defaultFeatureId,
      title: tc.title || item.title || '',
      testType: tc.testType || item.testType || 'UI',
      testPriority: tc.testPriority || item.testPriority || 'MEDIUM',
      testcaseStatus: tc.testcaseStatus || item.testcaseStatus || 'PASSED',
      executionStatus: (() => {
        let s = (ex.executionStatus || item.executionStatus || tc.testcaseStatus || 'UNEXECUTED').toUpperCase();
        if (s === 'BLOCKED') return 'BLOCK';
        if (s === 'PASSED') return 'PASS';
        if (s === 'FAILED') return 'FAIL';
        return s;
      })(),
      comments: tc.comments || ex.comments || item.comments || '',
      testData: ex.testData || item.testData || '',
      automationFeasibility: auto.automationFeasibility || item.automationFeasibility || '',
      bugsCount: bugSum.bugsCount ?? item.bugsCount ?? 0,
      createdBy: audit.createdBy || item.createdBy || '',
      updatedBy: audit.updatedBy || item.updatedBy || '',
      execution: {
        executionId: ex.executionId || item.executionId,
        executionNumber: ex.executionNumber || 1,
        precondition: ex.precondition || item.precondition || '',
        executionSteps: ex.executionSteps || item.executionSteps || '',
        uiValidations: val.uiValidations || ex.uiValidations || item.uiValidations || '',
        dbValidations: val.dbValidations || ex.dbValidations || item.dbValidations || '',
        comments: ex.comments || tc.comments || item.comments || ''
      }
    } as TestCase;
  }

  createTestCase(payload: TestCaseExecutionRequest): Observable<TestCase> {
    return this.http.post<any>(`${this.baseUrl}/testcase`, payload).pipe(
      map(res => {
        const data = res?.data || res;
        const tc = data?.testCaseResponse || data?.testCase || data;
        const ex = data?.testingExecutionResponse || data?.execution || {};
        return {
          testcaseId: tc.testcaseId || tc.id,
          testcaseFormatId: tc.testcaseFormatId || (tc.testcaseId ? `TC_${tc.testcaseId}` : 'TC'),
          featureId: tc.featureId || payload.testCase.featureId,
          title: tc.title || payload.testCase.title,
          testType: tc.testType || payload.testCase.testType,
          testPriority: tc.testPriority || payload.testCase.testPriority,
          testcaseStatus: tc.testcaseStatus || payload.testCase.testcaseStatus || 'NO_RUN',
          executionStatus: ex.executionStatus || 'UNEXECUTED',
          comments: tc.comments || '',
          execution: {
            executionId: ex.executionId,
            precondition: ex.precondition || payload.testingExecutionRequest?.precondition || '',
            executionSteps: ex.executionSteps || payload.testingExecutionRequest?.executionSteps || '',
            uiValidations: ex.uiValidations || payload.testingExecutionRequest?.uiValidations || '',
            dbValidations: ex.dbValidations || payload.testingExecutionRequest?.dbValidations || '',
            comments: ex.comments || ''
          }
        } as TestCase;
      })
    );
  }

  getTestCaseById(testcaseId: number): Observable<TestCase> {
    return this.http.get<any>(`${this.baseUrl}/testcase/${testcaseId}`).pipe(
      map(res => {
        const item = res?.data || res;
        return this.mapRawToTestCase(item);
      })
    );
  }

  updateTestCase(testcaseId: number, req: TestCasePutRequest): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/testcase/${testcaseId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  softDeleteTestCase(testcaseId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/testcase/soft/${testcaseId}`).pipe(
      map(res => res?.data || res)
    );
  }

  bulkDeleteTestCases(testCaseIds: number[]): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/testcase/bulk`, { body: testCaseIds }).pipe(
      map(res => res?.data || res)
    );
  }

  executeTestCase(testcaseId: number, executionStatus: 'PASS' | 'FAIL' | 'DESCOPE' | 'IN_PROGRESS' | 'BLOCK' | string): Observable<any> {
    let s = (executionStatus || '').toUpperCase();
    if (s === 'BLOCKED') s = 'BLOCK';
    if (s === 'PASSED') s = 'PASS';
    if (s === 'FAILED') s = 'FAIL';
    return this.http.patch<any>(`${this.baseUrl}/testcase/${testcaseId}`, { executionStatus: s });
  }

  patchTestCase(testcaseId: number, req: TestCasePatchRequest): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/testcase/${testcaseId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  uploadBulkExcel(featureId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('featureId', String(featureId));
    return this.http.post<any>(`${this.baseUrl}/testcase/upload`, formData);
  }

  downloadBlankTemplate(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/testcase/template`, {
      responseType: 'blob'
    });
  }

  downloadTemplate(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/testcase/${featureId}/template`, {
      responseType: 'blob'
    });
  }

  exportTestCases(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/feature/${featureId}/export`, {
      responseType: 'blob'
    });
  }

  getTestCaseHistory(testcaseId: number): Observable<TestCaseAuditLog[]> {
    return this.http.get<any>(`${this.baseUrl}/testcase/${testcaseId}/history`).pipe(
      map(res => res?.data || res || [])
    );
  }
}
