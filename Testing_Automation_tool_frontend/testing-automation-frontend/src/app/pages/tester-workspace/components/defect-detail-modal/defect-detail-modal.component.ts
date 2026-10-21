import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Bug, BugPutRequest, BugPatchRequest } from '../../../../model/workspace/workspace.model';
import { DeveloperUser } from '../../../../core/services/bug.service';

@Component({
  selector: 'app-defect-detail-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './defect-detail-modal.component.html',
  styleUrl: './defect-detail-modal.component.css'
})
export class DefectDetailModalComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() bug: Bug | null = null;
  @Input() developers: DeveloperUser[] = [];

  @Output() close = new EventEmitter<void>();
  @Output() updateBug = new EventEmitter<{ bugId: number; data: BugPutRequest }>();
  @Output() patchBug = new EventEmitter<{ bugId: number; data: BugPatchRequest }>();
  @Output() softDeleteBug = new EventEmitter<number>();
  @Output() changeStatus = new EventEmitter<{ bugId: number; status: string }>();
  @Output() changeAssignee = new EventEmitter<{ bugId: number; assignedTo: number }>();
  @Output() openChat = new EventEmitter<Bug>();
  @Output() openAttachments = new EventEmitter<Bug>();

  isEditMode = false;
  isAssigneeUnlocked = false;
  isStatusUnlocked = false;

  selectedDeveloperId: number | null = null;
  selectedNewStatus = '';
  editForm: BugPutRequest = {};

  get developerOptions(): DeveloperUser[] {
    return this.developers || [];
  }

  startEdit(): void {
    if (!this.bug) return;
    this.editForm = {
      title: this.bug.title,
      description: this.bug.description || '',
      severity: this.bug.severity || 'HIGH',
      priority: this.bug.priority || 'HIGH',
      category: this.bug.category || 'FUNCTIONAL',
      status: this.bug.status || 'OPEN',
      comments: this.bug.comments || ''
    };
    this.isEditMode = true;
    this.cdr.markForCheck();
  }

  cancelEdit(): void {
    this.isEditMode = false;
    this.cdr.markForCheck();
  }

  savePatchEdit(): void {
    this.saveEdit(false);
  }

  savePutEdit(): void {
    this.saveEdit(true);
  }

  saveEdit(forcePut: boolean = false): void {
    if (!this.bug) return;

    if (forcePut) {
      if (!this.editForm.title?.trim() || !this.editForm.description?.trim()) {
        alert('Title and Description are required for a full defect update.');
        return;
      }
      this.updateBug.emit({
        bugId: this.bug.bugId,
        data: this.editForm
      });
      this.isEditMode = false;
      this.cdr.markForCheck();
      return;
    }

    // Single / Partial field matching with backend BugPatchRequest
    const patchData: BugPatchRequest = {};

    const newTitle = (this.editForm.title || '').trim();
    const oldTitle = (this.bug.title || '').trim();
    if (newTitle && newTitle !== oldTitle) {
      patchData.title = newTitle;
    }

    const newDesc = (this.editForm.description || '').trim();
    const oldDesc = (this.bug.description || '').trim();
    if (newDesc !== oldDesc) {
      patchData.description = newDesc;
    }

    if (this.editForm.severity && this.editForm.severity !== this.bug.severity) {
      patchData.severity = this.editForm.severity;
    }

    if (this.editForm.priority && this.editForm.priority !== this.bug.priority) {
      patchData.priority = this.editForm.priority;
    }

    if (this.editForm.category && this.editForm.category !== this.bug.category) {
      patchData.category = this.editForm.category;
    }

    if (this.editForm.status && this.editForm.status !== this.bug.status) {
      patchData.status = this.editForm.status;
    }

    const newComments = (this.editForm.comments || '').trim();
    const oldComments = (this.bug.comments || '').trim();
    if (newComments !== oldComments) {
      patchData.comments = this.editForm.comments || '';
    }

    const changedCount = Object.keys(patchData).length;
    if (changedCount === 0) {
      this.isEditMode = false;
      this.cdr.markForCheck();
      return;
    }

    // Emit selective PATCH update
    this.patchBug.emit({
      bugId: this.bug.bugId,
      data: patchData
    });
    this.isEditMode = false;
    this.cdr.markForCheck();
  }

  onSoftDelete(): void {
    if (!this.bug) return;
    if (confirm(`Are you sure you want to soft delete defect "${this.bug.bugFormatId || this.bug.title}"?`)) {
      this.softDeleteBug.emit(this.bug.bugId);
    }
  }

  unlockAssignee(): void {
    this.isAssigneeUnlocked = true;
    this.selectedDeveloperId = this.bug?.assignedToId || null;
    this.cdr.markForCheck();
  }

  cancelAssigneeChange(): void {
    this.isAssigneeUnlocked = false;
    this.cdr.markForCheck();
  }

  confirmAssigneeChange(): void {
    if (!this.bug || !this.selectedDeveloperId) return;
    this.changeAssignee.emit({
      bugId: this.bug.bugId,
      assignedTo: this.selectedDeveloperId
    });
    this.isAssigneeUnlocked = false;
    this.cdr.markForCheck();
  }

  unlockStatus(): void {
    this.isStatusUnlocked = true;
    this.selectedNewStatus = this.bug?.status || 'OPEN';
    this.cdr.markForCheck();
  }

  cancelStatusChange(): void {
    this.isStatusUnlocked = false;
    this.cdr.markForCheck();
  }

  confirmStatusChange(): void {
    if (!this.bug || !this.selectedNewStatus) return;
    this.changeStatus.emit({
      bugId: this.bug.bugId,
      status: this.selectedNewStatus
    });
    this.isStatusUnlocked = false;
    this.cdr.markForCheck();
  }

  onClose(): void {
    this.isEditMode = false;
    this.isAssigneeUnlocked = false;
    this.isStatusUnlocked = false;
    this.close.emit();
  }
}
