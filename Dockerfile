FROM openjdk:17-jdk-slim as build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=cloud
ENV OPENAI_API_KEY=your-api-key
ENV JWT_SECRET=your-jwt-secret

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
