
##
## Build stage
##

FROM maven:3.8.1-openjdk-11-slim AS build
LABEL maintainer izzdin.b.ahmad@stud-hda.de
LABEL stage build

RUN mkdir -p /app
COPY /src/ /app/src
COPY ./pom.xml /app
WORKDIR /app
RUN mvn package


##
## Run stage
##
FROM openjdk:11.0-jre-slim
LABEL maintainer izzdin.b.ahmad@stud-hda.de
EXPOSE 6543/udp

RUN mkdir -p /app
COPY --from=build /app/target/tcp-socket-server-1.0-SNAPSHOT.jar /app
CMD [ "java", "-jar", "/app/tcp-socket-server-1.0-SNAPSHOT.jar" ]
