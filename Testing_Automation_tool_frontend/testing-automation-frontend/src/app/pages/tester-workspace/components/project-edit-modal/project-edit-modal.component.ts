import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-project-edit-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../modal-shared.css',
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-pen-to-square" style="color: var(--indigo-600);"></i> Edit Project Details</h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>
          <div class="form-group">
            <label>Project Name</label>
            <input type="text" class="form-control" [(ngModel)]="formData.name">
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label>Region</label>
              <input type="text" class="form-control" [(ngModel)]="formData.region" style="font-weight: 600;">
            </div>
            <div class="form-group">
              <label>Project Status</label>
              <select class="form-control" [(ngModel)]="formData.status">
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          </div>
          <button class="btn btn-primary" style="height: 40px;" (click)="save()">Save Project Changes</button>
        </div>
      </div>
    }
  `
})
export class ProjectEditModalComponent {
  @Input() isOpen = false;
  @Input() formData = {
    name: '',
    region: 'US-EAST',
    status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE'
  };

  @Output() closeModal = new EventEmitter<void>();
  @Output() saveProject = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }

  save(): void {
    this.saveProject.emit();
  }
}
