#!/usr/bin/env sh

while read -r line
do
  export $line
done < ../.env

export POSTGRES_HOST=localhost

java -jar target/coupon-service.jar