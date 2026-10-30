import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl, SafeUrl } from '@angular/platform-browser';
import { AttachmentItem } from '../../../../model/workspace/workspace.model';
import { ProjectService } from '../../../../core/services/project.service';

@Component({
  selector: 'app-attachments-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './attachments-modal.component.html',
  styleUrl: './attachments-modal.component.css'
})
export class AttachmentsModalComponent implements OnDestroy {
  readonly cdr = inject(ChangeDetectorRef);
  private readonly projectService = inject(ProjectService);
  private readonly sanitizer = inject(DomSanitizer);

  @Input() isOpen = false;
  @Input() title = 'Attachments & Evidence';
  @Input() subTitle = '';
  @Input() attachments: AttachmentItem[] = [];
  @Input() isUploading = false;

  @Output() close = new EventEmitter<void>();
  @Output() uploadFiles = new EventEmitter<File[]>();
  @Output() downloadAll = new EventEmitter<void>();
  @Output() downloadSingle = new EventEmitter<AttachmentItem>();
  @Output() viewSingle = new EventEmitter<AttachmentItem>();
  @Output() deleteAttachment = new EventEmitter<number>();

  // Preview State
  isPreviewOpen = false;
  isLoadingPreview = false;
  previewError: string | null = null;
  previewFile: AttachmentItem | null = null;
  previewFileType: 'image' | 'pdf' | 'text' | 'audio' | 'video' | 'unsupported' = 'unsupported';
  previewTextContent = '';
  previewBlobUrl: SafeResourceUrl | SafeUrl | null = null;
  private rawBlobUrl: string | null = null;
  imageZoom = 1;
  copySuccess = false;

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

