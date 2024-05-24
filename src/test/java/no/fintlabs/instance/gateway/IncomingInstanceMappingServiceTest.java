package no.fintlabs.instance.gateway;

import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.instance.gateway.model.vigo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class IncomingInstanceMappingServiceTest {

    @Mock
    private FileClient fileClient;

    @InjectMocks
    private IncomingInstanceMappingService incomingInstanceMappingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnInstanceObjectWhenIncomingInstanceIsValid() {
        UUID uuid = UUID.randomUUID();
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(uuid));

        InstanceObject result = incomingInstanceMappingService.map(4L, createValidIncomingInstanceWithTilleggsinformasjon()).block();

        Map<String, String> valuePerKey = result.getValuePerKey();
        assertEquals("Ola", valuePerKey.get("personaliaFornavn"));
        assertEquals("Nordmann", valuePerKey.get("personaliaMellomnavn"));
        assertEquals("Nordmannsen", valuePerKey.get("personaliaEtternavn"));
        assertEquals("12345678901", valuePerKey.get("personaliaFodselsnummer"));

        assertEquals("12345678", valuePerKey.get("kontaktinformasjonTelefonnummer"));
        assertEquals("ola@normann.no", valuePerKey.get("kontaktinformasjonEpostadresse"));

        assertEquals("Osloveien 1", valuePerKey.get("inntaksadresseGateadresse"));
        assertEquals("1234", valuePerKey.get("inntaksadressePostnummer"));
        assertEquals("Oslo", valuePerKey.get("inntaksadressePoststed"));

        assertEquals("Dokumenttittel", valuePerKey.get("dokumentTittel"));
        assertEquals("2021-01-01", valuePerKey.get("dokumentDato"));
        assertEquals("dokument.pdf", valuePerKey.get("dokumentFilnavn"));
        assertEquals("text/plain", valuePerKey.get("dokumentFormat"));
        assertEquals(uuid.toString(), valuePerKey.get("dokumentFil"));

        assertEquals("2024/2025", valuePerKey.get("tilleggsinformasjonSkolear"));
        assertEquals("1", valuePerKey.get("tilleggsinformasjonSkolenummer"));
        assertEquals("Oslo katedralskole", valuePerKey.get("tilleggsinformasjonSkolenavn"));
        assertEquals("LA1", valuePerKey.get("tilleggsinformasjonProgramomradekode"));
        assertEquals("Latin", valuePerKey.get("tilleggsinformasjonProgramomradenavn"));
        assertEquals("Ordinær", valuePerKey.get("tilleggsinformasjonSokertype"));
    }

    @Test
    void shouldReturnInstanceObjectWhenIncomingInstanceDoesNotHaveTilleggsinformasjon() {
        UUID uuid = UUID.randomUUID();
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(uuid));

        InstanceObject result = incomingInstanceMappingService.map(4L, createIncomingInstanceWithoutTilleggsinformasjon()).block();

        Map<String, String> valuePerKey = result.getValuePerKey();
        assertEquals("Ola", valuePerKey.get("personaliaFornavn"));
        assertEquals("Nordmann", valuePerKey.get("personaliaMellomnavn"));
        assertEquals("Nordmannsen", valuePerKey.get("personaliaEtternavn"));
        assertEquals("12345678901", valuePerKey.get("personaliaFodselsnummer"));

        assertEquals("12345678", valuePerKey.get("kontaktinformasjonTelefonnummer"));
        assertEquals("ola@normann.no", valuePerKey.get("kontaktinformasjonEpostadresse"));

        assertEquals("Osloveien 1", valuePerKey.get("inntaksadresseGateadresse"));
        assertEquals("1234", valuePerKey.get("inntaksadressePostnummer"));
        assertEquals("Oslo", valuePerKey.get("inntaksadressePoststed"));

        assertEquals("Dokumenttittel", valuePerKey.get("dokumentTittel"));
        assertEquals("2021-01-01", valuePerKey.get("dokumentDato"));
        assertEquals("dokument.pdf", valuePerKey.get("dokumentFilnavn"));
        assertEquals("text/plain", valuePerKey.get("dokumentFormat"));
        assertEquals(uuid.toString(), valuePerKey.get("dokumentFil"));
    }

    @Test
    void shouldReturnInstanceObjectWhenIncomingInstanceDoesHaveSomeTilleggsinformasjon() {
        UUID uuid = UUID.randomUUID();
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(uuid));

        InstanceObject result = incomingInstanceMappingService.map(4L, createValidIncomingInstanceWithSomeTilleggsinformasjon()).block();

        Map<String, String> valuePerKey = result.getValuePerKey();
        assertEquals("Ola", valuePerKey.get("personaliaFornavn"));
        assertEquals("Nordmann", valuePerKey.get("personaliaMellomnavn"));
        assertEquals("Nordmannsen", valuePerKey.get("personaliaEtternavn"));
        assertEquals("12345678901", valuePerKey.get("personaliaFodselsnummer"));

        assertEquals("12345678", valuePerKey.get("kontaktinformasjonTelefonnummer"));
        assertEquals("ola@normann.no", valuePerKey.get("kontaktinformasjonEpostadresse"));

        assertEquals("Osloveien 1", valuePerKey.get("inntaksadresseGateadresse"));
        assertEquals("1234", valuePerKey.get("inntaksadressePostnummer"));
        assertEquals("Oslo", valuePerKey.get("inntaksadressePoststed"));

        assertEquals("2024/2025", valuePerKey.get("tilleggsinformasjonSkolear"));
        assertEquals("Oslo katedralskole", valuePerKey.get("tilleggsinformasjonSkolenavn"));
        assertNull(valuePerKey.get("tilleggsinformasjonSkolenummer"));
    }

    //@Test - Prøver oss med fiktivt dokument på instanser uten dokument
    void shouldReturnInstanceObjectWhenIncomingInstanceDoesNotHaveDocument() {
        UUID uuid = UUID.randomUUID();
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(uuid));

        InstanceObject result = incomingInstanceMappingService.map(4L, createIncomingInstanceWithoutDocument()).block();

        Map<String, String> valuePerKey = result.getValuePerKey();
        assertEquals("Ola", valuePerKey.get("personaliaFornavn"));
        assertEquals("Nordmann", valuePerKey.get("personaliaMellomnavn"));
        assertEquals("Nordmannsen", valuePerKey.get("personaliaEtternavn"));
        assertEquals("12345678901", valuePerKey.get("personaliaFodselsnummer"));

        assertEquals("12345678", valuePerKey.get("kontaktinformasjonTelefonnummer"));
        assertEquals("ola@normann.no", valuePerKey.get("kontaktinformasjonEpostadresse"));

        assertEquals("Osloveien 1", valuePerKey.get("inntaksadresseGateadresse"));
        assertEquals("1234", valuePerKey.get("inntaksadressePostnummer"));
        assertEquals("Oslo", valuePerKey.get("inntaksadressePoststed"));

        assertNull(valuePerKey.get("dokumentTittel"));
        assertNull(valuePerKey.get("dokumentDato"));
        assertNull(valuePerKey.get("dokumentFilnavn"));
        assertNull(valuePerKey.get("dokumentFormat"));
        assertNull(valuePerKey.get("dokumentFil"));
    }

    @Test
    void shouldNotReturnInstansId(){
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(UUID.randomUUID()));

        InstanceObject result = incomingInstanceMappingService.map(4L, createValidIncomingInstanceWithTilleggsinformasjon()).block();

        assertFalse(result.getValuePerKey().containsKey("instansId"));
    }

    private IncomingInstance createValidIncomingInstanceWithTilleggsinformasjon() {
        return IncomingInstance.builder()
                .instansId("12345")
                .personalia(Personalia.builder()
                        .fodselsnummer("12345678901")
                        .fornavn("Ola")
                        .mellomnavn("Nordmann")
                        .etternavn("Nordmannsen")
                        .build())

                .kontaktinformasjon(Kontaktinformasjon.builder()
                        .telefonnummer("12345678")
                        .epostadresse("ola@normann.no")
                        .build())

                .inntaksadresse(Inntaksadresse.builder()
                        .gateadresse("Osloveien 1")
                        .postnummer("1234")
                        .poststed("Oslo")
                        .build())

                .dokument(Dokument.builder()
                        .tittel("Dokumenttittel")
                        .dato("2021-01-01")
                        .filnavn("dokument.pdf")
                        .format("text/plain")
                        .build())

                .tilleggsinformasjon(Tilleggsinformasjon.builder()
                        .skolear("2024/2025")
                        .skolenummer("1")
                        .skolenavn("Oslo katedralskole")
                        .programomradekode("LA1")
                        .programomradenavn("Latin")
                        .sokertype("Ordinær")
                        .build())
                .build();
    }

    private IncomingInstance createValidIncomingInstanceWithSomeTilleggsinformasjon() {
        return IncomingInstance.builder()
                .instansId("12345")
                .personalia(Personalia.builder()
                        .fodselsnummer("12345678901")
                        .fornavn("Ola")
                        .mellomnavn("Nordmann")
                        .etternavn("Nordmannsen")
                        .build())

                .kontaktinformasjon(Kontaktinformasjon.builder()
                        .telefonnummer("12345678")
                        .epostadresse("ola@normann.no")
                        .build())

                .inntaksadresse(Inntaksadresse.builder()
                        .gateadresse("Osloveien 1")
                        .postnummer("1234")
                        .poststed("Oslo")
                        .build())

                .dokument(Dokument.builder()
                        .tittel("Dokumenttittel")
                        .dato("2021-01-01")
                        .filnavn("dokument.pdf")
                        .format("text/plain")
                        .build())

                .tilleggsinformasjon(Tilleggsinformasjon.builder()
                        .skolear("2024/2025")
                        .skolenavn("Oslo katedralskole")
                        .build())
                .build();
    }

    private IncomingInstance createIncomingInstanceWithoutTilleggsinformasjon() {
        return IncomingInstance.builder()
                .instansId("12345")
                .personalia(Personalia.builder()
                        .fodselsnummer("12345678901")
                        .fornavn("Ola")
                        .mellomnavn("Nordmann")
                        .etternavn("Nordmannsen")
                        .build())

                .kontaktinformasjon(Kontaktinformasjon.builder()
                        .telefonnummer("12345678")
                        .epostadresse("ola@normann.no")
                        .build())

                .inntaksadresse(Inntaksadresse.builder()
                        .gateadresse("Osloveien 1")
                        .postnummer("1234")
                        .poststed("Oslo")
                        .build())

                .dokument(Dokument.builder()
                        .tittel("Dokumenttittel")
                        .dato("2021-01-01")
                        .filnavn("dokument.pdf")
                        .format("text/plain")
                        .build())

                .build();
    }

    private IncomingInstance createIncomingInstanceWithoutDocument() {
        return IncomingInstance.builder()
                .instansId("12345")
                .personalia(Personalia.builder()
                        .fodselsnummer("12345678901")
                        .fornavn("Ola")
                        .mellomnavn("Nordmann")
                        .etternavn("Nordmannsen")
                        .build())

                .kontaktinformasjon(Kontaktinformasjon.builder()
                        .telefonnummer("12345678")
                        .epostadresse("ola@normann.no")
                        .build())

                .inntaksadresse(Inntaksadresse.builder()
                        .gateadresse("Osloveien 1")
                        .postnummer("1234")
                        .poststed("Oslo")
                        .build())

                .build();
    }
}