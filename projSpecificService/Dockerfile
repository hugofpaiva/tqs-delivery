FROM maven:3.8.1-adoptopenjdk-11

WORKDIR /app

COPY . /app/

RUN sed -i "s/localhost/db-specific/g" src/main/resources/application.properties

RUN sed -i "s/5433/5432/g" src/main/resources/application.properties

RUN mvn -DskipTests clean package

ENTRYPOINT ["java","-jar","/app/target/humber-pecas-0.0.1-SNAPSHOT.jar"]