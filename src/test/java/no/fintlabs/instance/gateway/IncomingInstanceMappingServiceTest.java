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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(UUID.randomUUID()));
    }

    @Test
    void shouldReturnInstanceObjectWhenIncomingInstanceIsValid() {
        UUID uuid = UUID.randomUUID();
        when(fileClient.postFile(any(File.class))).thenReturn(Mono.just(uuid));

        InstanceObject result = incomingInstanceMappingService
                .map(4L, createValidIncomingInstanceWithTilleggsinformasjon().build()).block();

        Map<String, String> valuePerKey = result.getValuePerKey();
        assertEquals("Ola", valuePerKey.get("personaliaFornavn"));
        assertEquals("Nordmann", valuePerKey.get("personaliaMellomnavn"));
        assertEquals("Nordmannsen", valuePerKey.get("personaliaEtternavn"));
        assertEquals("12345678901", valuePerKey.get("personaliaFodselsnummer"));
        assertEquals("1980-12-31", valuePerKey.get("personaliaFodselsdato"));

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

        InstanceObject result = incomingInstanceMappingService
                .map(4L, createIncomingInstance().build()).block();

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
    void shouldAcceptFodselsdatoAsNull() {
        InstanceObject result = incomingInstanceMappingService
                .map(4L, createValidIncomingInstanceWithFodselsdatoAsNull().build()).block();

        assertFalse(result.getValuePerKey().containsKey("personaliaFodselsdato"));
    }

    @Test
    void shouldCreateCustomizedFodselsdato() {
        InstanceObject result = incomingInstanceMappingService
                .map(4L, createIncomingInstance().build()).block();

        assertEquals("311280", result.getValuePerKey().get("tilpassetFodselsdato1"));
        assertEquals("31.12.1980", result.getValuePerKey().get("tilpassetFodselsdato2"));

    }

    @Test
    void shouldCreateCustomizedFullName() {
        InstanceObject result = incomingInstanceMappingService
                .map(4L, createIncomingInstance().build()).block();

        assertEquals("Ola Nordmann Nordmannsen", result.getValuePerKey().get("tilpassetFulltNavn1"));
        assertEquals("Nordmannsen Ola Nordmann", result.getValuePerKey().get("tilpassetFulltNavn2"));
        assertEquals("Nordmannsen, Ola Nordmann", result.getValuePerKey().get("tilpassetFulltNavn3"));
    }

    @Test
    void shouldCreateCustomizedFullNameWitoutMiddleName() {
        InstanceObject result = incomingInstanceMappingService
                .map(4L, createIncomingInstance()
                        .personalia(Personalia.builder()
                                .fodselsnummer("12345678901")
                                .fornavn("Ola")
                                .etternavn("Nordmannsen")
                                .fodselsdato("19-12-3100")
                                .build()).build()).block();

        assertEquals("Ola Nordmannsen", result.getValuePerKey().get("tilpassetFulltNavn1"));
        assertEquals("Nordmannsen Ola", result.getValuePerKey().get("tilpassetFulltNavn2"));
        assertEquals("Nordmannsen, Ola", result.getValuePerKey().get("tilpassetFulltNavn3"));
    }

    @Test
    void shouldNotAcceptInvalidFodselsdato() {
        InstanceObject result = incomingInstanceMappingService
                .map(4L, createIncomingInstance()
                        .personalia(Personalia.builder()
                                .fodselsnummer("12345678901")
                                .fornavn("Ola")
                                .mellomnavn("Nordmann")
                                .etternavn("Nordmannsen")
                                .fodselsdato("19-12-3100")
                                .build())
                        .build()).block();

        assertFalse(result.getValuePerKey().containsKey("tilpassetFodselsdato1"));
        assertFalse(result.getValuePerKey().containsKey("tilpassetFodselsdato2"));
    }

    @Test
    void shouldNotReturnInstansId(){
        InstanceObject result = incomingInstanceMappingService
                .map(4L, createIncomingInstance().build()).block();

        assertFalse(result.getValuePerKey().containsKey("instansId"));
    }

    private IncomingInstance.IncomingInstanceBuilder createIncomingInstance() {
        return IncomingInstance.builder()
                .instansId("12345")
                .personalia(Personalia.builder()
                        .fodselsnummer("12345678901")
                        .fornavn("Ola")
                        .mellomnavn("Nordmann")
                        .etternavn("Nordmannsen")
                        .fodselsdato("1980-12-31")
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
                        .build());

    }

    private IncomingInstance.IncomingInstanceBuilder createValidIncomingInstanceWithTilleggsinformasjon() {
        return createIncomingInstance()
                .tilleggsinformasjon(Tilleggsinformasjon.builder()
                        .skolear("2024/2025")
                        .skolenummer("1")
                        .skolenavn("Oslo katedralskole")
                        .programomradekode("LA1")
                        .programomradenavn("Latin")
                        .sokertype("Ordinær")
                        .build());
    }

    private IncomingInstance.IncomingInstanceBuilder createValidIncomingInstanceWithFodselsdatoAsNull() {
        return createIncomingInstance()
                .personalia(Personalia.builder()
                        .fodselsnummer("12345678901")
                        .fornavn("Ola")
                        .mellomnavn("Nordmann")
                        .etternavn("Nordmannsen")
                        .fodselsdato(null)
                        .build());
    }
}