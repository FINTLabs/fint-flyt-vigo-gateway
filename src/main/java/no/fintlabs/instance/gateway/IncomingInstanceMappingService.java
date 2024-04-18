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

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        assert incomingInstance.getTilleggsinformasjon() != null;
        return Stream.of(
                Map.entry("personaliaFodselsnummer", incomingInstance.getPersonalia().getFodselsnummer()),
                Map.entry("personaliaFornavn", incomingInstance.getPersonalia().getFornavn()),
                Map.entry("personaliaMellomnavn", incomingInstance.getPersonalia().getMellomnavn()),
                Map.entry("personaliaEtternavn", incomingInstance.getPersonalia().getEtternavn()),

                Map.entry("kontaktinformasjonTelefonnummer", incomingInstance.getKontaktinformasjon().getTelefonnummer()),
                Map.entry("kontaktinformasjonEpostadresse", incomingInstance.getKontaktinformasjon().getEpostadresse()),

                Map.entry("inntaksadresseGateadresse", incomingInstance.getInntaksadresse().getGateadresse()),
                Map.entry("inntaksadressePostnummer", incomingInstance.getInntaksadresse().getPostnummer()),
                Map.entry("inntaksadressePoststed", incomingInstance.getInntaksadresse().getPoststed()),

                Map.entry("dokumentTittel", incomingInstance.getDokument().getTittel()),
                Map.entry("dokumentDato", incomingInstance.getDokument().getDato()),
                Map.entry("dokumentFilnavn", incomingInstance.getDokument().getFilnavn()),
                Map.entry("dokumentFormat", incomingInstance.getDokument().getFormat()),
                Map.entry("dokumentFil", uuid.toString()),

                Map.entry("tilleggsinformasjonSkolear", incomingInstance.getTilleggsinformasjon() != null ? incomingInstance.getTilleggsinformasjon().getSkolear() : ""),
                Map.entry("tilleggsinformasjonSkolenummer", incomingInstance.getTilleggsinformasjon() != null ? incomingInstance.getTilleggsinformasjon().getSkolenummer() : ""),
                Map.entry("tilleggsinformasjonSkolenavn", incomingInstance.getTilleggsinformasjon() != null ? incomingInstance.getTilleggsinformasjon().getSkolenavn() : ""),
                Map.entry("tilleggsinformasjonProgramomradekode", incomingInstance.getTilleggsinformasjon() != null ? incomingInstance.getTilleggsinformasjon().getProgramomradekode() : ""),
                Map.entry("tilleggsinformasjonProgramomradenavn", incomingInstance.getTilleggsinformasjon() != null ? incomingInstance.getTilleggsinformasjon().getProgramomradenavn() : ""),
                Map.entry("tilleggsinformasjonSokertype", incomingInstance.getTilleggsinformasjon() != null ? incomingInstance.getTilleggsinformasjon().getSokertype() : ""))

                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
