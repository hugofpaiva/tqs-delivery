FROM maven:3.8.1-adoptopenjdk-11

WORKDIR /app

ARG PROFILE=${PROFILE}

COPY . /app/

RUN sed -i "s/localhost/db-generic/g" src/main/resources/application.properties
RUN sed -i "s/prod/${PROFILE}/g" src/main/resources/application.properties


RUN mvn -DskipTests clean package

ENTRYPOINT ["java","-jar","/app/target/delivery-service-0.0.1-SNAPSHOT.jar"]

