package com.revendaveiculos.sistemaprincipal.domain.model.veiculo;

import com.revendaveiculos.sistemaprincipal.domain.exception.TransicaoStatusInvalidaException;
import com.revendaveiculos.sistemaprincipal.domain.vo.Preco;

import java.util.Map;
import java.util.Set;

/**
 * Entidade de dominio pura (sem anotacao JPA). Representa o veiculo
 * cadastrado no sistema-principal, a fonte de verdade dos dados-mestre.
 */
public class Veiculo {

    private static final Map<StatusVeiculo, Set<StatusVeiculo>> TRANSICOES_VALIDAS = Map.of(
            StatusVeiculo.DISPONIVEL, Set.of(StatusVeiculo.RESERVADO),
            StatusVeiculo.RESERVADO, Set.of(StatusVeiculo.VENDIDO, StatusVeiculo.DISPONIVEL),
            StatusVeiculo.VENDIDO, Set.of()
    );

    private Long id;
    private String marca;
    private String modelo;
    private Integer ano;
    private String cor;
    private Preco preco;
    private String placa;
    private StatusVeiculo status;

    private Veiculo(Long id, String marca, String modelo, Integer ano, String cor, Preco preco,
                     String placa, StatusVeiculo status) {
        this.id = id;
        this.marca = exigirNaoVazio(marca, "marca");
        this.modelo = exigirNaoVazio(modelo, "modelo");
        this.ano = exigirAnoValido(ano);
        this.cor = exigirNaoVazio(cor, "cor");
        this.preco = preco;
        this.placa = exigirNaoVazio(placa, "placa");
        this.status = status;
    }

    /** Cria um veiculo novo, ainda sem id, sempre DISPONIVEL. */
    public static Veiculo cadastrar(String marca, String modelo, Integer ano, String cor, Preco preco, String placa) {
        return new Veiculo(null, marca, modelo, ano, cor, preco, placa, StatusVeiculo.DISPONIVEL);
    }

    /** Reconstroi um veiculo existente (vindo da persistencia). */
    public static Veiculo restaurar(Long id, String marca, String modelo, Integer ano, String cor, Preco preco,
                                     String placa, StatusVeiculo status) {
        return new Veiculo(id, marca, modelo, ano, cor, preco, placa, status);
    }

    /** Atualiza os dados cadastrais. Nao permitido para veiculo ja vendido. */
    public void atualizarDados(String marca, String modelo, Integer ano, String cor, Preco preco, String placa) {
        if (status == StatusVeiculo.VENDIDO) {
            throw new TransicaoStatusInvalidaException("Nao e possivel editar um veiculo ja vendido");
        }
        this.marca = exigirNaoVazio(marca, "marca");
        this.modelo = exigirNaoVazio(modelo, "modelo");
        this.ano = exigirAnoValido(ano);
        this.cor = exigirNaoVazio(cor, "cor");
        this.preco = preco;
        this.placa = exigirNaoVazio(placa, "placa");
    }

    public void reservar() {
        transicionarPara(StatusVeiculo.RESERVADO);
    }

    public void confirmarVenda() {
        transicionarPara(StatusVeiculo.VENDIDO);
    }

    public void cancelarReserva() {
        transicionarPara(StatusVeiculo.DISPONIVEL);
    }

    /** Usado pelo AtualizarStatusVeiculoUseCase (callback do servico de vendas). */
    public void atualizarStatus(StatusVeiculo novoStatus) {
        transicionarPara(novoStatus);
    }

    private void transicionarPara(StatusVeiculo novoStatus) {
        Set<StatusVeiculo> permitidas = TRANSICOES_VALIDAS.get(this.status);
        if (permitidas == null || !permitidas.contains(novoStatus)) {
            throw new TransicaoStatusInvalidaException(
                    "Transicao de status invalida: " + this.status + " -> " + novoStatus);
        }
        this.status = novoStatus;
    }

    private static String exigirNaoVazio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " nao pode ser vazio");
        }
        return valor;
    }

    private static Integer exigirAnoValido(Integer ano) {
        if (ano == null || ano < 1900) {
            throw new IllegalArgumentException("ano invalido: " + ano);
        }
        return ano;
    }

    public Long getId() {
        return id;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public String getCor() {
        return cor;
    }

    public Preco getPreco() {
        return preco;
    }

    public String getPlaca() {
        return placa;
    }

    public StatusVeiculo getStatus() {
        return status;
    }
}
