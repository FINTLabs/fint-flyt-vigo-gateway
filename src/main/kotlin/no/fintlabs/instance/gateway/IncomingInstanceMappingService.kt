package no.fintlabs.instance.gateway

import no.fintlabs.gateway.instance.InstanceMapper
import no.fintlabs.gateway.instance.model.File
import no.fintlabs.gateway.instance.model.instance.InstanceObject
import no.fintlabs.instance.gateway.model.vigo.*
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.function.Function

@Service
class IncomingInstanceMappingService : InstanceMapper<IncomingInstance> {

    companion object {
        private val log = LoggerFactory.getLogger(IncomingInstanceMappingService::class.java)
        const val EMPTY_STRING = ""

        private fun createPersonalia(incomingInstance: IncomingInstance): Collection<Map.Entry<String, String>> {
            val personalia = incomingInstance.personalia
            return listOf(
                AbstractMap.SimpleEntry("personaliaFodselsnummer", personalia?.fodselsnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("personaliaFornavn", personalia?.fornavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("personaliaMellomnavn", personalia?.mellomnavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("personaliaEtternavn", personalia?.etternavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("personaliaFodselsdato", personalia?.fodselsdato ?: EMPTY_STRING)
            )
        }

        private fun createKontaktinformasjon(incomingInstance: IncomingInstance): Collection<Map.Entry<String, String>> {
            val kontaktinformasjon = incomingInstance.kontaktinformasjon
            return listOf(
                AbstractMap.SimpleEntry("kontaktinformasjonTelefonnummer", kontaktinformasjon?.telefonnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("kontaktinformasjonEpostadresse", kontaktinformasjon?.epostadresse ?: EMPTY_STRING)
            )
        }

        private fun createInntaksadresse(incomingInstance: IncomingInstance): Collection<Map.Entry<String, String>> {
            val inntaksadresse = incomingInstance.inntaksadresse
            return listOf(
                AbstractMap.SimpleEntry("inntaksadresseGateadresse", inntaksadresse?.gateadresse ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("inntaksadressePostnummer", inntaksadresse?.postnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("inntaksadressePoststed", inntaksadresse?.poststed ?: EMPTY_STRING)
            )
        }

        private fun createTilleggsinformasjon(incomingInstance: IncomingInstance): Collection<Map.Entry<String, String>> {
            val tillegg = incomingInstance.tilleggsinformasjon
            return listOf(
                AbstractMap.SimpleEntry("tilleggsinformasjonSkolear", tillegg?.skolear ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonSkolenummer", tillegg?.skolenummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonSkolenavn", tillegg?.skolenavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProgramomradekode", tillegg?.programomradekode ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProgramomradenavn", tillegg?.programomradenavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonSokertype", tillegg?.sokertype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonEndringstype", tillegg?.endringstype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonFylkessignatar", tillegg?.fylkessignatar ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKandidattype", tillegg?.kandidattype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKontraktsnummer", tillegg?.kontraktsnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKontraktspartEpost", tillegg?.kontraktspartEpost ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKontraktspartkode", tillegg?.kontraktspartkode ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKontraktspartnavn", tillegg?.kontraktspartnavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKontraktspartnummer", tillegg?.kontraktspartnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonKontraktstype", tillegg?.kontraktstype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonLastetOppAv", tillegg?.lastetOppAv ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonLastetOppDato", tillegg?.lastetOppDato ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonNemndnavn", tillegg?.nemndnavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonNemndnummer", tillegg?.nemndnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonOppmeldtDato", tillegg?.oppmeldtDato ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonOrganisasjonsnavn", tillegg?.organisasjonsnavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonOrganisasjonsnummer", tillegg?.organisasjonsnummer ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProgramomradekode", tillegg?.programomradekode ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProgramomradenavn", tillegg?.programomradenavn ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProvenr", tillegg?.provenr ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProvestatus", tillegg?.provestatus ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonProvetype", tillegg?.provetype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonResultatPraktisk", tillegg?.resultatPraktisk ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonResultatTeori", tillegg?.resultatTeori ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonSokertype", tillegg?.sokertype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonVedleggBeskrivelse", tillegg?.vedleggBeskrivelse ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonVedleggTittel", tillegg?.vedleggTittel ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonVedtaksresultat", tillegg?.vedtaksresultat ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonFnr", tillegg?.fnr ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonVgdoknr", tillegg?.vgdoknr ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonVgdoktype", tillegg?.vgdoktype ?: EMPTY_STRING),
                AbstractMap.SimpleEntry("tilleggsinformasjonUtsendtDato", tillegg?.utsendtDato ?: EMPTY_STRING)
            )
        }

        // 20242025 -> 2024/2025
        private fun formatedSkoleaar(skolear: String): String {
            return if (skolear.length == 8) {
                "${skolear.substring(0, 4)}/${skolear.substring(4)}"
            } else {
                log.warn("Not possible to format skoleaar: {}", skolear)
                skolear
            }
        }

        private fun formatedDate(fodselsdato: String, format: String): String {
            return try {
                LocalDate
                    .parse(fodselsdato, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern(format))
            } catch (e: DateTimeParseException) {
                EMPTY_STRING
            }
        }

        private fun toValuePerKey(incomingInstance: IncomingInstance, uuid: UUID?): Map<String, String> {
            val entries = mutableSetOf<Map.Entry<String, String>>()

            entries.addAll(createPersonalia(incomingInstance))
            entries.addAll(createInntaksadresse(incomingInstance))
            entries.addAll(createKontaktinformasjon(incomingInstance))
            entries.addAll(createTilleggsinformasjon(incomingInstance))

            incomingInstance.dokument?.let { dokument ->
                entries.add(AbstractMap.SimpleEntry("dokumentTittel", dokument.tittel ?: EMPTY_STRING))
                entries.add(AbstractMap.SimpleEntry("dokumentDato", dokument.dato ?: EMPTY_STRING))
                entries.add(AbstractMap.SimpleEntry("dokumentFilnavn", dokument.filnavn ?: EMPTY_STRING))
                entries.add(AbstractMap.SimpleEntry("dokumentFormat", dokument.format ?: EMPTY_STRING))
                uuid?.let { entries.add(AbstractMap.SimpleEntry("dokumentFil", it.toString())) }
            }

            entries.add(AbstractMap.SimpleEntry("tilpassetNavn1",
                listOf(
                    incomingInstance.personalia?.fornavn,
                    incomingInstance.personalia?.mellomnavn,
                    incomingInstance.personalia?.etternavn
                ).filterNotNull().filter(StringUtils::hasLength).joinToString(" ")
            ))

            entries.add(AbstractMap.SimpleEntry("tilpassetNavn2",
                listOf(
                    incomingInstance.personalia?.etternavn,
                    incomingInstance.personalia?.fornavn,
                    incomingInstance.personalia?.mellomnavn
                ).filterNotNull().filter(StringUtils::hasLength).joinToString(" ")
            ))

            entries.add(AbstractMap.SimpleEntry("tilpassetNavn3",
                listOf(
                    "${incomingInstance.personalia?.etternavn},",
                    incomingInstance.personalia?.fornavn,
                    incomingInstance.personalia?.mellomnavn
                ).filterNotNull().filter(StringUtils::hasLength).joinToString(" ")
            ))

            entries.add(AbstractMap.SimpleEntry("tilpassetFodselsdato1",
                incomingInstance.personalia?.fodselsdato?.let { formatedDate(it, "ddMMyy") } ?: EMPTY_STRING
            ))

            entries.add(AbstractMap.SimpleEntry("tilpassetFodselsdato2",
                incomingInstance.personalia?.fodselsdato?.let { formatedDate(it, "dd.MM.yyyy") } ?: EMPTY_STRING
            ))

            entries.add(AbstractMap.SimpleEntry("tilpassetSkolear",
                incomingInstance.tilleggsinformasjon?.skolear?.let { formatedSkoleaar(it) } ?: EMPTY_STRING
            ))

            return entries.associate { it.key to it.value }
        }
    }

    override fun map(
        sourceApplicationId: Long,
        incomingInstance: IncomingInstance,
        persistFile: Function<File, Mono<UUID>>
    ): Mono<InstanceObject> {
        return if (incomingInstance.dokument == null) {
            Mono.just(
                InstanceObject.builder()
                    .valuePerKey(toValuePerKey(incomingInstance, null))
                    .build()
            )
        } else {
            postFile(sourceApplicationId, incomingInstance, persistFile)
                .map { uuid ->
                    InstanceObject.builder()
                        .valuePerKey(toValuePerKey(incomingInstance, uuid))
                        .build()
                }
        }
    }

    private fun postFile(
        sourceApplicationId: Long,
        incomingInstance: IncomingInstance,
        persistFile: Function<File, Mono<UUID>>
    ): Mono<UUID> {
        val dokument = incomingInstance.dokument ?: throw IllegalStateException("Document is null")
        return persistFile.apply(
            File.builder()
                .name(dokument.filnavn ?: "")
                .type(MediaType.parseMediaType(dokument.format ?: "application/octet-stream"))
                .sourceApplicationId(sourceApplicationId)
                .sourceApplicationInstanceId(incomingInstance.instansId)
                .encoding("UTF-8")
                .base64Contents(dokument.fil ?: "")
                .build()
        )
    }
}
