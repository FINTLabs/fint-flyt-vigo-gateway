package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
import no.fintlabs.instance.gateway.model.vigo.Dokument;
import no.fintlabs.instance.gateway.model.vigo.IncomingInstance;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IncomingInstanceMappingService implements InstanceMapper<IncomingInstance> {

    private final FileClient fileClient;

    public IncomingInstanceMappingService(
            FileClient fileClient
    ) {
        this.fileClient = fileClient;
    }

    @Override
    public Mono<InstanceObject> map(Long sourceApplicationId, IncomingInstance incomingInstance) {
        if (incomingInstance.getDokument() == null) {
            IncomingInstance fiktivInstance = IncomingInstance.builder()
                    .instansId(incomingInstance.getInstansId())
                    .dokumenttype(incomingInstance.getDokumenttype())
                    .personalia(incomingInstance.getPersonalia())
                    .kontaktinformasjon(incomingInstance.getKontaktinformasjon())
                    .inntaksadresse(incomingInstance.getInntaksadresse())
                    .dokument(Dokument.builder()
                            .tittel("Fiktivt dokument")
                            .dato("1970-01-01")
                            .filnavn("fiktiv.pdf")
                            .format("application/pdf")
                            .fil("RXQgdmFubGlnIHZlZGxlZ2cK")
                            .build())
                    .tilleggsinformasjon(incomingInstance.getTilleggsinformasjon())
                    .build();
            return postFile(sourceApplicationId, fiktivInstance)
                    .map(uuid -> InstanceObject.builder()
                        .valuePerKey(toValuePerKey(fiktivInstance, uuid))
                        .build());
        }else {
            return postFile(sourceApplicationId, incomingInstance)
                    .map(uuid -> InstanceObject.builder()
                            .valuePerKey(toValuePerKey(incomingInstance, uuid))
                            .build());
        }

    }

    private static Map<String, String> toValuePerKey(IncomingInstance incomingInstance, UUID uuid) {
        Set<Map.Entry<String, String>> entries = new HashSet<>();


        entries.add(Map.entry("personaliaFodselsnummer", incomingInstance.getPersonalia().getFodselsnummer()));
        entries.add(Map.entry("personaliaFornavn", incomingInstance.getPersonalia().getFornavn()));
        entries.add(Map.entry("personaliaMellomnavn", incomingInstance.getPersonalia().getMellomnavn()));
        entries.add(Map.entry("personaliaEtternavn", incomingInstance.getPersonalia().getEtternavn()));

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

    private Mono<UUID> postFile(Long sourceApplicationId, IncomingInstance incomingInstance) {
        return fileClient.postFile(
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
