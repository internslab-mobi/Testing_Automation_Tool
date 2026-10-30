import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TestCase, Feature, Bug, TestCasePutRequest, TestCasePatchRequest, TestCaseAuditLog } from '../../../../model/workspace/workspace.model';
import { TestCaseService } from '../../../../core/services/testcase.service';

@Component({
  selector: 'app-testcase-detail-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './testcase-detail-modal.component.html',
  styleUrl: './testcase-detail-modal.component.css'
})
export class TestCaseDetailModalComponent implements OnChanges {
  readonly cdr = inject(ChangeDetectorRef);
  private readonly testCaseService = inject(TestCaseService);

  @Input() isOpen = false;
  @Input() testCase: TestCase | null = null;
  @Input() activeFeature: Feature | null = null;
  @Input() linkedDefects: Bug[] = [];
  @Input() startInEdit = false;

  @Output() close = new EventEmitter<void>();
  @Output() updateTestCase = new EventEmitter<{ testcaseId: number; data: TestCasePutRequest }>();
  @Output() patchTestCase = new EventEmitter<{ testcaseId: number; data: TestCasePatchRequest }>();
  @Output() softDeleteTestCase = new EventEmitter<number>();
  @Output() executeTestCase = new EventEmitter<{ testcaseId: number; status: 'PASS' | 'FAIL' | 'DESCOPE' | 'IN_PROGRESS' | 'BLOCK' | string }>();
  @Output() logDefect = new EventEmitter<TestCase>();
  @Output() openDefectDetail = new EventEmitter<Bug>();

  isEditMode = false;
  editForm: TestCasePutRequest = {};
  dynamicFieldsList: Array<{ key: string; value: string }> = [];

  // Audit Log State
  auditLogs: TestCaseAuditLog[] = [];
  isLoadingAudit = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['isOpen'] && this.isOpen) {
      if (this.startInEdit) {
        this.startEdit();
      } else {
        this.isEditMode = false;
      }
      this.loadAuditHistory();
    } else if (changes['testCase'] && this.isOpen) {
      this.loadAuditHistory();
    }
  }

  loadAuditHistory(): void {
    if (!this.testCase?.testcaseId) {
      this.auditLogs = [];
      return;
    }
    this.isLoadingAudit = true;
    this.testCaseService.getTestCaseHistory(this.testCase.testcaseId).subscribe({
      next: (logs) => {
        this.auditLogs = logs || [];
        this.isLoadingAudit = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.auditLogs = [];
        this.isLoadingAudit = false;
        this.cdr.markForCheck();
      }
    });
  }

  get normalizedStatus(): string {
    const s = (this.testCase?.executionStatus || this.testCase?.testcaseStatus || 'UNEXECUTED').toUpperCase();
    if (s === 'BLOCKED') return 'BLOCK';
    if (s === 'PASSED') return 'PASS';
    if (s === 'FAILED') return 'FAIL';
    return s;
  }

  isStatus(target: string): boolean {
    return this.normalizedStatus === target;
  }

  startEdit(): void {
    if (!this.testCase) return;
    this.editForm = {
      title: this.testCase.title || '',
      testType: this.testCase.testType || 'UI',
      testPriority: this.testCase.testPriority || 'MEDIUM',
      automationFeasibility: this.testCase.automationFeasibility || this.testCase.execution?.automationFeasibility || 'YES',
      executionStatus: this.normalizedStatus !== 'UNEXECUTED' ? this.normalizedStatus : 'PASS',
      testExecution: this.testCase.testExecution || this.testCase.execution?.testExecution || '',
      testValidation: this.testCase.testValidation || this.testCase.execution?.testValidation || '',
      precondition: this.testCase.execution?.precondition || '',
      testData: this.testCase.testData || this.testCase.execution?.testData || '',
      executionSteps: this.testCase.execution?.executionSteps || '',
      uiValidations: this.testCase.execution?.uiValidations || '',
      dbValidations: this.testCase.execution?.dbValidations || '',
      comments: this.testCase.comments || this.testCase.execution?.comments || ''
    };

    // Populate dynamicFieldsList from this.testCase.dynamicFields
    this.dynamicFieldsList = [];
    if (this.testCase.dynamicFields && typeof this.testCase.dynamicFields === 'object') {
      Object.entries(this.testCase.dynamicFields).forEach(([k, v]) => {
        this.dynamicFieldsList.push({ key: k, value: String(v ?? '') });
      });
    }

    this.isEditMode = true;
    this.cdr.markForCheck();
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.cdr.markForCheck();
  }

  addDynamicField(): void {
    this.dynamicFieldsList.push({ key: '', value: '' });
  }

  removeDynamicField(index: number): void {
    this.dynamicFieldsList.splice(index, 1);
  }

  getDynamicFieldEntries(): Array<{ key: string; value: any }> {
    if (!this.testCase?.dynamicFields || typeof this.testCase.dynamicFields !== 'object') {
      return [];
    }
    return Object.entries(this.testCase.dynamicFields).map(([key, value]) => ({ key, value }));
  }

  // Save by TestCasePutRequest (PUT /testcase/{id})
  saveEdit(): void {
    if (!this.testCase) return;

    if (!this.editForm.title?.trim()) {
      alert('Test case title is required.');
      return;
    }

    // Build dynamic fields map
    const dynamicMap: Record<string, any> = {};
    for (const f of this.dynamicFieldsList) {
      const k = (f.key || '').trim();
      if (k) {
        dynamicMap[k] = f.value ?? '';
      }
    }
    this.editForm.dynamicFields = dynamicMap;

    this.updateTestCase.emit({
      testcaseId: this.testCase.testcaseId,
      data: { ...this.editForm }
    });

    this.isEditMode = false;
    this.cdr.markForCheck();
  }

  onSoftDelete(): void {
    if (!this.testCase) return;
    if (confirm(`Are you sure you want to delete test case "${this.testCase.testcaseFormatId || this.testCase.title}"?`)) {
      this.softDeleteTestCase.emit(this.testCase.testcaseId);
    }
  }

  onExecute(status: string): void {
    if (!this.testCase || !status || status === 'UNEXECUTED' || status === 'NO_RUN') return;
    let s = status.toUpperCase();
    if (s === 'BLOCKED') s = 'BLOCK';
    if (s === 'PASSED') s = 'PASS';
    if (s === 'FAILED') s = 'FAIL';
    this.testCase.executionStatus = s;
    this.executeTestCase.emit({
      testcaseId: this.testCase.testcaseId,
      status: s
    });
    this.cdr.markForCheck();
    setTimeout(() => this.loadAuditHistory(), 600);
  }

  onLogDefect(): void {
    if (!this.testCase) return;
    this.logDefect.emit(this.testCase);
  }

  onClose(): void {
    this.isEditMode = false;
    this.close.emit();
  }
}
