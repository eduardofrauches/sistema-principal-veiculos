package com.revendaveiculos.sistemaprincipal.application.veiculo.mapper;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import com.revendaveiculos.sistemaprincipal.domain.vo.Preco;
import org.springframework.stereotype.Component;

@Component
public class VeiculoMapper {

    public Veiculo paraDominio(CadastrarVeiculoRequest request) {
        return Veiculo.cadastrar(
                request.marca(),
                request.modelo(),
                request.ano(),
                request.cor(),
                Preco.de(request.preco()),
                request.placa()
        );
    }
}
