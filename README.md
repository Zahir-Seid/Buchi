# Buchi
Welcome to the pet finder app called Buchi. Using this app, customers can search for pets (cats , dogs , birds etc…) that have no owners and request to adopt them.

## Stack
Spring Boot 4, PostgreSQL, Docker Compose.

## External Pet Search
Local pets are searched first. If the requested limit is not yet satisfied, Buchi then queries The Dog API and appends matching dog results after the local records.

Configuration:

- `DOG_API_KEY`
- `DOG_API_BASE_URL` optional, defaults to `https://api.thedogapi.com/v1`
- `PHOTO_UPLOAD_DIR`
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

## Run
```bash
docker-compose up --build
```

## API Notes
- `GET /api/get_pets` supports repeated query params for multi-select filters, e.g. `?type=Dog&type=Cat&age=baby&age=young&limit=5`
- `good_with_children` is supported on requests
- `POST /api/generate_report` accepts a JSON body with `from_date` and `to_date`
