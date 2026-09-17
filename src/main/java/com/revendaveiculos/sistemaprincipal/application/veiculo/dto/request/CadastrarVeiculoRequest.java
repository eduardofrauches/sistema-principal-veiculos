package com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CadastrarVeiculoRequest(
        @NotBlank(message = "marca e obrigatoria") String marca,
        @NotBlank(message = "modelo e obrigatorio") String modelo,
        @NotNull(message = "ano e obrigatorio") @Min(value = 1900, message = "ano invalido") Integer ano,
        @NotBlank(message = "cor e obrigatoria") String cor,
        @NotNull(message = "preco e obrigatorio") @Positive(message = "preco deve ser maior que zero") BigDecimal preco,
        @NotBlank(message = "placa e obrigatoria") String placa
) {
}
