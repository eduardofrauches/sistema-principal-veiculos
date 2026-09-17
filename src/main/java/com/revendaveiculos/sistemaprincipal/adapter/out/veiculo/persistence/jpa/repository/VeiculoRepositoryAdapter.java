package com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.repository;

import com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.entity.VeiculoEntity;
import com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.mapper.VeiculoEntityMapper;
import com.revendaveiculos.sistemaprincipal.application.veiculo.port.out.VeiculoRepositoryPort;
import com.revendaveiculos.sistemaprincipal.domain.model.veiculo.Veiculo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VeiculoRepositoryAdapter implements VeiculoRepositoryPort {

    private final VeiculoJpaRepository veiculoJpaRepository;
    private final VeiculoEntityMapper veiculoEntityMapper;

    public VeiculoRepositoryAdapter(VeiculoJpaRepository veiculoJpaRepository, VeiculoEntityMapper veiculoEntityMapper) {
        this.veiculoJpaRepository = veiculoJpaRepository;
        this.veiculoEntityMapper = veiculoEntityMapper;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        VeiculoEntity entitySalva = veiculoJpaRepository.save(veiculoEntityMapper.paraEntity(veiculo));
        return veiculoEntityMapper.paraDominio(entitySalva);
    }

    @Override
    public Optional<Veiculo> buscarPorId(Long id) {
        return veiculoJpaRepository.findById(id).map(veiculoEntityMapper::paraDominio);
    }
}
