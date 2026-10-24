package com.restaurante.app.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProdutoListagemDTO {
    private UUID produtoId;
    private String produtoNome;
    private String imagemPrincipalUrl;
    private BigDecimal precoAtual;

    public ProdutoListagemDTO(UUID produtoId, String produtoNome,
                              String imagemPrincipalUrl, BigDecimal precoAtual) {
        this.produtoId = produtoId;
        this.produtoNome = produtoNome;
        this.imagemPrincipalUrl = imagemPrincipalUrl;
        this.precoAtual = precoAtual;
    }
    // getters
}