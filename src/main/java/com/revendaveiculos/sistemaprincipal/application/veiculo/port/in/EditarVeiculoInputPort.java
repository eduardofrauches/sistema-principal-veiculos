package com.revendaveiculos.sistemaprincipal.application.veiculo.port.in;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.EditarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;

public interface EditarVeiculoInputPort {

    VeiculoResponse editar(Long id, EditarVeiculoRequest request);
}
