package no.fintlabs.instance.gateway.model.vigo

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncomingInstance(
    @field:NotNull
    val instansId: String,
    val dokumenttype: String? = null,
    val personalia: Personalia? = null,
    val kontaktinformasjon: Kontaktinformasjon? = null,
    val inntaksadresse: Inntaksadresse? = null,
    val dokument: Dokument? = null,
    val tilleggsinformasjon: Tilleggsinformasjon? = null
)
