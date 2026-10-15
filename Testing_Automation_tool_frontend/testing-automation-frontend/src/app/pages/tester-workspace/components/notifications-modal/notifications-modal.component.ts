import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationItem } from '../../models/tester-workspace.model';

@Component({
  selector: 'app-notifications-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (isOpen) {
      <div class="modal-overlay show" (click)="close()">
        <div class="modal-card notif-modal-card" (click)="$event.stopPropagation()">
          <!-- Modal Header -->
          <div class="modal-header">
            <div>
              <h3><i class="fa-solid fa-bell" style="color: var(--indigo-600);"></i> Workspace Notifications</h3>
              <p style="font-size: 11px; color: var(--text-muted); margin-top: 2px;">
                Alerts on developer bug fixes, discussions, and sprint updates
              </p>
            </div>
            <button class="modal-close" (click)="close()">&times;</button>
          </div>

          <!-- Notification Filter Tabs -->
          <div class="notif-filter-tabs">
            <button
              class="notif-filter-tab"
              [class.active]="filterCategory === 'ALL'"
              (click)="setFilterCategory('ALL')"
            >
              All ({{ notifications.length }})
            </button>
            <button
              class="notif-filter-tab"
              [class.active]="filterCategory === 'UNREAD'"
              (click)="setFilterCategory('UNREAD')"
            >
              Unread
            </button>
            <button
              class="notif-filter-tab"
              [class.active]="filterCategory === 'FIXED'"
              (click)="setFilterCategory('FIXED')"
            >
              Bug Fixes
            </button>
            <button
              class="notif-filter-tab"
              [class.active]="filterCategory === 'COMMENT'"
              (click)="setFilterCategory('COMMENT')"
            >
              Comments
            </button>
          </div>

          <!-- Notification Items Body -->
          <div class="notif-list-body">
            @for (notif of filteredNotifications; track notif.id) {
              <div class="notif-card-item" [class.unread]="!notif.read">
                <div
                  class="notif-icon-bubble"
                  [ngClass]="{
                    'notif-icon-fixed': notif.type === 'FIXED',
                    'notif-icon-chat': notif.type === 'COMMENT',
                    'notif-icon-tc': notif.type === 'TC'
                  }"
                >
                  @if (notif.type === 'FIXED') {
                    <i class="fa-solid fa-circle-check"></i>
                  } @else if (notif.type === 'COMMENT') {
                    <i class="fa-solid fa-comment-dots"></i>
                  } @else {
                    <i class="fa-solid fa-vial"></i>
                  }
                </div>

                <div class="notif-card-content">
                  <div class="notif-card-title">
                    <span>
                      {{ notif.title }} —
                      @if (notif.highlightId) {
                        <strong style="color: var(--emerald-600); font-family: var(--font-mono);">{{ notif.highlightId }}</strong>
                      }
                    </span>
                    @if (!notif.read) {
                      <span style="font-size: 10px; color: var(--rose-600); font-weight: 700;">● NEW</span>
                    }
                  </div>
                  <div class="notif-card-msg">{{ notif.message }}</div>
                  <div class="notif-card-meta">
                    <span><i class="fa-solid fa-layer-group"></i> {{ notif.featureName }}</span>
                    <span>•</span>
                    <span><i class="fa-regular fa-clock"></i> {{ notif.timeAgo }}</span>
                  </div>
                  <div class="notif-card-actions">
                    @if (notif.type === 'FIXED' || notif.type === 'COMMENT') {
                      <button
                        class="btn btn-secondary"
                        style="height: 24px; padding: 0 8px; font-size: 11px; color: var(--indigo-600); font-weight: 700;"
                        (click)="viewBug.emit(notif)"
                      >
                        <i class="fa-solid fa-comments"></i> View Bug & Chat
                      </button>
                    } @else {
                      <button
                        class="btn btn-secondary"
                        style="height: 24px; padding: 0 8px; font-size: 11px; color: var(--emerald-600); font-weight: 700;"
                        (click)="openFeature.emit(notif.featureName)"
                      >
                        <i class="fa-solid fa-arrow-right"></i> Open Feature
                      </button>
                    }
                    <button
                      class="btn btn-secondary"
                      style="height: 24px; padding: 0 8px; font-size: 10px;"
                      (click)="toggleRead.emit(notif)"
                    >
                      <i class="fa-solid fa-check"></i> {{ notif.read ? 'Mark unread' : 'Mark read' }}
                    </button>
                    <button
                      class="btn btn-secondary"
                      style="height: 24px; padding: 0 6px; font-size: 10px;"
                      (click)="dismiss.emit(notif)"
                    >
                      <i class="fa-solid fa-trash-can"></i>
                    </button>
                  </div>
                </div>
              </div>
            } @empty {
              <div style="text-align: center; padding: 24px; color: var(--text-muted); font-size: 12px;">
                <i class="fa-regular fa-bell-slash" style="font-size: 20px; margin-bottom: 6px; display: block;"></i>
                No notifications in this category.
              </div>
            }
          </div>
        </div>
      </div>
    }
  `
})
export class NotificationsModalComponent {
  @Input() isOpen = false;
  @Input() notifications: NotificationItem[] = [];
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() viewBug = new EventEmitter<NotificationItem>();
  @Output() openFeature = new EventEmitter<string>();
  @Output() toggleRead = new EventEmitter<NotificationItem>();
  @Output() dismiss = new EventEmitter<NotificationItem>();

  filterCategory: 'ALL' | 'UNREAD' | 'FIXED' | 'COMMENT' = 'ALL';

  get filteredNotifications(): NotificationItem[] {
    return this.notifications.filter(n => {
      if (this.filterCategory === 'ALL') return true;
      if (this.filterCategory === 'UNREAD') return !n.read;
      return n.type === this.filterCategory;
    });
  }

  setFilterCategory(cat: 'ALL' | 'UNREAD' | 'FIXED' | 'COMMENT'): void {
    this.filterCategory = cat;
  }

  close(): void {
    this.closeModal.emit();
  }
}
