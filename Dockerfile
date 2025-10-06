# Pasul 1: Folosim o imagine de bază care conține Java 21
# Eclipse Temurin este o distribuție open-source populară și de încredere a OpenJDK
FROM eclipse-temurin:21-jdk-jammy

# Pasul 2: Setăm un director de lucru în interiorul containerului
WORKDIR /app

# Pasul 3: Copiem fișierul JAR construit de Maven în container
# Argumentul JAR_FILE este o variabilă pe care o vom seta din docker-compose
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Pasul 4: Expunem portul pe care rulează aplicația Spring Boot
EXPOSE 8090

# Pasul 5: Comanda care va fi rulată la pornirea containerului
# Aceasta execută aplicația Java
ENTRYPOINT ["java", "-jar", "app.jar"]