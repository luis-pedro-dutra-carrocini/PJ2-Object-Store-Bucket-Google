package com.restaurante.app.controller;

import com.restaurante.app.entity.AdmUnidade;
import com.restaurante.app.entity.ImagemProduto;
import com.restaurante.app.entity.PrecoProduto;
import com.restaurante.app.entity.Produto;
import com.restaurante.app.entity.Unidade;
import com.restaurante.app.repository.AdmUnidadeRepository;
import com.restaurante.app.repository.ImagemProdutoRepository;
import com.restaurante.app.repository.PrecoProdutoRepository;
import com.restaurante.app.repository.ProdutoRepository;
import com.restaurante.app.repository.UnidadeRepository;
import com.restaurante.app.service.StorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adm/produtos")
@CrossOrigin
public class AdmProdutoController {

    private final ProdutoRepository produtoRepository;
    private final ImagemProdutoRepository imagemRepository;
    private final StorageService storageService;
    private final UnidadeRepository unidadeRepository;
    private final AdmUnidadeRepository admUnidadeRepository;
    private final PrecoProdutoRepository precoRepository;

    public AdmProdutoController(ProdutoRepository produtoRepository,
            ImagemProdutoRepository imagemRepository,
            StorageService storageService,
            UnidadeRepository unidadeRepository,
            AdmUnidadeRepository admUnidadeRepository,
        PrecoProdutoRepository precoRepository) {
        this.produtoRepository = produtoRepository;
        this.imagemRepository = imagemRepository;
        this.storageService = storageService;
        this.unidadeRepository = unidadeRepository;
        this.admUnidadeRepository = admUnidadeRepository;
        this.precoRepository = precoRepository;
    }

    // Ajustado para receber os IDs via URL
    @PostMapping("/{unidadeId}/{admUnidadeId}")
    public Produto criar(@PathVariable UUID unidadeId, @PathVariable UUID admUnidadeId, @RequestBody Produto produto) {
        Unidade unidade = unidadeRepository.findById(unidadeId).orElseThrow();
        AdmUnidade adm = admUnidadeRepository.findById(admUnidadeId).orElseThrow();

        produto.setUnidade(unidade);
        produto.setAdmUnidade(adm);
        produto.setProdutoDtCadastro(LocalDateTime.now());
        produto.setProdutoStatus("ATIVO");

        return produtoRepository.save(produto);
    }

