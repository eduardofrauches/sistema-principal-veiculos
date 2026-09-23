package com.revendaveiculos.sistemaprincipal.application.veiculo.port.in;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;

public interface CadastrarVeiculoInputPort {

    Veiculo cadastrar(CadastrarVeiculoRequest request);
}
