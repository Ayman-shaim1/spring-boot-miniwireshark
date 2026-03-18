# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

COPY src src

# Build JAR
RUN ./mvnw package -DskipTests -B

# Run stage (Debian-based for native pcap/JNA compatibility)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install libpcap required by pcap4j for packet capture
RUN apt-get update && apt-get install -y --no-install-recommends libpcap0.8 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080 9092

ENTRYPOINT ["java", "-jar", "app.jar"]
