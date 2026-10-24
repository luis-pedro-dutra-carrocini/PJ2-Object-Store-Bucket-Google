package com.restaurante.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "imagem_produto")
public class ImagemProduto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID imagemProdutoId;

    @Column(length = 20)
    private String imagemProdutoPrincipal;

    @Column(columnDefinition = "TEXT")
    private String imagemProdutoURL;

    private LocalDateTime imagemProdutoDtCadastro;

    @Column(length = 20)
    private String imagemProdutoStatus;

    private Integer imagemProdutoSequencia;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    @JsonIgnore
    private Produto produto;

}