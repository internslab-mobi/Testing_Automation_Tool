import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Project } from '../../../../model/workspace/workspace.model';

@Component({
  selector: 'app-project-detail-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './project-detail-modal.component.html',
  styleUrl: './project-detail-modal.component.css'
})
export class ProjectDetailModalComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() project: Project | null = null;
  @Input() attachmentCount = 0;

  @Output() close = new EventEmitter<void>();
  @Output() edit = new EventEmitter<Project>();
  @Output() openAttachments = new EventEmitter<Project>();

  onClose(): void {
    this.close.emit();
  }

  onEdit(): void {
    if (this.project) {
      this.edit.emit(this.project);
    }
  }

  onOpenAttachments(): void {
    if (this.project) {
      this.openAttachments.emit(this.project);
    }
  }
}
