import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-workspace-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  readonly cdr = inject(ChangeDetectorRef);
  private readonly router = inject(Router);

  goToProfile(event?: Event): void {
    if (event) {
      event.preventDefault();
    }
    this.router.navigate(['/profile']);
  }

  @Input() userName = 'Tester';
  @Input() userRole = 'TESTER';
  @Input() userInitials = 'TS';
  @Input() unreadNotificationCount = 0;
  @Input() activeProject: any = null;
  @Input() activeFeature: any = null;
  @Input() featureAttachmentCount = 0;
  @Input() selectedRegion = 'India';
  @Input() isOverviewLoading = false;
  @Input() isManagerUser = false;

  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() openNotifications = new EventEmitter<void>();
  @Output() logout = new EventEmitter<void>();
  @Output() backToManager = new EventEmitter<void>();
  @Output() openOverview = new EventEmitter<void>();
  @Output() openOverviewTab = new EventEmitter<'projects' | 'features' | 'testcases' | 'bugs'>();
  @Output() openFeatureAttachments = new EventEmitter<any>();
  @Output() refreshOverview = new EventEmitter<void>();
  @Output() createProject = new EventEmitter<void>();
  @Output() viewProjectDetails = new EventEmitter<any>();
  @Output() editProject = new EventEmitter<any>();

  onBackToManager(): void {
    this.backToManager.emit();
  }

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
