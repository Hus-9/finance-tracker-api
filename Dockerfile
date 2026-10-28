# Step 1 builds the jar using an image that already has Maven and Java 21 in it.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Copies just the pom first and downloads every dependency, Docker remembers this step so it only downloads again when the pom changes.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Now it copies the code and builds the jar, the tests get skipped here because GitHub runs them in CI.
COPY src ./src
RUN mvn -B -q package -DskipTests

# Step 2 starts again from a much smaller image that can only run Java, not build it.
FROM eclipse-temurin:21-jre-alpine
# Makes a normal user called app so the app doesn't run as root, if someone ever broke in they couldn't do much.
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
# Only the finished jar gets copied over from step 1, Maven and the source code get left behind.
COPY --from=build /workspace/target/finance-tracker-*.jar app.jar
# The app listens on port 8080 inside the container.
EXPOSE 8080
# The command that runs when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
