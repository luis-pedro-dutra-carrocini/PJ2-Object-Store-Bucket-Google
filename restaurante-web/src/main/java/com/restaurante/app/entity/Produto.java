package com.restaurante.app.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "produto")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID produtoId;

    @Column(length = 100)
    private String produtoNome;

    @Column(columnDefinition = "TEXT")
    private String produtoDescricao;

    private LocalDateTime produtoDtCadastro;

    @Column(length = 20)
    private String produtoStatus;

    @ManyToOne
    @JoinColumn(name = "adm_unidade_id")
    @JsonIgnore
    private AdmUnidade admUnidade;

    @ManyToOne
    @JoinColumn(name = "unidade_id", nullable = false)
    @JsonIgnore
    private Unidade unidade;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL)
    private List<ImagemProduto> imagens;
}