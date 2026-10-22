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
  @Input() selectedRegion = 'India';
  @Input() projects: Project[] = [];
  private _activeProject: Project | null = null;
  @Input()
  get activeProject(): Project | null {
    return this._activeProject;
  }
  set activeProject(val: Project | null) {
    const changed = this._activeProject?.projectId !== val?.projectId;
    this._activeProject = val;
    if (changed && val) {
      this.showProjectsPicker = false;
    }
  }
  @Input() features: Feature[] = [];
  @Input() activeFeature: Feature | null = null;
  @Input() projectAttachmentCount = 0;
  @Input() featureAttachmentCount = 0;
  @Input() isOverviewMode = false;

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

  isFilterExpanded = true;
  showProjectsPicker = false;

  toggleFilters(): void {
    this.isFilterExpanded = !this.isFilterExpanded;
    this.cdr.markForCheck();
  }

  openProjectsPicker(): void {
    this.showProjectsPicker = true;
    this.cdr.markForCheck();
  }

  closeProjectsPicker(): void {
    this.showProjectsPicker = false;
    this.cdr.markForCheck();
  }

  projectSearch = '';
  featureSearch = '';
  selectedModule = 'ALL';
  selectedSubModule = 'ALL';

  // Track comments toggled on touch / tap
  touchedCommentProjectId: number | null = null;
  touchedCommentFeatureId: number | null = null;

  toggleProjectComment(projectId: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.touchedCommentProjectId = this.touchedCommentProjectId === projectId ? null : projectId;
    this.cdr.markForCheck();
  }

  toggleFeatureComment(featureId: number, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.touchedCommentFeatureId = this.touchedCommentFeatureId === featureId ? null : featureId;
    this.cdr.markForCheck();
  }

  // Pagination for Projects in Sidebar (Default size 5)
  projectPage = 1;
  projectPageSize = 5;

  // Pagination for Features in Sidebar (Default size 5)
  featurePage = 1;
  featurePageSize = 5;

  get displayModules(): string[] {
    const set = new Set<string>();
    const isGlobalRegion = !this.selectedRegion || this.selectedRegion === 'ALL' || this.selectedRegion.toUpperCase() === 'GLOBAL';
    const regionProjects = this.projects.filter(p =>
      isGlobalRegion || (p.region || '').trim().toLowerCase() === this.selectedRegion.trim().toLowerCase()
    );
    for (const p of regionProjects) {
      const m = (p.module || '').trim();
      if (m) {
        set.add(m);
      }
    }
    return Array.from(set).sort();
  }

  get displaySubModules(): string[] {
    const set = new Set<string>();
    const isGlobalRegion = !this.selectedRegion || this.selectedRegion === 'ALL' || this.selectedRegion.toUpperCase() === 'GLOBAL';
    const isGlobalModule = !this.selectedModule || this.selectedModule === 'ALL';
    const filtered = this.projects.filter(p => {
      const matchReg = isGlobalRegion || (p.region || '').trim().toLowerCase() === this.selectedRegion.trim().toLowerCase();
      const matchMod = isGlobalModule || (p.module || '').trim().toLowerCase() === this.selectedModule.trim().toLowerCase();
      return matchReg && matchMod;
    });
    for (const p of filtered) {
      const sm = (p.subModule || '').trim();
      if (sm) {
        set.add(sm);
      }
    }
    return Array.from(set).sort();
  }

  onModuleSelected(moduleName: string): void {
    this.selectedModule = moduleName;
    this.selectedSubModule = 'ALL';
    this.projectPage = 1;
    this.cdr.markForCheck();
  }

  onSubModuleSelected(subModuleName: string): void {
    this.selectedSubModule = subModuleName;
    this.projectPage = 1;
    this.cdr.markForCheck();
  }

  get filteredProjects(): Project[] {
    const s = this.projectSearch.toLowerCase().trim();
    const isGlobalRegion = !this.selectedRegion || this.selectedRegion === 'ALL' || this.selectedRegion.toUpperCase() === 'GLOBAL';
    const isGlobalModule = !this.selectedModule || this.selectedModule === 'ALL';
    const isGlobalSubModule = !this.selectedSubModule || this.selectedSubModule === 'ALL';

    return this.projects.filter(p => {
      const matchRegion = isGlobalRegion || !p.region || p.region.trim().toLowerCase() === this.selectedRegion.trim().toLowerCase();
      const matchModule = isGlobalModule || !p.module || p.module.trim().toLowerCase() === this.selectedModule.trim().toLowerCase();
      const matchSubModule = isGlobalSubModule || !p.subModule || p.subModule.trim().toLowerCase() === this.selectedSubModule.trim().toLowerCase();
      const matchSearch = !s ||
        p.projectName.toLowerCase().includes(s) ||
        (p.region && p.region.toLowerCase().includes(s)) ||
        (p.module && p.module.toLowerCase().includes(s)) ||
        (p.subModule && p.subModule.toLowerCase().includes(s)) ||
        (p.comments && p.comments.toLowerCase().includes(s)) ||
        (p.description && p.description.toLowerCase().includes(s));
      return matchRegion && matchModule && matchSubModule && matchSearch;
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

  get displayRegions(): string[] {
    const set = new Set<string>();
    if (this.availableRegions && this.availableRegions.length > 0) {
      for (const r of this.availableRegions) {
        if (r && r.trim() && r.toUpperCase() !== 'ALL' && r.toUpperCase() !== 'GLOBAL') {
          set.add(r.trim());
        }
      }
    }
    // Also include any unique regions from projects
    if (this.projects && this.projects.length > 0) {
      for (const p of this.projects) {
        const r = (p.region || '').trim();
        if (r && r.toUpperCase() !== 'DEFAULT' && r.toUpperCase() !== 'ALL' && r.toUpperCase() !== 'GLOBAL') {
          set.add(r);
        }
      }
    }
    // Sane fallback defaults so regions are always visible
    if (set.size === 0) {
      ['India', 'Singapore', 'Malaysia', 'Indonesia', 'Vietnam'].forEach(r => set.add(r));
    }
    return Array.from(set);
  }

  onRegionSelected(region: string): void {
    this.selectedRegion = region;
    this.selectedModule = 'ALL';
    this.selectedSubModule = 'ALL';
    this.projectPage = 1;
    this.regionChange.emit(region);
    this.cdr.markForCheck();
  }

  onSelectProject(p: Project): void {
    this.showProjectsPicker = false;
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
