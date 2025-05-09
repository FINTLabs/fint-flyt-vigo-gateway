package no.fintlabs.instance.gateway

import no.fintlabs.gateway.instance.model.File
import no.fintlabs.gateway.instance.model.instance.InstanceObject
import no.fintlabs.instance.gateway.model.vigo.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Function
import org.junit.jupiter.api.Assertions.*
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`

class IncomingInstanceMappingServiceTest {
    @Mock
    private lateinit var persistFile: Function<File, Mono<UUID>>

    @InjectMocks
    private lateinit var incomingInstanceMappingService: IncomingInstanceMappingService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(persistFile.apply(any(File::class.java))).thenReturn(Mono.just(UUID.randomUUID()))
    }

    @Test
    fun shouldReturnInstanceObjectWhenIncomingInstanceIsValid() {
        val uuid = UUID.randomUUID()
        `when`(persistFile.apply(any(File::class.java))).thenReturn(Mono.just(uuid))

        val result = incomingInstanceMappingService
            .map(
                4L,
                createValidIncomingInstanceWithTilleggsinformasjon(),
                persistFile
            ).block()!!

        val valuePerKey = result.valuePerKey
        assertEquals("Ola", valuePerKey["personaliaFornavn"])
        assertEquals("Nordmann", valuePerKey["personaliaMellomnavn"])
        assertEquals("Nordmannsen", valuePerKey["personaliaEtternavn"])
        assertEquals("12345678901", valuePerKey["personaliaFodselsnummer"])
        assertEquals("1980-12-31", valuePerKey["personaliaFodselsdato"])

        assertEquals("12345678", valuePerKey["kontaktinformasjonTelefonnummer"])
        assertEquals("ola@normann.no", valuePerKey["kontaktinformasjonEpostadresse"])

        assertEquals("Osloveien 1", valuePerKey["inntaksadresseGateadresse"])
        assertEquals("1234", valuePerKey["inntaksadressePostnummer"])
        assertEquals("Oslo", valuePerKey["inntaksadressePoststed"])

        assertEquals("Dokumenttittel", valuePerKey["dokumentTittel"])
        assertEquals("2021-01-01", valuePerKey["dokumentDato"])
        assertEquals("dokument.pdf", valuePerKey["dokumentFilnavn"])
        assertEquals("text/plain", valuePerKey["dokumentFormat"])
        assertEquals(uuid.toString(), valuePerKey["dokumentFil"])

        assertEquals("20242025", valuePerKey["tilleggsinformasjonSkolear"])
        assertEquals("1", valuePerKey["tilleggsinformasjonSkolenummer"])
        assertEquals("Oslo katedralskole", valuePerKey["tilleggsinformasjonSkolenavn"])
        assertEquals("LA1", valuePerKey["tilleggsinformasjonProgramomradekode"])
        assertEquals("Latin", valuePerKey["tilleggsinformasjonProgramomradenavn"])
        assertEquals("Ordinær", valuePerKey["tilleggsinformasjonSokertype"])
    }

    @Test
    fun shouldReturnInstanceObjectWhenIncomingInstanceDoesNotHaveTilleggsinformasjon() {
        val uuid = UUID.randomUUID()
        `when`(persistFile.apply(any(File::class.java))).thenReturn(Mono.just(uuid))

        val result = incomingInstanceMappingService
            .map(
                4L,
                createIncomingInstance(),
                persistFile
            ).block()!!

        val valuePerKey = result.valuePerKey
        assertEquals("Ola", valuePerKey["personaliaFornavn"])
        assertEquals("Nordmann", valuePerKey["personaliaMellomnavn"])
        assertEquals("Nordmannsen", valuePerKey["personaliaEtternavn"])
        assertEquals("12345678901", valuePerKey["personaliaFodselsnummer"])

        assertEquals("12345678", valuePerKey["kontaktinformasjonTelefonnummer"])
        assertEquals("ola@normann.no", valuePerKey["kontaktinformasjonEpostadresse"])

        assertEquals("Osloveien 1", valuePerKey["inntaksadresseGateadresse"])
        assertEquals("1234", valuePerKey["inntaksadressePostnummer"])
        assertEquals("Oslo", valuePerKey["inntaksadressePoststed"])

        assertEquals("Dokumenttittel", valuePerKey["dokumentTittel"])
        assertEquals("2021-01-01", valuePerKey["dokumentDato"])
        assertEquals("dokument.pdf", valuePerKey["dokumentFilnavn"])
        assertEquals("text/plain", valuePerKey["dokumentFormat"])
        assertEquals(uuid.toString(), valuePerKey["dokumentFil"])

        assertEquals("", valuePerKey["tilleggsinformasjonSkolenavn"])
        assertEquals("", valuePerKey["tilleggsinformasjonSkolenummer"])
        assertEquals("", valuePerKey["tilleggsinformasjonProgramomradekode"])
        assertEquals("", valuePerKey["tilleggsinformasjonProgramomradenavn"])
        assertEquals("", valuePerKey["tilleggsinformasjonSokertype"])
    }

    @Test
    fun shouldReturnInstanceObjectWhenIncomingInstanceDoesHaveSomeTilleggsinformasjon() {
        val uuid = UUID.randomUUID()
        `when`(persistFile.apply(any(File::class.java))).thenReturn(Mono.just(uuid))

        val result = incomingInstanceMappingService.map(
            4L,
            createValidIncomingInstanceWithSomeTilleggsinformasjon(),
            persistFile
        ).block()!!

        val valuePerKey = result.valuePerKey
        assertEquals("Ola", valuePerKey["personaliaFornavn"])
        assertEquals("Nordmann", valuePerKey["personaliaMellomnavn"])
        assertEquals("Nordmannsen", valuePerKey["personaliaEtternavn"])
        assertEquals("12345678901", valuePerKey["personaliaFodselsnummer"])

        assertEquals("12345678", valuePerKey["kontaktinformasjonTelefonnummer"])
        assertEquals("ola@normann.no", valuePerKey["kontaktinformasjonEpostadresse"])

        assertEquals("Osloveien 1", valuePerKey["inntaksadresseGateadresse"])
        assertEquals("1234", valuePerKey["inntaksadressePostnummer"])
        assertEquals("Oslo", valuePerKey["inntaksadressePoststed"])
    }

    @Test
    fun shouldReturnInstanceObjectWhenIncomingInstanceDoesNotHaveDocument() {
        val uuid = UUID.randomUUID()
        `when`(persistFile.apply(any(File::class.java))).thenReturn(Mono.just(uuid))

        val result = incomingInstanceMappingService
            .map(
                4L,
                createIncomingInstanceWithoutDocument(),
                persistFile
            ).block()!!

        val valuePerKey = result.valuePerKey
        assertEquals("Ola", valuePerKey["personaliaFornavn"])
        assertEquals("Nordmann", valuePerKey["personaliaMellomnavn"])
        assertEquals("Nordmannsen", valuePerKey["personaliaEtternavn"])
        assertEquals("12345678901", valuePerKey["personaliaFodselsnummer"])

        assertEquals("12345678", valuePerKey["kontaktinformasjonTelefonnummer"])
        assertEquals("ola@normann.no", valuePerKey["kontaktinformasjonEpostadresse"])

        assertEquals("Osloveien 1", valuePerKey["inntaksadresseGateadresse"])
        assertEquals("1234", valuePerKey["inntaksadressePostnummer"])
        assertEquals("Oslo", valuePerKey["inntaksadressePoststed"])

        assertNull(valuePerKey["dokumentTittel"])
        assertNull(valuePerKey["dokumentDato"])
        assertNull(valuePerKey["dokumentFilnavn"])
        assertNull(valuePerKey["dokumentFormat"])
        assertNull(valuePerKey["dokumentFil"])
    }

    @Test
    fun shouldAcceptFodselsdatoAsNull() {
        val result = incomingInstanceMappingService
            .map(
                4L,
                createValidIncomingInstanceWithFodselsdatoAsNull(),
                persistFile
            ).block()!!

        assertEquals("", result.valuePerKey["personaliaFodselsdato"])
    }

    @Test
    fun shouldCreateCustomizedFodselsdato() {
        val result = incomingInstanceMappingService
            .map(
                4L,
                createIncomingInstance(),
                persistFile
            ).block()!!

        assertEquals("311280", result.valuePerKey["tilpassetFodselsdato1"])
        assertEquals("31.12.1980", result.valuePerKey["tilpassetFodselsdato2"])
    }

    @Test
    fun shouldCreateCustomizedFullName() {
        val result = incomingInstanceMappingService
            .map(
                4L,
                createIncomingInstance(),
                persistFile
            ).block()!!

        assertEquals("Ola Nordmann Nordmannsen", result.valuePerKey["tilpassetNavn1"])
        assertEquals("Nordmannsen Ola Nordmann", result.valuePerKey["tilpassetNavn2"])
        assertEquals("Nordmannsen, Ola Nordmann", result.valuePerKey["tilpassetNavn3"])
    }

    @Test
    fun shouldCreateCustomizedFullNameWithoutMiddleName() {
        val result = incomingInstanceMappingService
            .map(4L, IncomingInstance(
                instansId = "12345",
                personalia = Personalia(
                    fodselsnummer = "12345678901",
                    fornavn = "Ola",
                    etternavn = "Nordmannsen",
                    fodselsdato = "19-12-3100"
                )
            ), persistFile).block()!!

        assertEquals("Ola Nordmannsen", result.valuePerKey["tilpassetNavn1"])
        assertEquals("Nordmannsen Ola", result.valuePerKey["tilpassetNavn2"])
        assertEquals("Nordmannsen, Ola", result.valuePerKey["tilpassetNavn3"])
    }

    @Test
    fun shouldCreateCustomizedFullNameWithEmptyMiddleName() {
        val result = incomingInstanceMappingService
            .map(4L, IncomingInstance(
                instansId = "12345",
                personalia = Personalia(
                    fodselsnummer = "12345678901",
                    fornavn = "Ola",
                    mellomnavn = "",
                    etternavn = "Nordmannsen",
                    fodselsdato = "19-12-3100"
                )
            ), persistFile).block()!!

        assertEquals("Ola Nordmannsen", result.valuePerKey["tilpassetNavn1"])
        assertEquals("Nordmannsen Ola", result.valuePerKey["tilpassetNavn2"])
        assertEquals("Nordmannsen, Ola", result.valuePerKey["tilpassetNavn3"])
    }

    @Test
    fun shouldCreateCustomizedFullNameWithNullMiddleName() {
        val result = incomingInstanceMappingService
            .map(4L, IncomingInstance(
                instansId = "12345",
                personalia = Personalia(
                    fodselsnummer = "12345678901",
                    fornavn = "Ola",
                    mellomnavn = null,
                    etternavn = "Nordmannsen",
                    fodselsdato = "19-12-3100"
                )
            ), persistFile).block()!!

        assertEquals("Ola Nordmannsen", result.valuePerKey["tilpassetNavn1"])
        assertEquals("Nordmannsen Ola", result.valuePerKey["tilpassetNavn2"])
        assertEquals("Nordmannsen, Ola", result.valuePerKey["tilpassetNavn3"])
    }

    @Test
    fun shouldNotAcceptInvalidFodselsdato() {
        val result = incomingInstanceMappingService
            .map(4L, IncomingInstance(
                instansId = "12345",
                personalia = Personalia(
                    fodselsnummer = "12345678901",
                    fornavn = "Ola",
                    mellomnavn = "Nordmann",
                    etternavn = "Nordmannsen",
                    fodselsdato = "19-12-3100"
                )
            ), persistFile).block()!!

        assertEquals("", result.valuePerKey["tilpassetFodselsdato1"])
        assertEquals("", result.valuePerKey["tilpassetFodselsdato2"])
    }

    @Test
    fun shouldNotReturnInstansId() {
        val result = incomingInstanceMappingService
            .map(
                4L,
                createValidIncomingInstanceWithTilleggsinformasjon(),
                persistFile
            ).block()!!

        assertFalse(result.valuePerKey.containsKey("instansId"))
    }

    private fun createIncomingInstance() = IncomingInstance(
        instansId = "12345",
        personalia = Personalia(
            fodselsnummer = "12345678901",
            fornavn = "Ola",
            mellomnavn = "Nordmann",
            etternavn = "Nordmannsen",
            fodselsdato = "1980-12-31"
        ),
        kontaktinformasjon = Kontaktinformasjon(
            telefonnummer = "12345678",
            epostadresse = "ola@normann.no"
        ),
        inntaksadresse = Inntaksadresse(
            gateadresse = "Osloveien 1",
            postnummer = "1234",
            poststed = "Oslo"
        ),
        dokument = Dokument(
            tittel = "Dokumenttittel",
            dato = "2021-01-01",
            filnavn = "dokument.pdf",
            format = "text/plain"
        ),
        tilleggsinformasjon = Tilleggsinformasjon(
            skolenavn = ""
        )
    )

    private fun createValidIncomingInstanceWithTilleggsinformasjon() = createIncomingInstance().copy(
        tilleggsinformasjon = Tilleggsinformasjon(
            skolear = "20242025",
            skolenummer = "1",
            skolenavn = "Oslo katedralskole",
            programomradekode = "LA1",
            programomradenavn = "Latin",
            sokertype = "Ordinær"
        )
    )

    private fun createValidIncomingInstanceWithSomeTilleggsinformasjon() = IncomingInstance(
        instansId = "12345",
        personalia = Personalia(
            fodselsnummer = "12345678901",
            fornavn = "Ola",
            mellomnavn = "Nordmann",
            etternavn = "Nordmannsen"
        ),
        kontaktinformasjon = Kontaktinformasjon(
            telefonnummer = "12345678",
            epostadresse = "ola@normann.no"
        ),
        inntaksadresse = Inntaksadresse(
            gateadresse = "Osloveien 1",
            postnummer = "1234",
            poststed = "Oslo"
        ),
        dokument = Dokument(
            tittel = "Dokumenttittel",
            dato = "2021-01-01",
            filnavn = "dokument.pdf",
            format = "text/plain"
        ),
        tilleggsinformasjon = Tilleggsinformasjon(
            skolear = "20242025",
            skolenavn = "Oslo katedralskole"
        )
    )

    private fun createValidIncomingInstanceWithFodselsdatoAsNull() = createIncomingInstance().copy(
        personalia = createIncomingInstance().personalia?.copy(fodselsdato = null) ?: Personalia(
            fodselsnummer = "12345678901",
            fornavn = "Ola",
            mellomnavn = "Nordmann",
            etternavn = "Nordmannsen"
        )
    )

    private fun createIncomingInstanceWithoutDocument() = IncomingInstance(
        instansId = "12345",
        personalia = Personalia(
            fodselsnummer = "12345678901",
            fornavn = "Ola",
            mellomnavn = "Nordmann",
            etternavn = "Nordmannsen"
        ),
        kontaktinformasjon = Kontaktinformasjon(
            telefonnummer = "12345678",
            epostadresse = "ola@normann.no"
        ),
        inntaksadresse = Inntaksadresse(
            gateadresse = "Osloveien 1",
            postnummer = "1234",
            poststed = "Oslo"
        )
    )
}
