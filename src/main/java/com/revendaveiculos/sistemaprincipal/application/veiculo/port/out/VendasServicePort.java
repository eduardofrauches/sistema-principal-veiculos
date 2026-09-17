package com.revendaveiculos.sistemaprincipal.application.veiculo.port.out;

import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;

/** Gateway HTTP para o servico-vendas-veiculos, usado para sincronizar veiculos. */
public interface VendasServicePort {

    void sincronizarVeiculo(Veiculo veiculo);
}
