package com.restaurante.app.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "adm_geral_sistema")
public class AdmGeralSistema {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID admGeralSistemaId;

    @Column(length = 20)
    private String admGeralSistemaStatus;

    @Column(length = 100)
    private String admGeralSistemaNome;

    @Column(length = 20, unique = true)
    private String admGeralSistemaUsuario;

    @Column(columnDefinition = "TEXT")
    private String admGeralSistemaSenha;

    // getters e setters
}