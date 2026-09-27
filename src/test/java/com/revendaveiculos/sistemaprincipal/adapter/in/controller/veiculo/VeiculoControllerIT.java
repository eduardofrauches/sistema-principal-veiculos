package com.revendaveiculos.sistemaprincipal.adapter.in.controller.veiculo;

import com.jayway.jsonpath.JsonPath;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VendasServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integracao da API de veiculos: sobe a aplicacao inteira contra um
 * Postgres real (Testcontainers, nao H2) e exercita o VeiculoController via
 * MockMvc. A sincronizacao com o servico-vendas-veiculos (VendasServicePort)
 * e simulada, ja que nao e o alvo destes cenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class VeiculoControllerIT {

    private static final String MENSAGEM_PLACA_DUPLICADA = "Ja existe um veiculo cadastrado com esta placa";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VendasServicePort vendasServicePort;

    @Test
    void deveRetornar409AoCadastrarVeiculoComPlacaJaCadastrada() throws Exception {
        mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(veiculoComPlaca("AAA1A11")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(veiculoComPlaca("AAA1A11")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_PLACA_DUPLICADA));
    }

    @Test
    void deveRetornar409AoEditarVeiculoParaPlacaDeOutroVeiculo() throws Exception {
        mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON).content(veiculoComPlaca("BBB2B22")))
                .andExpect(status().isCreated());
        String outroVeiculo = mockMvc.perform(post("/veiculos").contentType(MediaType.APPLICATION_JSON)
                        .content(veiculoComPlaca("CCC3C33")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer idOutroVeiculo = JsonPath.read(outroVeiculo, "$.id");

        mockMvc.perform(put("/veiculos/{id}", idOutroVeiculo).contentType(MediaType.APPLICATION_JSON)
                        .content(veiculoComPlaca("BBB2B22")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_PLACA_DUPLICADA));
    }

    private static String veiculoComPlaca(String placa) {
        return """
                {"marca":"Fiat","modelo":"Uno","ano":2020,"cor":"Branco","preco":35000.00,"placa":"%s"}
                """.formatted(placa);
    }
}