    @PostMapping("/{produtoId}/imagens")
    public ImagemProduto uploadImagem(
            @PathVariable UUID produtoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "principal", defaultValue = "NAO") String principal) throws IOException {

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        // Se for principal, desmarca as outras imagens do mesmo produto
        if ("SIM".equals(principal)) {
            imagemRepository.findByProdutoProdutoIdOrderByImagemProdutoSequenciaAsc(produtoId)
                    .forEach(img -> {
                        img.setImagemProdutoPrincipal("NAO");
                        imagemRepository.save(img);
                    });
        }

        // Cria a entidade e salva no banco
        ImagemProduto img = new ImagemProduto();
        img.setProduto(produto);
        img.setImagemProdutoPrincipal(principal);
        img.setImagemProdutoDtCadastro(LocalDateTime.now());
        img.setImagemProdutoStatus("ATIVO");
        img.setImagemProdutoSequencia(
                imagemRepository.findByProdutoProdutoIdOrderByImagemProdutoSequenciaAsc(produtoId).size() + 1);
        
        // Salva no banco primeiro, ID é gerado
        img = imagemRepository.save(img);

        // Preparando o caminho no bucket
        UUID unidadeId = produto.getUnidade().getUnidadeId();
        UUID imagemId = img.getImagemProdutoId();
        
        String originalFilename = file.getOriginalFilename();
        String extensao = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extensao = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        // Caminho: produtos/{unidadeId}/{produtoId}/{imagemId}.extensao
        String caminhoNoBucket = "produtos/" + unidadeId + "/" + produtoId + "/" + imagemId + extensao;

        // Faz o upload no GCS
        String url = storageService.uploadFile(file, caminhoNoBucket);

        // Atualiza a entidade com a URL e salva de novo
        img.setImagemProdutoURL(url);
        return imagemRepository.save(img);
    }

    // Excluir imagem
    @DeleteMapping("/imagens/{imagemId}")
    public void excluirImagem(@PathVariable UUID imagemId) throws IOException {
        ImagemProduto img = imagemRepository.findById(imagemId).orElseThrow();
        storageService.deleteFile(img.getImagemProdutoURL());
        imagemRepository.delete(img);
    }

    // LISTAR PRODUTOS DA UNIDADE
    @GetMapping("/{unidadeId}/produtos")
    public List<Produto> listarProdutos(@PathVariable UUID unidadeId) {
        return produtoRepository.findByUnidadeUnidadeId(unidadeId);
    }

    // EDITAR PRODUTO
    @PutMapping("/produtos/{produtoId}")
    public Produto atualizarProduto(@PathVariable UUID produtoId, @RequestBody Produto produtoAtualizado) {
        Produto p = produtoRepository.findById(produtoId).orElseThrow();
        p.setProdutoNome(produtoAtualizado.getProdutoNome());
        p.setProdutoDescricao(produtoAtualizado.getProdutoDescricao());
        p.setProdutoStatus(produtoAtualizado.getProdutoStatus());
        return produtoRepository.save(p);
    }

    // BUSCAR PREÇO ATUAL (PRODUTO)
    @GetMapping("/produtos/{produtoId}/preco-atual")
    public ResponseEntity<?> precoAtual(@PathVariable UUID produtoId) {
        List<PrecoProduto> precos = precoRepository.findByProdutoProdutoIdAndPrecoProdutoStatus(produtoId, "ATIVO");
        if(precos.isEmpty()) {
            return ResponseEntity.ok(Map.of("precoProdutoValor", 0));
        }
        return ResponseEntity.ok(Map.of("precoProdutoValor", precos.get(0).getPrecoProdutoValor()));
    }

    // CADASTRAR NOVO PREÇO (INATIVANDO O ANTIGO)
    @PostMapping("/produtos/{produtoId}/preco")
    public ResponseEntity<?> cadastrarPreco(@PathVariable UUID produtoId, @RequestBody Map<String, String> body) {
        Produto produto = produtoRepository.findById(produtoId).orElseThrow();
        
        // Inativa os preços antigos deste produto
        List<PrecoProduto> precosAntigos = precoRepository.findByProdutoProdutoIdAndPrecoProdutoStatus(produtoId, "ATIVO");
        precosAntigos.forEach(p -> p.setPrecoProdutoStatus("INATIVO"));
        precoRepository.saveAll(precosAntigos);

        // Cria o novo preço
        PrecoProduto novoPreco = new PrecoProduto();
        novoPreco.setPrecoProdutoValor(new BigDecimal(body.get("valor")));
        novoPreco.setPrecoProdutoStatus("ATIVO");
        novoPreco.setAdmUnidade(produto.getAdmUnidade());
        novoPreco.setProduto(produto);

        return ResponseEntity.ok(precoRepository.save(novoPreco));
    }

    // ROTA PARA TORNAR UMA IMAGEM PRINCIPAL
    @PutMapping("/imagens/{imagemId}/principal")
    public ResponseEntity<?> tornarImagemPrincipal(@PathVariable UUID imagemId) {
        ImagemProduto novaPrincipal = imagemRepository.findById(imagemId)
                .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));
        
        // Busca todas as imagens do produto e marca a nova como principal, as outras como NAO
        List<ImagemProduto> todasImagens = imagemRepository.findByProdutoProdutoIdOrderByImagemProdutoSequenciaAsc(novaPrincipal.getProduto().getProdutoId());
        
        for (ImagemProduto img : todasImagens) {
            if (img.getImagemProdutoId().equals(imagemId)) {
                img.setImagemProdutoPrincipal("SIM");
            } else {
                img.setImagemProdutoPrincipal("NAO");
            }
        }
        imagemRepository.saveAll(todasImagens);
        
        return ResponseEntity.ok().build();
    }


}