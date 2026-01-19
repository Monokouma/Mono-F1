FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copie les fichiers de config Gradle d'abord (layer caché)
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY app/build.gradle.kts ./app/

# Télécharge les dépendances (caché tant que build.gradle change pas)
RUN gradle dependencies --no-daemon || true

# Copie le code
COPY . .

# Build
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app/build/libs/*-all.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]