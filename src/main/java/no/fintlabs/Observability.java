package no.fintlabs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class Observability {

    private final Counter vigoOtDocumentCounter;

    public Observability(MeterRegistry registry) {
        vigoOtDocumentCounter = Counter.builder("vigo-gateway.ot.counter")
                .description("Number of documents received from VIGO")
                .register(registry);
    }

    public void incrementVigoDocumentCounters(String dokumenttype) {
        vigoOtDocumentCounter.increment();
    }
}
