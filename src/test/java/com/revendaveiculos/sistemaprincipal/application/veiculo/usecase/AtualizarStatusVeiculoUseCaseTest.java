package com.revendaveiculos.sistemaprincipal.application.veiculo.usecase;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.AtualizarStatusVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.domain.exception.TransicaoStatusInvalidaException;
import com.revendaveiculos.sistemaprincipal.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.StatusVeiculo;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import com.revendaveiculos.sistemaprincipal.domain.vo.Preco;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtualizarStatusVeiculoUseCaseTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    private AtualizarStatusVeiculoUseCase useCase;

    private Veiculo veiculo(StatusVeiculo status) {
        return Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), "ABC1D23", status);
    }

    @Test
    void deveAtualizarStatusQuandoTransicaoValida() {
        useCase = new AtualizarStatusVeiculoUseCase(veiculoRepositoryPort);

        Veiculo veiculo = veiculo(StatusVeiculo.DISPONIVEL);
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(veiculo));
        when(veiculoRepositoryPort.salvar(veiculo)).thenReturn(veiculo);

        Veiculo response = useCase.atualizarStatus(1L,
                new AtualizarStatusVeiculoRequest(StatusVeiculo.RESERVADO));

        assertThat(response.getStatus()).isEqualTo(StatusVeiculo.RESERVADO);
        verify(veiculoRepositoryPort).salvar(veiculo);
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
        useCase = new AtualizarStatusVeiculoUseCase(veiculoRepositoryPort);

        when(veiculoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.atualizarStatus(99L,
                new AtualizarStatusVeiculoRequest(StatusVeiculo.VENDIDO)))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verify(veiculoRepositoryPort, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoTransicaoInvalida() {
        useCase = new AtualizarStatusVeiculoUseCase(veiculoRepositoryPort);

        Veiculo vendido = veiculo(StatusVeiculo.VENDIDO);
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(vendido));

        assertThatThrownBy(() -> useCase.atualizarStatus(1L,
                new AtualizarStatusVeiculoRequest(StatusVeiculo.RESERVADO)))
                .isInstanceOf(TransicaoStatusInvalidaException.class);

        verify(veiculoRepositoryPort, never()).salvar(any());
    }
}
