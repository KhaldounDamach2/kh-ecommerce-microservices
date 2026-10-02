FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="Khaldoun Damach" \
      description="Auth Service for E-Commerce Microservices"

RUN apk add --no-cache curl \
    && addgroup -S -g 1001 appuser \
    && adduser -S -D -H -u 1001 -G appuser appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar
RUN chown appuser:appuser /app/app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
