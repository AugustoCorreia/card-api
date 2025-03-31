FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw package -DskipTests
ENTRYPOINT ["java", "-jar", "target/Card-API-0.0.1-SNAPSHOT.jar"]