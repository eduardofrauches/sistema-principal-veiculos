package com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response;

import java.math.BigDecimal;

public record VeiculoResponse(
        Long id,
        String marca,
        String modelo,
        Integer ano,
        String cor,
        BigDecimal preco,
        String placa,
        String status
) {
}
