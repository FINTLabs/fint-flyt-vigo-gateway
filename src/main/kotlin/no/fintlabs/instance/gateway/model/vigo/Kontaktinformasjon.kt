package no.fintlabs.instance.gateway.model.vigo

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Kontaktinformasjon(
    val epostadresse: String? = null,
    val telefonnummer: String? = null
)
