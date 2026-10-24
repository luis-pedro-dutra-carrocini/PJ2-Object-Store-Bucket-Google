package com.restaurante.app.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "unidade")
public class Unidade {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID unidadeId;

    @Column(length = 100)
    private String unidadeNome;

    @Column(columnDefinition = "TEXT")
    private String unidadeDescricao;

    @Column(length = 15)
    private String unidadeTelefone;

    @Column(length = 256)
    private String unidadeEmail;

    @Column(length = 20)
    private String unidadeStatus = "ATIVO"; 
    
}