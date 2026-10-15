import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-feature-edit-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-pen-to-square" style="color: var(--indigo-600);"></i> Edit Feature Details</h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>
          <div class="form-group">
            <label>Feature Name</label>
            <input type="text" class="form-control" [(ngModel)]="formData.name">
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label>Sprint</label>
              <input type="number" class="form-control" [(ngModel)]="formData.sprint">
            </div>
            <div class="form-group">
              <label>Feature Version</label>
              <input type="text" class="form-control" [(ngModel)]="formData.version">
            </div>
          </div>
          <div class="form-group">
            <label>Status</label>
            <select class="form-control" [(ngModel)]="formData.status">
              <option value="ACTIVE">ACTIVE</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="COMPLETED">COMPLETED</option>
              <option value="ON_HOLD">ON_HOLD</option>
            </select>
          </div>
          <button class="btn btn-primary" style="height: 40px;" (click)="save()">Save Feature Changes</button>
        </div>
      </div>
    }
  `
})
export class FeatureEditModalComponent {
  @Input() isOpen = false;
  @Input() formData = {
    name: '',
    sprint: 1,
    version: 'v1.0.0',
    status: 'ACTIVE'
  };

  @Output() closeModal = new EventEmitter<void>();
  @Output() saveFeature = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }

  save(): void {
    this.saveFeature.emit();
  }
}