  onViewFile(file: AttachmentItem): void {
    if (!file) return;
    const attachmentId = file.attachmentId || file.id;
    this.previewFile = file;
    this.isPreviewOpen = true;
    this.previewError = null;
    this.previewTextContent = '';
    this.previewBlobUrl = null;
    this.revokeBlobUrl();
    this.imageZoom = 1;
    this.copySuccess = false;

    // Emit event in case parent also tracks it
    this.viewSingle.emit(file);

    const ext = this.getFileExtension(file.fileName);
    this.previewFileType = this.detectCategory(ext, file.fileType || file.contentType);

    if (!attachmentId) {
      this.previewError = 'Attachment ID is missing. Unable to preview file.';
      this.isLoadingPreview = false;
      this.cdr.markForCheck();
      return;
    }

    this.isLoadingPreview = true;
    this.cdr.markForCheck();

    this.projectService.viewSingleAttachment(attachmentId).subscribe({
      next: async (blob) => {
        try {
          if (blob.type) {
            const detectedFromMime = this.detectCategoryFromMime(blob.type);
            if (detectedFromMime !== 'unsupported') {
              this.previewFileType = detectedFromMime;
            }
          }

          if (this.previewFileType === 'text') {
            this.previewTextContent = await blob.text();
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          } else if (this.previewFileType === 'image') {
            this.rawBlobUrl = URL.createObjectURL(blob);
            this.previewBlobUrl = this.sanitizer.bypassSecurityTrustUrl(this.rawBlobUrl);
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          } else if (this.previewFileType === 'pdf' || this.previewFileType === 'audio' || this.previewFileType === 'video') {
            this.rawBlobUrl = URL.createObjectURL(blob);
            this.previewBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.rawBlobUrl);
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          } else {
            // Unsupported binary format
            this.previewFileType = 'unsupported';
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          }
        } catch (e: any) {
          this.previewError = 'Unable to parse file for display. You can download the file directly.';
          this.isLoadingPreview = false;
          this.cdr.markForCheck();
        }
      },
      error: () => {
        // Fallback: try downloadSingleAttachment
        this.projectService.downloadSingleAttachment(attachmentId).subscribe({
          next: async (blob) => {
            try {
              if (this.previewFileType === 'text') {
                this.previewTextContent = await blob.text();
              } else if (this.previewFileType === 'image') {
                this.rawBlobUrl = URL.createObjectURL(blob);
                this.previewBlobUrl = this.sanitizer.bypassSecurityTrustUrl(this.rawBlobUrl);
              } else if (this.previewFileType === 'pdf' || this.previewFileType === 'audio' || this.previewFileType === 'video') {
                this.rawBlobUrl = URL.createObjectURL(blob);
                this.previewBlobUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.rawBlobUrl);
              }
              this.isLoadingPreview = false;
              this.cdr.markForCheck();
            } catch {
              this.previewError = 'Failed to load file preview. Please download the file.';
              this.isLoadingPreview = false;
              this.cdr.markForCheck();
            }
          },
          error: () => {
            this.previewError = 'Failed to fetch attachment from server.';
            this.isLoadingPreview = false;
            this.cdr.markForCheck();
          }
        });
      }
    });
  }

  closePreview(): void {
    this.isPreviewOpen = false;
    this.isLoadingPreview = false;
    this.previewError = null;
    this.previewFile = null;
    this.previewTextContent = '';
    this.previewBlobUrl = null;
    this.revokeBlobUrl();
    this.cdr.markForCheck();
  }

  copyText(): void {
    if (!this.previewTextContent) return;
    navigator.clipboard.writeText(this.previewTextContent).then(() => {
      this.copySuccess = true;
      this.cdr.markForCheck();
      setTimeout(() => {
        this.copySuccess = false;
        this.cdr.markForCheck();
      }, 2000);
    }).catch(() => {
      this.copySuccess = false;
    });
  }

  zoomIn(): void {
    if (this.imageZoom < 3) {
      this.imageZoom = Math.min(3, +(this.imageZoom + 0.25).toFixed(2));
      this.cdr.markForCheck();
    }
  }

  zoomOut(): void {
    if (this.imageZoom > 0.4) {
      this.imageZoom = Math.max(0.4, +(this.imageZoom - 0.25).toFixed(2));
      this.cdr.markForCheck();
    }
  }

  resetZoom(): void {
    this.imageZoom = 1;
    this.cdr.markForCheck();
  }

  downloadFromPreview(): void {
    if (!this.previewFile) return;
    this.onDownloadSingle(this.previewFile);
  }

  getPreviewIconClass(): string {
    switch (this.previewFileType) {
      case 'image': return 'fa-file-image text-emerald';
      case 'pdf': return 'fa-file-pdf text-rose';
      case 'text': return 'fa-file-code text-indigo';
      case 'audio': return 'fa-file-audio text-amber';
      case 'video': return 'fa-file-video text-purple';
      default: return 'fa-file-lines text-slate';
    }
  }

  private detectCategory(ext: string, mime?: string): 'image' | 'pdf' | 'text' | 'audio' | 'video' | 'unsupported' {
    const imgExts = ['png', 'jpg', 'jpeg', 'gif', 'svg', 'webp', 'bmp', 'ico'];
    const textExts = ['txt', 'log', 'json', 'csv', 'xml', 'md', 'yaml', 'yml', 'html', 'css', 'js', 'ts', 'java', 'py', 'sql', 'sh', 'bat', 'properties', 'env', 'ini', 'conf'];
    const audioExts = ['mp3', 'wav', 'ogg'];
    const videoExts = ['mp4', 'webm'];

    if (imgExts.includes(ext)) return 'image';
    if (ext === 'pdf') return 'pdf';
    if (textExts.includes(ext)) return 'text';
    if (audioExts.includes(ext)) return 'audio';
    if (videoExts.includes(ext)) return 'video';

    if (mime) {
      return this.detectCategoryFromMime(mime);
    }
    return 'unsupported';
  }

  private detectCategoryFromMime(mime: string): 'image' | 'pdf' | 'text' | 'audio' | 'video' | 'unsupported' {
    const m = mime.toLowerCase();
    if (m.startsWith('image/')) return 'image';
    if (m.includes('pdf')) return 'pdf';
    if (m.startsWith('text/') || m.includes('json') || m.includes('xml') || m.includes('javascript') || m.includes('csv')) return 'text';
    if (m.startsWith('audio/')) return 'audio';
    if (m.startsWith('video/')) return 'video';
    return 'unsupported';
  }

  private getFileExtension(filename?: string): string {
    if (!filename) return '';
    const parts = filename.split('.');
    return parts.length > 1 ? parts.pop()!.toLowerCase().trim() : '';
  }

  private revokeBlobUrl(): void {
    if (this.rawBlobUrl) {
      try {
        URL.revokeObjectURL(this.rawBlobUrl);
      } catch { }
      this.rawBlobUrl = null;
    }
  }

  onDelete(attachmentId?: number): void {
    if (!attachmentId) return;
    if (confirm('Are you sure you want to delete this attachment file?')) {
      this.deleteAttachment.emit(attachmentId);
      this.cdr.markForCheck();
    }
  }

  onClose(): void {
    this.closePreview();
    this.close.emit();
  }

  ngOnDestroy(): void {
    this.revokeBlobUrl();
  }
}
