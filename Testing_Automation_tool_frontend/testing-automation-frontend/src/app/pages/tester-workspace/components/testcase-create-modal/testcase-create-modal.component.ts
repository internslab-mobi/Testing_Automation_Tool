import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-testcase-create-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-vial" style="color: var(--emerald-600);"></i> Add Test Case to Feature</h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>
          <div class="form-group">
            <label>Feature Context</label>
            <input
              type="text"
              class="form-control"
              [value]="featureName"
              readonly
              style="font-weight: 700; color: var(--indigo-600);"
            />
          </div>
          <div class="form-group">
            <label>Test Case Title</label>
            <input
              type="text"
              class="form-control"
              [(ngModel)]="formData.title"
              placeholder="e.g. Verify lockout after 5 incorrect password attempts"
            />
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label>Type</label>
              <select class="form-control" [(ngModel)]="formData.type">
                <option value="FUNCTIONAL">FUNCTIONAL</option>
                <option value="SECURITY">SECURITY</option>
                <option value="REGRESSION">REGRESSION</option>
              </select>
            </div>
            <div class="form-group">
              <label>Priority</label>
              <select class="form-control" [(ngModel)]="formData.priority">
                <option value="CRITICAL">P1 - CRITICAL</option>
                <option value="HIGH">P2 - HIGH</option>
                <option value="MEDIUM">P3 - MEDIUM</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>UI Validation</label>
            <input
              type="text"
              class="form-control"
              [(ngModel)]="formData.uiValidation"
              placeholder="Expected visual behavior or toast"
            />
          </div>
          <div class="form-group">
            <label>DB Validation</label>
            <input
              type="text"
              class="form-control"
              [(ngModel)]="formData.dbValidation"
              placeholder="Expected table row check"
            />
          </div>
          <button class="btn btn-primary" style="height: 40px;" (click)="save()">Save Test Case</button>
        </div>
      </div>
    }
  `
})
export class TestcaseCreateModalComponent {
  @Input() isOpen = false;
  @Input() featureName = '';
  @Input() formData = {
    title: '',
    type: 'FUNCTIONAL' as 'FUNCTIONAL' | 'SECURITY' | 'REGRESSION',
    priority: 'CRITICAL' as 'CRITICAL' | 'HIGH' | 'MEDIUM',
    uiValidation: '',
    dbValidation: ''
  };

  @Output() closeModal = new EventEmitter<void>();
  @Output() saveTestcase = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }

  save(): void {
    this.saveTestcase.emit();
  }
}
