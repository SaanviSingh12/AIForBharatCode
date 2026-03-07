FROM public.ecr.aws/docker/library/maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM public.ecr.aws/docker/library/eclipse-temurin:21-jre
WORKDIR /app

# Lambda Web Adapter
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.3 /lambda-adapter /opt/extensions/lambda-adapter

COPY --from=build /app/target/sahayak-backend-1.0.0.jar app.jar

ENV PORT=8080
ENV AWS_LWA_INVOKE_MODE=response_stream

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
