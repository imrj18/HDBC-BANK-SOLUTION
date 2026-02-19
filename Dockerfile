# ---------- BUILD STAGE ----------
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy everything (parent + modules)
COPY . .

# Build all modules
RUN mvn clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

ARG JAR_FILE

COPY --from=build /app/${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]
