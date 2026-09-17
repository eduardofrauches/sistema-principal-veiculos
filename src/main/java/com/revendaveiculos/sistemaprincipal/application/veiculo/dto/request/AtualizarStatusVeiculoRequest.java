package com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request;

import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.StatusVeiculo;
import jakarta.validation.constraints.NotNull;

/** Payload do callback enviado pelo servico-vendas-veiculos. */
public record AtualizarStatusVeiculoRequest(
        @NotNull(message = "status e obrigatorio") StatusVeiculo status
) {
}
