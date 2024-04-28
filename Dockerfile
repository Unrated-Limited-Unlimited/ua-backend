FROM adoptopenjdk/openjdk17:alpine-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8000

# Command to run your application
CMD ["java", "-jar", "app.jar"]

