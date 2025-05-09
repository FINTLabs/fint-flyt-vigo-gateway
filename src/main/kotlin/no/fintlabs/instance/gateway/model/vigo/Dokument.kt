package no.fintlabs.instance.gateway.model.vigo

import com.fasterxml.jackson.annotation.JsonInclude
import no.fintlabs.gateway.instance.validation.constraints.ValidBase64

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Dokument(
    val tittel: String? = null,
    val dato: String? = null,
    val filnavn: String? = null,
    @field:ValidBase64
    val fil: String? = null,
    val format: String? = null
)
