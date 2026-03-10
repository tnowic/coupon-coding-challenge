#!/usr/bin/env sh

while read -r line
do
  export $line
done < ../.env

#loacl postgres instance
export POSTGRES_HOST=localhost
#local wiremock instance for ip api
#export IP_API_URL=http://localhost:8085/ip-api/json

./mvnw clean verify