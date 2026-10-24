package com.restaurante.app.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "adm_unidade")
public class AdmUnidade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID admUnidadeId;

    @Column(length = 100)
    private String admUnidadeNome;

    @Column(length = 20, unique = true)
    private String admUnidadeUsuario;

    @Column(columnDefinition = "TEXT")
    private String admUnidadeSenha;

    @Column(length = 20)
    private String admUnidadeStatus = "ATIVO";

    @ManyToOne
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

}