FROM maven:latest

WORKDIR /app

ADD . /app

# ADD pom.xml /app/pom.xml
# RUN mvn dependency:resolve
# RUN mvn verify

# Adding source, compile and package into a fat jar
# ADD src/main /app/src/main
RUN mvn package

EXPOSE 8090

CMD java -jar target/question-service-1.0-SNAPSHOT-fat.jar
