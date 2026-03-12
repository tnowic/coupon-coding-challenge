FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/coupon-service.jar ./coupon-service.jar

EXPOSE 8080

CMD ["java", "-jar", "coupon-service.jar"]