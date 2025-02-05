package no.fintlabs.instance.gateway

import no.fintlabs.gateway.instance.InstanceProcessor
import no.fintlabs.gateway.instance.kafka.ArchiveCaseIdRequestService
import no.fintlabs.instance.gateway.model.Status
import no.fintlabs.instance.gateway.model.vigo.IncomingInstance
import no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("$EXTERNAL_API/vigo/instances")
class InstanceController(
    private val instanceProcessor: InstanceProcessor<IncomingInstance>,
    private val archiveCaseIdRequestService: ArchiveCaseIdRequestService,
    private val sourceApplicationAuthorizationService: SourceApplicationAuthorizationService
) {
    private val log = LoggerFactory.getLogger(InstanceController::class.java)

    @PostMapping("instance")
    fun postIncomingInstance(
        @RequestBody incomingInstance: IncomingInstance,
        @AuthenticationPrincipal authenticationMono: Mono<Authentication>
    ): Mono<ResponseEntity<*>> {
        log.debug("Incoming instance: {}", incomingInstance.instansId)

        return authenticationMono.flatMap { authentication ->
            instanceProcessor.processInstance(
                authentication,
                incomingInstance
            )
        }
    }
}
