package no.novari.flyt.instance.gateway.model.vigo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.novari.flyt.instance.gateway.validation.constraints.ValidBase64;

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
