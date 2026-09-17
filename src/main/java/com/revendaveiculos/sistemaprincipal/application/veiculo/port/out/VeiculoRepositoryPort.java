package com.revendaveiculos.sistemaprincipal.application.veiculo.port.out;

import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;

import java.util.Optional;

public interface VeiculoRepositoryPort {

    Veiculo salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(Long id);
}
