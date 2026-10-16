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
  @Input() activeProject: any = null;
  @Input() activeFeature: any = null;
  @Input() featureAttachmentCount = 0;
  @Input() selectedRegion = 'ALL';
  @Input() isOverviewLoading = false;

  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() openNotifications = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  @Output() openOverview = new EventEmitter<void>();
  @Output() openOverviewTab = new EventEmitter<'projects' | 'features' | 'testcases' | 'bugs'>();
  @Output() openFeatureAttachments = new EventEmitter<any>();
  @Output() refreshOverview = new EventEmitter<void>();
  @Output() createProject = new EventEmitter<void>();

  onSelectOverviewTab(tab: 'projects' | 'features' | 'testcases' | 'bugs'): void {
    this.openOverviewTab.emit(tab);
    this.cdr.markForCheck();
  }

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
    this.cdr.markForCheck();
  }

  onOpenOverview(): void {
    this.openOverview.emit();
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
