#!/usr/bin/env sh

docker network create coupon-net
docker stop coupon-db
docker rm coupon-db
docker run -d --name coupon-db --env-file ../.env -v pgdata:/var/lib/postgresql/data --network coupon-net -p 5432:5432 coupon-db