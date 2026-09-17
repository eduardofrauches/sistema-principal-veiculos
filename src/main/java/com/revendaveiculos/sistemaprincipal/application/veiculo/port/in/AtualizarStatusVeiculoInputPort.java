package com.revendaveiculos.sistemaprincipal.application.veiculo.port.in;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.AtualizarStatusVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;

/** Callback acionado pelo servico-vendas-veiculos para manter o status coerente. */
public interface AtualizarStatusVeiculoInputPort {

    VeiculoResponse atualizarStatus(Long id, AtualizarStatusVeiculoRequest request);
}
