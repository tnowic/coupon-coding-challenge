#!/usr/bin/env sh

docker network create coupon-net
docker stop wiremock-ip-resolver
docker rm wiremock-ip-resolver
docker run -d --name wiremock-ip-resolver --network coupon-net -p 8085:8080 -v $PWD:/home/wiremock wiremock/wiremock:3.13.2 --global-response-templating