package com.revendaveiculos.sistemaprincipal.application.veiculo.usecase;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.AtualizarStatusVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.AtualizarStatusVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Service;

/**
 * Callback usado pelo servico-vendas-veiculos para manter o status do
 * veiculo coerente no sistema-principal (ex.: RESERVADO ao iniciar uma
 * venda, VENDIDO ao aprovar o pagamento, DISPONIVEL se cancelado).
 */
@Service
public class AtualizarStatusVeiculoUseCase implements AtualizarStatusVeiculoInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VeiculoMapper veiculoMapper;

    public AtualizarStatusVeiculoUseCase(VeiculoRepositoryPort veiculoRepositoryPort,
                                          VeiculoMapper veiculoMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    public VeiculoResponse atualizarStatus(Long id, AtualizarStatusVeiculoRequest request) {
        Veiculo veiculo = veiculoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        veiculo.atualizarStatus(request.status());

        Veiculo veiculoAtualizado = veiculoRepositoryPort.salvar(veiculo);
        return veiculoMapper.paraResponse(veiculoAtualizado);
    }
}
