FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copy jar from build context (expect target/cloud-storage-backend-*.jar)
ARG JAR_FILE=target/cloud-storage-backend-0.1.0.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]


