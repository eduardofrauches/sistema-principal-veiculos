package com.revendaveiculos.sistemaprincipal.application.veiculo.port.in;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.EditarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;

public interface EditarVeiculoInputPort {

    Veiculo editar(Long id, EditarVeiculoRequest request);
}
