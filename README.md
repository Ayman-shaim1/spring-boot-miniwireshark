# Mini-Wireshark

Spring Boot application for real-time network packet capture (pcap4j), with history stored in PostgreSQL, dashboard, and JSON/CSV export.

## Run with Docker

> Prerequisites: PostgreSQL (in Docker or on the host). Create the `miniwireshark` database first.

```bash
docker build -t miniwireshark .
```

The database can be **in Docker** (another container) or **on the host**. Pass the URL and optionally `username`/`password` if different from `postgres`/`123456`:

**PostgreSQL on host** (Mac/Windows):
```bash
docker run -p 8080:8080 -p 9092:9092 \
  -e spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/miniwireshark \
  -e spring.datasource.username=postgres \
  -e spring.datasource.password=YOUR_PASSWORD \
  miniwireshark
```

**PostgreSQL on host** (Linux):
```bash
docker run --add-host=host.docker.internal:host-gateway -p 8080:8080 -p 9092:9092 \
  -e spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/miniwireshark \
  -e spring.datasource.username=postgres \
  -e spring.datasource.password=YOUR_PASSWORD \
  miniwireshark
```

**PostgreSQL in Docker** (same network, container named `postgres`):
```bash
docker run -p 8080:8080 -p 9092:9092 --network=host \
  -e spring.datasource.url=jdbc:postgresql://postgres:5432/miniwireshark \
  -e spring.datasource.username=postgres \
  -e spring.datasource.password=YOUR_PASSWORD \
  miniwireshark
```
*Or connect containers via `--link postgres:postgres`.*

- REST API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Socket.IO: port 9092
