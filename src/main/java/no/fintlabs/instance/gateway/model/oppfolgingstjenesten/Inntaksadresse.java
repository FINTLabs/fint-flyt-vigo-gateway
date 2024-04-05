package no.fintlabs.instance.gateway.model.oppfolgingstjenesten;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class Inntaksadresse {

    private String gateadresse;
    private String postnummer;
    private String poststed;
}
