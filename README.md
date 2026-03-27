# FINT FLYT VIGO Gateway

VIGO-specific FINT Flyt gateway. The service receives incoming instances, maps them to Flyt instance objects, uploads files, and publishes the result into the Flyt flow.

## Local development

- Java 21
- Gradle 9.1 wrapper
- `./gradlew build`
- `docker build .`

## Set up metadata with Bruno

1. Install Bruno: `brew install --cask bruno`
2. Open folder `bruno` in Bruno
3. Get `user_session` cookie from https://flyt.vigoiks.no (NB! To make this work, you need to disable 'Send Cookies automatically' in the settings).
