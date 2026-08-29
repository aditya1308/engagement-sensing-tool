const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export interface QuestionResponse {
    id: string;
    themeId: string;
    theme: string;
    question: string;
    questionType: "TEXT" | "RATING" | "YES_NO";
}

const readErrorMessage = async (response: Response): Promise<string> => {
    const text = await response.text();

    if (!text) {
        return `Request failed (${response.status})`;
    }

    try {
        const body = JSON.parse(text) as {
            message?: string;
            error?: string;
        };

        return body.message || body.error || text;
    } catch {
        return text;
    }
};

export const uploadQuestionsExcel = async (
    file: File
): Promise<QuestionResponse[]> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(
        `${API_BASE_URL}/api/questions/upload-excel`,
        {
            method: "POST",
            body: formData
        }
    );

    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }

    return response.json() as Promise<QuestionResponse[]>;
};

export const exportQuestionsExcel = async (): Promise<void> => {
    const response = await fetch(
        `${API_BASE_URL}/api/questions/export-excel`
    );

    if (!response.ok) {
        throw new Error(await readErrorMessage(response));
    }

    const blob = await response.blob();
    const contentDisposition = response.headers.get("content-disposition");
    const fileNameMatch = contentDisposition?.match(
        /filename\*?=(?:UTF-8''|\")?([^";]+)/i
    );
    const fileName = fileNameMatch?.[1]
        ? decodeURIComponent(fileNameMatch[1].replace(/"/g, ""))
        : "questions.xlsx";

    const downloadUrl = URL.createObjectURL(blob);
    const anchor = document.createElement("a");

    anchor.href = downloadUrl;
    anchor.download = fileName;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();

    URL.revokeObjectURL(downloadUrl);
};
