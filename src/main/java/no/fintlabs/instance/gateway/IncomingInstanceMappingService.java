package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.instance.gateway.model.vigo.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class IncomingInstanceMappingService implements InstanceMapper<IncomingInstance> {

    public static final String EMPTY_STRING = "";

    @Override
    public Mono<InstanceObject> map(
            Long sourceApplicationId,
            IncomingInstance incomingInstance,
            Function<File, Mono<UUID>> persistFile
    ) {
        if (incomingInstance.getDokument() == null) {
            return Mono.just(InstanceObject.builder()
                    .valuePerKey(toValuePerKey(incomingInstance, null))
                    .build());
        } else {
            return postFile(sourceApplicationId, incomingInstance, persistFile)
                    .map(uuid -> InstanceObject.builder()
                            .valuePerKey(toValuePerKey(incomingInstance, uuid))
                            .build());
        }
    }

    private static Map<String, String> toValuePerKey(IncomingInstance incomingInstance, UUID uuid) {
        Set<Map.Entry<String, String>> entries = new HashSet<>();

        entries.addAll(createPersonalia(incomingInstance));
        entries.addAll(createInntaksadresse(incomingInstance));
        entries.addAll(createKontaktinformasjon(incomingInstance));
        entries.addAll(createTilleggsinformasjon(incomingInstance));

        Optional.ofNullable(incomingInstance.getDokument()).ifPresent(dokument -> {
            entries.add(Map.entry("dokumentTittel", dokument.getTittel()));
            entries.add(Map.entry("dokumentDato", dokument.getDato()));
            entries.add(Map.entry("dokumentFilnavn", dokument.getFilnavn()));
            entries.add(Map.entry("dokumentFormat", dokument.getFormat()));
            entries.add(Map.entry("dokumentFil", uuid.toString()));
        });

        entries.add(Map.entry("tilpassetNavn1",
                Stream.of(incomingInstance.getPersonalia().getFornavn(),
                                incomingInstance.getPersonalia().getMellomnavn(),
                                incomingInstance.getPersonalia().getEtternavn())
                        .filter(StringUtils::hasLength)
                        .collect(Collectors.joining(" "))
        ));

        entries.add(Map.entry("tilpassetNavn2",
                Stream.of(incomingInstance.getPersonalia().getEtternavn(),
                                incomingInstance.getPersonalia().getFornavn(),
                                incomingInstance.getPersonalia().getMellomnavn())
                        .filter(StringUtils::hasLength)
                        .collect(Collectors.joining(" "))
        ));

        entries.add(Map.entry("tilpassetNavn3",
                Stream.of(incomingInstance.getPersonalia().getEtternavn() + ",",
                                incomingInstance.getPersonalia().getFornavn(),
                                incomingInstance.getPersonalia().getMellomnavn())
                        .filter(StringUtils::hasLength)
                        .collect(Collectors.joining(" "))
        ));

        entries.add(Map.entry("tilpassetFodselsdato1",
                Optional.ofNullable(incomingInstance.getPersonalia())
                        .map(Personalia::getFodselsdato)
                        .map(fodselsdato -> formatedDate(fodselsdato, "ddMMyy"))
                        .orElse(EMPTY_STRING)
        ));

        entries.add(Map.entry("tilpassetFodselsdato2",
                Optional.ofNullable(incomingInstance.getPersonalia())
                        .map(Personalia::getFodselsdato)
                        .map(fodselsdato -> formatedDate(fodselsdato, "dd.MM.yyyy"))
                        .orElse(EMPTY_STRING)
        ));

        entries.add(Map.entry("tilpassetSkolear",
                Optional.ofNullable(incomingInstance.getTilleggsinformasjon())
                        .map(Tilleggsinformasjon::getSkolear)
                        .map(IncomingInstanceMappingService::formatedSkoleaar)
                        .orElse(EMPTY_STRING)
        ));

        return entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Collection<? extends Map.Entry<String, String>> createPersonalia(IncomingInstance incomingInstance) {
        Optional<Personalia> personalia = Optional.ofNullable(incomingInstance.getPersonalia());
        return List.of(
                Map.entry("personaliaFodselsnummer",
                        incomingInstance.getPersonalia().getFodselsnummer()),
                Map.entry("personaliaFornavn",
                        incomingInstance.getPersonalia().getFornavn()),
                Map.entry("personaliaMellomnavn",
                        personalia.map(Personalia::getMellomnavn).orElse(EMPTY_STRING)),
                Map.entry("personaliaEtternavn",
                        incomingInstance.getPersonalia().getEtternavn()),
                Map.entry("personaliaFodselsdato",
                        personalia.map(Personalia::getFodselsdato).orElse(EMPTY_STRING))
        );
    }

    private static Collection<? extends Map.Entry<String, String>> createKontaktinformasjon(IncomingInstance incomingInstance) {
        Optional<Kontaktinformasjon> kontaktinformasjon = Optional.ofNullable(incomingInstance.getKontaktinformasjon());
        return List.of(
                Map.entry("kontaktinformasjonTelefonnummer",
                        kontaktinformasjon.map(Kontaktinformasjon::getTelefonnummer).orElse(EMPTY_STRING)),
                Map.entry("kontaktinformasjonEpostadresse",
                        kontaktinformasjon.map(Kontaktinformasjon::getEpostadresse).orElse(EMPTY_STRING))
        );
    }

    private static Collection<? extends Map.Entry<String, String>> createInntaksadresse(IncomingInstance incomingInstance) {
        Optional<Inntaksadresse> inntaksadresse = Optional.ofNullable(incomingInstance.getInntaksadresse());
        return List.of(
                Map.entry("inntaksadresseGateadresse",
                        inntaksadresse.map(Inntaksadresse::getGateadresse).orElse(EMPTY_STRING)),
                Map.entry("inntaksadressePostnummer",
                        inntaksadresse.map(Inntaksadresse::getPostnummer).orElse(EMPTY_STRING)),
                Map.entry("inntaksadressePoststed",
                        inntaksadresse.map(Inntaksadresse::getPoststed).orElse(EMPTY_STRING))
        );

    }

    private static Collection<? extends Map.Entry<String, String>> createTilleggsinformasjon(IncomingInstance incomingInstance) {
        Optional<Tilleggsinformasjon> tillegg = Optional.ofNullable(incomingInstance.getTilleggsinformasjon());
        return List.of(
                Map.entry("tilleggsinformasjonSkolear",
                        tillegg.map(Tilleggsinformasjon::getSkolear).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonSkolenummer",
                        tillegg.map(Tilleggsinformasjon::getSkolenummer).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonSkolenavn",
                        tillegg.map(Tilleggsinformasjon::getSkolenavn).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProgramomradekode",
                        tillegg.map(Tilleggsinformasjon::getProgramomradekode).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProgramomradenavn",
                        tillegg.map(Tilleggsinformasjon::getProgramomradenavn).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonSokertype",
                        tillegg.map(Tilleggsinformasjon::getSokertype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonEndringstype",
                        tillegg.map(Tilleggsinformasjon::getEndringstype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonFylkessignatar",
                        tillegg.map(Tilleggsinformasjon::getFylkessignatar).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKandidattype",
                        tillegg.map(Tilleggsinformasjon::getKandidattype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKontraktsnummer",
                        tillegg.map(Tilleggsinformasjon::getKontraktsnummer).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKontraktspartEpost",
                        tillegg.map(Tilleggsinformasjon::getKontraktspartEpost).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKontraktspartkode",
                        tillegg.map(Tilleggsinformasjon::getKontraktspartkode).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKontraktspartnavn",
                        tillegg.map(Tilleggsinformasjon::getKontraktspartnavn).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKontraktspartnummer",
                        tillegg.map(Tilleggsinformasjon::getKontraktspartnummer).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonKontraktstype",
                        tillegg.map(Tilleggsinformasjon::getKontraktstype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonLastetOppAv",
                        tillegg.map(Tilleggsinformasjon::getLastetOppAv).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonLastetOppDato",
                        tillegg.map(Tilleggsinformasjon::getLastetOppDato).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonNemndnavn",
                        tillegg.map(Tilleggsinformasjon::getNemndnavn).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonNemndnummer",
                        tillegg.map(Tilleggsinformasjon::getNemndnummer).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonOppmeldtDato",
                        tillegg.map(Tilleggsinformasjon::getOppmeldtDato).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonOrganisasjonsnavn",
                        tillegg.map(Tilleggsinformasjon::getOrganisasjonsnavn).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonOrganisasjonsnummer",
                        tillegg.map(Tilleggsinformasjon::getOrganisasjonsnummer).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProgramomradekode",
                        tillegg.map(Tilleggsinformasjon::getProgramomradekode).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProgramomradenavn",
                        tillegg.map(Tilleggsinformasjon::getProgramomradenavn).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProvenr",
                        tillegg.map(Tilleggsinformasjon::getProvenr).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProvestatus",
                        tillegg.map(Tilleggsinformasjon::getProvestatus).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonProvetype",
                        tillegg.map(Tilleggsinformasjon::getProvetype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonResultatPraktisk",
                        tillegg.map(Tilleggsinformasjon::getResultatPraktisk).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonResultatTeori",
                        tillegg.map(Tilleggsinformasjon::getResultatTeori).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonSokertype",
                        tillegg.map(Tilleggsinformasjon::getSokertype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonVedleggBeskrivelse",
                        tillegg.map(Tilleggsinformasjon::getVedleggBeskrivelse).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonVedleggTittel",
                        tillegg.map(Tilleggsinformasjon::getVedleggTittel).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonVedtaksresultat",
                        tillegg.map(Tilleggsinformasjon::getVedtaksresultat).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonFnr",
                        tillegg.map(Tilleggsinformasjon::getFnr).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonVgdoknr",
                        tillegg.map(Tilleggsinformasjon::getVgdoknr).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonVgdoktype",
                        tillegg.map(Tilleggsinformasjon::getVgdoktype).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonUtsendtDato",
                        tillegg.map(Tilleggsinformasjon::getUtsendtDato).orElse(EMPTY_STRING))
        );
    }

    // 20242025 -> 2024/2025
    private static String formatedSkoleaar(String skolear) {
        if (skolear.length() == 8) {
            return skolear.substring(0, 4) + "/" + skolear.substring(4);
        } else {
            log.warn("Not posible to format skoleaar: {}", skolear);
            return skolear;
        }
    }

    private static String formatedDate(String fodselsdato, String format) {
        try {
            return LocalDate
                    .parse(fodselsdato, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern(format));

        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Mono<UUID> postFile(
            Long sourceApplicationId,
            IncomingInstance incomingInstance,
            Function<File, Mono<UUID>> persistFile
    ) {
        return persistFile.apply(
                File.builder()
                        .name(incomingInstance.getDokument().getFilnavn())
                        .type(MediaType.parseMediaType(incomingInstance.getDokument().getFormat()))
                        .sourceApplicationId(sourceApplicationId)
                        .sourceApplicationInstanceId(incomingInstance.getInstansId())
                        .encoding("UTF-8")
                        .base64Contents(incomingInstance.getDokument().getFil())
                        .build());
    }
}
