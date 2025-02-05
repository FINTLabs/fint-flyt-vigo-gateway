package no.fintlabs.instance.gateway.model.vigo

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Inntaksadresse(
    val gateadresse: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null
)
