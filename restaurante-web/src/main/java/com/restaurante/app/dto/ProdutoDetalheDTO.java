package com.restaurante.app.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProdutoDetalheDTO {
    private UUID produtoId;
    private String produtoNome;
    private String produtoDescricao;
    private BigDecimal precoAtual;
    private List<ImagemDTO> imagens;

    public static class ImagemDTO {
        private UUID imagemProdutoId;
        private String imagemProdutoURL;
        private String imagemProdutoPrincipal;
    }
}