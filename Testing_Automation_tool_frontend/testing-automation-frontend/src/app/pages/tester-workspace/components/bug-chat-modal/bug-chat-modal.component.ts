import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DefectItem, ChatMessage } from '../../models/tester-workspace.model';

@Component({
  selector: 'app-bug-chat-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (isOpen && activeChatBug) {
      <div class="modal-overlay show" (click)="close()">
        <div class="chat-modal-box" (click)="$event.stopPropagation()">
          <div class="chat-modal-header">
            <div>
              <h3 style="font-size: 14px; font-weight: 700; color: var(--text-main);">
                <i class="fa-solid fa-comments" style="color: var(--emerald-600);"></i> Discussion: {{ activeChatBug.id }}
              </h3>
              <p style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">
                Linked Test Case: {{ activeChatBug.originTestCaseId }} • Assignee: {{ activeChatBug.assignee }} • Severity: {{ activeChatBug.severity }}
              </p>
            </div>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>

          <div class="chat-modal-body">
            @for (msg of chatMessages; track $index) {
              <div class="chat-bubble" [ngClass]="msg.sender">
                <strong>{{ msg.senderName }}:</strong>
                <div>{{ msg.text }}</div>
                <span style="font-size: 9px; opacity: 0.6; display: block; margin-top: 4px;">{{ msg.timestamp }}</span>
              </div>
            } @empty {
              <div style="text-align: center; padding: 24px; color: var(--text-muted); font-size: 12px;">
                <i class="fa-regular fa-comments" style="font-size: 24px; margin-bottom: 6px; display: block;"></i>
                No messages yet. Send a note to the developer below.
              </div>
            }
          </div>

          <div class="chat-modal-footer">
            <input
              type="text"
              class="form-control"
              [(ngModel)]="newChatMessage"
              placeholder="Type reply to developer..."
              style="flex: 1;"
              (keyup.enter)="sendMessage()"
            />
            <button class="btn btn-secondary" style="padding: 0 10px;" title="Attach Screenshot" (click)="attachScreenshot.emit()">
              <i class="fa-solid fa-paperclip"></i>
            </button>
            <button class="btn btn-primary" style="padding: 0 16px;" (click)="sendMessage()">
              <i class="fa-solid fa-paper-plane"></i> Send
            </button>
          </div>
        </div>
      </div>
    }
  `
})
export class BugChatModalComponent {
  @Input() isOpen = false;
  @Input() activeChatBug: DefectItem | null = null;
  @Input() chatMessages: ChatMessage[] = [];
  
  @Output() closeChat = new EventEmitter<void>();
  @Output() send = new EventEmitter<string>();
  @Output() attachScreenshot = new EventEmitter<void>();

  newChatMessage = '';

  close(): void {
    this.closeChat.emit();
  }

  sendMessage(): void {
    if (!this.newChatMessage.trim()) return;
    this.send.emit(this.newChatMessage.trim());
    this.newChatMessage = '';
  }
}
