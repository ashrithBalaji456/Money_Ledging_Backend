# 📘 Money Lending Management System — Backend Deep-Dive & Line-by-Line Code Explanation

Welcome to the comprehensive, zero-knowledge architectural and line-by-line explanation of the **Money Lending Management System Backend**.

Whether you are completely new to programming, Java, or the Spring Boot ecosystem, this document is crafted to explain **every single concept, architectural design pattern, workflow, class, method, annotation, and configuration setting** in plain, simple English with technical precision.

---

## 📑 Table of Contents

1. [Zero-Knowledge Primer: Core Concepts Explained](#1-zero-knowledge-primer-core-concepts-explained)
2. [End-to-End Workflow & Architecture](#2-end-to-end-workflow--architecture)
3. [Master Annotation Encyclopedia](#3-master-annotation-encyclopedia)
4. [Build & Environment Configuration](#4-build--environment-configuration)
   - [pom.xml (Dependencies & Build Specs)](#pomxml)
   - [application.properties (System Configuration)](#applicationproperties)
   - [Dockerfile & .dockerignore (Containerization)](#dockerfile---dockerignore)
5. [Application Bootstrap & Seeding](#5-application-bootstrap--seeding)
   - [LendingApplication.java](#lendingapplicationjava)
   - [AdminUserSeeder.java](#adminuserseederjava)
   - [OpenApiConfig.java](#openapiconfigjava)
6. [Security & Authentication Module](#6-security--authentication-module)
   - [SecurityConfig.java](#securityconfigjava)
   - [JwtTokenProvider.java](#jwttokenproviderjava)
   - [JwtAuthenticationFilter.java](#jwtauthenticationfilterjava)
   - [CustomUserDetailsService.java](#customuserdetailsservicejava)
7. [Domain Entities & Database Enums](#7-domain-entities--database-enums)
   - [Enums (InterestType, InterestRateType, LoanStatus, PaymentStatus)](#enums)
   - [User.java](#userjava)
   - [Borrower.java](#borrowerjava)
   - [Loan.java](#loanjava)
   - [Payment.java](#paymentjava)
8. [Data Persistence Layer (Repositories)](#8-data-persistence-layer-repositories)
   - [UserRepository.java](#userrepositoryjava)
   - [BorrowerRepository.java](#borrowerrepositoryjava)
   - [LoanRepository.java](#loanrepositoryjava)
   - [PaymentRepository.java](#paymentrepositoryjava)
9. [Data Transfer Objects (DTOs) & Request Payloads](#9-data-transfer-objects-dtos--request-payloads)
   - [Auth DTOs: LoginRequest, LoginResponse, SignupRequest](#auth-dtos)
   - [BorrowerDTO.java](#borrowerdtojava)
   - [Loan DTOs: LoanCreationRequest, LoanDTO, LoanPreviewResponse](#loan-dtos)
   - [Payment DTOs: PaymentRecordRequest, PaymentDTO](#payment-dtos)
   - [DashboardSummaryDTO.java](#dashboardsummarydtojava)
10. [Business Logic & Mathematical Engine (Services)](#10-business-logic--mathematical-engine-services)
    - [InterestCalculationService.java (Math Engine)](#interestcalculationservicejava)
    - [BorrowerService.java](#borrowerservicejava)
    - [LoanService.java](#loanservicejava)
    - [PaymentService.java](#paymentservicejava)
    - [DashboardService.java](#dashboardservicejava)
11. [Presentation Layer (REST Controllers)](#11-presentation-layer-rest-controllers)
    - [AuthController.java](#authcontrollerjava)
    - [BorrowerController.java](#borrowercontrollerjava)
    - [LoanController.java](#loancontrollerjava)
    - [PaymentController.java](#paymentcontrollerjava)
    - [DashboardController.java](#dashboardcontrollerjava)
    - [HealthController.java](#healthcontrollerjava)
12. [Exception Handling & Resilience](#12-exception-handling--resilience)
    - [GlobalExceptionHandler.java](#globalexceptionhandlerjava)
    - [Custom Exceptions](#custom-exceptions)
13. [Automated Testing Suite](#13-automated-testing-suite)
    - [LendingServiceTests.java](#lendingservicetestsjava)

---

## 1. Zero-Knowledge Primer: Core Concepts Explained

If you have never touched backend web development before, the terminology can feel overwhelming. Let us demystify the core building blocks first.

### What is a Backend?
In computer software, applications are typically divided into two sides:
* **The Frontend (Client):** The visual interface you see in your browser (built with React, HTML, CSS). Buttons, cards, and forms.
* **The Backend (Server):** The "brain" and memory of the system. It runs on a computer/server, listens for requests, verifies passwords, calculates interest rates, saves records permanently into a database, and returns data securely.

### What is a REST API?
* **API (Application Programming Interface):** A standardized way for the frontend and backend to talk to each other.
* **REST (Representational State Transfer):** A popular architectural style using standard HTTP requests:
  * `GET`: "Give me data" (e.g., fetch list of borrowers).
  * `POST`: "Create new data" (e.g., issue a new loan).
  * `PUT`: "Update existing data" (e.g., edit borrower phone number).
  * `DELETE`: "Remove data" (e.g., delete a borrower).
* **JSON (JavaScript Object Notation):** The text format used to package data sent back and forth, formatted in key-value pairs (like `{"name": "John", "amount": 5000}`).

### What is Spring Boot?
Java is an object-oriented programming language. **Spring Boot** is a world-class framework built on top of Java that gives you pre-built tools (web servers, database connectors, security engines) so you don't have to code a web server from scratch.
Key concepts inside Spring Boot:
* **Inversion of Control (IoC) & Dependency Injection (DI):** Instead of you manually creating objects using `new Service()`, Spring creates, configures, and injects them wherever needed automatically. These managed objects are called **Beans**.
* **Layered Architecture:** Code is divided into distinct layers (Controller $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Database) so each part does only one job.

### What is an ORM & JPA/Hibernate?
* **Database (PostgreSQL):** A relational database organizing tables with rows and columns.
* **Entities:** Java classes that represent rows in database tables.
* **JPA (Java Persistence API) & Hibernate:** The ORM (Object-Relational Mapping) framework that automatically translates Java objects into SQL queries and vice versa. You never have to manually write `INSERT INTO ...` or `SELECT * FROM ...` for standard operations.

### What is JWT (JSON Web Token) Authentication?
In modern stateless web apps:
1. The user logs in with their username and password.
2. The server verifies the credentials and creates a digitally signed cryptographic string called a **JWT Token**.
3. The server sends this token back to the frontend.
4. For every future request, the frontend sends this token in the `Authorization: Bearer <token>` header.
5. The backend validates the signature. If valid, it immediately knows who the user is without querying the database for every single click.

---

## 2. End-to-End Workflow & Architecture

```
[ Frontend (React / Web App) ]
           │
           │ HTTP Request + Authorization: Bearer <JWT>
           ▼
┌─────────────────────────────────────────────────────────────┐
│                   SPRING BOOT BACKEND                       │
│                                                             │
│ 1. [ JwtAuthenticationFilter ]                              │
│    Inspects HTTP header, validates JWT cryptographic key,   │
│    extracts username, and populates SecurityContext.        │
│                                                             │
│ 2. [ SecurityConfig ]                                       │
│    Verifies permissions. Allows public routes or enforces    │
│    authentication for private endpoints.                     │
│                                                             │
│ 3. [ REST Controller ]                                      │
│    Validates input format (@Valid), parses URL parameters,  │
│    and forwards request to the Service layer.               │
│                                                             │
│ 4. [ Service Layer ]                                        │
│    Executes core business logic: interest math formulas,    │
│    multi-tenant user isolation, balances, and schedules.    │
│                                                             │
│ 5. [ Repository Layer (Spring Data JPA) ]                   │
│    Translates entity method calls into optimized SQL.       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               │ SQL Query / Transaction
                               ▼
                   ┌────────────────────────┐
                   │ PostgreSQL Database    │
                   │ (Tables: users, loans, │
                   │  borrowers, payments)  │
                   └────────────────────────┘
```

### Complete Lifecycle of a Loan Creation Request:
1. **Frontend sends POST `/api/loans`** with details: borrower ID, principal ₹100,000, interest rate 12%, duration 12 months.
2. **`JwtAuthenticationFilter`** reads the Bearer token, decodes it with the secret key, extracts the username, and places the user in the security context.
3. **`LoanController.createLoan(...)`** receives the request payload, verifies that numbers are not negative via validation annotations, and calls `LoanService`.
4. **`LoanService`**:
   - Queries `BorrowerRepository` to verify the borrower belongs to the currently logged-in user (ensuring data privacy).
   - Passes figures into `InterestCalculationService` to compute total interest, total payable amount, and monthly installments.
   - Saves the new `Loan` record to PostgreSQL.
   - Calculates the exact payment schedule (12 monthly installments with dueDate and expectedAmount) and saves them in batch via `PaymentRepository`.
   - Links the first installment date as `nextPaymentDate` on the loan.
5. **Response Return:** The newly generated Loan is mapped into a `LoanDTO` and sent back to the frontend with HTTP Status `201 Created`.

---

## 3. Master Annotation Encyclopedia

Here is a comprehensive glossary explaining every single annotation used across the codebase:

| Annotation | Where It Appears | Plain-English Explanation |
|---|---|---|
| `@SpringBootApplication` | `LendingApplication` | The master bootstrap annotation. Enables component auto-scanning, configuration discovery, and Spring Boot auto-configuration. |
| `@Configuration` | Config classes | Tells Spring that this class contains configuration recipes and Bean factory methods. |
| `@Bean` | Config methods | Tells Spring: "Execute this method, take the returned object, and manage it as a reusable component in the ApplicationContext." |
| `@Component` | Helpers, Seeders, Filters | Marks a general class as a Spring-managed Bean so it can be automatically discovered and injected. |
| `@Service` | Service classes | Specialization of `@Component` indicating that this class contains core business logic, calculations, and rules. |
| `@Repository` | Repository interfaces | Specialization of `@Component` indicating database access. Enables automatic exception translation into Spring data access exceptions. |
| `@RestController` | Controllers | Combines `@Controller` and `@ResponseBody`. Tells Spring this class handles HTTP endpoints and returns data serialized directly as JSON. |
| `@RequestMapping` | Controllers | Defines the base URL path prefix (e.g., `/api/borrowers`) for all endpoints inside the controller. |
| `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Controller methods | Binds an HTTP method (`GET`, `POST`, `PUT`, `DELETE`) to a specific Java method. |
| `@PathVariable` | Controller method parameters | Extracts variables directly from the URL path (e.g., the `id` from `/api/loans/{id}`). |
| `@RequestParam` | Controller method parameters | Extracts query string parameters from the URL (e.g., `?query=ramesh` in `/api/borrowers?query=ramesh`). |
| `@RequestBody` | Controller method parameters | Instructs Spring to read the JSON payload from the incoming HTTP request body and convert it into a Java object. |
| `@Valid` | Controller method parameters | Triggers automatic validation on request objects before the method body runs. If validation fails, Spring throws `MethodArgumentNotValidException`. |
| `@Entity` | Domain models | Marks a Java class as an ORM database entity that maps to a database table. |
| `@Table(name = "...")` | Domain models | Specifies the exact name of the table in the relational database. |
| `@Id` | Domain models | Declares the primary key field of the database entity. |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | Primary key fields | Tells PostgreSQL to auto-increment the ID number on every new record inserted. |
| `@Column(...)` | Entity fields | Customizes database column properties (e.g., `nullable = false`, column name, precision, scale). |
| `@Enumerated(EnumType.STRING)` | Entity Enum fields | Stores enum values in the database as text strings (e.g., `"ACTIVE"`) rather than fragile numeric integers (`0`, `1`). |
| `@ManyToOne(...)` | Entity relationship fields | Defines a foreign-key relationship where many child records (e.g., Loans) point to one parent record (e.g., Borrower). |
| `@JoinColumn(name = "...")` | Entity foreign-key fields | Specifies the exact name of the foreign key column in the database (e.g., `borrower_id`). |
| `@PrePersist` | Entity lifecycle methods | A callback method automatically triggered right before an entity is first saved to the database. |
| `@PreUpdate` | Entity lifecycle methods | A callback method automatically triggered right before an existing entity is updated in the database. |
| `@Transactional` | Services & Service methods | Encloses execution in an atomic database transaction. If an error occurs, changes are rolled back automatically. If `readOnly = true`, optimizes read performance. |
| `@Data` | DTOs | Lombok shortcut that auto-generates getters, setters, `equals()`, `hashCode()`, and `toString()`. |
| `@Getter`, `@Setter` | Entities | Lombok annotations that auto-generate getter and setter methods without cluttering the code. |
| `@NoArgsConstructor`, `@AllArgsConstructor` | Entities & DTOs | Lombok annotations generating empty default constructors and constructors accepting all class fields. |
| `@Builder` | Entities & DTOs | Implements the clean Builder design pattern (e.g., `User.builder().username("...").build()`). |
| `@RequiredArgsConstructor` | Classes with dependencies | Lombok generates a constructor for all `final` fields, enabling clean constructor-based dependency injection. |
| `@Slf4j` | Classes needing logs | Lombok injects a logger instance named `log` for logging messages (`log.info()`, `log.warn()`). |
| `@Value("${...}")` | Fields in Beans | Injects configuration values from `application.properties` or environment variables into Java fields. |
| `@Query(...)` | Repository methods | Allows defining custom JPQL (Java Persistence Query Language) queries. |
| `@Param("...")` | Repository method parameters | Binds a Java method argument to a named parameter inside a `@Query`. |
| `@RestControllerAdvice` | `GlobalExceptionHandler` | Intercepts exceptions thrown across any controller in the application and transforms them into standardized JSON error responses. |
| `@ExceptionHandler(...)` | Exception handler methods | Specifies which Exception class a method is designed to catch and handle. |
| `@NotBlank`, `@NotNull`, `@Min`, `@DecimalMin`, `@Pattern`, `@Size` | DTO fields | Validation constraints that enforce data integrity before any business logic executes. |

---

## 4. Build & Environment Configuration

### pom.xml
The `pom.xml` (Project Object Model) is the blueprint for Apache Maven. It lists project dependencies, plugins, and Java versions.

* **Lines 1–4 (`<project ...>`):** Standard XML declarations defining the Maven 4.0.0 schema.
* **Lines 5–10 (`<parent>`):** Inherits from `spring-boot-starter-parent` version `3.3.2`. This provides sensible defaults, dependency management, and compatible plugin configurations.
* **Lines 11–15 (`<groupId>`, `<artifactId>`, `<version>`, `<name>`, `<description>`):** Project coordinates uniquely identifying this artifact as `com.example:lending:0.0.1-SNAPSHOT`.
* **Lines 17–21 (`<properties>`):** Global variables:
  * `<java.version>17</java.version>`: Requires modern Java 17 LTS.
  * `<jjwt.version>0.11.5</jjwt.version>`: Sets the version for the JJWT library.
  * `<springdoc.version>2.5.0</springdoc.version>`: Sets the version for OpenAPI/Swagger UI generation.
* **Lines 23–93 (`<dependencies>`):**
  * `spring-boot-starter-data-jpa`: Pulls in Hibernate, Spring Data JPA, and HikariCP connection pooling to talk to relational databases.
  * `spring-boot-starter-web`: Pulls in Spring MVC, REST controllers, and an embedded Apache Tomcat web server.
  * `spring-boot-starter-validation`: Pulls in the Jakarta Bean Validation API and Hibernate Validator to inspect fields with `@NotNull`, `@NotBlank`, etc.
  * `spring-boot-starter-security`: Activates Spring Security's authentication, authorization, and filter chain.
  * `postgresql`: The official JDBC driver enabling Java to communicate with PostgreSQL. Marked `<scope>runtime</scope>` because it is only needed when executing queries.
  * `lombok`: Compile-time code generator that automatically writes boilerplate getters, setters, builders, and constructors. Marked `<optional>true</optional>`.
  * `jjwt-api`, `jjwt-impl`, `jjwt-jackson`: The Java JWT library for creating, signing, parsing, and verifying JSON Web Tokens with JSON serialization.
  * `springdoc-openapi-starter-webmvc-ui`: Automatically inspects all controllers and generates interactive Swagger documentation accessible at `/swagger-ui/index.html`.
  * `spring-boot-starter-test` & `spring-security-test`: Provides JUnit 5, Mockito, and AssertJ test harnesses.
* **Lines 95–110 (`<build><plugins>`):** Packages the application into an executable, self-contained "Fat JAR" containing all required libraries and the embedded Tomcat server. Excludes Lombok from the final JAR to keep it lean.

---

### application.properties
Located at `src/main/resources/application.properties`, this file configures the runtime environment:

* **Line 1 (`spring.application.name=money-lending-app`):** Names the application in system logs and metrics.
* **Line 2 (`server.port=8080`):** Defines the TCP port the embedded web server listens on.
* **Lines 4–8 (Database Configuration):**
  * `spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/Money_Lending}`: Points to the PostgreSQL database. Uses `${ENV_VAR:default_value}` syntax—if the `DB_URL` environment variable exists (e.g., in Docker/production), it uses that; otherwise, it falls back to `localhost:5432/Money_Lending`.
  * `spring.datasource.username=${DB_USERNAME:postgres}`: The database login username.
  * `spring.datasource.password=${DB_PASSWORD:1234}`: The database login password.
  * `spring.datasource.driver-class-name=org.postgresql.Driver`: Specifies the PostgreSQL JDBC driver.
* **Lines 10–14 (JPA/Hibernate Settings):**
  * `spring.jpa.hibernate.ddl-auto=update`: Automatically updates the database schema to match Java entity classes without dropping existing data.
  * `spring.jpa.show-sql=true`: Prints generated SQL queries in the terminal for debugging.
  * `spring.jpa.properties.hibernate.format_sql=true`: Formats SQL queries with indents so they are human-readable in logs.
  * `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect`: Configures SQL generation optimized for PostgreSQL.
* **Lines 16–18 (JWT Configuration):**
  * `jwt.secret=${JWT_SECRET:secret123...}`: Secret cryptographic key used to sign and verify JWT tokens.
  * `jwt.expiration=86400000`: Token validity in milliseconds ($86,400,000 \text{ ms} = 24 \text{ hours}$).
* **Lines 20–21 (CORS Configuration):**
  * `cors.allowed-origins=${ALLOWED_ORIGINS:...}`: Specifies which web domains are permitted to call the API, preventing unauthorized cross-origin browser attacks while allowing localhost and Vercel deployments.
* **Lines 23–25 (Logging Levels):**
  * Sets Spring Web logging to `INFO` and internal application logging (`com.example.lending`) to `DEBUG` for fine-grained observability.

---

### Dockerfile & .dockerignore
Enables containerizing the application so it can run identically on any machine or cloud provider without installing Java or Maven manually.

#### Dockerfile Breakdown:
* **Stage 1 (Build Stage, Lines 1–10):**
  * `FROM maven:3.8.5-openjdk-17-slim AS build`: Uses a lightweight image with Maven and OpenJDK 17.
  * `WORKDIR /app`: Creates `/app` as the working directory.
  * `COPY pom.xml .`: Copies dependency definitions.
  * `RUN mvn dependency:go-offline -B`: Pre-downloads all dependencies so Docker can cache them.
  * `COPY src ./src`: Copies the source code.
  * `RUN mvn clean package -DskipTests -B`: Compiles and packages the application into `target/lending-0.0.1-SNAPSHOT.jar`.
* **Stage 2 (Runtime Stage, Lines 13–23):**
  * `FROM eclipse-temurin:17-jdk-jammy`: Starts a fresh, lean production image containing only the Java Runtime Environment (no compiler or source files, keeping the image small and secure).
  * `WORKDIR /app`: Sets working directory.
  * `COPY --from=build /app/target/lending-0.0.1-SNAPSHOT.jar app.jar`: Copies only the compiled JAR from Stage 1.
  * `EXPOSE 8080`: Documents that container traffic runs on port 8080.
  * `ENTRYPOINT ["java", "-jar", "app.jar"]`: Launches the application when the container starts.

#### .dockerignore:
Prevents local clutter (`target`, `.git`, `.idea`, `*.md`) from being copied into the Docker build context.

---

## 5. Application Bootstrap & Seeding

### LendingApplication.java
```java
package com.example.lending;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LendingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LendingApplication.class, args);
    }
}
```
* **Line 1:** Declares the root package. Everything inside this package or its sub-packages will be automatically scanned by Spring.
* **Lines 3–4:** Imports Spring Boot bootstrap classes.
* **Line 6 (`@SpringBootApplication`):** Tells Spring Boot to turn on component scanning, autoconfiguration, and property support.
* **Line 7:** Defines the main entry class.
* **Line 8 (`main` method):** Standard Java entry point.
* **Line 9 (`SpringApplication.run(...)`):** Starts the embedded web server, creates the Spring ApplicationContext, builds all Beans, and establishes database connections.

---

### AdminUserSeeder.java
Implements `CommandLineRunner`, meaning its `run()` method executes automatically **once** immediately after the server starts up.

* **Lines 12–15:** Marked with `@Component`, `@RequiredArgsConstructor`, `@Slf4j`, and implements `CommandLineRunner`.
* **Lines 17–19:** Dependencies:
  * `UserRepository`: To check and save user accounts.
  * `PasswordEncoder`: To hash passwords securely with BCrypt.
  * `JdbcTemplate`: Direct JDBC access to execute raw SQL statements when needed.
* **Lines 24–29:** Fixes a database constraint issue: Drops any obsolete Hibernate check constraints (`loans_interest_type_check`) that could prevent new enum types from saving properly.
* **Lines 31–41:** Checks `userRepository.findByUsername("admin")`. If absent, it builds an admin account with username `"admin"` and password `"admin123"` (hashed via BCrypt) and saves it to the database.
* **Lines 43–52:** Legacy data migration: Finds any borrower records that have a `null` `user_id` and attaches them to the default admin user so that existing data remains accessible under multi-tenant security rules.

---

### OpenApiConfig.java
Configures Swagger UI documentation.
* **Lines 11–12:** `@Configuration` class named `OpenApiConfig`.
* **Lines 14–29 (`customOpenAPI()` method):** Returns an `OpenAPI` Bean.
  * Sets API title, version, and description.
  * Configures a security scheme named `bearerAuth` of type `HTTP` with scheme `bearer` and format `JWT`.
  * This adds the **Authorize** lock button in the Swagger UI, allowing developers to paste their JWT token and test protected endpoints directly from the browser.

---

## 6. Security & Authentication Module

### SecurityConfig.java
Defines the security perimeter of the entire backend.

* **Lines 27–30:** Marked `@Configuration`, `@EnableWebSecurity`, `@RequiredArgsConstructor`.
* **Lines 32–36:** Injects `JwtAuthenticationFilter`, `CustomUserDetailsService`, and reads `cors.allowed-origins`.
* **Lines 38–53 (`securityFilterChain`):** Configures HTTP request handling:
  * `.cors(...)`: Enables Cross-Origin Resource Sharing using our custom configuration.
  * `.csrf(AbstractHttpConfigurer::disable)`: Disables CSRF (Cross-Site Request Forgery) protection because JWTs in HTTP headers are immune to browser CSRF cookie exploits.
  * `.authorizeHttpRequests(...)`:
    * Public (`permitAll`): `/api/auth/login`, `/api/auth/register`, `/health`, and all `/swagger-ui/**` routes.
    * Private (`authenticated`): `.anyRequest().authenticated()` requires all other endpoints to supply a valid JWT token.
  * `.sessionManagement(STATELESS)`: Tells Spring Security never to create an HTTP session cookie. Every request must be authenticated independently via token.
  * `.authenticationProvider(...)`: Registers our database authentication provider.
  * `.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)`: Inserts our custom JWT filter ahead of Spring's default login filter.
* **Lines 55–61 (`authenticationProvider`):** Configures a `DaoAuthenticationProvider` pairing our `CustomUserDetailsService` with our `BCryptPasswordEncoder`.
* **Lines 63–66 (`authenticationManager`):** Exposes Spring's `AuthenticationManager` as a Bean so `AuthController` can authenticate login attempts.
* **Lines 68–71 (`passwordEncoder`):** Returns a `BCryptPasswordEncoder` Bean that securely hashes passwords using a salted one-way hashing algorithm.
* **Lines 73–85 (`corsConfigurationSource`):** Parses allowed origins from `application.properties`, permits `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, allows standard authorization headers, and enables credentials.

---

### JwtTokenProvider.java
Generates, parses, and validates cryptographic JSON Web Tokens.

* **Lines 18–30:** Constructor reads `jwt.secret` and `jwt.expiration` from configuration. Converts the secret string into an HMAC SHA-256 cryptographic `signingKey`.
* **Lines 32–35 (`generateToken`):** Creates an empty claims map and calls `createToken(claims, username)`.
* **Lines 37–45 (`createToken`):**
  * Uses `Jwts.builder()`:
  * `.setClaims(claims)`: Custom payload data.
  * `.setSubject(username)`: Identifies token owner.
  * `.setIssuedAt(...)`: Sets current timestamp.
  * `.setExpiration(...)`: Sets future expiry date (current time + 24 hours).
  * `.signWith(signingKey, SignatureAlgorithm.HS256)`: Digitally signs the token using HMAC SHA-256.
  * `.compact()`: Serializes everything into the standard three-part string `header.payload.signature`.
* **Lines 47–50 (`validateToken`):** Confirms that the username extracted from the token matches the user database record and that the token is not expired.
* **Lines 52–75:** Helper methods:
  * `extractUsername`: Reads the token subject.
  * `extractExpiration`: Reads token expiration date.
  * `extractAllClaims`: Decodes the token using the secret key and extracts all claims.
  * `isTokenExpired`: Checks if expiration date is in the past.

---

### JwtAuthenticationFilter.java
A servlet filter extending `OncePerRequestFilter`, guaranteeing it runs exactly once per incoming HTTP request.

* **Lines 26–31:** Overrides `doFilterInternal(request, response, filterChain)`.
* **Lines 33–40:** Reads the `Authorization` header:
  * If the header is missing or does not start with `"Bearer "`, it bypasses JWT authentication and passes the request along the filter chain (public routes will succeed; protected routes will be blocked later by Spring Security).
* **Lines 42–44:** Strips the `"Bearer "` prefix (first 7 characters) to get the raw token string, and extracts the username via `jwtTokenProvider.extractUsername(jwt)`.
* **Lines 46–58:**
  * If a valid username is present and the request is not already authenticated (`SecurityContextHolder.getContext().getAuthentication() == null`):
  * Loads user details via `userDetailsService.loadUserByUsername(username)`.
  * Validates the token against the user details.
  * If valid, builds a `UsernamePasswordAuthenticationToken` containing the user details, credentials (`null`), and authorities (roles).
  * Attaches HTTP request details and stores the authentication token in the `SecurityContextHolder`.
  * From this moment onward, Spring Security treats the request as fully authenticated.
* **Lines 59–63:** Gracefully catches any token decoding errors (e.g., malformed or expired token) and logs them without crashing the server.
* **Line 65:** Calls `filterChain.doFilter(request, response)` to pass the request to the next filter or controller.

---

### CustomUserDetailsService.java
Implements Spring Security's `UserDetailsService` interface.
* **Lines 22–30 (`loadUserByUsername`):**
  * Queries `UserRepository.findByUsername(username)`.
  * If not found, throws `UsernameNotFoundException`.
  * Returns a standard Spring Security `User` object containing:
    1. The username.
    2. The BCrypt-hashed password.
    3. The granted authority list created as `List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))`.

---

## 7. Domain Entities & Database Enums

### Enums
Enums represent fixed sets of allowed options.

#### InterestType.java
* `SIMPLE_INTEREST`: Standard simple interest calculation ($P \times R \times T$).
* `FIXED_INTEREST`: A flat, custom interest amount manually entered by the lender.
* `INTEREST_ONLY`: The borrower pays only interest each month, with the principal due at the end.

#### InterestRateType.java
* `ANNUAL`: The interest rate is expressed per year (divided by 12 for monthly intervals).
* `MONTHLY`: The interest rate is expressed per month directly.

#### LoanStatus.java
* `ACTIVE`: Loan is currently running and payments are being made.
* `COMPLETED`: The loan has been completely paid off in full.
* `OVERDUE`: At least one installment payment has passed its due date without being paid.
* `CANCELLED`: The loan was cancelled or voided.

#### PaymentStatus.java
* `UPCOMING`: Installment is scheduled for the future.
* `DUE_SOON`: Installment is due today or within the next 3 days.
* `PAID`: Installment has been paid in full.
* `PARTIALLY_PAID`: A partial amount has been paid, but some balance remains.
* `OVERDUE`: The due date has passed without full payment.

---

### User.java
Represents an application user/lender account in the `users` table.

* **Lines 8–15:** Annotated with `@Entity`, `@Table(name = "users")`, Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
* **Lines 17–19 (`id`):** Primary Key, `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
* **Lines 21–22 (`username`):** `@Column(nullable = false, unique = true)` — ensures no two users can register with the same username.
* **Lines 24–25 (`password`):** `@Column(nullable = false)` — stores the BCrypt hash.
* **Lines 27–28 (`role`):** Stores user permission role (e.g., `"ADMIN"`).
* **Lines 30–34 (`createdAt`, `updatedAt`):** Audit timestamps.
* **Lines 36–45 (`@PrePersist`, `@PreUpdate`):** Automatically initializes `createdAt` and `updatedAt` to `LocalDateTime.now()` before saving or updating.

---

### Borrower.java
Represents a customer who borrows money in the `borrowers` table.

* **Lines 17–19 (`id`):** Primary Key.
* **Lines 21–25 (`fullName`, `phoneNumber`):** Required borrower identity fields.
* **Lines 27–34 (`address`, `aadharLastFourDigits`, `notes`):** Optional background and identification fields.
* **Lines 36–38 (`user`):**
  * `@ManyToOne(fetch = FetchType.LAZY)`
  * `@JoinColumn(name = "user_id")`
  * Links this borrower directly to the user/lender who created them. This is the foundation of **multi-tenancy**—one lender cannot view or alter another lender's borrowers.
* **Lines 40–41 (`active`):** Soft delete flag (defaults to `true`). When deleted, `active` becomes `false` so historical financial records are preserved.
* **Lines 49–59:** Lifecycle methods setting timestamps and ensuring `active = true` on creation.

---

### Loan.java
Represents a loan contract in the `loans` table.

* **Lines 23–25 (`borrower`):** `@ManyToOne(fetch = FetchType.LAZY, optional = false)` pointing to the borrower.
* **Lines 27–32 (`principalAmount`, `interestRate`):** Monetary values using `BigDecimal` with `precision = 15, scale = 2` (up to 15 digits total, exactly 2 decimal places to avoid floating-point rounding errors).
* **Lines 33–40 (`interestType`, `interestRateType`):** Stored as strings in the database via `@Enumerated(EnumType.STRING)`.
* **Lines 41–45 (`loanStartDate`, `durationInMonths`):** When the loan begins and how many months it lasts.
* **Lines 47–55 (`totalInterest`, `totalPayableAmount`, `monthlyInstallment`):** Precomputed totals.
* **Lines 56–60 (`remainingPrincipal`, `remainingAmount`):** Running balances that decrement as payments are recorded.
* **Lines 62–63 (`nextPaymentDate`):** Tracks when the next upcoming installment is due.
* **Lines 65–67 (`status`):** `ACTIVE`, `COMPLETED`, `OVERDUE`, or `CANCELLED`.
* **Lines 78–90:** Lifecycle hooks initializing audit timestamps and defaulting `status = ACTIVE`.

---

### Payment.java
Represents an individual installment payment in the `payments` table.

* **Lines 23–25 (`loan`):** Foreign key referencing the parent `Loan`.
* **Lines 27–34 (`installmentNumber`, `dueDate`, `expectedAmount`):** Which installment this is (e.g., month 1 of 12), when it is due, and the expected amount.
* **Lines 36–40 (`paidAmount`, `paidDate`):** How much has been paid so far and on what date.
* **Lines 42–44 (`status`):** Installment status enum.
* **Lines 75–90 (`getDerivedStatus(LocalDate date)`):**
  Dynamically calculates the true real-time status of the installment against any reference date (typically today):
  1. If `paidAmount >= expectedAmount`: Returns `PaymentStatus.PAID`.
  2. If `paidAmount > 0`: Returns `PaymentStatus.PARTIALLY_PAID`.
  3. If `today > dueDate`: Returns `PaymentStatus.OVERDUE`.
  4. If `dueDate` is within the next 3 days: Returns `PaymentStatus.DUE_SOON`.
  5. Otherwise: Returns `PaymentStatus.UPCOMING`.

---

## 8. Data Persistence Layer (Repositories)

Spring Data JPA generates real SQL implementations of these interfaces at runtime automatically.

### UserRepository.java
* Extends `JpaRepository<User, Long>`.
* `Optional<User> findByUsername(String username)`: Spring Data derives the query `SELECT * FROM users WHERE username = ?` automatically based on the method name.

---

### BorrowerRepository.java
* Extends `JpaRepository<Borrower, Long>`.
* `List<Borrower> findByActiveTrueAndUserId(Long userId)`: Finds all active borrowers belonging to a specific user.
* `@Query(...) List<Borrower> searchActiveBorrowers(...)`: Custom JPQL search query:
  ```sql
  SELECT b FROM Borrower b 
  WHERE b.active = true 
    AND b.user.id = :userId 
    AND (LOWER(b.fullName) LIKE LOWER(CONCAT('%', :query, '%')) 
         OR b.phoneNumber LIKE CONCAT('%', :query, '%'))
  ```
  Searches borrowers by case-insensitive name substring or phone number match, restricted to the logged-in user.

---

### LoanRepository.java
* Extends `JpaRepository<Loan, Long>`.
* `@Query("SELECT l FROM Loan l JOIN FETCH l.borrower b WHERE b.active = true AND b.user.id = :userId")`:
  Uses `JOIN FETCH` to load both the Loan and its related Borrower in a single SQL query, completely eliminating the common "N+1 query performance problem."
* `findByBorrowerId(Long borrowerId)`: Returns all loans for a given borrower.
* `findByBorrowerIdAndStatus(...)`: Returns loans filtered by borrower and status.
* `countByStatus(LoanStatus status)`: Returns total count of loans with a given status.

---

### PaymentRepository.java
* Extends `JpaRepository<Payment, Long>`.
* `findByLoanIdOrderByInstallmentNumberAsc(Long loanId)`: Fetches the entire repayment schedule for a loan ordered by installment number (1, 2, 3...).
* `@Query(...) findOverduePayments(...)`:
  Fetches all unpaid installments (`paidAmount < expectedAmount`) whose `dueDate < :date`, sorted chronologically. Uses `JOIN FETCH` on loan and borrower for efficiency.
* `@Query(...) findPaymentsDueSoon(...)`:
  Fetches all unpaid installments whose `dueDate` falls between today and 3 days from today.
* `@Query(...) findRecentPayments(...)`:
  Fetches installments where `paidDate IS NOT NULL`, ordered by `paidDate DESC` to show the latest completed transactions.

---

## 9. Data Transfer Objects (DTOs) & Request Payloads

### Why do we use DTOs?
* **Security & Decoupling:** We never expose raw database entities directly to the client. DTOs hide sensitive internal database fields (like password hashes or internal keys).
* **Input Validation:** Validation annotations (`@NotBlank`, `@DecimalMin`) belong on DTO request objects, keeping entity classes clean.

### Auth DTOs
* **`LoginRequest.java`:** Accepts `username` and `password` with `@NotBlank` constraints.
* **`LoginResponse.java`:** Returns `token` (JWT string), `username`, and `role`.
* **`SignupRequest.java`:** Validates `username` (3–50 characters) and `password` (minimum 6 characters).

### BorrowerDTO.java
Contains full borrower profile data plus dynamically aggregated statistics:
* `activeLoans`: Count of currently open loans.
* `outstandingAmount`: Total unpaid money owed.
* `nextPaymentDate`: Earliest upcoming payment due date across all active loans.
* Includes validation patterns for phone number (`^\+?[0-9\s\-]{10,15}$`) and Aadhaar last 4 digits (`^$|^[0-9]{4}$`).

### Loan DTOs
* **`LoanCreationRequest.java`:** The payload to issue a new loan. Validates that `borrowerId` is present, `principalAmount >= 0.01`, and `durationInMonths >= 1`.
* **`LoanDTO.java`:** Comprehensive loan view returned to the frontend, including interest rates, total payable, balances, and borrower names.
* **`LoanPreviewResponse.java`:** Lightweight response used by the loan preview calculator to let users see total interest, total payable, and monthly installments before finalizing the loan.

### Payment DTOs
* **`PaymentRecordRequest.java`:** Payload sent when collecting a payment (`paidAmount >= 0.01`, `paidDate`, optional `notes`).
* **`PaymentDTO.java`:** Represents an installment with its derived status, whether it is late, and how many days overdue (`lateDays`).

### DashboardSummaryDTO.java
Carries aggregated business metrics for the dashboard:
* `totalLent`: Total principal disbursed.
* `totalCollected`: Total money collected to date.
* `outstanding`: Total unpaid money remaining.
* `dueSoonCount`: Number of installments due in the next 3 days.
* `overdueCount`: Number of installments overdue.
* `activeLoansCount`: Total active loans.

---

## 10. Business Logic & Mathematical Engine (Services)

### InterestCalculationService.java
The core financial calculation engine.

#### 1. Simple Interest (`calculateSimpleInterest`)
Used when a percentage rate is applied over time:
* **Annual Rate Formula:**
  $$\text{Interest Amount} = \text{Principal} \times \left(\frac{\text{Rate}}{100}\right) \times \left(\frac{\text{Duration in Months}}{12}\right)$$
  Code implementation:
  ```java
  BigDecimal rateFactor = rate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
  BigDecimal timeFactor = duration.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
  interestAmount = principal.multiply(rateFactor).multiply(timeFactor).setScale(2, RoundingMode.HALF_UP);
  ```
* **Monthly Rate Formula:**
  $$\text{Interest Amount} = \text{Principal} \times \left(\frac{\text{Rate}}{100}\right) \times \text{Duration in Months}$$
* **Totals:**
  $$\text{Total Payable} = \text{Principal} + \text{Interest Amount}$$
  $$\text{Monthly Installment} = \frac{\text{Total Payable}}{\text{Duration in Months}}$$

#### 2. Interest-Only Loans (`calculateInterestOnly`)
The borrower pays only the interest portion each month:
* **Annual:** $\text{Monthly Installment} = \text{Principal} \times \left(\frac{\text{Rate}}{1200}\right)$
* **Monthly:** $\text{Monthly Installment} = \text{Principal} \times \left(\frac{\text{Rate}}{100}\right)$
* $\text{Total Interest} = \text{Monthly Installment} \times \text{Duration}$
* $\text{Total Payable} = \text{Principal} + \text{Total Interest}$

---

### BorrowerService.java
Manages borrower lifecycle and enforces security boundaries.

* **`getCurrentUser()` (Lines 31–36):** Reads the authenticated username from `SecurityContextHolder` and fetches the corresponding `User` from `UserRepository`.
* **`createBorrower` (Lines 39–53):** Creates a new `Borrower`, attaches `user = currentUser`, saves to database, and returns `BorrowerDTO`.
* **`updateBorrower` (Lines 56–73):** Verifies that the borrower belongs to `currentUser`. If someone attempts to update another user's borrower, it throws `AccessDeniedException`.
* **`getActiveBorrowers` (Lines 76–87):** Returns active borrowers belonging to the current user. If a search query is provided, executes the search query.
* **`deleteBorrower` (Lines 102–113):** Soft-deletes by setting `active = false`.
* **`mapToDTO` (Lines 115–154):** Aggregates borrower metrics:
  * Counts loans where status is `ACTIVE` or `OVERDUE`.
  * Sums `remainingAmount` across all non-cancelled loans to calculate `outstandingAmount`.
  * Computes the earliest `nextPaymentDate`.

---

### LoanService.java
Handles loan origination and schedule generation.

* **`createLoan` (Lines 47–150):**
  1. Validates borrower ownership against `currentUser`.
  2. Computes interest according to the selected `interestType` (Fixed, Interest-Only, or Simple).
  3. Builds and persists the `Loan` entity with status `ACTIVE`.
  4. **Schedule Generation Loop (Lines 114–142):**
     * Iterates month $i = 1$ to `durationInMonths`.
     * Calculates `dueDate = loanStartDate.plusMonths(i)`.
     * Handles rounding precision: In the final installment ($i = \text{duration}$), calculates `expected = totalPayable.subtract(accumulatedInstallments)`. This prevents rounding discrepancies of pennies or cents over multi-year loans.
     * Builds and saves all `Payment` installments in batch via `paymentRepository.saveAll(...)`.
  5. Sets the loan's `nextPaymentDate` to installment #1's due date.
* **`previewLoan` (Lines 153–190):** Calculates projected totals without saving anything to the database, allowing frontend instant previews.
* **`getLoans`, `getLoanById`, `getLoansByBorrowerId`:** Retrieve loans while enforcing tenant access boundaries.

---

### PaymentService.java
Manages repayment processing, balance deductions, and status changes.

* **`recordPayment` & `recordPartialPayment` (Lines 95–140):**
  1. Finds the payment installment by ID.
  2. Verifies ownership via `currentUser`.
  3. Ensures the payment is not already fully paid (`remainingExpected > 0`).
  4. Validates that the submitted amount does not exceed the remaining expected balance.
  5. Calls `processPaymentDetails(...)`.
* **`processPaymentDetails` (Lines 141–194):**
  * Increments `payment.paidAmount` by the newly paid amount.
  * Updates `paidDate` and `notes`.
  * Recalculates installment status via `payment.getDerivedStatus(today)`.
  * **Proportional Principal Deduction Formula:**
    $$\text{Principal Portion Paid} = \text{Paid Amount} \times \left(\frac{\text{Loan Principal Amount}}{\text{Loan Total Payable Amount}}\right)$$
  * Decrements `loan.remainingPrincipal` and `loan.remainingAmount` (bounded at zero).
  * **Loan Completion / Status Check:**
    * If `remainingAmount <= 0`: Marks `loan.status = COMPLETED` and clears `nextPaymentDate`.
    * Otherwise: Scans all installments to find the earliest unpaid installment and sets it as the new `nextPaymentDate`. If that date is before today, flags the loan as `OVERDUE`; otherwise `ACTIVE`.
  * Persists updates to both Payment and Loan.

---

### DashboardService.java
Aggregates summary statistics for the user dashboard.

* **`getDashboardSummary()` (Lines 36–80):**
  * Fetches all loans belonging to the current user.
  * Iterates through all non-cancelled loans to compute:
    * `totalLent`: Sum of `principalAmount`.
    * `outstanding`: Sum of `remainingAmount`.
    * `totalCollected`: Sum of $(\text{totalPayableAmount} - \text{remainingAmount})$.
    * `activeLoansCount`: Number of `ACTIVE` or `OVERDUE` loans.
  * Queries `PaymentRepository` to count:
    * `dueSoonCount`: Unpaid installments due within the next 3 days.
    * `overdueCount`: Unpaid installments past their due date.
  * Packages everything into `DashboardSummaryDTO`.

---

## 11. Presentation Layer (REST Controllers)

### AuthController.java
Base URL: `/api/auth`
* **`POST /login`:** Authenticates credentials via Spring Security's `AuthenticationManager`. Extracts authorities, generates a signed JWT token via `JwtTokenProvider`, and returns HTTP 200 with the token.
* **`POST /register`:** Checks if the username already exists (returns HTTP 409 Conflict if taken). Otherwise, encodes the password using BCrypt and saves the new User with role `ADMIN`. Returns HTTP 201 Created.

---

### BorrowerController.java
Base URL: `/api/borrowers`
* **`POST /`:** Validates and creates a new borrower. Returns HTTP 201 Created.
* **`GET /`:** Retrieves all active borrowers, with optional search filtering (`?query=`). Returns HTTP 200.
* **`GET /{id}`:** Retrieves a single borrower by ID. Returns HTTP 200.
* **`PUT /{id}`:** Updates borrower profile details. Returns HTTP 200.
* **`DELETE /{id}`:** Performs soft-delete. Returns HTTP 204 No Content.

---

### LoanController.java
Base URL: `/api/loans`
* **`POST /`:** Creates a new loan and generates repayment installments. Returns HTTP 201 Created.
* **`POST /preview`:** Simulates interest calculation without persisting. Returns HTTP 200.
* **`GET /`:** Fetches loans, optionally filtered by status (`?status=ACTIVE`). Returns HTTP 200.
* **`GET /{id}`:** Fetches loan by ID. Returns HTTP 200.
* **`GET /borrower/{borrowerId}`:** Fetches all loans associated with a specific borrower. Returns HTTP 200.

---

### PaymentController.java
Base URL: `/api/payments`
* **`GET /{id}`:** Fetches payment installment by ID.
* **`GET /loan/{loanId}`:** Fetches all installments for a given loan.
* **`GET /due-soon`:** Fetches installments due in the next 3 days.
* **`GET /overdue`:** Fetches overdue installments.
* **`GET /recent`:** Fetches recently paid installments.
* **`POST /{id}/pay`:** Records a full or standard payment against an installment.
* **`POST /{id}/partial-payment`:** Records a partial payment against an installment.

---

### DashboardController.java
Base URL: `/api/dashboard`
* **`GET /summary`:** Returns financial totals and alert counters.
* **`GET /due-soon`:** Returns due soon installment list for dashboard widgets.
* **`GET /overdue`:** Returns overdue installment list for priority follow-up.
* **`GET /recent-payments`:** Returns recent transactions list.

---

### HealthController.java
* **`GET /health`:** Unprotected public route returning `"OK"`. Used by container orchestration (Docker health checks, Kubernetes liveness probes) and uptime monitors to verify the server is running.

---

## 12. Exception Handling & Resilience

### GlobalExceptionHandler.java
Annotated with `@RestControllerAdvice`. It intercepts exceptions thrown anywhere in the application and formats them into a standardized, client-friendly JSON response:
```json
{
  "success": false,
  "message": "Error details here",
  "timestamp": "2026-10-26T16:45:00"
}
```

* **`handleResourceNotFound(ResourceNotFoundException ex)`:** Returns HTTP `404 Not Found`.
* **`handleInvalidBusinessLogic(RuntimeException ex)`:** Catches `InvalidLoanException` or `InvalidPaymentException` and returns HTTP `400 Bad Request`.
* **`handleValidationErrors(MethodArgumentNotValidException ex)`:** Triggered when `@Valid` fails on a request body. Gathers all field error messages and joins them with commas, returning HTTP `400 Bad Request`.
* **`handleGeneralException(Exception ex)`:** Catches any unforeseen errors, logs them, and returns HTTP `500 Internal Server Error`.

### Custom Exceptions
* **`ResourceNotFoundException.java`:** Thrown when an ID does not exist in the database.
* **`InvalidLoanException.java`:** Thrown when loan creation parameters violate business constraints.
* **`InvalidPaymentException.java`:** Thrown when a payment exceeds expected amounts or is made against an already settled installment.

---

## 13. Automated Testing Suite

### LendingServiceTests.java
Contains unit tests verifying core financial calculations and business logic using **JUnit 5** and **Mockito**.

* **`@Mock` vs `@InjectMocks`:**
  * `@Mock` creates simulated mock versions of `PaymentRepository` and `LoanRepository`.
  * `@InjectMocks` instantiates the real `PaymentService` and `InterestCalculationService`, injecting the mock repositories into them.
* **`testInterestCalculation()` (Lines 42–57):**
  * Tests that ₹100,000 principal at 12% annual interest for 12 months produces exactly ₹12,000 interest, ₹112,000 total payable, and ₹9,333.33 monthly installment.
* **`testMonthlyPaymentCalculation()` (Lines 59–75):**
  * Verifies equal distribution of payments without interest.
* **`testDueSoonAndOverdueStatusDerivation()` (Lines 77–108):**
  * Tests the `getDerivedStatus(LocalDate)` runtime logic across all date scenarios: due today, due in 1–3 days (`DUE_SOON`), due in 4 days (`UPCOMING`), due yesterday (`OVERDUE`), and due 5 days ago (`OVERDUE`).
* **`testPaidStatusDerivation()` & `testPartialPaymentStatusDerivation()` (Lines 110–127):**
  * Verifies transitions to `PAID` and `PARTIALLY_PAID` based on monetary amounts.
* **`testRecordPaymentOperation()` (Lines 129–176):**
  * Mocks recording a ₹1,000 payment against a loan installment.
  * Asserts that `status` changes to `PAID`.
  * Verifies proportional balance deduction: asserts `loan.remainingAmount` decreases by ₹1,000 and `loan.remainingPrincipal` decreases by ₹833.33 ($1000 \times \frac{10000}{12000}$).
  * Verifies repository `.save()` methods are invoked exactly once via Mockito's `verify(...)`.

---

## Summary Checklist

| Component | Responsibility | Primary Tech / Pattern |
|---|---|---|
| **Build & Config** | Project lifecycle & environment settings | Maven, Java 17, `application.properties` |
| **Authentication** | Token issuing & stateless validation | Spring Security, JJWT, BCrypt |
| **Entities** | Database schema representation | JPA, Hibernate, PostgreSQL |
| **Repositories** | Data access & optimized querying | Spring Data JPA, JPQL |
| **Services** | Core business logic & interest math | Java BigDecimal, Transactions |
| **Controllers** | REST API endpoints | Spring Web MVC, Jakarta Validation |
| **Exceptions** | Global error interception | `@RestControllerAdvice`, Custom Exceptions |
| **Testing** | Verification of calculations & workflows | JUnit 5, Mockito |
