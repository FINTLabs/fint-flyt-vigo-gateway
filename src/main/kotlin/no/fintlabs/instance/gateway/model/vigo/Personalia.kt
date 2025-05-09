package no.fintlabs.instance.gateway.model.vigo

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Personalia(
    val fodselsnummer: String? = null,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val fodselsdato: String? = null
)
