package com.restaurante.app.repository;

import com.restaurante.app.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    List<Produto> findByUnidadeUnidadeIdAndProdutoStatus(UUID unidadeId, String status);
    List<Produto> findByUnidadeUnidadeId(UUID unidadeId);
}
