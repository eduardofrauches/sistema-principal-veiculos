package com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.repository;

import com.revendaveiculos.sistemaprincipal.adapter.out.veiculo.persistence.jpa.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, Long> {
}
