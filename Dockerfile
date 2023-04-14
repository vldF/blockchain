FROM gradle:7.4.2-jdk17-alpine as build
WORKDIR /workspace/app

COPY build.gradle.kts settings.gradle.kts gradlew gradle.properties ./
COPY src src

RUN gradle build --no-daemon --console=verbose

FROM eclipse-temurin:17-jdk-alpine as blockchain-run
VOLUME /app

ENV PORT=1042
ENV NODES=""

COPY --from=build /workspace/app/build/libs/blockchain-all.jar ./app/
