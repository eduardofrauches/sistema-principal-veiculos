package com.revendaveiculos.sistemaprincipal.application.veiculo.usecase;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.EditarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VendasServicePort;
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
class EditarVeiculoUseCaseTest {

    @Mock
    private VeiculoRepositoryPort veiculoRepositoryPort;

    @Mock
    private VendasServicePort vendasServicePort;

    private EditarVeiculoUseCase useCase;

    private EditarVeiculoRequest requestValido() {
        return new EditarVeiculoRequest("Toyota", "Corolla", 2023, "Preto",
                BigDecimal.valueOf(98000), "ABC1D23");
    }

    private Veiculo veiculoExistente(StatusVeiculo status) {
        return Veiculo.restaurar(1L, "Toyota", "Corolla", 2022, "Prata",
                Preco.de(BigDecimal.valueOf(95000)), "ABC1D23", status);
    }

    @Test
    void deveEditarVeiculoDisponivelESincronizar() {
        useCase = new EditarVeiculoUseCase(veiculoRepositoryPort, vendasServicePort);

        Veiculo existente = veiculoExistente(StatusVeiculo.DISPONIVEL);
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(existente));
        when(veiculoRepositoryPort.salvar(any(Veiculo.class))).thenReturn(existente);

        Veiculo response = useCase.editar(1L, requestValido());

        assertThat(response.getMarca()).isEqualTo("Toyota");
        assertThat(response.getCor()).isEqualTo("Preto");
        verify(veiculoRepositoryPort).salvar(existente);
        verify(vendasServicePort).sincronizarVeiculo(existente);
    }

    @Test
    void deveLancarExcecaoQuandoVeiculoNaoEncontrado() {
        useCase = new EditarVeiculoUseCase(veiculoRepositoryPort, vendasServicePort);

        when(veiculoRepositoryPort.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.editar(99L, requestValido()))
                .isInstanceOf(VeiculoNaoEncontradoException.class);

        verifyNoInteractions(vendasServicePort);
        verify(veiculoRepositoryPort, never()).salvar(any());
    }

    @Test
    void deveLancarExcecaoAoEditarVeiculoJaVendido() {
        useCase = new EditarVeiculoUseCase(veiculoRepositoryPort, vendasServicePort);

        Veiculo vendido = veiculoExistente(StatusVeiculo.VENDIDO);
        when(veiculoRepositoryPort.buscarPorId(1L)).thenReturn(Optional.of(vendido));

        assertThatThrownBy(() -> useCase.editar(1L, requestValido()))
                .isInstanceOf(TransicaoStatusInvalidaException.class)
                .hasMessageContaining("ja vendido") // acento removido de proposito (mensagem em ASCII)
                .satisfies(ex -> assertThat(ex.getMessage()).containsIgnoringCase("vendido"));

        verify(veiculoRepositoryPort, never()).salvar(any());
        verifyNoInteractions(vendasServicePort);
    }
}
