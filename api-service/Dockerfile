# ---- Stage 1: build the JAR ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Stage 2: run it ----
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 1001 --no-create-home app \
 && mkdir -p /data/uploads \
 && chown -R app:app /data
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]