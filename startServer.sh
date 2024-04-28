#! /bin/bash
export MICRONAUT_ENVIRONMENTS=prod
/home/ulu/.sdkman/candidates/gradle/current/bin/gradle build clean && /home/ulu/.sdkman/candidates/gradle/current/bin/gradle build
docker build -t ua-backend .
docker stop ua-backend
docker remove ua-backend
docker run --network host --restart=always --name ua-backend -p 8000:8000 -e MICRONAUT_ENVIRONMENTS=prod -e DB_PASSWORD=$1 -e ADMIN_PASS=$1 -d ua-backend:latest

