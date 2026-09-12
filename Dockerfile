# Stage 1 (Build)
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN ./gradlew build -Dquarkus.package.type=fast-jar -x test --no-daemon

# Stage 2 (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/quarkus-app/lib/ /app/lib/
COPY --from=build /app/build/quarkus-app/*.jar /app/
COPY --from=build /app/build/quarkus-app/app/ /app/app/
COPY --from=build /app/build/quarkus-app/quarkus/ /app/quarkus/
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=${PORT} -jar /app/quarkus-run.jar"]
