*[This document in Polish](README_pl.md)*
# REST service for managing discount coupons

## Project Description

REST service responsible for managing discount coupons.

The system provides the following functionalities:

* registering a coupon's use by a user,
* creating a new coupon (without authentication support)

Each coupon contains the following information:

* unique coupon code (up to 20 alphanumeric characters),
* creation date,
* maximum number of possible uses,
* current number of uses,
* country code for which the coupon is intended (optional field - no code means that a user from any country can apply this coupon)
* availability for registered users only - a true/false flag indicates that the coupon is intended only for registered app users - their IDs must already exist in the database

# Business Requirements Description:

* The coupon code should be unique and case-insensitive.
* The coupon should be limited to a maximum number of uses – first come, first served.
* The country defined in the coupon restricts coupon use to users from that country (based on the HTTP request's IP address).
* When the coupon has reached its maximum number of uses, attempts to use it result in an error with an appropriate message. The same applies if the coupon code does not exist, the attempted use is from a prohibited country, or the user has already used the coupon.
* A user can only use the coupon once – the user ID of a user already defined in the database must be passed in the request.

# Technical Description

The project consists of three modules:

**REST Service Module (service)**

The module was written in Java using the **Spring Boot** framework. It is built using Maven. The application uses a Postgresql database to store coupon data, coupon redemption data, and registered user data. The free REST service [http://ip-api.com](http://ip-api.com) is used to identify the country from which HTTP coupon requests originate. It is also possible to reconfigure the URL used to retrieve the country code based on the IP address from the HTTP request using the IPAPI_URL parameter.

Building an application using maven with the command:
```bash
./mvnw clean package
```
Building with integration tests running:
```bash 
./mvnw clean verify 
```
*(Since the integration tests use the [Testcontainers](https://testcontainers.com/) famework, there must be a Docker image named coupon-db for which a container is created for the duration of the tests. For these tests, a database with defined registered users must be running. More information about the coupon-db container can be found in the description of the database module)*

To run the application, set the following environment variables:
* POSTGRES_DB - defines the Postgres database name
* POSTGRES_USER - defines the database username
* POSTGRES_PASSWORD - defines the database username
* POSTGRES_HOST - defines the database server address
* POSTGRES_PORT - defines the port on which the database listens

Sample variable values for the local environment are included in the [.env](.env) file.
To run the application locally, you can use the following script: [run-local-service.sh](./service/run-local-service.sh)

Optionally, you can run the service as a Docker container called **coupon-service**. For this purpose, a [Dockerfile](./service/Dockerfile) file has been prepared. Simple shell scripts have also been prepared to build the image: [docker-build-service.sh](./service/docker-build-service.sh) and run the container: [docker-run-service.sh](./service/docker-run-service.sh).
To run a ready-made environment, you can also use the prepared docker-compose configuration [docker-compose](./docker-compose.yaml), which uses predefined environment variables from the [.env](.env) file. Command:
```bash  
docker-compose up
 ```
The service provides the following endpoints:

* POST /coupons - for creating a coupon
* GET /coupons/{code} - for retrieving coupon data with a selected code
* GET /coupons - for retrieving a list of created coupons (the results can be paginated using the optional 'page' and 'size' parameters)
* POST /coupons/{code}/apply - for applying a coupon with a selected code by the user

Detailed API documentation using swagger-ui is available after running the application at the following url:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Examples of using the above endpoints have been prepared in the attached Postman collection:

[coupon-challenge.postman_collection.json](./coupon-challenge.postman_collection.json)

**Database Module (database)**

Contains configuration for the Postgresql database. The database schema and application users are defined in the file: [init.sql](database/init.sql)

A Docker image definition for the database was created using the init.sql file in the file [Dockerfile](./database/Dockerfile).
Simple shell scripts were prepared for creating the image: [docker-build-coupon-db.sh](./database/docker-build-coupon-db.sh) and running the Postgresql database as a Docker container named **coupon-db**: [docker-run-coupon-db.sh](./database/docker-run-coupon-db.sh)

**IP API Service Mock Module (wiremock)**

Contains the [Wiremock](https://wiremock.org) framework configuration, which can be used to replace the external ip-api.com service call handling. The ip-api.com service retrieves the country code based on the IP address.
The mock service exposes the endpoint:
```bash  
/ip-api/json/{ipaddr}?fields=countryCode,query
```
returning the same data structure in the response as the actual service([see documentation of the api](https://ip-api.com/docs/api:json))

The mock assumes that for IP addresses {ipaddr}:
* starting with the digit 3 - countryCode is not returned in the response (this corresponds to the case when the country of origin cannot be determined from a given IP address)
* starting with the digit 2 - returns countryCode: PL
* starting with other digits - returns countryCode: UK

A simple shell script has been prepared to run the mock as a container in Docker called **wiremock-ip-resolver**: [docker-run-wiremock-ip-resolver.sh](./wiremock/docker-run-wiremock-ip-resolver.sh)

Adding a header: **X-Forwarded-For** with the IP address value to the http request gives you the ability to overwrite the address that will be recognized in the application as the address of origin of the request.

An example API test using a mock can be found in the attached Postman collection:
[coupon-challenge.postman_collection.json](./coupon-challenge.postman_collection.json)