package no.fintlabs.instance.gateway.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class Status {
    private final String instansId;
    private final String saksreferanse;
}
