import { Component, Input, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Project, Feature } from '../../../../model/workspace/workspace.model';

@Component({
  selector: 'app-workspace-sidebar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  readonly cdr = inject(ChangeDetectorRef);

  @Input() isOpen = false;
  @Input() availableRegions: string[] = [];
  @Input() selectedRegion = 'ALL';
  @Input() projects: Project[] = [];
  @Input() activeProject: Project | null = null;
  @Input() features: Feature[] = [];
  @Input() activeFeature: Feature | null = null;
  @Input() projectAttachmentCount = 0;
  @Input() featureAttachmentCount = 0;

  @Output() regionChange = new EventEmitter<string>();
  @Output() selectProject = new EventEmitter<Project>();
  @Output() createProject = new EventEmitter<void>();
  @Output() editProject = new EventEmitter<Project>();
  @Output() openProjectAttachments = new EventEmitter<Project>();
  @Output() selectFeature = new EventEmitter<Feature>();
  @Output() createFeature = new EventEmitter<void>();
  @Output() editFeature = new EventEmitter<Feature>();
  @Output() openFeatureAttachments = new EventEmitter<Feature>();
  @Output() closeSidebar = new EventEmitter<void>();
  @Output() openOverview = new EventEmitter<void>();

  projectSearch = '';
  featureSearch = '';

  // Pagination for Projects in Sidebar (Default size 5)
  projectPage = 1;
  projectPageSize = 5;

  // Pagination for Features in Sidebar (Default size 5)
  featurePage = 1;
  featurePageSize = 5;

  get filteredProjects(): Project[] {
    const s = this.projectSearch.toLowerCase().trim();
    return this.projects.filter(p => {
      const matchRegion = (this.selectedRegion === 'ALL') || !p.region || p.region === this.selectedRegion;
      const matchSearch = !s || p.projectName.toLowerCase().includes(s) || (p.region && p.region.toLowerCase().includes(s));
      return matchRegion && matchSearch;
    });
  }

  get paginatedProjects(): Project[] {
    const start = (this.projectPage - 1) * this.projectPageSize;
    return this.filteredProjects.slice(start, start + this.projectPageSize);
  }

  get totalProjectPages(): number {
    return Math.max(1, Math.ceil(this.filteredProjects.length / this.projectPageSize));
  }

  prevProjectPage(): void {
    if (this.projectPage > 1) {
      this.projectPage--;
      this.cdr.markForCheck();
    }
  }

  nextProjectPage(): void {
    if (this.projectPage < this.totalProjectPages) {
      this.projectPage++;
      this.cdr.markForCheck();
    }
  }

  get filteredFeatures(): Feature[] {
    const s = this.featureSearch.toLowerCase().trim();
    return this.features.filter(f => {
      return !s || f.featureName.toLowerCase().includes(s) || (f.featureVersion && f.featureVersion.toLowerCase().includes(s));
    });
  }

  get paginatedFeatures(): Feature[] {
    const start = (this.featurePage - 1) * this.featurePageSize;
    return this.filteredFeatures.slice(start, start + this.featurePageSize);
  }

  get totalFeaturePages(): number {
    return Math.max(1, Math.ceil(this.filteredFeatures.length / this.featurePageSize));
  }

  prevFeaturePage(): void {
    if (this.featurePage > 1) {
      this.featurePage--;
      this.cdr.markForCheck();
    }
  }

  nextFeaturePage(): void {
    if (this.featurePage < this.totalFeaturePages) {
      this.featurePage++;
      this.cdr.markForCheck();
    }
  }

  onRegionSelected(region: string): void {
    this.selectedRegion = region;
    this.regionChange.emit(region);
    this.cdr.markForCheck();
  }

  onSelectProject(p: Project): void {
    this.selectProject.emit(p);
    this.cdr.markForCheck();
  }

  onSelectFeature(f: Feature): void {
    this.selectFeature.emit(f);
    this.cdr.markForCheck();
  }

  trackByProjectId(_index: number, p: Project): number {
    return p.projectId;
  }

  trackByFeatureId(_index: number, f: Feature): number {
    return f.featureId;
  }
}
