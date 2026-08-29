import type {
    DashboardResponse,
    EmployeeResponse,
    EmployeeSurveyResponse
} from "../types/hr";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const handleResponse = async <T>(response: Response): Promise<T> => {
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `Request failed (${response.status})`);
    }

    return response.json();
};

export const getHrDashboard = async (): Promise<DashboardResponse> => {
    const response = await fetch(`${API_BASE_URL}/api/hr/dashboard`);
    return handleResponse<DashboardResponse>(response);
};

export const getEmployees = async (): Promise<EmployeeResponse[]> => {
    const response = await fetch(`${API_BASE_URL}/api/hr/employees`);
    return handleResponse<EmployeeResponse[]>(response);
};

export const getEmployee = async (id: string): Promise<EmployeeResponse> => {
    const response = await fetch(`${API_BASE_URL}/api/hr/employees/${id}`);
    return handleResponse<EmployeeResponse>(response);
};

export const getEmployeeResponses = async (
    id: string
): Promise<EmployeeSurveyResponse> => {
    const response = await fetch(
        `${API_BASE_URL}/api/hr/employees/${id}/responses`
    );

    return handleResponse<EmployeeSurveyResponse>(response);
};
