FROM maven:3.2.5-jdk-8

ADD . /data/

WORKDIR /data

RUN mvn clean package

# NOTE: install sprint-boot dependencies in the Docker image
RUN mvn spring-boot:start

VOLUME /root/.m2

EXPOSE 8080

CMD ["java", "-jar", "target/solr-indexer-1.0.0.war"]
