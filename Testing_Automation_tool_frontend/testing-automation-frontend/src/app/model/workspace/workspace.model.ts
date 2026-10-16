export interface Project {
  projectId: number;
  projectName: string;
  region: string;
  description?: string;
  comments?: string;
  status?: string;
  createdDate?: string;
}

export interface ProjectCreateRequest {
  projectName: string;
  region: string;
  description?: string;
  comments?: string;
  status?: string;
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
  precondition?: string;
  executionSteps?: string;
  uiValidations?: string;
  dbValidations?: string;
  comments?: string;
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
  testData?: string;
  automationFeasibility?: string;
  bugsCount?: number;
  createdBy?: string;
  updatedBy?: string;
  execution?: TestCaseExecutionDetails;
}

export interface TestCaseCreatePayload {
  testCase: {
    featureId: number;
    title: string;
    testType: string;
    testPriority: string;
    testcaseStatus: string;
    comments?: string;
  };
  testingExecutionRequest: {
    precondition: string;
    executionSteps: string;
    uiValidations: string;
    dbValidations: string;
  };
}

export interface TestCasePutRequest {
  title?: string;
  testType?: string;
  testPriority?: string;
  precondition?: string;
  executionSteps?: string;
  uiValidations?: string;
  dbValidations?: string;
  comments?: string;
}

export interface BugWorkflow {
  workflowId?: number;
  assignedTo?: string;
  reportedBy?: string;
  status?: string;
}

export interface Bug {
  bugId: number;
  bugFormatId?: string;
  testcaseId?: number;
  testcaseFormatId?: string;
  featureId?: number;
  title: string;
  description?: string;
  severity: string; // CRITICAL, HIGH, MEDIUM, LOW
  priority?: string;
  status: string; // OPEN, IN_PROGRESS, RESOLVED, FIXED, CLOSED, REOPENED, NOT_A_BUG, DEFERRED
  category?: string;
  bugOccurrence?: number;
  workflow?: BugWorkflow;
  comments?: string;
  assignedTo?: string;
  assignedToId?: number;
  reportedBy?: string;
  createdAt?: string;
}

export interface BugCreateRequest {
  title: string;
  description: string;
  severity: string;
  priority: string;
  category: string;
  status: string;
  comments?: string;
  assignedTo?: number;
}

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
