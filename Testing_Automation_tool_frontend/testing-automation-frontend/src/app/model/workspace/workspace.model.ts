export interface Project {
  projectId: number;
  projectName: string;
  region: string;
  regionId?: number;
  module?: string;
  moduleId?: number;
  subModule?: string;
  subModuleId?: number;
  description?: string;
  comments?: string;
  status?: string;
  isActive?: boolean;
  isDeleted?: boolean;
  version?: number;
  createdBy?: string;
  createdByName?: string;
  createdDate?: string;
  createdAt?: string;
  updatedBy?: string;
  updatedByName?: string;
  updatedAt?: string;
}

export interface ProjectCreateRequest {
  projectName: string;
  region: number | string | null;
  module: number | string | null;
  subModule: number | string | null;
  moduleId?: number;
  subModuleId?: number;
  description?: string;
  comments?: string;
  status?: string;
}

export interface ModuleItem {
  moduleId: number;
  moduleName: string;
}

export interface SubModuleItem {
  subModuleId: number;
  subModuleName: string;
  moduleId?: number;
}

export interface RegionItem {
  countryId: number;
  countryName: string;
}

export interface Feature {
  featureId: number;
  projectId: number;
  featureName: string;
  sprint: number;
  featureVersion?: string;
  description?: string;
  comments?: string;
  status?: string;
  createdDate?: string;
}

export interface FeatureCreateRequest {
  projectId: number;
  featureName: string;
  sprint: number;
  featureVersion?: string;
  description?: string;
  comments?: string;
}

export interface TestCaseExecutionDetails {
  executionId?: number;
  executionNumber?: number;
  testExecution?: string;
  testValidation?: string;
  precondition?: string;
  testData?: string;
  executionSteps?: string;
  uiValidations?: string;
  dbValidations?: string;
  comments?: string;
  automationFeasibility?: string;
}

export interface TestCase {
  testcaseId: number;
  testcaseFormatId?: string;
  featureId: number;
  title: string;
  testType: string; // UI, API, DB, E2E
  testPriority: string; // CRITICAL, HIGH, MEDIUM, LOW
  testcaseStatus: string; // PASSED, FAILED, DESCOPE, NO_RUN
  executionStatus?: string; // PASS, FAIL, DESCOPE
  comments?: string;
  testExecution?: string;
  testValidation?: string;
  testData?: string;
  automationFeasibility?: string;
  bugsCount?: number;
  createdBy?: string;
  updatedBy?: string;
  createdAt?: string;
  updatedAt?: string;
  dynamicFields?: Record<string, any>;
  execution?: TestCaseExecutionDetails;
}

export interface TestCaseRequest {
  featureId: number;
  testcaseFormatId?: string;
  title: string;
  testType: string;
  testPriority: string;
  testcaseStatus?: string;
  comments?: string;
  createdBy?: number;
  dynamicFields?: Record<string, any>;
}

export interface TestingExecutionRequest {
  automationFeasibility?: string;
  testExecution?: string;
  testValidation?: string;
  precondition: string;
  testData?: string;
  executionSteps: string;
  uiValidations: string;
  dbValidations: string;
  comments?: string;
}

export interface TestCaseExecutionRequest {
  testCase: TestCaseRequest;
  testingExecutionRequest: TestingExecutionRequest;
}

// Backward-compatibility alias
export type TestCaseCreatePayload = TestCaseExecutionRequest;

export interface TestCasePutRequest {
  title?: string;
  testType?: string;
  testPriority?: string;
  testExecution?: string;
  testValidation?: string;
  precondition?: string;
  testData?: string;
  executionSteps?: string;
  uiValidations?: string;
  dbValidations?: string;
  comments?: string;
  automationFeasibility?: string;
  executionStatus?: string;
  dynamicFields?: Record<string, any>;
}

export interface BugWorkflow {
  workflowId?: number;
  assignedTo?: string;
  assignedToId?: number;
  reportedBy?: string;
  reportedById?: number;
  status?: string;
}

export enum BugSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum BugPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export enum BugCategory {
  PRE_PRODUCTION = 'PRE_PRODUCTION',
  POST_PRODUCTION = 'POST_PRODUCTION',
  ENHANCED = 'ENHANCED'
}

export enum BugStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  FIXED = 'FIXED',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED',
  REOPENED = 'REOPENED',
  NOT_A_BUG = 'NOT_A_BUG',
  DEFERRED = 'DEFERRED'
}

export interface Bug {
  bugId: number;
  bugFormatId?: string;
  testcaseId?: number;
  testcaseFormatId?: string;
  featureId?: number;
  featureName?: string;
  projectName?: string;
  sprint?: number;
  title: string;
  description?: string;
  severity: BugSeverity | string;
  priority?: BugPriority | string;
  status: BugStatus | string;
  category?: BugCategory | string;
  bugOccurrence?: number;
  workflow?: BugWorkflow;
  comments?: string;
  assignedTo?: string;
  assignedToId?: number;
  reportedBy?: string;
  reportedById?: number;
  createdAt?: string;
  dynamicFields?: Record<string, any>;
}

export interface BugRequest {
  title: string;
  description?: string;
  severity?: BugSeverity | string;
  priority?: BugPriority | string;
  category?: BugCategory | string;
  status?: BugStatus | string;
  assignedTo?: number;
  comments?: string;
  dynamicFields?: Record<string, any>;
}

// Backward-compatibility alias
export type BugCreateRequest = BugRequest;

export interface BugPutRequest {
  title?: string;
  description?: string;
  priority?: string;
  severity?: string;
  status?: string;
  category?: string;
  assignedTo?: number;
  comments?: string;
}

export interface AttachmentItem {
  attachmentId?: number;
  id?: number;
  fileName: string;
  fileType?: string;
  fileSize?: number;
  contentType?: string;
  uploaderName?: string;
  uploadedAt?: string;
  createdAt?: string;
}

export interface BugComment {
  commentId?: number;
  comment: string;
  creatorName?: string;
  createdByFullName?: string;
  createdByUsername?: string;
  createdAt?: string;
}

export interface InAppNotification {
  notificationId: number;
  message: string;
  type?: string; // FIXED, COMMENT, TC, ALERT
  notificationStatus: 'READ' | 'UNREAD';
  bugId?: number;
  bugFormatId?: string;
  createdAt?: string;
}

export interface ProjectPatchRequest {
  projectName?: string;
  description?: string;
  region?: number | string | null;
  module?: number | string | null;
  subModule?: number | string | null;
  comments?: string;
  status?: string;
}

export interface FeaturePatchRequest {
  featureName?: string;
  description?: string;
  status?: string;
  duration?: number;
  sprint?: number;
  version?: string;
  featureVersion?: string;
  startTime?: string;
  startTimer?: boolean;
  endTimer?: boolean;
  days?: number;
  hours?: number;
  minutes?: number;
  comments?: string;
  isActive?: boolean;
  testCaseId?: number[];
}

export interface TestCasePatchRequest {
  title?: string;
  testType?: string;
  testPriority?: string;
  testExecution?: string;
  testValidation?: string;
  precondition?: string;
  testData?: string;
  executionSteps?: string;
  uiValidations?: string;
  dbValidations?: string;
  isActive?: boolean;
  comments?: string;
  dynamicFields?: Record<string, any>;
  executionStatus?: string;
  automationFeasibility?: string;
}

export interface BugPatchRequest {
  title?: string;
  description?: string;
  priority?: string;
  severity?: string;
  status?: string;
  category?: string;
  assignedTo?: number;
  comments?: string;
  dynamicFields?: Record<string, any>;
}

