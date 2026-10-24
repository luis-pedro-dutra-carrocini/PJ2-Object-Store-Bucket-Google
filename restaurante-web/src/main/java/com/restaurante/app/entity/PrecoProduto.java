package com.restaurante.app.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "preco_produto")
public class PrecoProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID precoProdutoId;

    @Column(precision = 10, scale = 2)
    private java.math.BigDecimal precoProdutoValor;

    @Column(length = 20)
    private String precoProdutoStatus;

    @ManyToOne
    @JoinColumn(name = "adm_unidade_id")
    private AdmUnidade admUnidade;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

}