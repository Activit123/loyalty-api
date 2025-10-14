# stage 1: build with Maven (uses JDK 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

# copy Maven wrapper & settings if exist, then whole source
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# copy source
COPY src ./src

# if you use additional modules or resources, copy them too
# RUN build (skip tests to accelerate, elimină -DskipTests dacă vrei teste)
RUN if [ -f "./mvnw" ]; then chmod +x ./mvnw && ./mvnw -B -DskipTests package; else mvn -B -DskipTests package; fi

# stage 2: runtime image with Eclipse Temurin 21
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# copy jar from build stage
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
