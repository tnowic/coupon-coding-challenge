package pl.nowito.coupon;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

@SpringBootTest
@Testcontainers
class CouponServiceApplicationTest {

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost/coupons";
        RestAssured.port = 8080;
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> couponDbContainer = new PostgreSQLContainer<>(DockerImageName.parse("coupon-db:latest")
            .asCompatibleSubstituteFor("postgres:16-alpine"))
            .withExposedPorts(5432);


}
