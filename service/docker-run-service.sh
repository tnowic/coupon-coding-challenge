#!/usr/bin/env sh

docker network create coupon-net
docker stop coupon-service
docker rm coupon-service
docker run -d --name coupon-service --env-file ../.env --network coupon-net -p 8080:8080 coupon-service