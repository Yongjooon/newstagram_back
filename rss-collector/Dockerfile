FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew :rss-collector:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/rss-collector/build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=dev
ENTRYPOINT ["java", "-jar", "app.jar"]
