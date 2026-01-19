FROM gradle:8.5-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY app/build.gradle ./app/

RUN gradle dependencies --no-daemon || true

COPY . .

RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app/build/libs/*-all.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]