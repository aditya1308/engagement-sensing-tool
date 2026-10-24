import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    getEmployees,
    getHrDashboard,
    sendDashboardReportEmail
} from "../services/hrService";
import {
    exportQuestionsExcel,
    uploadQuestionsExcel
} from "../services/questionService";

import type {
    DashboardResponse,
    EmployeeResponse,
    SurveyStatus
} from "../types/hr";

import "./HrDashboardPage.css";

function HrDashboardPage() {
    const navigate = useNavigate();

    const [dashboard, setDashboard] =
        useState<DashboardResponse | null>(null);

    const [employees, setEmployees] =
        useState<EmployeeResponse[]>([]);

    const [search, setSearch] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [sendingReport, setSendingReport] = useState(false);
    const [reportMessage, setReportMessage] = useState("");
    const [reportError, setReportError] = useState("");

    const [questionFile, setQuestionFile] =
        useState<File | null>(null);
    const [uploadingQuestions, setUploadingQuestions] =
        useState(false);
    const [exportingQuestions, setExportingQuestions] =
        useState(false);
    const [questionMessage, setQuestionMessage] =
        useState("");
    const [questionError, setQuestionError] =
        useState("");

    const [themesOpen, setThemesOpen] =
        useState(false);
    const [questionManagementOpen, setQuestionManagementOpen] =
        useState(false);
    const [employeesOpen, setEmployeesOpen] =
        useState(false);

    useEffect(() => {
        loadDashboard();
    }, []);

    const loadDashboard = async () => {
        try {
            setLoading(true);
            setError("");

            const [dashboardData, employeeData] = await Promise.all([
                getHrDashboard(),
                getEmployees()
            ]);

            setDashboard(dashboardData);
            setEmployees(employeeData);
        } catch (error) {
            console.error(error);
            setError("Unable to load HR dashboard.");
        } finally {
            setLoading(false);
        }
    };


    const handleSendDashboardReport = async () => {
        try {
            setSendingReport(true);
            setReportMessage("");
            setReportError("");
            const result = await sendDashboardReportEmail();
            setReportMessage(result.message || "Dashboard report sent successfully.");
        } catch (error) {
            console.error(error);
            setReportError(
                error instanceof Error
                    ? error.message
                    : "Unable to send the dashboard report."
            );
        } finally {
            setSendingReport(false);
        }
    };

    const filteredEmployees = useMemo(() => {
        const query = search.trim().toLowerCase();

        if (!query) {
            return employees;
        }

        return employees.filter((employee) =>
            [
                employee.name,
                employee.employeeId,
                employee.email,
                employee.department
            ].some((value) =>
                value?.toLowerCase().includes(query)
            )
        );
    }, [employees, search]);

    const statusClass = (status: SurveyStatus | null) => {
        switch (status) {
            case "COMPLETED":
                return "status completed";
            case "IN_PROGRESS":
                return "status in-progress";
            default:
                return "status pending";
        }
    };


    const handleQuestionUpload = async () => {
        if (!questionFile) {
            setQuestionError("Please select an .xlsx file first.");
            return;
        }

        try {
            setUploadingQuestions(true);
            setQuestionError("");
            setQuestionMessage("");

            const uploadedQuestions =
                await uploadQuestionsExcel(questionFile);

            setQuestionMessage(
                `${uploadedQuestions.length} new question${
                    uploadedQuestions.length === 1 ? "" : "s"
                } uploaded successfully. These questions will be used for new surveys.`
            );
            setQuestionFile(null);

            const fileInput = document.getElementById(
                "question-excel-file"
            ) as HTMLInputElement | null;

            if (fileInput) {
                fileInput.value = "";
            }
        } catch (error) {
            console.error(error);
            setQuestionError(
                error instanceof Error
                    ? error.message
                    : "Unable to upload questions."
            );
        } finally {
            setUploadingQuestions(false);
        }
    };

    const handleQuestionExport = async () => {
        try {
            setExportingQuestions(true);
            setQuestionError("");
            setQuestionMessage("");

            await exportQuestionsExcel();
            setQuestionMessage("Question export downloaded successfully.");
        } catch (error) {
            console.error(error);
            setQuestionError(
                error instanceof Error
                    ? error.message
                    : "Unable to export questions."
            );
        } finally {
            setExportingQuestions(false);
        }
    };

    if (loading) {
        return <div className="hr-message">Loading dashboard...</div>;
    }

    if (error || !dashboard) {
        return (
            <div className="hr-message error-message">
                <p>{error || "Dashboard data unavailable."}</p>
                <button onClick={loadDashboard}>Retry</button>
            </div>
        );
    }

    return (
        <div className="hr-page">
            <header className="hr-topbar">
                <div>
                    <div className="hr-brand">Engagement Sensing</div>
                    <div className="hr-subtitle">HR Business Partner Dashboard</div>
                </div>
                <div className="hr-header-actions">
                    <button
                        className="refresh-button"
                        type="button"
                        disabled={sendingReport}
                        onClick={handleSendDashboardReport}
                    >
                        {sendingReport ? "Sending Report..." : "Send Report"}
                    </button>
                    <button className="refresh-button" onClick={loadDashboard}>
                        Refresh
                    </button>
                </div>
            </header>

            <main className="hr-content">
                <section className="hr-heading">
                    <div>
                        <h1>Engagement Overview</h1>
                        <p>Monitor participation and employee sentiment.</p>
                    </div>
                </section>

                {(reportMessage || reportError) && (
                    <div className={`report-feedback ${reportError ? "failure" : "success"}`} role="status">
                        {reportError || reportMessage}
                    </div>
                )}

                <section className="metric-grid">
                    <MetricCard
                        label="Employees"
                        value={dashboard.totalEmployees.toString()}
                    />
                    <MetricCard
                        label="Response Rate"
                        value={`${dashboard.responseRate.toFixed(1)}%`}
                    />
                    <MetricCard
                        label="Engagement Score"
                        value={dashboard.overallEngagementScore.toFixed(2)}
                        caption="out of 5"
                    />
                    <MetricCard
                        label="Completed Surveys"
                        value={dashboard.completedSurveys.toString()}
                        caption={`${dashboard.pendingSurveys} pending`}
                    />
                </section>

                <section className="dashboard-panel collapsible-panel">
                    <button
                        className="collapsible-heading"
                        type="button"
                        aria-expanded={themesOpen}
                        onClick={() => setThemesOpen((open) => !open)}
                    >
                        <div>
                            <h2>Theme Scores</h2>
                            <p>Average scored responses by engagement theme.</p>
                        </div>
                        <span
                            className={`collapse-arrow ${themesOpen ? "open" : ""}`}
                            aria-hidden="true"
                        >
                            ▾
                        </span>
                    </button>

                    <div
                        className={`collapsible-body ${themesOpen ? "open" : ""}`}
                        aria-hidden={!themesOpen}
                    >
                        <div className="collapsible-body-inner">
                            <div className="collapsible-content">
                                {dashboard.themeMetrics.length === 0 ? (
                                    <div className="empty-state">No scored responses yet.</div>
                                ) : (
                                    <div className="theme-list">
                                        {dashboard.themeMetrics.map((metric) => {
                                            const percentage = Math.max(
                                                0,
                                                Math.min(100, (metric.score / 5) * 100)
                                            );

                                            return (
                                                <div className="theme-row" key={metric.theme}>
                                                    <div className="theme-row-heading">
                                                        <span>{metric.theme}</span>
                                                        <strong>{metric.score.toFixed(2)}</strong>
                                                    </div>
                                                    <div className="theme-track">
                                                        <div
                                                            className="theme-fill"
                                                            style={{ width: `${percentage}%` }}
                                                        />
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </section>

                <section className="dashboard-panel question-management-panel collapsible-panel">
                    <button
                        className="collapsible-heading"
                        type="button"
                        aria-expanded={questionManagementOpen}
                        onClick={() =>
                            setQuestionManagementOpen((open) => !open)
                        }
                    >
                        <div>
                            <h2>Question Management</h2>
                            <p>
                                Upload a new Excel question set for future surveys,
                                or export the complete question history.
                            </p>
                        </div>
                        <span
                            className={`collapse-arrow ${
                                questionManagementOpen ? "open" : ""
                            }`}
                            aria-hidden="true"
                        >
                            ▾
                        </span>
                    </button>

                    <div
                        className={`collapsible-body ${questionManagementOpen ? "open" : ""}`}
                        aria-hidden={!questionManagementOpen}
                    >
                        <div className="collapsible-body-inner">
                            <div className="collapsible-content">
                    <div className="question-management-grid">
                        <div className="question-action-card">
                            <span className="question-action-label">
                                Upload new questions
                            </span>
                            <h3>Replace the active question set</h3>
                            <p>
                                Upload an .xlsx file containing Theme, Question and
                                Question Type. Existing questions remain available
                                for historical survey responses but will no longer be
                                used for new surveys.
                            </p>

                            <div className="question-file-row">
                                <input
                                    id="question-excel-file"
                                    className="question-file-input"
                                    type="file"
                                    accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                    onChange={(event) => {
                                        setQuestionFile(
                                            event.target.files?.[0] ?? null
                                        );
                                        setQuestionError("");
                                        setQuestionMessage("");
                                    }}
                                />

                                <button
                                    className="question-primary-button"
                                    type="button"
                                    disabled={!questionFile || uploadingQuestions}
                                    onClick={handleQuestionUpload}
                                >
                                    {uploadingQuestions
                                        ? "Uploading..."
                                        : "Upload Excel"}
                                </button>
                            </div>

                            {questionFile && (
                                <div className="selected-question-file">
                                    Selected: {questionFile.name}
                                </div>
                            )}
                        </div>

                        <div className="question-action-card export-card">
                            <span className="question-action-label">
                                Export questions
                            </span>
                            <h3>Download question history</h3>
                            <p>
                                Download all questions as an Excel file, including
                                previous inactive questions and the current active
                                question set.
                            </p>

                            <button
                                className="question-secondary-button"
                                type="button"
                                disabled={exportingQuestions}
                                onClick={handleQuestionExport}
                            >
                                {exportingQuestions
                                    ? "Preparing Excel..."
                                    : "Export Questions"}
                            </button>
                        </div>
                    </div>

                    {questionMessage && (
                        <div className="question-feedback success">
                            {questionMessage}
                        </div>
                    )}

                    {questionError && (
                        <div className="question-feedback failure">
                            {questionError}
                        </div>
                    )}
                            </div>
                        </div>
                    </div>
                </section>

                <section className="dashboard-panel employee-panel collapsible-panel">
                    <button
                        className="collapsible-heading"
                        type="button"
                        aria-expanded={employeesOpen}
                        onClick={() => setEmployeesOpen((open) => !open)}
                    >
                        <div>
                            <h2>Employees</h2>
                            <p>Select an employee to review survey responses.</p>
                        </div>
                        <span
                            className={`collapse-arrow ${employeesOpen ? "open" : ""}`}
                            aria-hidden="true"
                        >
                            ▾
                        </span>
                    </button>

                    <div
                        className={`collapsible-body ${employeesOpen ? "open" : ""}`}
                        aria-hidden={!employeesOpen}
                    >
                        <div className="collapsible-body-inner">
                            <div className="collapsible-content">
                            <div className="employee-tools">
                                <input
                                    className="employee-search"
                                    type="search"
                                    placeholder="Search employees..."
                                    value={search}
                                    onChange={(event) => setSearch(event.target.value)}
                                />
                            </div>

                    <div className="employee-table-wrapper">
                        <table className="employee-table">
                            <thead>
                                <tr>
                                    <th>Employee</th>
                                    <th>ID</th>
                                    <th>Department</th>
                                    <th>Survey</th>
                                    <th />
                                </tr>
                            </thead>
                            <tbody>
                                {filteredEmployees.map((employee) => (
                                    <tr key={employee.id}>
                                        <td>
                                            <div className="employee-name">
                                                {employee.name}
                                            </div>
                                            <div className="employee-email">
                                                {employee.email}
                                            </div>
                                        </td>
                                        <td>{employee.employeeId}</td>
                                        <td>{employee.department || "—"}</td>
                                        <td>
                                            <span className={statusClass(employee.surveyStatus)}>
                                                {employee.surveyStatus || "PENDING"}
                                            </span>
                                        </td>
                                        <td className="view-cell">
                                            <button
                                                className="view-button"
                                                onClick={() =>
                                                    navigate(`/hr/employees/${employee.id}`)
                                                }
                                            >
                                                View
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {filteredEmployees.length === 0 && (
                        <div className="empty-state">No employees found.</div>
                    )}
                            </div>
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
}

function MetricCard({
    label,
    value,
    caption
}: {
    label: string;
    value: string;
    caption?: string;
}) {
    return (
        <article className="metric-card">
            <span>{label}</span>
            <strong>{value}</strong>
            {caption && <small>{caption}</small>}
        </article>
    );
}

export default HrDashboardPage;
