package no.fintlabs.instance.gateway.model.oppfolgingstjenesten;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;


@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class IncomingInstance {

    @NotNull
    private final String instansId;
    private final String dokumenttype;
    private final Personalia personalia;
    private final Kontaktinformasjon kontaktinformasjon;
    private final Inntaksadresse inntaksadresse;
    private final Dokument dokument;
}
