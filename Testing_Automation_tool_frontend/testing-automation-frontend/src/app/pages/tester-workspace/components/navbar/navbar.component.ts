import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-workspace-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() userName = 'Tester';
  @Input() userRole = 'TESTER';
  @Input() userInitials = 'TS';
  @Input() unreadNotificationCount = 0;

  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() openNotifications = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
    this.cdr.markForCheck();
  }

  onOpenNotifications(): void {
    this.openNotifications.emit();
    this.cdr.markForCheck();
  }

  onLogout(): void {
    this.logout.emit();
  }
}
