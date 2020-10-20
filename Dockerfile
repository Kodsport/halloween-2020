FROM gradle:jdk14
ENV SPOOKY=GLOBAL

COPY settings.gradle /src/app/
COPY gradle.properties /src/app/
COPY build.gradle /src/app/
COPY spooky.jar /src/app/
COPY src /src/app/src
WORKDIR /src/app
RUN gradle build
CMD java --enable-preview -jar /src/app/build/libs/halloween-2020-jvm.jar
