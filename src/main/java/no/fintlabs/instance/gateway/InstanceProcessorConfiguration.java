package no.fintlabs.instance.gateway;

import no.fintlabs.instance.gateway.model.vigo.IncomingInstance;
import no.novari.flyt.instance.gateway.InstanceProcessor;
import no.novari.flyt.instance.gateway.InstanceProcessorFactoryService;
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
                incomingInstance -> Optional.ofNullable(incomingInstance.getDokumenttype()),
                incomingInstance -> Optional.ofNullable(incomingInstance.getInstansId()),
                incomingInstanceMappingService
        );
    }
}
