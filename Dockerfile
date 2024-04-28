FROM openjdk:17

COPY . .

# Expose the port your app runs on
EXPOSE 8000

# Command to run your application
CMD ["java", "-jar", "build/libs/unrated-0.1-all.jar"]

