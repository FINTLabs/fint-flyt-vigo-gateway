package no.novari.flyt.instance.gateway.model.vigo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class Kontaktinformasjon {

    private String epostadresse;
    private String telefonnummer;
}
