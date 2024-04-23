package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.kafka.ArchiveCaseIdRequestService;
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

    @GetMapping("{instanceId}/status")
    public Mono<ResponseEntity<String>> getStatus(
            @AuthenticationPrincipal Mono<Authentication> authenticationMono,
            @PathVariable String instanceId
    ) {
        return authenticationMono.map(authentication ->
                archiveCaseIdRequestService.getArchiveCaseId(
                                SourceApplicationAuthorizationUtil.getSourceApplicationId(authentication),
                                instanceId
                        )
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build())
        );
    }

}
