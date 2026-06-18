package no.novari.flyt.instance.gateway;

import no.novari.flyt.gateway.webinstance.InstanceProcessor;
import no.novari.flyt.gateway.webinstance.InstanceProcessorFactoryService;
import no.novari.flyt.instance.gateway.model.vigo.IncomingInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstanceProcessorConfiguration {

    @Bean
    public InstanceProcessor<IncomingInstance> incomingInstanceProcessor(
            InstanceProcessorFactoryService instanceProcessorFactoryService,
            IncomingInstanceMappingService incomingInstanceMappingService
    ) {
        return instanceProcessorFactoryService.createInstanceProcessor(
                IncomingInstance::getDokumenttype,
                IncomingInstance::getInstansId,
                incomingInstanceMappingService
        );
    }
}
