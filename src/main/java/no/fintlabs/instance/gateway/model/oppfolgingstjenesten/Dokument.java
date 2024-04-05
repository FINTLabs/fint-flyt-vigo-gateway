package no.fintlabs.instance.gateway.model.oppfolgingstjenesten;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.gateway.instance.validation.constraints.ValidBase64;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class Dokument {

    private String tittel;
    private String dato;
    private String filnavn;

    @ValidBase64
    private String fil;
    private String format;
}
