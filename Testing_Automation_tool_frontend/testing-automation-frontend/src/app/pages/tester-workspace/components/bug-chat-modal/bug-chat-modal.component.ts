import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Bug, BugComment } from '../../../../model/workspace/workspace.model';

@Component({
  selector: 'app-bug-chat-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bug-chat-modal.component.html',
  styleUrl: './bug-chat-modal.component.css'
})
export class BugChatModalComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() bug: Bug | null = null;
  @Input() comments: BugComment[] = [];
  @Input() isLoading = false;

  @Output() close = new EventEmitter<void>();
  @Output() sendMessage = new EventEmitter<string>();

  newMessage = '';

  onSend(): void {
    if (!this.newMessage.trim()) return;
    this.sendMessage.emit(this.newMessage.trim());
    this.newMessage = '';
    this.cdr.markForCheck();
  }

  onClose(): void {
    this.close.emit();
  }
}
