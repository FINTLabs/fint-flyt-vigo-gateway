FROM gradle:7.5-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java17
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/project/build/libs/fint-flyt-vigo-gateway-*.jar /data/app.jar
CMD ["/data/app.jar"]
