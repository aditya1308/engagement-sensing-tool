export type Role = "EMPLOYEE" | "HR";

export type SurveyStatus =
    | "PENDING"
    | "IN_PROGRESS"
    | "COMPLETED";

export type QuestionType =
    | "TEXT"
    | "RATING"
    | "YES_NO";

export interface ThemeMetricResponse {
    theme: string;
    score: number;
}

export interface DashboardResponse {
    totalEmployees: number;
    totalSurveys: number;
    completedSurveys: number;
    pendingSurveys: number;
    responseRate: number;
    overallEngagementScore: number;
    themeMetrics: ThemeMetricResponse[];
}

export interface EmployeeResponse {
    id: string;
    employeeId: string;
    name: string;
    email: string;
    department: string;
    role: Role;
    surveyStatus: SurveyStatus | null;
}

export interface AnswerResponse {
    questionId: string;
    question: string;
    theme: string;
    questionType: QuestionType;
    answer: string | null;
    score: number | null;
    followUpExplanation: string | null;
}

export interface EmployeeSurveyItem {
    surveyId: string;
    status: SurveyStatus;
    startedAt: string | null;
    completedAt: string | null;
    assignedAt: string | null;
    answers: AnswerResponse[];
}

export interface EmployeeSurveyResponse {
    id: string;
    employeeId: string;
    name: string;
    email: string;
    department: string;
    surveys: EmployeeSurveyItem[];
}
