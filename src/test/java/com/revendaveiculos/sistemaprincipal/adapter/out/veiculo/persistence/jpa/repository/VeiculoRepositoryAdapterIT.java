package com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.repository;

import com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.mapper.VeiculoEntityMapper;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import com.revendaveiculos.sistemaprincipal.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integracao real do adapter de persistencia: sobe um Postgres
 * de verdade via Testcontainers (nao H2) e exercita VeiculoRepositoryAdapter
 * ponta a ponta contra o banco.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({VeiculoEntityMapper.class, VeiculoRepositoryAdapter.class})
class VeiculoRepositoryAdapterIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private VeiculoRepositoryAdapter veiculoRepositoryAdapter;

    @Test
    void deveSalvarERecuperarVeiculoPorId() {
        Veiculo novo = Veiculo.cadastrar("Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), "ABC1D23");

        Veiculo salvo = veiculoRepositoryAdapter.salvar(novo);
        assertThat(salvo.getId()).isNotNull();

        Optional<Veiculo> encontrado = veiculoRepositoryAdapter.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getMarca()).isEqualTo("Toyota");
        assertThat(encontrado.get().getPreco().valor()).isEqualByComparingTo("95000.00");
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        Optional<Veiculo> encontrado = veiculoRepositoryAdapter.buscarPorId(999_999L);

        assertThat(encontrado).isEmpty();
    }

    @Test
    void deveAtualizarVeiculoExistente() {
        Veiculo salvo = veiculoRepositoryAdapter.salvar(
                Veiculo.cadastrar("Honda", "Civic", 2021, "Preto",
                        Preco.de(BigDecimal.valueOf(90000)), "XYZ9K88"));

        salvo.reservar();
        veiculoRepositoryAdapter.salvar(salvo);

        Optional<Veiculo> encontrado = veiculoRepositoryAdapter.buscarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
    }
}
