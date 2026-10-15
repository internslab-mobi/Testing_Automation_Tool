import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FileAttachment } from '../../models/tester-workspace.model';

@Component({
  selector: 'app-file-attachments-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h3>
              <i class="fa-solid fa-paperclip" [style.color]="iconColor"></i>
              {{ title }}
            </h3>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>

          <div style="display: flex; flex-direction: column; gap: 8px; max-height: 180px; overflow-y: auto;">
            @for (file of attachments; track file.name) {
              <div style="display: flex; justify-content: space-between; align-items: center; background: #f8fafc; border: 1px solid var(--border-default); border-radius: var(--radius-sm); padding: 8px 12px; font-size: 12px;">
                <div>
                  <i class="fa-solid" [ngClass]="file.icon" [style.color]="file.color"></i>
                  <strong style="margin-left: 6px;">{{ file.name }}</strong> ({{ file.size }})
                </div>
                <button class="btn btn-secondary" style="height: 24px; padding: 0 8px; font-size: 10px;" (click)="download.emit(file.name)">
                  <i class="fa-solid fa-download"></i>
                </button>
              </div>
            } @empty {
              <div style="text-align: center; padding: 14px; color: var(--text-muted); font-size: 12px;">
                {{ emptyText }}
              </div>
            }
          </div>

          <div
            style="border: 2px dashed #cbd5e1; background: #f8fafc; border-radius: var(--radius-md); padding: 18px; text-align: center; cursor: pointer;"
            (click)="upload.emit()"
          >
            <i class="fa-solid fa-cloud-arrow-up" [style.color]="iconColor" style="font-size: 20px; margin-bottom: 4px;"></i>
            <div style="font-size: 12px; font-weight: 600;">{{ uploadPrompt }}</div>
          </div>
        </div>
      </div>
    }
  `
})
export class FileAttachmentsModalComponent {
  @Input() isOpen = false;
  @Input() title = 'File Attachments';
  @Input() iconColor = 'var(--emerald-600)';
  @Input() attachments: FileAttachment[] = [];
  @Input() emptyText = 'No attachments uploaded.';
  @Input() uploadPrompt = 'Click to upload files';

  @Output() closeModal = new EventEmitter<void>();
  @Output() download = new EventEmitter<string>();
  @Output() upload = new EventEmitter<void>();

  close(): void {
    this.closeModal.emit();
  }
}
