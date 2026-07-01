FROM gradle:jdk25 AS builder
USER root
COPY . .
RUN gradle --no-daemon build

FROM gcr.io/distroless/java25
ENV JAVA_TOOL_OPTIONS=-XX:+ExitOnOutOfMemoryError
COPY --from=builder /home/gradle/build/libs/fint-flyt-vigo-gateway-*.jar /data/app.jar
CMD ["/data/app.jar"]
