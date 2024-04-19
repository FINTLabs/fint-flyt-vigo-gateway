package no.fintlabs.instance.gateway.model.vigo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class Tilleggsinformasjon {

    private String skolear;
    private String skolenummer;
    private String skolenavn;
    private String programomradekode;
    private String programomradenavn;
    private String sokertype;
}
