# Mini-Wireshark

Application Spring Boot de capture de paquets réseau en temps réel (pcap4j), avec historique en base (PostgreSQL), dashboard et export JSON/CSV.

## Lancer avec Docker

> Prérequis : PostgreSQL (dans Docker ou sur la machine hôte). Créer la base `miniwireshark`.

```bash
docker build -t miniwireshark .
```

La base peut être **dans Docker** (autre conteneur) ou **sur l’hôte**. Passer l’URL, et optionnellement `username`/`password` si différents de `postgres`/`123456` :

**PostgreSQL sur l’hôte** (Mac/Windows) :
```bash
docker run -p 8080:8080 -p 9092:9092 \
  -e spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/miniwireshark \
  -e spring.datasource.username=postgres \
  -e spring.datasource.password=VOTRE_MOT_DE_PASSE \
  miniwireshark
```

**PostgreSQL sur l’hôte** (Linux) :
```bash
docker run --add-host=host.docker.internal:host-gateway -p 8080:8080 -p 9092:9092 \
  -e spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/miniwireshark \
  -e spring.datasource.username=postgres \
  -e spring.datasource.password=VOTRE_MOT_DE_PASSE \
  miniwireshark
```

**PostgreSQL dans Docker** (même réseau, conteneur nommé `postgres`) :
```bash
docker run -p 8080:8080 -p 9092:9092 --network=host \
  -e spring.datasource.url=jdbc:postgresql://postgres:5432/miniwireshark \
  -e spring.datasource.username=postgres \
  -e spring.datasource.password=VOTRE_MOT_DE_PASSE \
  miniwireshark
```
*Ou lier les conteneurs via `--link postgres:postgres`.*

- API REST : http://localhost:8080
- Swagger : http://localhost:8080/swagger-ui.html
- Socket.IO : port 9092
