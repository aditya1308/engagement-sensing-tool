import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    getEmployees,
    getHrDashboard
} from "../services/hrService";

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
                <button className="refresh-button" onClick={loadDashboard}>
                    Refresh
                </button>
            </header>

            <main className="hr-content">
                <section className="hr-heading">
                    <div>
                        <h1>Engagement Overview</h1>
                        <p>Monitor participation and employee sentiment.</p>
                    </div>
                </section>

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

                <section className="dashboard-panel">
                    <div className="panel-heading">
                        <div>
                            <h2>Theme Scores</h2>
                            <p>Average scored responses by engagement theme.</p>
                        </div>
                    </div>

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
                </section>

                <section className="dashboard-panel employee-panel">
                    <div className="panel-heading employee-heading">
                        <div>
                            <h2>Employees</h2>
                            <p>Select an employee to review survey responses.</p>
                        </div>

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
