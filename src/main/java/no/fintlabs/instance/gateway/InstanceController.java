package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseIdRequestService;
import no.fintlabs.instance.gateway.model.Status;
import no.fintlabs.instance.gateway.model.vigo.IncomingInstance;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/vigo/instances")
public class InstanceController {

    private final InstanceProcessor<IncomingInstance> instanceProcessor;

    private final ArchiveCaseIdRequestService archiveCaseIdRequestService;

    public InstanceController(InstanceProcessor<IncomingInstance> instanceProcessor,
                              ArchiveCaseIdRequestService archiveCaseIdRequestService) {
        this.instanceProcessor = instanceProcessor;
        this.archiveCaseIdRequestService = archiveCaseIdRequestService;
    }

    @PostMapping("instance")
    public Mono<ResponseEntity<?>> postIncomingInstance(
            @RequestBody IncomingInstance incomingInstance,
            @AuthenticationPrincipal Mono<Authentication> authenticationMono
    ) {
        log.debug("Incoming instance: {}", incomingInstance.getInstansId());

        return authenticationMono.flatMap(
                authentication -> instanceProcessor.processInstance(
                        authentication,
                        incomingInstance
                )
        );
    }

    @GetMapping("status/{instanceId}")
    public Mono<ResponseEntity<Status>> getInstanceStatus(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String instanceId
    ) {
        return authenticationMono.map(authentication ->
                {
                    Long applicationId = SourceApplicationAuthorizationUtil.getSourceApplicationId(authentication);

                    log.debug("Get status for instance: {} in sourceApplication: {}", instanceId, applicationId);

                    return archiveCaseIdRequestService.getArchiveCaseId(applicationId, instanceId)
                            .map(caseId -> ResponseEntity.ok(toStatus(instanceId, caseId)))
                            .orElse(ResponseEntity.notFound().build());
                }
        );
    }

    private static Status toStatus(String instanceId, String caseId) {
        return Status.builder()
                .instansId(instanceId)
                .saksreferanse(caseId)
                .build();
    }

}
