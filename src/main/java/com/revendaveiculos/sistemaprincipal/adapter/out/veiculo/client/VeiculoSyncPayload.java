package com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.client;

import java.math.BigDecimal;

/**
 * Payload enviado ao endpoint POST /veiculos/sync do servico-vendas-veiculos.
 * Note que "placa" nao e enviada: o servico de vendas so guarda uma copia
 * resumida do veiculo (ver arquitetura-revenda-veiculos.md).
 */
public record VeiculoSyncPayload(
        Long id,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        BigDecimal preco,
        String status
) {
}
