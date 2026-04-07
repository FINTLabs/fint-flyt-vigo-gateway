package no.novari.flyt.instance.gateway;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class Observability {

    private final Counter.Builder documentCounter;
    private final MeterRegistry meterRegistry;

    public Observability(MeterRegistry registry) {
        this.meterRegistry = registry;
        documentCounter = Counter.builder("vigo-gateway.document.counter")
                .description("Number of documents received from VIGO");
    }

    public void incrementVigoDocumentCounters(String dokumenttype) {
        documentCounter.tag("dokumenttype", dokumenttype)
                        .register(meterRegistry)
                .increment();
    }
}
