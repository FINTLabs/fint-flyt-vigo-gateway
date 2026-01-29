package no.novari.flyt.instance.gateway.model.vigo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class Personalia {

    private String fodselsnummer;
    private String fornavn;
    private String mellomnavn;
    private String etternavn;
    private String fodselsdato;
}
