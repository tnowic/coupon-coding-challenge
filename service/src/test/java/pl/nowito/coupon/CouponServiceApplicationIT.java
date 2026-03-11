package pl.nowito.coupon;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pl.nowito.coupon.error.support.ErrorMessageSupportEnum;
import pl.nowito.coupon.service.IpResolverService;

import java.util.Optional;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
class CouponServiceApplicationIT {

    @MockitoBean
    private IpResolverService ipResolverService;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost/coupons";
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> couponDbContainer = new PostgreSQLContainer<>(DockerImageName.parse("coupon-db:latest")
            .asCompatibleSubstituteFor("postgres:16-alpine"))
            .withExposedPorts(5432);

    @Test
    void testCouponCreatedSuccessfullyAllData() {
        String body = """
                {
                  "code": "Wiosna26",
                  "maxCounter": 100,
                  "countryCode": "pl",
                  "isForRegUsers": true
                }
                """;

        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post()
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", greaterThanOrEqualTo(0))
                .body("code", equalTo("WIOSNA26"))
                .body("createTimestamp", notNullValue())
                .body("maxCounter", equalTo(100))
                .body("counter", equalTo(0))
                .body("countryCode", equalTo("PL"))
                .body("isForRegUsers", equalTo(true));
    }

    @Test
    void testCouponCreatedSuccessfullyOnlyNotNulls() {
        String body = """
                {
                  "code": "PromocjaLetnia2026",
                  "maxCounter": 222
                }
                """;
        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post()
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
                .contentType(ContentType.JSON)
                .body("id", greaterThanOrEqualTo(0))
                .body("code", equalTo("PROMOCJALETNIA2026"))
                .body("createTimestamp", notNullValue())
                .body("maxCounter", equalTo(222))
                .body("counter", equalTo(0))
                .body("countryCode", nullValue())
                .body("isForRegUsers", equalTo(false));
    }

    @Test
    void testCouponCreationValidationIncorrectCode() {
        String body = """
                {
                  "code": "WIOSNA_26",
                  "maxCounter": 100,
                  "countryCode": "PL",
                  "isForRegUsers": true
                }
                """;

        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post()
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("message", startsWith("Validation failed for object='createCouponRequest'"))
                .body("errors[0].field", equalTo("code"))
                .body("errors[0].rejectedValue", equalTo("WIOSNA_26"));
    }

    @Test
    void testCouponCreationValidationIncorrectCountryCode() {
        String body = """
                {
                  "code": "LATO27",
                  "maxCounter": 555,
                  "countryCode": "Poland",
                  "isForRegUsers": false
                }
                """;

        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post()
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("message", startsWith("Validation failed for object='createCouponRequest'"))
                .body("errors[0].field", equalTo("countryCode"))
                .body("errors[0].rejectedValue", equalTo("Poland"));
    }

    @Test
    void testCouponCreationDuplicateCouponCode() {
        String body = """
                {
                  "code": "MOJELATO27",
                  "maxCounter": 44,
                  "countryCode": "PL",
                  "isForRegUsers": false
                }
                """;

        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post();

        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post()
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CONFLICT.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_DUPLICATE_COUPON_CODE.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. Duplicate coupon code: MOJELATO27"))
                .body("suggestion", equalTo("Please use another coupon code"));
    }

    @Test
    void testGetCoupon() {
        String body = """
                {
                  "code": "Jesien2026",
                  "maxCounter": 999,
                  "countryCode": "DK",
                  "isForRegUsers": false
                }
                """;

        with().body(body)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post();

        with()
                .when()
                .log().all()
                .get(RestAssured.baseURI + "/Jesien2026")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", greaterThanOrEqualTo(0))
                .body("code", equalTo("JESIEN2026"))
                .body("createTimestamp", notNullValue())
                .body("maxCounter", equalTo(999))
                .body("counter", equalTo(0))
                .body("countryCode", equalTo("DK"))
                .body("isForRegUsers", equalTo(false));
    }

