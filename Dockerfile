# stage 1: build with Maven (uses JDK 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

# copy Maven wrapper & settings if exist, then whole source
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# copy source
COPY uploads ./uploads
COPY src ./src

# RUN build (skip tests to accelerate, elimină -DskipTests dacă vrei teste)
# MODIFICAT: Adaugă argumentul -Dmaven.compiler.parameters=true și compilerArgs
RUN if [ -f "./mvnw" ]; then \
    chmod +x ./mvnw && \
    ./mvnw -B -DskipTests -Dmaven.compiler.parameters=true -Dmaven.compiler.fork=true -Dmaven.compiler.compilerArgs=--enable-preview package; \
else \
    # Versiunea clasică mvn (mai puțin sigură, dar adaugă preview)
    mvn -B -DskipTests -Dmaven.compiler.parameters=true -Dmaven.compiler.fork=true -Dmaven.compiler.compilerArgs=--enable-preview package; \
fi

# stage 2: runtime image with Eclipse Temurin 21
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# copy jar from build stage
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/${JAR_FILE} app.jar
COPY --from=build /workspace/uploads ./uploads

EXPOSE 8090

# MODIFICAT: Adaugă --enable-preview la ENTRYPOINT
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]