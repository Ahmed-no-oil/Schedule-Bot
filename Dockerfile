FROM gradle:7-jdk17-alpine as build

COPY . /appl

WORKDIR /appl

RUN gradle build --info

FROM eclipse-temurin:17-jre

WORKDIR /appl

COPY --from=build /appl/build/libs/schedule-bot-0.0.1-SNAPSHOT.jar schedule-bot-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "schedule-bot-0.0.1-SNAPSHOT.jar"]
