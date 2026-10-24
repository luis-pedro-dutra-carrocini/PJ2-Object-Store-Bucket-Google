package com.restaurante.app.controller;

import com.restaurante.app.repository.AdmUnidadeRepository;
import com.restaurante.app.repository.ImagemProdutoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/adm-unidade")
@CrossOrigin
public class AdmUnidadeController {

    private final AdmUnidadeRepository admUnidadeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdmUnidadeController(AdmUnidadeRepository admUnidadeRepository,
                                PasswordEncoder passwordEncoder, 
                                ImagemProdutoRepository imagemProdutoRepository) {
        this.admUnidadeRepository = admUnidadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // LOGIN DO ADM UNIDADE
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String usuario = credenciais.get("usuario");
        String senha = credenciais.get("senha");

        return admUnidadeRepository.findByAdmUnidadeUsuario(usuario)
                .map(adm -> {
                    if (passwordEncoder.matches(senha, adm.getAdmUnidadeSenha()) 
                            && "ATIVO".equals(adm.getAdmUnidadeStatus())) {
                        
                        if (!"ATIVO".equals(adm.getUnidade().getUnidadeStatus())) {
                            return ResponseEntity.status(401).body(Map.of("erro", "A unidade deste administrador está inativa."));
                        }

                        return ResponseEntity.ok().body(Map.of(
                                "admUnidadeId", adm.getAdmUnidadeId(),
                                "admUnidadeNome", adm.getAdmUnidadeNome(),
                                "unidadeId", adm.getUnidade().getUnidadeId(),
                                "unidadeNome", adm.getUnidade().getUnidadeNome()
                        ));
                    }
                    return ResponseEntity.status(401).body(Map.of("erro", "Senha incorreta ou usuário inativo"));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("erro", "Usuário não encontrado")));
    }

}