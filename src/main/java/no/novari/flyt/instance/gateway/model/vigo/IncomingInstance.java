package no.novari.flyt.instance.gateway.model.vigo;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;


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
    private final Postadresse postadresse;
    private final Dokument dokument;
    private final Tilleggsinformasjon tilleggsinformasjon;
}
