# Employee Engagement Sensing Platform

A full-stack employee engagement platform that periodically collects employee feedback through structured surveys and gives HR a dashboard for monitoring participation, engagement scores, theme-level trends, and individual survey responses.

The application automatically assigns surveys based on an employee's date of joining, sends survey invitations by email, supports multiple question types, tracks the complete survey lifecycle, and provides HR-facing analytics.

## Features

### Employee Survey Flow

- Register employees and HR users
- Automatically assign an employee's first survey 6 months after their date of joining
- Schedule subsequent surveys every 6 months
- Create one answer record for every available question when a survey is assigned
- Send the employee an email containing a direct survey link
- Save answers as the employee progresses through the survey
- Complete surveys from the web application

### HR Dashboard

- View total employees
- View total surveys
- View completed surveys
- View pending surveys
- Calculate survey response rate
- Calculate overall engagement score
- View engagement scores grouped by theme
- Browse employees
- View an individual employee's survey history and responses

### Question Management

- Upload questions in batches
- Associate questions with engagement themes
- Reuse the question bank when new surveys are assigned

### Email Notifications

- Send survey invitations using Spring Mail
- Generate employee-specific survey URLs
- Store notification delivery status
- Track sent and failed email notifications

## Tech Stack

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- MySQL
- Spring Mail
- Lombok
- Springdoc OpenAPI / Swagger
- Maven

### Frontend

- React 19
- TypeScript
- Vite
- React Router
- Fetch API
- CSS

## Survey Scheduling

The backend determines survey eligibility using the employee's joining date.

For an employee with no previous survey:

```text
first survey date = date of joining + 6 months
```

For employees who have already received a survey:

```text
next survey date = latest survey assigned date + 6 months
```
## API Details

### Survey APIs

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/surveys` | Create a survey |
| `GET` | `/api/surveys/my?employeeId={employeeId}` | Get the employee's current survey |
| `GET` | `/api/surveys/{surveyId}?employeeId={employeeId}` | Get a survey and its questions |
| `POST` | `/api/surveys/{surveyId}/start?employeeId={employeeId}` | Start a survey |
| `POST` | `/api/surveys/{surveyId}/answers?employeeId={employeeId}` | Submit an answer |
| `POST` | `/api/surveys/{surveyId}/complete?employeeId={employeeId}` | Complete a survey |

### HR APIs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/hr/dashboard` | Get HR dashboard statistics |
| `GET` | `/api/hr/themes` | Get engagement scores by theme |
| `GET` | `/api/hr/employees` | Get employees |
| `GET` | `/api/hr/employees/{employeeId}` | Get an employee |
| `GET` | `/api/hr/employees/{id}/responses` | Get an employee's survey history and responses |

## Frontend Routes

| Route | Description |
|---|---|
| `/` | Application landing page |
| `/survey/:surveyId` | Employee survey page |
| `/hr` | HR dashboard |
| `/hr/employees/:id` | Employee detail and survey response page |

Employee invitation links are generated in this format:

```text
http://localhost:5173/survey/{surveyId}?employeeId={employeeId}
```

## Getting Started

### Prerequisites

Install:

- Java 21
- Maven, or use the included Maven Wrapper
- MySQL 8+
- Node.js
- npm

## 1. Clone the Repository

```bash
git clone https://github.com/aditya1308/engagement-sensing-tool.git
cd engagement-sensing-tool
```

## 2. Create the MySQL Database

Start MySQL and create the database:

```sql
CREATE DATABASE engagement_db;
```

The application currently uses Hibernate:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

Therefore, the required tables are created/updated automatically when the backend starts.

## 3. Configure the Backend

Open:

```text
Backend/engagement.tool/src/main/resources/application.yaml
```

Configure the database:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/engagement_db
    username: root
    password: YOUR_MYSQL_PASSWORD
    driver-class-name: com.mysql.cj.jdbc.Driver
```

Configure email if survey invitation emails are required:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: YOUR_EMAIL
    password: YOUR_APP_PASSWORD
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

Set the frontend URL:

```yaml
app:
  frontend-url: http://localhost:5173
```

> Do not commit database passwords, mail passwords, API keys, or other secrets to Git. For deployed environments, use environment variables or a secret manager.

## 4. Run the Backend

### Windows

```bash
cd Backend\engagement.tool
mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
cd Backend/engagement.tool
./mvnw spring-boot:run
```

Or, if Maven is installed globally:

```bash
mvn spring-boot:run
```

Backend:

```text
http://localhost:8090
```

## 5. Swagger / OpenAPI

After the backend starts, open:

```text
http://localhost:8090/swagger-ui/index.html
```

Swagger can be used to test registration, question upload, survey, and HR APIs.

## 6. Configure the Frontend

Open:

```text
Frontend/engagement-frontend/.env
```

Set:

```env
VITE_API_BASE_URL=http://localhost:8090
```

## 7. Run the Frontend

```bash
cd Frontend/engagement-frontend
npm install
npm run dev
```

Frontend:

```text
http://localhost:5173
```

## HR Dashboard Metrics

The HR dashboard currently calculates:

- Total employees
- Total surveys
- Completed surveys
- Pending surveys
- Response rate

```text
response rate = completed surveys / total surveys * 100
```

It also calculates:

- Overall engagement score
- Theme-wise average engagement scores
- Employee-level survey history
- Employee-level answers

## Security Notes

The current project is an MVP/development implementation.

Before using it in a production environment:

- Add authentication
- Add authorization for HR APIs
- Avoid trusting a query-string `employeeId` as the only employee identifier
- Move mail and database credentials to environment variables
- Configure production CORS restrictions
- Use HTTPS
- Add validation to request DTOs
- Add centralized exception handling
- Add audit logging where required
- Review privacy requirements before storing employee feedback

## Build Commands

### Backend

```bash
cd Backend/engagement.tool
./mvnw clean package
```

Windows:

```bash
mvnw.cmd clean package
```

### Frontend

```bash
cd Frontend/engagement-frontend
npm install
npm run build
```

The frontend production build is generated under:

```text
dist/
```

## Repository

GitHub:

```text
https://github.com/aditya1308/engagement-sensing-tool
```

## Author

**Aditya Prasad**

GitHub: [@aditya1308](https://github.com/aditya1308)

## License

No license has been specified for this repository yet.

If the project is intended to be open source, add a suitable license such as MIT, Apache-2.0, or another license appropriate for the project.
