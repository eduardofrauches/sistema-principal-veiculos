package com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.entity;

import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.StatusVeiculo;
import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class VeiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private String cor;

    @Column(nullable = false, precision = 12, scale = 2)
    private java.math.BigDecimal preco;

    @Column(nullable = false, unique = true)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVeiculo status;

    protected VeiculoEntity() {
        // exigido pelo JPA
    }

    public VeiculoEntity(Long id, String marca, String modelo, Integer ano, String cor,
                          java.math.BigDecimal preco, String placa, StatusVeiculo status) {
        this.id = id;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cor = cor;
        this.preco = preco;
        this.placa = placa;
        this.status = status;
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

    public java.math.BigDecimal getPreco() {
        return preco;
    }

    public String getPlaca() {
        return placa;
    }

    public StatusVeiculo getStatus() {
        return status;
    }
}
