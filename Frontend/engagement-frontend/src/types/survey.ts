export type QuestionType =
    | "TEXT"
    | "RATING"
    | "YES_NO";

export type SurveyStatus =
    | "PENDING"
    | "IN_PROGRESS"
    | "COMPLETED";

export interface QuestionAnswer {
    answerId: string;
    questionId: string;
    question: string;
    theme: string;
    questionType: QuestionType;
    answer: string | null;
    score: number | null;
    followUpExplanation: string | null;
}

export interface SurveyResponse {
    surveyId: string;
    status: SurveyStatus;
    assignedAt: string;
    startedAt: string | null;
    completedAt: string | null;
    questions: QuestionAnswer[];
}

export interface SubmitAnswerRequest {
    answerId: string;
    answer: string;
    score: number | null;
    followUpExplanation?: string | null;
}