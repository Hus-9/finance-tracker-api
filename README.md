# Personal Finance Tracker API

![CI](https://github.com/Hus-9/finance-tracker-api/actions/workflows/ci.yml/badge.svg)

A REST API for keeping track of your money. You can sign up, add your bank accounts, log income and spending against categories, set monthly budgets and get reports showing where your money went and whether you went over budget.

I built it to learn how a real backend fits together, from the database and security all the way to tests, Docker and CI.

## Features 

- **Sign up and log in** with JWT tokens. Passwords are hashed with BCrypt and never sent back by the API.
- **Accounts** such as current, savings or credit, each with its own currency.
- **Categories** for income and spending. Every new user starts with six: Salary, Rent, Food, Transport, Bills and Entertainment.
- **Transactions** with filters for date range, category and type, plus paging with the newest first.
- **Monthly budgets** for spending categories.
- **Reports**: a monthly summary (income, spending, net and spending per category) and a budget status report showing what's  left and what's gone over.
- **Swagger UI** to try every endpoint in the browser.

## Tech stack

| Area | Tools |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 (Web, Data JPA, Security, Validation) |
| Database | PostgreSQL 16 with Flyway migrations |
| Auth | Spring Security, stateless JWT (jjwt) and BCrypt |
| API docs | springdoc OpenAPI and Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers and JaCoCo |
| Build and run | Maven, Docker, Docker Compose and GitHub Actions |

## Running it

All you need is Docker. The Docker build compiles the app itself, so you don't need Java or Maven installed for this.

1. Clone the repo:
   ```bash
   git clone https://github.com/Hus-9/finance-tracker-api.git
   cd finance-tracker-api
   ```
   
2. Copy `.env.example` to a new file called `.env` and fill in the two values:
    - `DB_PASSWORD` is any password you like for the local database.
    - `JWT_SECRET` has to be base64 and at least 32 bytes once decoded. You can make one with `openssl rand -base64 32`, or in PowerShell with:

     ```powershell
     $b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
     ```

3. Start the database and the API:

   ```bash
   docker compose up --build
   ```

4. Open http://localhost:8080/swagger-ui.html

**Running it from an IDE instead:** start just the database with `docker compose up -d db`, then run `FinanceTrackerApplication`. The app reads the same `.env` file. You'll need Java 21 and Maven for this.

## Trying it out

1. `POST /api/auth/register` with an email and a password of at least 8 characters.
2. `POST /api/auth/login` with the same details and copy the `accessToken` from the response.
3. Click **Authorize** at the top of Swagger and paste the token. Every other endpoint now runs as you.
4. Create an account, add some transactions, set a budget and look at the reports.

## Endpoints

Everything apart from register and login needs `Authorization: Bearer` header with token.

| Method | Path | What it does |
|---|---|---|
| POST | `/api/auth/register` | Create a user|
| POST | `/api/auth/login` | Log in and get a token |
| GET | `/api/users/me` | The logged in user |
| GET, POST | `/api/accounts` | List or create accounts |
| GET, PUT, DELETE | `/api/accounts/{id}` | Get, update or delete an account (deleting it removes its transactions too) |
| GET, POST | `/api/categories` | List or create categories |
| PUT, DELETE | `/api/categories/{id}` | Update or delete a category (not allowed while transactions use it) |
| GET | `/api/transactions` | List transactions, filtered by `from`, `to`, `categoryId` and `type`, paged with `page` and `size` (up to 100) |
| POST | `/api/transactions` | Add a transaction |
| GET, PUT, DELETE | `/api/transactions/{id}` | Get, update or delete a transaction | 
| GET, POST | `/api/budgets` | List budgets (optionally for one `month`) or create one |
| GET | `/api/reports/monthly-summary?month=2026-10` | Income, spending, net and spending per category for a month |
| GET | `/api/reports/budget-status?month=2026-10` | How much of each budget is spent and what's left |

## Design decisions

- **You can only ever see your own data.** Every lookup includes the logged in user's id, so asking for someone else's account, transactions or category returns **404**, not 403. a 403 would confirm it exists. There's a whole integration test class that tries to get at another user's data.
- **Money is never a double.** Amounts are `BigDecimal` in Java and `NUMERIC(12,2)` in PostgreSQL, so 0.10 + 0.20 is exactly 0.30. There's a test for that too.
- **Only DTOs go in and out.** Entities never leave the service layer, so things like the password hash can't end up in a response by accident.
- **Stateless auth.** The server keeps no sessions. Every request carries its own signed token, which runs out after 60 minutes.
- **The database schema is versioned.** Flyway builds the tables from numbered SQL migrations and Hibernate is set to `validate`, so the app won't start if the code and the tables don't match.
- **Bad data gets stopped twice.** Jakarta Validation rejects bad input with a 400 and the database has its own rules (unique category names, positive amounts, foreign keys) as a backup.
- **Every error looks the same.** A global exception handler makes sure every error, including a missing token or a URL that doesn't exist, comes back in one format:

```json
{
  "timestamp": "2026-10-28T12:00:00Z",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/accounts",
  "errors": [
    "currency: must be 3 capital letters, e.g. GBP",
    "name: must not be blank"
    ]
}
```

- **No secrets in the code.** The database password and the JWT key come from environment variables or a `.env` file that git ignores.

## Tests

18 tests with 93% code coverage, measured by JaCoCo.

- **Unit tests** use Mockito to check  the service logic on its own: adding transactions, the report maths  and signing up.
- **Integration tests** use Testcontainers to start the whole app against a real PostgreSQL in Docker, then go through the API with MockMvc like a real client would. They cover a full flow from sign up to reports, exact money maths, the error format and every way one user might try to reach another user's data.

Run them with Docker running:

```bash
mvn verify
```

The coverage report ends up in `target/site/jacoco/index.html`. GitHub Actions runs the same command on every push.

## Project structure

```
src/main/java/com/husnain/financetracker
├── config        Swagger and password encoder setup
├── controller    REST endpoints
├── dto           Request and response records
├── exception     Custom exceptions and the global error handler
├── model         JPA entities
├── repository    Spring Data repositories and transaction filters
├── security      JWT filter, token service and user lookup
└── service       Business logic
src/main/resources/db/migration      Flyway SQL migrations, V1 to V5
```

## What I'd add next

- Importing transactions from a bank statement CSV
- Editing and deleting budgets
- Refresh tokens so you don't have to log in again every hour