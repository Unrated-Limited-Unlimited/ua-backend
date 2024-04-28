#! /bin/bash
export MICRONAUT_ENVIRONMENTS=prod
gradle build clean && gradle build
docker build -t ua-backend .
docker stop ua-backend
docker remove ua-backend
docker run --restart=always --name ua-backend -p 8000:8000 -d ua-backend:latest -e MICRONAUT_ENVIRONMENTS=prod -e DB_PASSWORD=$1 ADMIN_PASS=$1

