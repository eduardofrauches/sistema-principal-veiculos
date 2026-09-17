package com.revendaveiculos.sistemaprincipal.application.veiculo.usecase;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VendasServicePort;
import com.revendaveiculos.sistemaprincipal.domain.exception.PrecoInvalidoException;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastrarVeiculoUseCaseTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    @Mock
    private VendasServicePort vendasServicePort;

    private final VeiculoMapper veiculoMapper = new VeiculoMapper();

    private CadastrarVeiculoUseCase useCase;

    private CadastrarVeiculoRequest requestValido() {
        return new CadastrarVeiculoRequest("Toyota", "Corolla", 2022, "Prata",
                BigDecimal.valueOf(95000), "ABC1D23");
    }

    @Test
    void deveCadastrarVeiculoESincronizarComServicoDeVendas() {
        useCase = new CadastrarVeiculoUseCase(veiculoRepositoryPort, vendasServicePort, veiculoMapper);

        Veiculo veiculoSalvo = Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                com.revendaveiculos.sistemaprincipal.domain.vo.Preco.de(BigDecimal.valueOf(95000)),
                "ABC1D23", StatusVeiculo.DISPONIVEL);
        when(veiculoRepositoryPort.salvar(any(Veiculo.class))).thenReturn(veiculoSalvo);

        VeiculoResponse response = useCase.cadastrar(requestValido());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("DISPONIVEL");

        ArgumentCaptor<Veiculo> captor = ArgumentCaptor.forClass(Veiculo.class);
        verify(veiculoRepositoryPort).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusVeiculo.DISPONIVEL);

        verify(vendasServicePort).sincronizarVeiculo(veiculoSalvo);
    }

    @Test
    void deveLancarExcecaoParaPrecoInvalido() {
        useCase = new CadastrarVeiculoUseCase(veiculoRepositoryPort, vendasServicePort, veiculoMapper);

        CadastrarVeiculoRequest requestInvalido = new CadastrarVeiculoRequest(
                "Toyota", "Corolla", 2022, "Prata", BigDecimal.valueOf(-1), "ABC1D23");

        assertThatThrownBy(() -> useCase.cadastrar(requestInvalido))
                .isInstanceOf(PrecoInvalidoException.class);

        verifyNoInteractions(veiculoRepositoryPort, vendasServicePort);
    }
}
