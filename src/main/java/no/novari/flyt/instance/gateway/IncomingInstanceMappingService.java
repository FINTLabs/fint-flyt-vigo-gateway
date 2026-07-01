package no.novari.flyt.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import kotlin.jvm.functions.Function1;
import no.novari.flyt.gateway.webinstance.InstanceMapper;
import no.novari.flyt.gateway.webinstance.model.File;
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject;
import no.novari.flyt.instance.gateway.model.vigo.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class IncomingInstanceMappingService implements InstanceMapper<IncomingInstance> {

    public static final String EMPTY_STRING = "";

    @NotNull
    @Override
    public InstanceObject map(
            long sourceApplicationId,
            @NotNull IncomingInstance incomingInstance,
            @NotNull Function1<? super File, UUID> persistFile
    ) {
        if (incomingInstance.getDokument() == null) {
            return new InstanceObject(toValuePerKey(incomingInstance, null), new HashMap<>());
        } else {
            UUID uuid = postFile(sourceApplicationId, incomingInstance, persistFile);
            return new InstanceObject(toValuePerKey(incomingInstance, uuid), new HashMap<>());
        }
    }

    private static Map<String, String> toValuePerKey(IncomingInstance incomingInstance, UUID uuid) {
        Set<Map.Entry<String, String>> entries = new HashSet<>();

        entries.addAll(createPersonalia(incomingInstance));
        entries.addAll(createInntaksadresse(incomingInstance));
        entries.addAll(createPostadresse(incomingInstance));
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

        entries.add(Map.entry("tilpassetSendtdato1",
                Optional.ofNullable(incomingInstance.getTilleggsinformasjon())
                        .map(Tilleggsinformasjon::getSendtDato)
                        .map(sendtdato -> formatedDate(sendtdato, "dd.MM.yyyy"))
                        .orElse(EMPTY_STRING)
        ));

        entries.add(Map.entry("tilpassetSendtdato2",
                Optional.ofNullable(incomingInstance.getTilleggsinformasjon())
                        .map(Tilleggsinformasjon::getSendtDato)
                        .map(sendtdato -> formatedDate(sendtdato, "ddMMyy"))
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

    private static Collection<? extends Map.Entry<String, String>> createPostadresse(IncomingInstance incomingInstance) {
        Optional<Postadresse> postadresse = Optional.ofNullable(incomingInstance.getPostadresse());
        return List.of(
                Map.entry("postadresseGateadresse",
                        postadresse.map(Postadresse::getGateadresse).orElse(EMPTY_STRING)),
                Map.entry("postadressePostnummer",
                        postadresse.map(Postadresse::getPostnummer).orElse(EMPTY_STRING)),
                Map.entry("postadressePoststed",
                        postadresse.map(Postadresse::getPoststed).orElse(EMPTY_STRING))
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
                        tillegg.map(Tilleggsinformasjon::getUtsendtDato).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonArstall",
                        tillegg.map(Tilleggsinformasjon::getArstall).orElse(EMPTY_STRING)),
                Map.entry("tilleggsinformasjonSendtDato",
                        tillegg.map(Tilleggsinformasjon::getSendtDato).orElse(EMPTY_STRING))
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

    private static String formatedDate(String date, String format) {
        try {
            return LocalDate
                    .parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .format(DateTimeFormatter.ofPattern(format));

        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @NotNull
    private UUID postFile(
            long sourceApplicationId,
            @NotNull IncomingInstance incomingInstance,
            @NotNull Function1<? super File, UUID> persistFile
    ) {
        return persistFile.invoke(new File(
                incomingInstance.getDokument().getFilnavn(),
                sourceApplicationId,
                incomingInstance.getInstansId(),
                MediaType.parseMediaType(incomingInstance.getDokument().getFormat()),
                "UTF-8",
                incomingInstance.getDokument().getFil()
        ));
    }
}
