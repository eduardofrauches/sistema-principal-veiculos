package com.revendaveiculos.sistemaprincipal.application.veiculo.usecase;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.mapper.VeiculoMapper;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.CadastrarVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VendasServicePort;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Service;

@Service
public class CadastrarVeiculoUseCase implements CadastrarVeiculoInputPort {

    private final VeiculoRepositoryPort veiculoRepositoryPort;
    private final VendasServicePort vendasServicePort;
    private final VeiculoMapper veiculoMapper;

    public CadastrarVeiculoUseCase(VeiculoRepositoryPort veiculoRepositoryPort,
                                    VendasServicePort vendasServicePort,
                                    VeiculoMapper veiculoMapper) {
        this.veiculoRepositoryPort = veiculoRepositoryPort;
        this.vendasServicePort = vendasServicePort;
        this.veiculoMapper = veiculoMapper;
    }

    @Override
    public Veiculo cadastrar(CadastrarVeiculoRequest request) {
        Veiculo veiculo = veiculoMapper.paraDominio(request);
        Veiculo veiculoSalvo = veiculoRepositoryPort.salvar(veiculo);
        vendasServicePort.sincronizarVeiculo(veiculoSalvo);
        return veiculoSalvo;
    }
}
