package com.revendaveiculos.sistemaprincipal.adapter.in.controller.veiculo;

import com.revendaveiculos.sistemaprincipal.adapter.in.presenter.veiculo.VeiculoPresenter;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.AtualizarStatusVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.EditarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.AtualizarStatusVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.CadastrarVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.EditarVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Depende apenas das interfaces de application/veiculo/port/in (que devolvem
 * a Entity de dominio) e do VeiculoPresenter (que a formata em
 * VeiculoResponse) — nunca diretamente dos UseCases ou dos ports/out.
 */
@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final CadastrarVeiculoInputPort cadastrarVeiculoInputPort;
    private final EditarVeiculoInputPort editarVeiculoInputPort;
    private final AtualizarStatusVeiculoInputPort atualizarStatusVeiculoInputPort;
    private final VeiculoPresenter veiculoPresenter;

    public VeiculoController(CadastrarVeiculoInputPort cadastrarVeiculoInputPort,
                              EditarVeiculoInputPort editarVeiculoInputPort,
                              AtualizarStatusVeiculoInputPort atualizarStatusVeiculoInputPort,
                              VeiculoPresenter veiculoPresenter) {
        this.cadastrarVeiculoInputPort = cadastrarVeiculoInputPort;
        this.editarVeiculoInputPort = editarVeiculoInputPort;
        this.atualizarStatusVeiculoInputPort = atualizarStatusVeiculoInputPort;
        this.veiculoPresenter = veiculoPresenter;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
        Veiculo veiculo = cadastrarVeiculoInputPort.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoPresenter.apresentar(veiculo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> editar(@PathVariable Long id, @Valid @RequestBody EditarVeiculoRequest request) {
        Veiculo veiculo = editarVeiculoInputPort.editar(id, request);
        return ResponseEntity.ok(veiculoPresenter.apresentar(veiculo));
    }

    /** Callback acionado pelo servico-vendas-veiculos. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<VeiculoResponse> atualizarStatus(@PathVariable Long id,
                                                             @Valid @RequestBody AtualizarStatusVeiculoRequest request) {
        Veiculo veiculo = atualizarStatusVeiculoInputPort.atualizarStatus(id, request);
        return ResponseEntity.ok(veiculoPresenter.apresentar(veiculo));
    }
}
