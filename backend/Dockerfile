# Multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# docker CLI lets the backend orchestrate execution containers via the
# host daemon's socket (mounted in docker-compose.yml) - code still runs
# in sibling containers with the sandbox limits applied per run.
RUN apk add --no-cache docker-cli \
    && addgroup -S appgroup && adduser -S appuser -G appgroup
RUN mkdir -p /tmp/code-exec && chown appuser:appgroup /tmp/code-exec
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
