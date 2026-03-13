#!/usr/bin/env sh

while read -r line
do
  export $line
done < ../.env

# local postgres instance
export POSTGRES_HOST=localhost
# local wiremock instance for ip api
export IPAPI_URL=http://localhost:8085/ip-api/json
# by removing IPAPI_URL default url will be used
#unset IPAPI_URL
java -Djava.net.preferIPv4Stack=true -jar target/coupon-service.jar