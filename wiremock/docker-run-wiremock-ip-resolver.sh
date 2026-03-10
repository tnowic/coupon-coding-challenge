#!/usr/bin/env sh

docker run -d --name wiremock-ip-resolver -p 8085:8080 -v $PWD:/home/wiremock wiremock/wiremock:3.13.2 --global-response-templating