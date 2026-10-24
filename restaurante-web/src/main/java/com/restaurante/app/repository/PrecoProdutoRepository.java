package com.restaurante.app.repository;

import com.restaurante.app.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PrecoProdutoRepository extends JpaRepository<PrecoProduto, UUID> {
    List<PrecoProduto> findByProdutoProdutoIdAndPrecoProdutoStatus(UUID produtoId, String status);
}
