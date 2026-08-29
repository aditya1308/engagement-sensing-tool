import { useEffect, useState } from "react";
import {
    useParams,
    useSearchParams
} from "react-router-dom";

import {
    getSurvey,
    startSurvey,
    submitAnswer,
    completeSurvey
} from "../services/surveyService";

import type {
    SurveyResponse,
    QuestionAnswer
} from "../types/survey";

import "./SurveyPage.css";


function SurveyPage() {

    const { surveyId } = useParams<{
        surveyId: string;
    }>();

    const [searchParams] = useSearchParams();

    const employeeId =
        searchParams.get("employeeId");


    const [survey, setSurvey] =
        useState<SurveyResponse | null>(null);

    const [currentQuestionIndex, setCurrentQuestionIndex] =
        useState(0);

    const [answer, setAnswer] =
        useState("");

    const [loading, setLoading] =
        useState(true);

    const [submitting, setSubmitting] =
        useState(false);

    const [error, setError] =
        useState("");

    const [started, setStarted] =
        useState(false);


    /*
     * Load survey
     */
    useEffect(() => {

        if (!surveyId || !employeeId) {

            setError(
                "Invalid survey link."
            );

            setLoading(false);

            return;
        }

        loadSurvey();

    }, [surveyId, employeeId]);


    const loadSurvey = async () => {

        try {

            const data =
                await getSurvey(
                    surveyId!,
                    employeeId!
                );

            setSurvey(data);

            if (
                data.status === "IN_PROGRESS"
            ) {
                setStarted(true);
            }

        } catch (error) {

            console.error(error);

            setError(
                "Unable to load the survey."
            );

        } finally {

            setLoading(false);
        }
    };


    /*
     * Start survey
     */
    const handleStartSurvey = async () => {

        try {

            setError("");

            setSubmitting(true);

            const updatedSurvey =
                await startSurvey(
                    surveyId!,
                    employeeId!
                );

            setSurvey(updatedSurvey);

            setStarted(true);

        } catch (error) {

            console.error(error);

            setError(
                "Unable to start the survey."
            );

        } finally {

            setSubmitting(false);
        }
    };


    /*
     * Current question
     */
    const currentQuestion:
        QuestionAnswer | undefined =
        survey?.questions[
            currentQuestionIndex
        ];


    /*
     * Submit answer
     */
    const handleSubmitAnswer = async () => {

        if (!currentQuestion) {
            return;
        }

        if (!answer.trim()) {

            setError(
                "Please provide an answer."
            );

            return;
        }

        try {

            setError("");

            setSubmitting(true);

            await submitAnswer(
                surveyId!,
                employeeId!,
                {
                    answerId:
                        currentQuestion.answerId,

                    answer:
                        answer,

                    score:
                        currentQuestion.questionType === "RATING"
                            ? Number(answer)
                            : null
                }
            );


            /*
             * Next question
             */
            if (
                currentQuestionIndex <
                survey!.questions.length - 1
            ) {

                setCurrentQuestionIndex(
                    currentQuestionIndex + 1
                );

                setAnswer("");

            } else {

                await handleCompleteSurvey();

            }

        } catch (error) {

            console.error(error);

            setError(
                "Unable to save your answer."
            );

        } finally {

            setSubmitting(false);
        }
    };


    /*
     * Complete survey
     */
    const handleCompleteSurvey = async () => {

        try {

            const completedSurvey =
                await completeSurvey(
                    surveyId!,
                    employeeId!
                );

            setSurvey(
                completedSurvey
            );

        } catch (error) {

            console.error(error);

            setError(
                "Unable to complete the survey."
            );
        }
    };


    /*
     * Common Footer
     */
    const Footer = () => (

        <footer className="survey-footer">

            <div className="footer-content">

                <span>
                    © {new Date().getFullYear()}
                    {" "}
                    Société Générale
                </span>

                <span>
                    Employee Engagement Platform
                </span>

            </div>

        </footer>
    );


    /*
     * Loading
     */
    if (loading) {

        return (
            <div className="page-layout">

                <header className="survey-topbar">

                    <img
                        src="/societe-generale-logo.jpg"
                        alt="Société Générale"
                        className="sg-logo"
                    />

                </header>

                <main className="survey-container">

                    <div className="survey-card">

                        <div className="loader">

                            <div className="spinner"></div>

                            <p>
                                Loading your survey...
                            </p>

                        </div>

                    </div>

                </main>

                <Footer />

            </div>
        );
    }


    /*
     * Invalid / Error
     */
    if (error && !survey) {

        return (
            <div className="page-layout">

                <header className="survey-topbar">

                    <img
                        src="/societe-generale-logo.jpg"
                        alt="Société Générale"
                        className="sg-logo"
                    />

                </header>

                <main className="survey-container">

                    <div className="survey-card error-card">

                        <div className="error-icon">
                            !
                        </div>

                        <h2>
                            Something went wrong
                        </h2>

                        <p>
                            {error}
                        </p>

                    </div>

                </main>

                <Footer />

            </div>
        );
    }


    if (!survey) {
        return null;
    }


    /*
     * Completed
     */
    if (
        survey.status === "COMPLETED"
    ) {

        return (
            <div className="page-layout">

                <header className="survey-topbar">

                    <img
                        src="/societe-generale-logo.jpg"
                        alt="Société Générale"
                        className="sg-logo"
                    />

                </header>

                <main className="survey-container">

                    <div className="survey-card completion-card">

                        <div className="success-icon">
                            ✓
                        </div>

                        <h1>
                            Survey Completed
                        </h1>

                        <p>
                            Thank you for taking the time
                            to share your feedback.
                        </p>

                        <p className="completion-message">
                            Your responses have been
                            successfully recorded.
                        </p>

                    </div>

                </main>

                <Footer />

            </div>
        );
    }


    /*
     * Introduction
     */
    if (!started) {

        return (
            <div className="page-layout">

                <header className="survey-topbar">

                    <img
                        src="/societe-generale-logo.jpg"
                        alt="Société Générale"
                        className="sg-logo"
                    />

                </header>

                <main className="survey-container">

                    <div className="survey-card introduction-card">

                        <div className="red-line"></div>

                        <h1>
                            Employee Engagement
                            <br />
                            Survey
                        </h1>

                        <p className="description">
                            We value your feedback.
                            This survey helps us understand
                            your experience and identify
                            areas where we can improve.
                        </p>

                        <div className="survey-info">

                            <div>

                                <strong>
                                    {survey.questions.length}
                                </strong>

                                <span>
                                    Questions
                                </span>

                            </div>

                            <div>

                                <strong>
                                    ~5
                                </strong>

                                <span>
                                    Minutes
                                </span>

                            </div>

                        </div>

                        {error && (

                            <p className="form-error">
                                {error}
                            </p>

                        )}

                        <button
                            className="start-button"
                            onClick={
                                handleStartSurvey
                            }
                            disabled={submitting}
                        >

                            {submitting
                                ? "Starting..."
                                : "Start Survey"
                            }

                        </button>

                        <p className="privacy-note">
                            Your feedback helps us create
                            a better workplace.
                        </p>

                    </div>

                </main>

                <Footer />

            </div>
        );
    }


    /*
     * Question Screen
     */
    return (
        <div className="page-layout">

            <header className="survey-topbar">

                <img
                    src="/societe-generale-logo.jpg"
                    alt="Société Générale"
                    className="sg-logo"
                />

                <span className="employee-survey-label">
                    Employee Engagement
                </span>

            </header>


            <main className="survey-container">

                <div className="survey-card question-card">

                    <div className="question-header">

                        <div>

                            <span className="question-label">
                                QUESTION
                            </span>

                            <span className="question-counter">
                                {currentQuestionIndex + 1}
                                {" / "}
                                {survey.questions.length}
                            </span>

                        </div>

                    </div>


                    <div className="progress-container">

                        <div
                            className="progress-bar"
                            style={{
                                width:
                                    `${
                                        (
                                            (
                                                currentQuestionIndex + 1
                                            )
                                            /
                                            survey.questions.length
                                        ) * 100
                                    }%`
                            }}
                        />

                    </div>


                    {currentQuestion && (

                        <div className="question-section">

                            <div className="theme">
                                {currentQuestion.theme}
                            </div>

                            <h2>
                                {currentQuestion.question}
                            </h2>


                            {/* RATING */}

                            {currentQuestion.questionType ===
                                "RATING" && (

                                <div className="rating-container">

                                    <div className="rating-buttons">

                                        {[1, 2, 3, 4, 5].map(
                                            (value) => (

                                                <button
                                                    key={value}
                                                    className={
                                                        answer ===
                                                        String(value)
                                                            ? "rating selected"
                                                            : "rating"
                                                    }
                                                    onClick={() =>
                                                        setAnswer(
                                                            String(value)
                                                        )
                                                    }
                                                >
                                                    {value}
                                                </button>

                                            )
                                        )}

                                    </div>

                                    <div className="rating-labels">

                                        <span>
                                            Very dissatisfied
                                        </span>

                                        <span>
                                            Very satisfied
                                        </span>

                                    </div>

                                </div>
                            )}


                            {/* YES NO */}

                            {currentQuestion.questionType ===
                                "YES_NO" && (

                                <div className="yes-no-container">

                                    <button
                                        className={
                                            answer === "YES"
                                                ? "choice selected"
                                                : "choice"
                                        }
                                        onClick={() =>
                                            setAnswer("YES")
                                        }
                                    >
                                        Yes
                                    </button>

                                    <button
                                        className={
                                            answer === "NO"
                                                ? "choice selected"
                                                : "choice"
                                        }
                                        onClick={() =>
                                            setAnswer("NO")
                                        }
                                    >
                                        No
                                    </button>

                                </div>
                            )}


                            {/* TEXT */}

                            {currentQuestion.questionType ===
                                "TEXT" && (

                                <textarea
                                    className="text-answer"
                                    value={answer}
                                    onChange={(event) =>
                                        setAnswer(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Type your answer here..."
                                    rows={6}
                                />

                            )}


                            {error && (

                                <p className="form-error">
                                    {error}
                                </p>

                            )}


                            <button
                                className="next-button"
                                onClick={
                                    handleSubmitAnswer
                                }
                                disabled={submitting}
                            >

                                {submitting
                                    ? "Saving..."
                                    : currentQuestionIndex ===
                                      survey.questions.length - 1
                                        ? "Complete Survey"
                                        : "Continue"
                                }

                                {!submitting && (
                                    <span>
                                        →
                                    </span>
                                )}

                            </button>

                        </div>

                    )}

                </div>

            </main>

            <Footer />

        </div>
    );
}

export default SurveyPage;