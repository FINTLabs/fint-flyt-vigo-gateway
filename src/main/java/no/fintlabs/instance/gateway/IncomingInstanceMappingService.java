package no.fintlabs.instance.gateway;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.gateway.instance.InstanceMapper;
import no.fintlabs.gateway.instance.model.File;
import no.fintlabs.gateway.instance.model.instance.InstanceObject;
import no.fintlabs.gateway.instance.web.FileClient;
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
        return postFile(sourceApplicationId, incomingInstance)
                .map(uuid -> InstanceObject.builder()
                        .valuePerKey(toValuePerKey(incomingInstance, uuid))
                        .build());
    }

    private static Map<String, String> toValuePerKey(IncomingInstance incomingInstance, UUID uuid) {
        Set<Map.Entry<String, String>> entries = new HashSet<>();


        entries.add(Map.entry("personaliaFodselsnummer", incomingInstance.getPersonalia().getFodselsnummer()));
        entries.add(Map.entry("personaliaFornavn", incomingInstance.getPersonalia().getFornavn()));
        entries.add(Map.entry("personaliaMellomnavn", incomingInstance.getPersonalia().getMellomnavn()));
        entries.add(Map.entry("personaliaEtternavn", incomingInstance.getPersonalia().getEtternavn()));

        Optional.ofNullable(incomingInstance.getPersonalia().getFodselsdato())
                .ifPresent(fodselsdato -> entries.add(Map.entry("personaliaFodselsdato", fodselsdato)));

        entries.add(Map.entry("kontaktinformasjonTelefonnummer", incomingInstance.getKontaktinformasjon().getTelefonnummer()));
        entries.add(Map.entry("kontaktinformasjonEpostadresse", incomingInstance.getKontaktinformasjon().getEpostadresse()));

        entries.add(Map.entry("inntaksadresseGateadresse", incomingInstance.getInntaksadresse().getGateadresse()));
        entries.add(Map.entry("inntaksadressePostnummer", incomingInstance.getInntaksadresse().getPostnummer()));
        entries.add(Map.entry("inntaksadressePoststed", incomingInstance.getInntaksadresse().getPoststed()));

        entries.add(Map.entry("dokumentTittel", incomingInstance.getDokument().getTittel()));
        entries.add(Map.entry("dokumentDato", incomingInstance.getDokument().getDato()));
        entries.add(Map.entry("dokumentFilnavn", incomingInstance.getDokument().getFilnavn()));
        entries.add(Map.entry("dokumentFormat", incomingInstance.getDokument().getFormat()));
        entries.add(Map.entry("dokumentFil", uuid.toString()));

        Optional.ofNullable(incomingInstance.getTilleggsinformasjon()).ifPresent(tilleggsinformasjon -> entries.addAll(Arrays.asList(
                    Map.entry("tilleggsinformasjonSkolear", tilleggsinformasjon.getSkolear()),
                    Map.entry("tilleggsinformasjonSkolenummer", tilleggsinformasjon.getSkolenummer()),
                    Map.entry("tilleggsinformasjonSkolenavn", tilleggsinformasjon.getSkolenavn()),
                    Map.entry("tilleggsinformasjonProgramomradekode", tilleggsinformasjon.getProgramomradekode()),
                    Map.entry("tilleggsinformasjonProgramomradenavn", tilleggsinformasjon.getProgramomradenavn()),
                    Map.entry("tilleggsinformasjonSokertype", tilleggsinformasjon.getSokertype())))
        );

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
