package no.fintlabs.instance.gateway.model.vigo;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder
public class Tilleggsinformasjon {

    private String endringstype;
    private String fylkessignatar;
    private String kandidattype;
    private String kontraktsnummer;
    private String kontraktspartEpost;
    private String kontraktspartkode;
    private String kontraktspartnavn;
    private String kontraktspartnummer;
    private String kontraktstype;
    private String lastetOppAv;
    private String lastetOppDato;
    private String nemndnavn;
    private String nemndnummer;
    private String oppmeldtDato;
    private String organisasjonsnavn;
    private String organisasjonsnummer;
    private String programomradekode;
    private String programomradenavn;
    private String provenr;
    private String provestatus;
    private String provetype;
    private String resultatPraktisk;
    private String resultatTeori;
    private String skolear;
    private String skolenavn;
    private String skolenummer;
    private String sokertype;
    private String vedleggBeskrivelse;
    private String vedleggTittel;
    private String vedtaksresultat;

    // FAGDOK fields - may change in the future
    private String fnr;
    private String vgdoknr;
    private String vgdoktype;
    private String utsendtDato;
}
