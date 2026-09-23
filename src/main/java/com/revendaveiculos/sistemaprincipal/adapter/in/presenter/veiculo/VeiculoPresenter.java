package com.revendaveiculos.sistemaprincipal.adapter.in.presenter.veiculo;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Component;

/**
 * Unica responsavel por formatar a Entity de dominio Veiculo na resposta
 * HTTP (VeiculoResponse). Os UseCases devolvem a Entity; o Controller chama
 * este Presenter antes de montar o ResponseEntity.
 */
@Component
public class VeiculoPresenter {

    public VeiculoResponse apresentar(Veiculo veiculo) {
        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                veiculo.getCor(),
                veiculo.getPreco().valor(),
                veiculo.getPlaca(),
                veiculo.getStatus().name()
        );
    }
}
