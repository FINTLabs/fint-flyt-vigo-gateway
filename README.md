# FINT FLYT OPPFOLGINGSTJENETEN GATEWAY

## Overview
This code creates a gateway for VIGO OT.


### Kustomize Configuration
Adjust the following setting in the `base/flais.yaml`:
**`fint.flyt.resource-server.security.api.external.authorized-client-ids`**:
   - Set the `sourceApplicationId` provided by `fint-flyt-authorization-service` (the value should be an integer).
   - it ensures that requests coming from a specific client-id will only be allowed in if the ID is what `fint-flyt-authorization-service` says it should be.
   - this ID will be passed through FINT Flyt to ensure the data package received by this gateway belongs to the given SourceApplication

### Application Configuration
Configure the application settings in `src/main/resources/application-local-staging.yaml`:
  - **`server.port`**:
  - When testing locally, select server port 81xx where xx is the sourceApplicationId (with leading zero) e.g. if sourceApplicationId is 3, the port would be 8103
  - also change `fint.flyt.resource-server.security.api.external.authorized-client-ids` here

### Processing instances
You need to create an InstanceProcessor for each instance model that is to be processed. This is done using InstanceProcessorFactoryService. See example code in InstanceProcessorConfiguration.
When creating an InstanceProcessor, you need to provide an implementation of InstanceMapper. The mapper is used to convert the incoming model to the internal InstanceObject model in Fint Flyt. See example code in IncomingInstanceMappingService.

## Testing locally
Run docker-compose.yaml. This will create containers for local kafka broker. You can then view kafka messages here: http://localhost:19000/

Clone this repo: https://github.com/FINTLabs/fint-flyt-authorization-service and run this service locally. You need to provide an env. variable that is your client-id and client-secret. 
This service acts as a middleware that will intercept the request to your gateway and return a valid source-application-id. If this is not running, you will get a HTTP 403 forbidden on every request. Be aware, that if you restart you gateway or fint-flyt-authorization-service, you must wait 20-30 seconds for the service to sync messages over kafka locally. If you run the request too soon you will receive a 403.

## Additional Documentation
For more detailed instructions on setting up and deploying your instance of the FINT FLYT gateway, refer to the documentation:
https://fintlabs.atlassian.net/wiki/spaces/FINTKB/pages/379355169/FINT+Flyt+Arkitektur+event+driven

This is also where you document the SourceApplication (data models, requests, endpoints etc)
