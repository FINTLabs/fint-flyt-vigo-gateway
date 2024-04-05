package no.fintlabs.instance.gateway;

import no.fintlabs.gateway.instance.InstanceProcessor;
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService;
import no.fintlabs.instance.gateway.model.oppfolgingstjenesten.IncomingInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<IncomingInstance> incomingInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            IncomingInstanceMappingService incomingInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                "journalpost",
                incomingInstance -> Optional.ofNullable(incomingInstance.getInstansId()),
                incomingInstanceMappingService
        );
    }


}
