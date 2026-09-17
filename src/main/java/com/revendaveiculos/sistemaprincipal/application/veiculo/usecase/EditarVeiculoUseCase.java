package com.revendaveiculos.sistemaprincipal.application.veiculo.usecase;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.EditarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.EditarVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VendasServicePort;
import com.revendaveiculos.sistemaprincipal.domain.exception.VeiculoNaoEncontradoException;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import com.revendaveiculos.sistemaprincipal.domain.vo.Preco;
import org.springframework.stereotype.Service;

@Service
public class EditarVeiculoUseCase implements EditarVeiculoInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VendasServicePort vendasServicePort;
    private final VeiculoMapper veiculoMapper;

    public EditarVeiculoUseCase(VeiculoRepositoryPort veiculoRepositoryPort,
                                 VendasServicePort vendasServicePort,
                                 VeiculoMapper veiculoMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.vendasServicePort = vendasServicePort;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    public VeiculoResponse editar(Long id, EditarVeiculoRequest request) {
        Veiculo veiculo = veiculoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        veiculo.atualizarDados(
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor(),
                Preco.de(request.preco()),
                request.placa()
        );

        Veiculo veiculoAtualizado = veiculoRepositoryPort.salvar(veiculo);
        vendasServicePort.sincronizarVeiculo(veiculoAtualizado);
        return veiculoMapper.paraResponse(veiculoAtualizado);
    }
}
