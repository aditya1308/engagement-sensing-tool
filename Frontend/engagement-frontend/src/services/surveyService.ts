import type {
    SurveyResponse,
    SubmitAnswerRequest
} from "../types/survey";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL;


/**
 * Get survey
 */
export const getSurvey = async (
    surveyId: string,
    employeeId: string
): Promise<SurveyResponse> => {

    const url =
        `${API_BASE_URL}/api/surveys/${surveyId}?employeeId=${encodeURIComponent(employeeId)}`;

    console.log("GET:", url);

    const response = await fetch(url);

    if (!response.ok) {

        const errorText = await response.text();

        console.error(
            "Get survey error:",
            response.status,
            errorText
        );

        throw new Error(
            `Unable to load survey (${response.status})`
        );
    }

    return response.json();
};


/**
 * Start survey
 */
export const startSurvey = async (
    surveyId: string,
    employeeId: string
): Promise<SurveyResponse> => {

    const url =
        `${API_BASE_URL}/api/surveys/${surveyId}/start?employeeId=${encodeURIComponent(employeeId)}`;

    console.log("POST:", url);

    const response = await fetch(url, {
        method: "POST"
    });

    if (!response.ok) {

        const errorText = await response.text();

        console.error(
            "Start survey error:",
            response.status,
            errorText
        );

        throw new Error(
            `Unable to start survey (${response.status})`
        );
    }

    return response.json();
};


/**
 * Submit one answer
 */
export const submitAnswer = async (
    surveyId: string,
    employeeId: string,
    request: SubmitAnswerRequest
): Promise<SurveyResponse> => {

    const url =
        `${API_BASE_URL}/api/surveys/${surveyId}/answers?employeeId=${encodeURIComponent(employeeId)}`;

    console.log("POST:", url);
    console.log("Answer:", request);

    const response = await fetch(url, {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(request)
    });

    if (!response.ok) {

        const errorText = await response.text();

        console.error(
            "Submit answer error:",
            response.status,
            errorText
        );

        throw new Error(
            `Unable to submit answer (${response.status})`
        );
    }

    return response.json();
};


/**
 * Complete survey
 */
export const completeSurvey = async (
    surveyId: string,
    employeeId: string
): Promise<SurveyResponse> => {

    const url =
        `${API_BASE_URL}/api/surveys/${surveyId}/complete?employeeId=${encodeURIComponent(employeeId)}`;

    console.log("POST:", url);

    const response = await fetch(url, {
        method: "POST"
    });

    if (!response.ok) {

        const errorText = await response.text();

        console.error(
            "Complete survey error:",
            response.status,
            errorText
        );

        throw new Error(
            `Unable to complete survey (${response.status})`
        );
    }

    return response.json();
};