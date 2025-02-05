package no.fintlabs.instance.gateway

import no.fintlabs.gateway.instance.InstanceProcessor
import no.fintlabs.gateway.instance.InstanceProcessorFactoryService
import no.fintlabs.instance.gateway.model.vigo.IncomingInstance
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.Optional

@Configuration
class InstanceProcessorConfiguration {

    @Bean
    fun incomingInstanceProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        incomingInstanceMappingService: IncomingInstanceMappingService
    ): InstanceProcessor<IncomingInstance> {
        return instanceProcessorFactoryService.createInstanceProcessor(
            { incomingInstance -> Optional.ofNullable(incomingInstance.dokumenttype) },
            { incomingInstance -> Optional.ofNullable(incomingInstance.instansId) },
            incomingInstanceMappingService
        )
    }
}
