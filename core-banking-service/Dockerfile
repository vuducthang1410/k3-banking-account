# Stage 1: Use Maven for building common library
FROM maven:3.9.9-sapmachine-17 AS common-lib-build
LABEL authors="ctv"
WORKDIR /app
COPY ./common-library/ ./common-library
WORKDIR /app/common-library
RUN mvn clean package

# Stage 2: Use Maven for building core banking service
FROM maven:3.9.9-sapmachine-17 AS build
LABEL authors="ctv"
WORKDIR /app
COPY ./core-banking-service/ ./core
WORKDIR /app/core
COPY --from=common-lib-build /app/common-library/target/common-library-0.0.1-SNAPSHOT.jar ./lib/
RUN mvn install:install-file -Dfile=./lib/common-library-0.0.1-SNAPSHOT.jar \
    -DgroupId=com.system \
    -DartifactId=common-library \
    -Dversion=0.0.1-SNAPSHOT \
    -Dpackaging=jar
RUN mvn clean package -DskipTests

# Stage 3: Use OpenJDK for running
FROM openjdk:24-slim-bullseye
LABEL authors="ctv"
WORKDIR /app
COPY --from=build /app/core/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]

# Build command: docker build -t core_banking_image -f core-banking-service/Dockerfile .
# Run command: docker run --name core_banking_container -p 8090:8090 core_banking_image