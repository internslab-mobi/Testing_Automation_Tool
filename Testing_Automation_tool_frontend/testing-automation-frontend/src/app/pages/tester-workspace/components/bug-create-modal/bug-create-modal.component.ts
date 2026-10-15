import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-bug-create-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3 style="color: var(--rose-600);"><i class="fa-solid fa-bug"></i> Log Defect (Auto-Mapped from TC)</h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label>Mapped Feature</label>
              <input type="text" class="form-control" [value]="formData.featureName" readonly style="font-weight: 700; color: var(--indigo-600);">
            </div>
            <div class="form-group">
              <label>Origin Test Case</label>
              <input type="text" class="form-control" [value]="formData.originTestCaseId" readonly style="font-weight: 700; color: var(--indigo-600);">
            </div>
          </div>
          <div class="form-group">
            <label>Format ID (Auto-Generated)</label>
            <input
              type="text"
              class="form-control"
              [value]="formData.id"
              readonly
              style="color: var(--rose-600); font-weight: 800; font-family: var(--font-mono);"
            />
          </div>
          <div class="form-group">
            <label>Defect Title</label>
            <input type="text" class="form-control" [(ngModel)]="formData.title">
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label>Severity</label>
              <select class="form-control" [(ngModel)]="formData.severity">
                <option value="CRITICAL">CRITICAL</option>
                <option value="HIGH">HIGH</option>
                <option value="MEDIUM">MEDIUM</option>
              </select>
            </div>
            <div class="form-group">
              <label>Assign Developer</label>
              <input
                type="text"
                class="form-control"
                [(ngModel)]="formData.assignee"
                placeholder="e.g. Lead Developer"
              />
            </div>
          </div>
          <button
            class="btn btn-primary"
            style="height: 40px; background: var(--rose-600);"
            (click)="save()"
          >
            <i class="fa-solid fa-paper-plane"></i> Log Defect & Notify Developer
          </button>
        </div>
      </div>
    }
  `
})
export class BugCreateModalComponent {
  @Input() isOpen = false;
  @Input() formData = {
    featureName: '',
    originTestCaseId: '',
    originTestCaseRawId: 1,
    id: '',
    title: '',
    severity: 'CRITICAL' as 'CRITICAL' | 'HIGH' | 'MEDIUM',
    assignee: 'Lead Developer'
  };

  @Output() closeModal = new EventEmitter<void>();
  @Output() saveDefect = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }

  save(): void {
    this.saveDefect.emit();
  }
}
