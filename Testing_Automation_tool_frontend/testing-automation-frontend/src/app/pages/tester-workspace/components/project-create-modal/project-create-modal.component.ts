import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-project-create-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../modal-shared.css',
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-folder-plus" style="color: var(--emerald-600);"></i> Create Project in Region</h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>
          <div class="form-group">
            <label>Project Region</label>
            <input
              type="text"
              class="form-control"
              [(ngModel)]="formData.region"
              placeholder="e.g. India, US-East, Singapore, Europe"
              style="font-weight: 600;"
            />
          </div>
          <div class="form-group">
            <label>Project Name</label>
            <input
              type="text"
              class="form-control"
              [(ngModel)]="formData.name"
              placeholder="e.g. Wealth Management Platform"
            />
          </div>
          <button class="btn btn-primary" style="height: 40px;" (click)="create()">Save Project</button>
        </div>
      </div>
    }
  `
})
export class ProjectCreateModalComponent {
  @Input() isOpen = false;
  @Input() formData = {
    region: 'US-EAST',
    name: ''
  };

  @Output() closeModal = new EventEmitter<void>();
  @Output() createProject = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }

  create(): void {
    this.createProject.emit();
  }
}
