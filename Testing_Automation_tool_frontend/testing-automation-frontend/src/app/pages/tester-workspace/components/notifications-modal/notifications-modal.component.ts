import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InAppNotification } from '../../../../model/workspace/workspace.model';

@Component({
  selector: 'app-notifications-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications-modal.component.html',
  styleUrl: './notifications-modal.component.css'
})
export class NotificationsModalComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() notifications: InAppNotification[] = [];
  @Input() unreadCount = 0;

  @Output() close = new EventEmitter<void>();
  @Output() toggleRead = new EventEmitter<InAppNotification>();
  @Output() dismiss = new EventEmitter<InAppNotification>();
  @Output() markAllRead = new EventEmitter<void>();

  activeFilter: 'ALL' | 'UNREAD' | 'FIXED' | 'COMMENT' = 'ALL';

  get filteredNotifications(): InAppNotification[] {
    return this.notifications.filter(n => {
      if (this.activeFilter === 'UNREAD') return n.notificationStatus === 'UNREAD';
      if (this.activeFilter === 'FIXED') {
        return n.type === 'FIXED' || (n.message || '').toLowerCase().includes('fixed') || (n.message || '').toLowerCase().includes('resolved');
      }
      if (this.activeFilter === 'COMMENT') {
        return n.type === 'COMMENT' || (n.message || '').toLowerCase().includes('comment') || (n.message || '').toLowerCase().includes('replied');
      }
      return true;
    });
  }

  setFilter(filter: 'ALL' | 'UNREAD' | 'FIXED' | 'COMMENT'): void {
    this.activeFilter = filter;
    this.cdr.markForCheck();
  }

  onToggle(n: InAppNotification): void {
    this.toggleRead.emit(n);
    this.cdr.markForCheck();
  }

  onDismiss(n: InAppNotification, event: Event): void {
    event.stopPropagation();
    this.dismiss.emit(n);
    this.cdr.markForCheck();
  }

  onMarkAllRead(): void {
    this.markAllRead.emit();
    this.cdr.markForCheck();
  }

  onClose(): void {
    this.close.emit();
  }

  trackByNotificationId(_index: number, n: InAppNotification): number {
    return n.notificationId;
  }
}
