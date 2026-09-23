package com.revendaveiculos.sistemaprincipal.adapter.in.presenter.veiculo;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import com.revendaveiculos.sistemaprincipal.domain.vo.Preco;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoPresenterTest {

    private final VeiculoPresenter presenter = new VeiculoPresenter();

    @Test
    void deveApresentarVeiculoComoResponse() {
        Veiculo veiculo = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), "ABC1D23", StatusVeiculo.DISPONIVEL);

        VeiculoResponse response = presenter.apresentar(veiculo);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.marca()).isEqualTo("Toyota");
        assertThat(response.modelo()).isEqualTo("Corolla");
        assertThat(response.ano()).isEqualTo(2022);
        assertThat(response.cor()).isEqualTo("Prata");
        assertThat(response.preco()).isEqualByComparingTo(BigDecimal.valueOf(95000));
        assertThat(response.placa()).isEqualTo("ABC1D23");
        assertThat(response.status()).isEqualTo("DISPONIVEL");
    }
}
