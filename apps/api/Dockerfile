FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

# Cache dependency downloads separately from source compilation
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -q

# Build the fat jar — skip codegen (generated sources are committed to src/main/generated)
COPY src src
RUN ./mvnw package -DskipTests -Djooq.codegen.skip=true -q

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /build/target/api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