    @Test
    void testGetCouponNotFound() {
        with()
                .when()
                .log().all()
                .get(RestAssured.baseURI + "/NieMaMnieTu")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_COUPON_CODE_NOT_FOUND.getErrorCode()))
                .body("errorMessage", equalTo("Coupon code: NieMaMnieTu not found"))
                .body("suggestion", equalTo("Please check your code and try again"));
    }

    @Test
    void testApplyCouponForNonRegisteredCustomer() {

        String createCouponRequest = """
                {
                  "code": "ZIMA27",
                  "maxCounter": 2,
                  "isForRegUsers": false
                }
                """;

        with().body(createCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post();

        with()
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/ZIMA27/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", greaterThanOrEqualTo(0))
                .body("code", equalTo("ZIMA27"))
                .body("createTimestamp", notNullValue())
                .body("maxCounter", equalTo(2))
                .body("counter", equalTo(1))
                .body("countryCode", nullValue())
                .body("isForRegUsers", equalTo(false));

        with()
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/ZIMA27/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("code", equalTo("ZIMA27"))
                .body("maxCounter", equalTo(2))
                .body("counter", equalTo(2));

        // maxCounter reached
        with()
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/ZIMA27/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_COUPON_MAX_COUNTER_REACHED.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. A counter of coupon code: ZIMA27 reached its maximum possible value: 2"))
                .body("suggestion", equalTo("Please use another coupon code to apply"));
    }

    @Test
    void testApplyCouponForRegisteredCustomers() {
        Mockito.when(ipResolverService.getCountryCodeForIpAddr(Mockito.anyString())).thenReturn(Optional.of("PL"));

        String createCouponRequest = """
                {
                  "code": "NOWYROK",
                  "maxCounter": 10,
                  "countryCode": "PL",
                  "isForRegUsers": true
                }
                """;

        with().body(createCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post();

        String applyCouponRequest = """
                {
                  "customerId": 1
                }
                """;

        with().body(applyCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/NOWYROK/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", greaterThanOrEqualTo(0))
                .body("code", equalTo("NOWYROK"))
                .body("createTimestamp", notNullValue())
                .body("maxCounter", equalTo(10))
                .body("counter", equalTo(1))
                .body("countryCode", equalTo("PL"))
                .body("isForRegUsers", equalTo(true));

        // missing customer in request body
        with()
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/NOWYROK/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_COUPON_FOR_REGISTERED_CUSTOMERS_ONLY.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. Coupon code: NOWYROK is available only for registered customers"))
                .body("suggestion", equalTo("Please provide customer id in request body"));

        String nonExistedCustomerInApplyCouponRequest = """
                {
                  "customerId": 999
                }
                """;

        with().body(nonExistedCustomerInApplyCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/NOWYROK/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_REGISTERED_CUSTOMER_NOT_FOUND.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. Registered customer id: 999 not found"))
                .body("suggestion", equalTo("Please provide valid customer id in request body"));

    }

    @Test
    void testApplyCouponFailedForIncorrectCountryCode() {
        Mockito.when(ipResolverService.getCountryCodeForIpAddr(Mockito.anyString())).thenReturn(Optional.of("PL"));

        String createCouponRequest = """
                {
                  "code": "MERRYCHRISTMAS26",
                  "maxCounter": 100,
                  "countryCode": "UK"
                }
                """;

        with().body(createCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post();

        with()
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/MerryChristmas26/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_COUPON_COUNTRY_CODE_RESTRICTED.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. Request origin country code: PL differs from coupon restricted country code: UK when applying to coupon code: MERRYCHRISTMAS26"))
                .body("suggestion", equalTo("Your public ip address must be in a country that this coupon is restricted to"));

        // country code for request origin cannot be resolved
        Mockito.when(ipResolverService.getCountryCodeForIpAddr(Mockito.anyString())).thenReturn(Optional.empty());

        with()
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/MERRYCHRISTMAS26/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_COUNTRY_CODE_FOR_REQUEST_NOT_FOUND.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. Request origin country could not be determined when applying to coupon code: MERRYCHRISTMAS26 restricted to country code: UK"))
                .body("suggestion", equalTo("Please verify whether valid public ip is being used in your http request"));
    }

    @Test
    void testApplyCouponFailedForSameCustomer() {

        String createCouponRequest = """
                {
                  "code": "WIELKANOC2026",
                  "maxCounter": 13,
                  "isForRegUsers": true
                }
                """;

        with().body(createCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post();

        String applyCouponRequest = """
                {
                  "customerId": 1
                }
                """;

        with().body(applyCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/WIELKANOC2026/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.JSON)
                .body("id", greaterThanOrEqualTo(0))
                .body("code", equalTo("WIELKANOC2026"))
                .body("createTimestamp", notNullValue())
                .body("maxCounter", equalTo(13))
                .body("counter", equalTo(1))
                .body("countryCode", nullValue())
                .body("isForRegUsers", equalTo(true));

        // same customer tries to apply for the same coupon
        with().body(applyCouponRequest)
                .contentType(ContentType.JSON)
                .when()
                .log().all()
                .post(RestAssured.baseURI + "/WielkaNoc2026/apply").then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errorCode", equalTo(ErrorMessageSupportEnum.ERROR_CUSTOMER_ALREADY_APPLIED_FOR_COUPON.getErrorCode()))
                .body("errorMessage", equalTo("Business rule violation. Customer with id: 1 has already applied coupon code: WIELKANOC2026"))
                .body("suggestion", equalTo("Please use other coupon code or choose different customer id"));
    }
}
