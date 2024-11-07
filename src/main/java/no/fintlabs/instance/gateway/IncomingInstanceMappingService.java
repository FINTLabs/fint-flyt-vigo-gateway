package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.instance.gateway.model.vigo.IncomingInstance;
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

        entries.add(Map.entry("personaliaFodselsnummer", incomingInstance.getPersonalia().getFodselsnummer()));
        entries.add(Map.entry("personaliaFornavn", incomingInstance.getPersonalia().getFornavn()));

        Optional.ofNullable(incomingInstance.getPersonalia().getMellomnavn())
                .ifPresent(mellomnavn -> entries.add(Map.entry("personaliaMellomnavn", mellomnavn)));

        entries.add(Map.entry("personaliaEtternavn", incomingInstance.getPersonalia().getEtternavn()));

        entries.add(Map.entry("tilpassetNavn1",
                Stream.of(incomingInstance.getPersonalia().getFornavn(),
                                incomingInstance.getPersonalia().getMellomnavn(),
                                incomingInstance.getPersonalia().getEtternavn())
                        .filter(s -> StringUtils.hasLength(s))
                        .collect(Collectors.joining(" "))
        ));

        entries.add(Map.entry("tilpassetNavn2",
                Stream.of(incomingInstance.getPersonalia().getEtternavn(),
                                incomingInstance.getPersonalia().getFornavn(),
                                incomingInstance.getPersonalia().getMellomnavn())
                        .filter(s -> StringUtils.hasLength(s))
                        .collect(Collectors.joining(" "))
        ));

        entries.add(Map.entry("tilpassetNavn3",
                Stream.of(incomingInstance.getPersonalia().getEtternavn() + ",",
                                incomingInstance.getPersonalia().getFornavn(),
                                incomingInstance.getPersonalia().getMellomnavn())
                        .filter(s -> StringUtils.hasLength(s))
                        .collect(Collectors.joining(" "))
        ));

        Optional.ofNullable(incomingInstance.getPersonalia().getFodselsdato())
                .ifPresent(fodselsdato -> {
                    entries.add(Map.entry("personaliaFodselsdato", fodselsdato));

                    Optional.ofNullable(formatedDate(fodselsdato, "ddMMyy")).ifPresent(formatedFodselsdato ->
                            entries.add(Map.entry("tilpassetFodselsdato1", formatedFodselsdato)));

                    Optional.ofNullable(formatedDate(fodselsdato, "dd.MM.yyyy")).ifPresent(formatedFodselsdato ->
                            entries.add(Map.entry("tilpassetFodselsdato2", formatedFodselsdato)));

                });

        Optional.ofNullable(incomingInstance.getTilleggsinformasjon()).ifPresent(tilleggsinformasjon ->
            Optional.ofNullable(tilleggsinformasjon.getSkolear()).ifPresent(skolear ->
                    entries.add(Map.entry("tilpassetSkolear", formatedSkoleaar(skolear)))));

        entries.add(Map.entry("kontaktinformasjonTelefonnummer", incomingInstance.getKontaktinformasjon().getTelefonnummer()));
        entries.add(Map.entry("kontaktinformasjonEpostadresse", incomingInstance.getKontaktinformasjon().getEpostadresse()));

        entries.add(Map.entry("inntaksadresseGateadresse", incomingInstance.getInntaksadresse().getGateadresse()));
        entries.add(Map.entry("inntaksadressePostnummer", incomingInstance.getInntaksadresse().getPostnummer()));
        entries.add(Map.entry("inntaksadressePoststed", incomingInstance.getInntaksadresse().getPoststed()));

        Optional.ofNullable(incomingInstance.getDokument()).ifPresent(dokument -> {
            entries.add(Map.entry("dokumentTittel", dokument.getTittel()));
            entries.add(Map.entry("dokumentDato", dokument.getDato()));
            entries.add(Map.entry("dokumentFilnavn", dokument.getFilnavn()));
            entries.add(Map.entry("dokumentFormat", dokument.getFormat()));
            entries.add(Map.entry("dokumentFil", uuid.toString()));
        });

        Optional.ofNullable(incomingInstance.getTilleggsinformasjon()).ifPresent(tilleggsinformasjon -> {

            Optional.ofNullable(tilleggsinformasjon.getSkolear())
                    .ifPresent(skolear -> entries.add(Map.entry("tilleggsinformasjonSkolear", skolear)));

            Optional.ofNullable(tilleggsinformasjon.getSkolenummer())
                    .ifPresent(skolenummer -> entries.add(Map.entry("tilleggsinformasjonSkolenummer", skolenummer)));

            Optional.ofNullable(tilleggsinformasjon.getSkolenavn())
                    .ifPresent(skolenavn -> entries.add(Map.entry("tilleggsinformasjonSkolenavn", skolenavn)));

            Optional.ofNullable(tilleggsinformasjon.getProgramomradekode())
                    .ifPresent(programomradekode -> entries.add(Map.entry("tilleggsinformasjonProgramomradekode", programomradekode)));

            Optional.ofNullable(tilleggsinformasjon.getProgramomradenavn())
                    .ifPresent(programomradenavn -> entries.add(Map.entry("tilleggsinformasjonProgramomradenavn", programomradenavn)));

            Optional.ofNullable(tilleggsinformasjon.getSokertype())
                    .ifPresent(sokertype -> entries.add(Map.entry("tilleggsinformasjonSokertype", sokertype)));

        });

        return entries.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // 20242025 -> 2024/2025
    private static String formatedSkoleaar(String skolear) {
        int length = skolear.length();
        if (length == 8) {
            return skolear.substring(0, 4) + "/" + skolear.substring(4);
        } else {
            log.warn("Not posible to format skoleaar: {}", skolear);
            return skolear;
        }
    }

    private static String formatedDate(String fodselsdato, String format) {
        try {
            LocalDate date = LocalDate.parse(fodselsdato, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formattedDate = date.format(DateTimeFormatter.ofPattern(format));
            return formattedDate;
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
