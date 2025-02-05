package no.fintlabs.instance.gateway.model.vigo

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Tilleggsinformasjon(
    val endringstype: String? = null,
    val fylkessignatar: String? = null,
    val kandidattype: String? = null,
    val kontraktsnummer: String? = null,
    val kontraktspartEpost: String? = null,
    val kontraktspartkode: String? = null,
    val kontraktspartnavn: String? = null,
    val kontraktspartnummer: String? = null,
    val kontraktstype: String? = null,
    val lastetOppAv: String? = null,
    val lastetOppDato: String? = null,
    val nemndnavn: String? = null,
    val nemndnummer: String? = null,
    val oppmeldtDato: String? = null,
    val organisasjonsnavn: String? = null,
    val organisasjonsnummer: String? = null,
    val programomradekode: String? = null,
    val programomradenavn: String? = null,
    val provenr: String? = null,
    val provestatus: String? = null,
    val provetype: String? = null,
    val resultatPraktisk: String? = null,
    val resultatTeori: String? = null,
    val skolear: String? = null,
    val skolenavn: String? = null,
    val skolenummer: String? = null,
    val sokertype: String? = null,
    val vedleggBeskrivelse: String? = null,
    val vedleggTittel: String? = null,
    val vedtaksresultat: String? = null,
    
    // FAGDOK fields
    val fnr: String? = null,
    val vgdoknr: String? = null,
    val vgdoktype: String? = null,
    val utsendtDato: String? = null
)
