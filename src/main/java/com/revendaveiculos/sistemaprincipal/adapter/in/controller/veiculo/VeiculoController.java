package com.revendaveiculos.sistemaprincipal.adapter.in.controller.veiculo;

import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.AtualizarStatusVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.CadastrarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.request.EditarVeiculoRequest;
import com.revendaveiculos.sistemaprincipal.application.veiculo.dto.response.VeiculoResponse;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.AtualizarStatusVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.CadastrarVeiculoInputPort;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.in.EditarVeiculoInputPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Depende apenas das interfaces de application/veiculo/port/in — nunca
 * diretamente dos UseCases ou dos ports/out.
 */
@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final CadastrarVeiculoInputPort cadastrarVeiculoInputPort;
    private final EditarVeiculoInputPort editarVeiculoInputPort;
    private final AtualizarStatusVeiculoInputPort atualizarStatusVeiculoInputPort;

    public VeiculoController(CadastrarVeiculoInputPort cadastrarVeiculoInputPort,
                              EditarVeiculoInputPort editarVeiculoInputPort,
                              AtualizarStatusVeiculoInputPort atualizarStatusVeiculoInputPort) {
        this.cadastrarVeiculoInputPort = cadastrarVeiculoInputPort;
        this.editarVeiculoInputPort = editarVeiculoInputPort;
        this.atualizarStatusVeiculoInputPort = atualizarStatusVeiculoInputPort;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody CadastrarVeiculoRequest request) {
        VeiculoResponse response = cadastrarVeiculoInputPort.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> editar(@PathVariable Long id, @Valid @RequestBody EditarVeiculoRequest request) {
        return ResponseEntity.ok(editarVeiculoInputPort.editar(id, request));
    }

    /** Callback acionado pelo servico-vendas-veiculos. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<VeiculoResponse> atualizarStatus(@PathVariable Long id,
                                                             @Valid @RequestBody AtualizarStatusVeiculoRequest request) {
        return ResponseEntity.ok(atualizarStatusVeiculoInputPort.atualizarStatus(id, request));
    }
}
