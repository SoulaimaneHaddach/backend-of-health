# ====== Stage 1: Build ======
FROM maven:3.9.6-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ====== Stage 2: Run ======
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError"
ENTRYPOINT ["sh","-c","java $JAVA_TOOL_OPTIONS -XX:+ExitOnOutOfMemoryError -jar app.jar"]
