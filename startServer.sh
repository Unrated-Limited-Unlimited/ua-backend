#! /bin/bash
export MICRONAUT_ENVIRONMENTS=prod
gradle build clean && gradle build && java -jar build/libs/unrated-0.1-all.jar