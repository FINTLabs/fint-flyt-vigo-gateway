package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.instance.gateway.model.vigo.IncomingInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;

@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/vigo/instances")
public class InstanceController {

    private final InstanceProcessor<IncomingInstance> instanceProcessor;

    public InstanceController(InstanceProcessor<IncomingInstance> instanceProcessor) {
        this.instanceProcessor = instanceProcessor;
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

}
