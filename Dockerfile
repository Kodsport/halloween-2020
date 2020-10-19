FROM gradle:jdk14
ENV SPOOKY=GLOBAL

COPY . /src/app
WORKDIR /src/app
RUN gradle build
CMD java --enable-preview -jar /src/app/build/libs/halloween-2020-jvm.jar
