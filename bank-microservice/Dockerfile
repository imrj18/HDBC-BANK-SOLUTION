# ---------- STAGE 1 (Build) ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


# ---------- STAGE 2 (Runtime) ----------
FROM eclipse-temurin:17-jre-alpine

LABEL authors="ritik.jani"

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/logs && chmod -R 777 /app/logs

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
