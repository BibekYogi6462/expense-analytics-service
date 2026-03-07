FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/finance-tracker-api-0.0.1-SNAPSHOT.jar finance-tracker-v1.0.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "finance-tracker-v1.0.jar"]

