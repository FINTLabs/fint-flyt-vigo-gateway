FROM gradle:9.3.0-jdk21-corretto AS builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java21
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-flyt-vigo-gateway-*.jar /data/app.jar
CMD ["/data/app.jar"]
