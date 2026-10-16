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

  projectSearch = '';
  featureSearch = '';

  get filteredProjects(): Project[] {
    const s = this.projectSearch.toLowerCase().trim();
    return this.projects.filter(p => {
      const matchRegion = (this.selectedRegion === 'ALL') || !p.region || p.region === this.selectedRegion;
      const matchSearch = !s || p.projectName.toLowerCase().includes(s) || (p.region && p.region.toLowerCase().includes(s));
      return matchRegion && matchSearch;
    });
  }

  get filteredFeatures(): Feature[] {
    const s = this.featureSearch.toLowerCase().trim();
    return this.features.filter(f => {
      return !s || f.featureName.toLowerCase().includes(s) || (f.featureVersion && f.featureVersion.toLowerCase().includes(s));
    });
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
