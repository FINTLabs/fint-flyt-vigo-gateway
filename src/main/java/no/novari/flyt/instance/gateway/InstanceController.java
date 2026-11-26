package no.novari.flyt.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.gateway.webinstance.InstanceProcessor;
import no.novari.flyt.gateway.webinstance.HistoryClient;
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import no.novari.flyt.instance.gateway.model.Status;
import no.novari.flyt.instance.gateway.model.vigo.IncomingInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static no.novari.flyt.webresourceserver.UrlPaths.EXTERNAL_API;


@Slf4j
@RestController
@RequestMapping(EXTERNAL_API + "/vigo/instances")
public class InstanceController {

    private final InstanceProcessor<IncomingInstance> instanceProcessor;
    private final HistoryClient historyClient;
    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService;
    private final Observability observability;

    public InstanceController(
            InstanceProcessor<IncomingInstance> instanceProcessor,
            HistoryClient historyClient,
            SourceApplicationAuthorizationService sourceApplicationAuthorizationService,
            Observability observability
    ) {
        this.instanceProcessor = instanceProcessor;
        this.historyClient = historyClient;
        this.sourceApplicationAuthorizationService = sourceApplicationAuthorizationService;
        this.observability = observability;
    }

    @PostMapping("instance")
    public ResponseEntity<Void> postIncomingInstance(
            @RequestBody IncomingInstance incomingInstance,
            Authentication authentication
    ) {
        log.debug("Incoming instance: {}", incomingInstance.getInstansId());

        observability.incrementVigoDocumentCounters(incomingInstance.getDokumenttype());

        return instanceProcessor.processInstance(
                authentication,
                incomingInstance
        );
    }

    @GetMapping("status/{instanceId}")
    public ResponseEntity<Status> getInstanceStatus(
            Authentication authentication,
            @PathVariable String instanceId
    ) {
        long applicationId = sourceApplicationAuthorizationService.getSourceApplicationId(authentication);

        log.debug("Get status for instance: {} in sourceApplication: {}", instanceId, applicationId);

        String caseId = historyClient.getArchiveInstanceId(applicationId, instanceId);
        if (caseId != null) {
            return ResponseEntity.ok(Status.builder()
                    .instansId(instanceId)
                    .destinasjonsId(caseId)
                    .status("Instans godtatt av destinasjon")
                    .build());
        }

        return ResponseEntity
                .badRequest()
                .body(Status.builder()
                        .instansId(instanceId)
                        .status("Ukjent status")
                        .build());
    }

}
