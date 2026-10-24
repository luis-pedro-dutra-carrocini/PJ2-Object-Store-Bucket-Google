package com.restaurante.app.repository;

import com.restaurante.app.entity.AdmUnidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdmUnidadeRepository extends JpaRepository<AdmUnidade, UUID> {
    Optional<AdmUnidade> findByAdmUnidadeUsuario(String usuario);
}