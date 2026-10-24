package com.restaurante.app.repository;

import com.restaurante.app.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {
    Unidade findByUnidadeNomeIgnoreCase(String nome);
}
