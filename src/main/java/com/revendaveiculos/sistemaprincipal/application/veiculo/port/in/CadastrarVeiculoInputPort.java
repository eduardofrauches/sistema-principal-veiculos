package com.revendaveiculos.sistemaprincipal.application.veiculo.port.in;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;

public interface CadastrarVeiculoInputPort {

    VeiculoResponse cadastrar(CadastrarVeiculoRequest request);
}
