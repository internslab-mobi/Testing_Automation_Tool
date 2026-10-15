import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-feature-create-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: '../modal-shared.css',
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3><i class="fa-solid fa-layer-group" style="color: var(--indigo-600);"></i> Add Feature to Project</h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>
          <div class="form-group">
            <label>Feature Name</label>
            <input
              type="text"
              class="form-control"
              [(ngModel)]="formData.name"
              placeholder="e.g. Card Freeze Toggle"
            />
          </div>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
            <div class="form-group">
              <label>Sprint Number</label>
              <input type="number" class="form-control" [(ngModel)]="formData.sprint">
            </div>
            <div class="form-group">
              <label>Version</label>
              <input type="text" class="form-control" [(ngModel)]="formData.version">
            </div>
          </div>
          <button class="btn btn-primary" style="height: 40px;" (click)="create()">Save Feature</button>
        </div>
      </div>
    }
  `
})
export class FeatureCreateModalComponent {
  @Input() isOpen = false;
  @Input() formData = {
    name: '',
    sprint: 1,
    version: 'v1.0.0'
  };

  @Output() closeModal = new EventEmitter<void>();
  @Output() createFeature = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }

  create(): void {
    this.createFeature.emit();
  }
}
