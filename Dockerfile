# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jre

WORKDIR /app

# The Gradle build runs before the image build, so the image only packages the
# runnable JAR produced by the app module.
COPY build/libs/app.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
