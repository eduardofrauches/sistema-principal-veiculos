package com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.client;

import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VendasServicePort;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementa VendasServicePort chamando o servico-vendas-veiculos via HTTP
 * (POST /veiculos/sync). A URL base vem de application.yml
 * (servico-vendas.base-url), nunca hardcoded.
 *
 * Uma falha na sincronizacao NAO derruba o cadastro/edicao local: o
 * servico de vendas foi projetado para funcionar com uma copia local,
 * entao o pior caso e essa copia ficar temporariamente desatualizada.
 */
@Component
public class VendasServiceHttpAdapter implements VendasServicePort {

    private static final Logger log = LoggerFactory.getLogger(VendasServiceHttpAdapter.class);

    private final RestTemplate restTemplate;
    private final String servicoVendasBaseUrl;

    public VendasServiceHttpAdapter(RestTemplate restTemplate,
                                     @Value("${servico-vendas.base-url}") String servicoVendasBaseUrl) {
        this.restTemplate = restTemplate;
        this.servicoVendasBaseUrl = servicoVendasBaseUrl;
    }

    @Override
    public void sincronizarVeiculo(Veiculo veiculo) {
        VeiculoSyncPayload payload = new VeiculoSyncPayload(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPreco().valor(),
                veiculo.getStatus().name()
        );
        try {
            restTemplate.postForEntity(servicoVendasBaseUrl + "/veiculos/sync", payload, Void.class);
        } catch (RestClientException e) {
            log.warn("Falha ao sincronizar veiculo {} com o servico-vendas-veiculos: {}",
                    veiculo.getId(), e.getMessage());
        }
    }
}
