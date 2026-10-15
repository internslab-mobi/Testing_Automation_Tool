export interface FeatureItem {
  id: string;
  rawId?: number;
  name: string;
  sprint: string;
  sprintNum: number;
  bugs: number;
  tcCount: number;
  status: 'BUGS' | 'PASSED';
}

export interface ProjectItem {
  id: string;
  rawId?: number;
  name: string;
  version: string;
  status: 'ACTIVE' | 'INACTIVE';
  region: string;
  features: FeatureItem[];
}

export interface TestCaseItem {
  id: string;
  rawId?: number;
  title: string;
  featureName: string;
  featureId?: number;
  precondition: string;
  type: 'FUNCTIONAL' | 'SECURITY' | 'REGRESSION';
  priority: 'CRITICAL' | 'HIGH' | 'MEDIUM';
  uiValidation: string;
  dbValidation: string;
  status: 'PASSED' | 'FAILED';
}

export interface DefectItem {
  id: string;
  rawId?: number;
  title: string;
  featureName: string;
  originTestCaseId: string;
  originTestCaseRawId?: number;
  precondition: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM';
  assignee: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'FIXED' | 'CLOSED';
}

export interface NotificationItem {
  id: string;
  rawId?: number;
  type: 'FIXED' | 'COMMENT' | 'TC';
  title: string;
  highlightId?: string;
  isNew: boolean;
  read: boolean;
  message: string;
  featureName: string;
  timeAgo: string;
  originTestCaseId?: string;
}

export interface ChatMessage {
  sender: 'tester' | 'dev';
  senderName: string;
  text: string;
  timestamp: string;
}

export interface FileAttachment {
  name: string;
  size: string;
  icon: string;
  color: string;
}
