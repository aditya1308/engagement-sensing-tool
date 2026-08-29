import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
    getEmployee,
    getEmployeeResponses
} from "../services/hrService";

import type {
    EmployeeResponse,
    EmployeeSurveyResponse
} from "../types/hr";

import "./HrDashboardPage.css";

function EmployeeDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [employee, setEmployee] =
        useState<EmployeeResponse | null>(null);

    const [responses, setResponses] =
        useState<EmployeeSurveyResponse | null>(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!id) {
            setError("Invalid employee id.");
            setLoading(false);
            return;
        }

        loadEmployee(id);
    }, [id]);

    const loadEmployee = async (employeeId: string) => {
        try {
            setLoading(true);
            setError("");

            const [employeeData, responseData] = await Promise.all([
                getEmployee(employeeId),
                getEmployeeResponses(employeeId)
            ]);

            setEmployee(employeeData);
            setResponses(responseData);
        } catch (error) {
            console.error(error);
            setError("Unable to load employee responses.");
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="hr-message">Loading employee...</div>;
    }

    if (error || !employee || !responses) {
        return (
            <div className="hr-message error-message">
                <p>{error || "Employee data unavailable."}</p>
                <button onClick={() => navigate("/hr")}>Back to dashboard</button>
            </div>
        );
    }

    return (
        <div className="hr-page">
            <header className="hr-topbar">
                <div>
                    <div className="hr-brand">Engagement Sensing</div>
                    <div className="hr-subtitle">Employee Responses</div>
                </div>
                <button className="refresh-button" onClick={() => navigate("/hr")}>
                    Back
                </button>
            </header>

            <main className="hr-content">
                <section className="employee-profile-card">
                    <div>
                        <span className="profile-label">Employee</span>
                        <h1>{employee.name}</h1>
                        <p>{employee.email}</p>
                    </div>

                    <div className="profile-meta">
                        <div>
                            <span>Employee ID</span>
                            <strong>{employee.employeeId}</strong>
                        </div>
                        <div>
                            <span>Department</span>
                            <strong>{employee.department || "—"}</strong>
                        </div>
                        <div>
                            <span>Survey Status</span>
                            <strong>{employee.surveyStatus || "PENDING"}</strong>
                        </div>
                    </div>
                </section>

                {responses.surveys.length === 0 ? (
                    <section className="dashboard-panel">
                        <div className="empty-state">
                            No survey responses are available for this employee.
                        </div>
                    </section>
                ) : (
                    responses.surveys.map((survey, surveyIndex) => (
                        <section className="dashboard-panel" key={survey.surveyId}>
                            <div className="panel-heading survey-detail-heading">
                                <div>
                                    <h2>Survey {surveyIndex + 1}</h2>
<p>
    Status: <strong>{survey.status}</strong>

    {survey.completedAt ? (
        <> · Completed {formatDate(survey.completedAt)}</>
    ) : survey.assignedAt ? (
        <> · Assigned {formatDate(survey.assignedAt)}</>
    ) : null}
</p>
                                </div>
                            </div>

                            <div className="response-list">
                                {survey.answers.map((answer, index) => (
                                    <article
                                        className="response-card"
                                        key={`${survey.surveyId}-${answer.questionId}`}
                                    >
                                        <div className="response-number">
                                            {index + 1}
                                        </div>
                                        <div className="response-content">
                                            <div className="response-theme">
                                                {answer.theme}
                                            </div>
                                            <h3>{answer.question}</h3>
                                            <div className="answer-box">
                                                {answer.answer || "No answer"}
                                            </div>
                                            {answer.score !== null && (
                                                <div className="response-score">
                                                    Score: <strong>{answer.score}</strong>
                                                </div>
                                            )}
                                        </div>
                                    </article>
                                ))}
                            </div>
                        </section>
                    ))
                )}
            </main>
        </div>
    );
}

function formatDate(value: string) {
    return new Intl.DateTimeFormat("en", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }).format(new Date(value));
}

export default EmployeeDetailPage;
