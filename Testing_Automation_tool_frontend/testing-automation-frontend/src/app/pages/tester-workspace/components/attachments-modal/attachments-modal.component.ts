import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AttachmentItem } from '../../../../model/workspace/workspace.model';

@Component({
  selector: 'app-attachments-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attachments-modal.component.html',
  styleUrl: './attachments-modal.component.css'
})
export class AttachmentsModalComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() title = 'Attachments & Evidence';
  @Input() subTitle = '';
  @Input() attachments: AttachmentItem[] = [];
  @Input() isUploading = false;

  @Output() close = new EventEmitter<void>();
  @Output() uploadFiles = new EventEmitter<File[]>();
  @Output() downloadAll = new EventEmitter<void>();
  @Output() downloadSingle = new EventEmitter<AttachmentItem>();
  @Output() deleteAttachment = new EventEmitter<number>();

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || !input.files.length) return;
    const files = Array.from(input.files);
    this.uploadFiles.emit(files);
    input.value = '';
    this.cdr.markForCheck();
  }

  onDownloadSingle(file: AttachmentItem): void {
    if (!file) return;
    this.downloadSingle.emit(file);
  }

  onDelete(attachmentId?: number): void {
    if (!attachmentId) return;
    if (confirm('Are you sure you want to delete this attachment file?')) {
      this.deleteAttachment.emit(attachmentId);
      this.cdr.markForCheck();
    }
  }

  onClose(): void {
    this.close.emit();
  }
}
