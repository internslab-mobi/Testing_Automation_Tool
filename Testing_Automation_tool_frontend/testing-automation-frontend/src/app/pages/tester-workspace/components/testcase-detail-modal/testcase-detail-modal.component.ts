import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TestCase, Feature, Bug, TestCasePutRequest } from '../../../../model/workspace/workspace.model';

@Component({
  selector: 'app-testcase-detail-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './testcase-detail-modal.component.html',
  styleUrl: './testcase-detail-modal.component.css'
})
export class TestCaseDetailModalComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() testCase: TestCase | null = null;
  @Input() activeFeature: Feature | null = null;
  @Input() linkedDefects: Bug[] = [];

  @Output() close = new EventEmitter<void>();
  @Output() updateTestCase = new EventEmitter<{ testcaseId: number; data: TestCasePutRequest }>();
  @Output() softDeleteTestCase = new EventEmitter<number>();
  @Output() executeTestCase = new EventEmitter<{ testcaseId: number; status: 'PASS' | 'FAIL' | 'DESCOPE' }>();
  @Output() logDefect = new EventEmitter<TestCase>();
  @Output() openDefectDetail = new EventEmitter<Bug>();

  isEditMode = false;
  editForm: TestCasePutRequest = {};

  startEdit(): void {
    if (!this.testCase) return;
    this.editForm = {
      title: this.testCase.title,
      testType: this.testCase.testType || 'UI',
      testPriority: this.testCase.testPriority || 'MEDIUM',
      precondition: this.testCase.execution?.precondition || '',
      executionSteps: this.testCase.execution?.executionSteps || '',
      uiValidations: this.testCase.execution?.uiValidations || '',
      dbValidations: this.testCase.execution?.dbValidations || '',
      comments: this.testCase.comments || this.testCase.execution?.comments || ''
    };
    this.isEditMode = true;
    this.cdr.markForCheck();
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.cdr.markForCheck();
  }

  saveEdit(): void {
    if (!this.testCase || !this.editForm.title?.trim()) return;
    this.updateTestCase.emit({
      testcaseId: this.testCase.testcaseId,
      data: this.editForm
    });
    this.isEditMode = false;
    this.cdr.markForCheck();
  }

  onSoftDelete(): void {
    if (!this.testCase) return;
    if (confirm(`Are you sure you want to soft delete test case "${this.testCase.testcaseFormatId || this.testCase.title}"?`)) {
      this.softDeleteTestCase.emit(this.testCase.testcaseId);
    }
  }

  onExecute(status: 'PASS' | 'FAIL' | 'DESCOPE'): void {
    if (!this.testCase) return;
    this.executeTestCase.emit({
      testcaseId: this.testCase.testcaseId,
      status
    });
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
