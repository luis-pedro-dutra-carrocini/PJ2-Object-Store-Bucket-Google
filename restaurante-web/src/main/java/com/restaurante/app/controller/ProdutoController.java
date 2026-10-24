package com.restaurante.app.controller;

import com.restaurante.app.entity.ImagemProduto;
import com.restaurante.app.entity.PrecoProduto;
import com.restaurante.app.entity.Produto;
import com.restaurante.app.entity.Unidade;
import com.restaurante.app.repository.PrecoProdutoRepository;
import com.restaurante.app.repository.ProdutoRepository;
import com.restaurante.app.repository.UnidadeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin
public class ProdutoController {

    private final ProdutoRepository produtoRepository;
    private final UnidadeRepository unidadeRepository;
    private final PrecoProdutoRepository precoRepository;

    public ProdutoController(ProdutoRepository produtoRepository,
                             UnidadeRepository unidadeRepository,
                             PrecoProdutoRepository precoRepository) {
        this.produtoRepository = produtoRepository;
        this.unidadeRepository = unidadeRepository;
        this.precoRepository = precoRepository;
    }

    // LISTAR PRODUTOS POR ID DA UNIDADE (PUBLICA)
    @GetMapping
    public ResponseEntity<?> listarPorUnidade(@RequestParam String unidadeId) {
        try {
            UUID id = UUID.fromString(unidadeId);
            Unidade u = unidadeRepository.findById(id).orElse(null);
            
            if (u == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Unidade não encontrada."));
            }

            List<Produto> produtos = produtoRepository.findByUnidadeUnidadeIdAndProdutoStatus(id, "ATIVO");
            List<Map<String, Object>> resposta = new ArrayList<>();

            for (Produto p : produtos) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("produtoId", p.getProdutoId());
                dto.put("produtoNome", p.getProdutoNome());
                dto.put("produtoDescricao", p.getProdutoDescricao());

                // Busca a imagem principal
                String imagemPrincipalUrl = null;
                if (p.getImagens() != null) {
                    for (ImagemProduto img : p.getImagens()) {
                        if ("SIM".equals(img.getImagemProdutoPrincipal())) {
                            imagemPrincipalUrl = img.getImagemProdutoURL();
                            break;
                        }
                    }
                    // Se não achar principal, pega a primeira
                    if (imagemPrincipalUrl == null && !p.getImagens().isEmpty()) {
                        imagemPrincipalUrl = p.getImagens().get(0).getImagemProdutoURL();
                    }
                }
                dto.put("imagemPrincipalUrl", imagemPrincipalUrl);

                // Busca o preço atual
                List<PrecoProduto> precos = precoRepository.findByProdutoProdutoIdAndPrecoProdutoStatus(p.getProdutoId(), "ATIVO");
                BigDecimal precoAtual = precos.isEmpty() ? BigDecimal.ZERO : precos.get(0).getPrecoProdutoValor();
                dto.put("precoAtual", precoAtual);

                resposta.add(dto);
            }

            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "ID da unidade inválido."));
        }
    }

    // DETALHAR PRODUTO (PARA O MODAL - PUBLICA)
    @GetMapping("/{id}")
    public ResponseEntity<?> detalhar(@PathVariable UUID id) {
        Produto p = produtoRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();

        List<PrecoProduto> precos = precoRepository.findByProdutoProdutoIdAndPrecoProdutoStatus(id, "ATIVO");
        BigDecimal precoAtual = precos.isEmpty() ? BigDecimal.ZERO : precos.get(0).getPrecoProdutoValor();

        Map<String, Object> dto = new HashMap<>();
        dto.put("produtoId", p.getProdutoId());
        dto.put("produtoNome", p.getProdutoNome());
        dto.put("produtoDescricao", p.getProdutoDescricao());
        dto.put("precoAtual", precoAtual);
        
        // Ordena as imagens para a principal vir primeiro
        List<ImagemProduto> imagens = p.getImagens() != null ? p.getImagens() : new ArrayList<>();
        imagens.sort((i1, i2) -> {
            if ("SIM".equals(i1.getImagemProdutoPrincipal())) return -1;
            if ("SIM".equals(i2.getImagemProdutoPrincipal())) return 1;
            return i1.getImagemProdutoSequencia().compareTo(i2.getImagemProdutoSequencia());
        });
        dto.put("imagens", imagens);

        return ResponseEntity.ok(dto);
    }

    // BUSCAR DADOS DA UNIDADE (PARA O TÍTULO - PUBLICA)
    @GetMapping("/unidade/{unidadeId}")
    public ResponseEntity<?> buscarUnidade(@PathVariable UUID unidadeId) {
        return unidadeRepository.findById(unidadeId)
                .map(unidade -> ResponseEntity.ok(Map.of(
                        "unidadeNome", unidade.getUnidadeNome(),
                        "unidadeDescricao", unidade.getUnidadeDescricao()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}