import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TestCase, TestCaseCreatePayload, TestCasePutRequest } from '../../model/workspace/workspace.model';

@Injectable({
  providedIn: 'root'
})
export class TestCaseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiGatewayUrl;

  getTestCases(featureId: number): Observable<TestCase[]> {
    return this.http.get<any>(`${this.baseUrl}/testcase?featureId=${featureId}&page=0&size=100`).pipe(
      map(res => {
        const pageData = res?.data || res;
        const rawList = pageData?.content || (Array.isArray(pageData) ? pageData : []);
        return rawList.map((item: any) => {
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
            featureId: tc.featureId || item.featureId || featureId,
            title: tc.title || item.title || '',
            testType: tc.testType || item.testType || 'UI',
            testPriority: tc.testPriority || item.testPriority || 'MEDIUM',
            testcaseStatus: tc.testcaseStatus || item.testcaseStatus || 'PASSED',
            executionStatus: ex.executionStatus || item.executionStatus || tc.testcaseStatus || 'UNEXECUTED',
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
        });
      })
    );
  }

  createTestCase(payload: TestCaseCreatePayload): Observable<TestCase> {
    return this.http.post<any>(`${this.baseUrl}/testcases`, payload).pipe(
      map(res => res?.data || res)
    );
  }

  updateTestCase(testcaseId: number, req: TestCasePutRequest): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/testcases/${testcaseId}`, req).pipe(
      map(res => res?.data || res)
    );
  }

  softDeleteTestCase(testcaseId: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/testcases/soft/${testcaseId}`).pipe(
      map(res => res?.data || res)
    );
  }

  executeTestCase(testcaseId: number, executionStatus: 'PASS' | 'FAIL' | 'DESCOPE'): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/testcases/${testcaseId}`, { executionStatus });
  }

  uploadBulkExcel(featureId: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('featureId', String(featureId));
    return this.http.post<any>(`${this.baseUrl}/testcases/upload`, formData);
  }

  downloadTemplate(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/testcases/${featureId}/template`, {
      responseType: 'blob'
    });
  }

  exportTestCases(featureId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/feature/${featureId}/export`, {
      responseType: 'blob'
    });
  }
}
