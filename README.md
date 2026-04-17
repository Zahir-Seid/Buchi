# 🐾 Buchi – Pet Finder App

Buchi is a Spring Boot application that helps users discover and adopt pets (dogs, cats, birds, etc.) that currently have no owners.



## Features

* Search for available pets with filters
* Adopt pets through a simple API
* Combine **local database results** with external API data
* Upload and store pet images
* Generate adoption reports



## Tech Stack

* Java + Spring Boot
* PostgreSQL
* Docker & Docker Compose
* Maven



## Architecture (Core API Logic)

This project follows a **layered architecture** where controllers handle HTTP requests, services contain business logic, and repositories manage database access.

### High-Level Request Flow

```mermaid
graph TD
    Client --> Controller[REST Controllers]

    Controller --> DTO[Request DTO Validation]
    DTO --> Service[Service Layer]

    subgraph Core Logic
        Service --> LocalSearch[Query Local Database]
        Service --> ExternalSearch[Call DogApiClient]

        LocalSearch --> Repo[Repository Layer]
        Repo --> DB[(PostgreSQL)]

        ExternalSearch --> DogAPI[The Dog API]

        Service --> Merge[Merge + Rank Results]
        Merge --> ResponseDTO[Response DTO Builder]
    end

    ResponseDTO --> Controller
    Controller --> Client
```



## Core Endpoint Flow (`GET /api/get_pets`)

This is the **main business logic** of the application:

```mermaid
graph TD
    Client --> GetPets[GET /api/get_pets]

    GetPets --> Validate[Validate Filters]

    Validate --> LocalDB[Search Local DB First]
    LocalDB --> Check{Enough Results?}

    Check -->|Yes| ReturnLocal[Return Local Results]

    Check -->|No| DogAPI[Fetch from Dog API]
    DogAPI --> Normalize[Normalize External Data]

    Normalize --> Merge[Merge Local + External]
    Merge --> Limit[Apply Limit]

    Limit --> Response[Return Combined Results]
```



## Adoption Flow

```mermaid
graph TD
    Client --> Adopt[POST /api/adopt]

    Adopt --> ValidateIDs[Validate customer_id & pet_id]
    ValidateIDs --> CheckExist{Exist in DB?}

    CheckExist -->|No| Error[Return Error]

    CheckExist -->|Yes| Save[Create Adoption Record]
    Save --> DB[(PostgreSQL)]

    DB --> Success[Return adoption_id]
```



## Report Generation Flow

```mermaid
graph TD
    Client --> Report[POST /api/generate_report]

    Report --> ValidateDates[Validate Date Range]

    ValidateDates --> QueryDB[Query Adoptions Table]

    QueryDB --> Aggregate1[Group by Pet Type]
    QueryDB --> Aggregate2[Group by Week]

    Aggregate1 --> BuildResponse
    Aggregate2 --> BuildResponse

    BuildResponse --> Return[Return Report JSON]
```



## External Pet Search

Buchi first searches pets stored locally.
If the requested result limit isn’t met, it fetches additional dogs from **The Dog API** and merges the results.

## Database Design (ER Diagram)

The application uses a relational data model centered around customers, pets, and adoptions.

```mermaid
erDiagram
    customers {
        UUID id PK
        VARCHAR name
        VARCHAR phone UK
        TIMESTAMP created_at
    }

    pets {
        UUID id PK
        VARCHAR type
        VARCHAR gender
        VARCHAR size
        VARCHAR age
        BOOLEAN good_with_children
        TIMESTAMP created_at
    }

    pet_photos {
        UUID pet_id FK
        VARCHAR photo_url
    }

    adoptions {
        UUID id PK
        UUID customer_id FK
        UUID pet_id FK
        TIMESTAMP created_at
    }

    %% Relationships
    customers ||--o{ adoptions : makes
    pets ||--|{ adoptions : "is adopted in"
    pets ||--o{ pet_photos : has
```


### Design Notes

* A **customer** can adopt multiple pets over time
* Each **adoption** links a customer to a pet
* A **pet** can have multiple photos
* `good_with_children` enables filtering in search queries
* External API data is **not persisted**, only merged at runtime



### Configuration

```bash
DOG_API_KEY=your_api_key
DOG_API_BASE_URL=https://api.thedogapi.com/v1   # optional
PHOTO_UPLOAD_DIR=uploads/

DB_HOST=postgres
DB_PORT=5432
DB_NAME=petfinder
DB_USERNAME=postgres
DB_PASSWORD=postgres
```


## Running the App

### 1. Build and start services

```bash
docker-compose up --build
```

### 2. Run in background (detached)

```bash
docker-compose up -d
```

### 3. Stop services

```bash
docker-compose down
```



## API Overview

### Search Pets

```http
GET /api/get_pets
```

Example:

```bash
/api/get_pets?type=Dog&type=Cat&age=baby&age=young&limit=5
```



### Create Pet

```http
POST /api/pets
```



### Create Customer

```http
POST /api/customers
```



### Adopt Pet

```http
POST /api/adopt
```



### Generate Report

```http
POST /api/generate_report
```

```json
{
  "from_date": "2024-01-01",
  "to_date": "2024-12-31"
}
```



## Project Structure

* `controller/` → REST endpoints
* `service/` → business logic
* `repository/` → database access
* `entity/` → JPA models
* `dto/` → request/response objects
* `config/` → configuration



## Running Tests

```bash
./mvnw test
```
