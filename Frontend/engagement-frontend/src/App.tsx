import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import SurveyPage from "./pages/SurveyPage";
import HrDashboardPage from "./pages/HrDashboardPage";
import EmployeeDetailPage from "./pages/EmployeeDetailPage";

function Home() {
    return (
        <div
            style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center"
            }}
        >
            <div style={{ textAlign: "center" }}>
                <h1>Employee Engagement Platform</h1>
                <p>Please use the survey link provided in your email.</p>
            </div>
        </div>
    );
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/survey/:surveyId" element={<SurveyPage />} />
                <Route path="/hr" element={<HrDashboardPage />} />
                <Route path="/hr/employees/:id" element={<EmployeeDetailPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
