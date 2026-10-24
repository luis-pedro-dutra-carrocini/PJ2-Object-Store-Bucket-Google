package com.restaurante.app.repository;

import com.restaurante.app.entity.AdmGeralSistema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdmGeralSistemaRepository extends JpaRepository<AdmGeralSistema, UUID> {
    Optional<AdmGeralSistema> findByAdmGeralSistemaUsuario(String usuario);
}