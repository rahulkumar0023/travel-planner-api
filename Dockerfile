# Dockerfile start
# syntax=docker/dockerfile:1.7

########################################
# Stage 1 — build with cached Maven repo
########################################
FROM eclipse-temurin:21-jdk AS build

# Enable BuildKit cache for Maven
# (CI will supply the cache; local builds also benefit)
WORKDIR /app

# 1) Copy only pom.xml first to leverage layer cache for deps
COPY pom.xml ./

# Pre-fetch dependencies using cache mount
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

# 2) Copy sources and build
COPY src ./src

# Build the fat JAR (skip tests for deploy speed; tests should run in CI)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package

# Find the JAR (adjust if your artifactId/classifier differs)
RUN export JAR_FILE=$(ls target/*SNAPSHOT.jar || ls target/*.jar) && \
    cp $JAR_FILE /app/app.jar

########################################
# Stage 2 — small runtime image
########################################
FROM eclipse-temurin:21-jre AS runtime

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8" \
    PORT=8080

WORKDIR /opt/app
COPY --from=build /app/app.jar ./app.jar

EXPOSE 8080
# Tell Spring to use Render-provided $PORT
ENTRYPOINT ["sh", "-lc", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
# Dockerfile end