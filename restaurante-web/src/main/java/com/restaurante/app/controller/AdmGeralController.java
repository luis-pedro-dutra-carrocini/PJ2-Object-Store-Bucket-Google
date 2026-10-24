package com.restaurante.app.controller;

import com.restaurante.app.entity.AdmUnidade;
import com.restaurante.app.entity.Unidade;
import com.restaurante.app.repository.AdmGeralSistemaRepository;
import com.restaurante.app.repository.AdmUnidadeRepository;
import com.restaurante.app.repository.UnidadeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/adm-geral")
@CrossOrigin
public class AdmGeralController {

    private final AdmGeralSistemaRepository admGeralRepository;
    private final UnidadeRepository unidadeRepository;
    private final AdmUnidadeRepository admUnidadeRepository;
    private final PasswordEncoder passwordEncoder;

    public AdmGeralController(AdmGeralSistemaRepository admGeralRepository,
            UnidadeRepository unidadeRepository,
            AdmUnidadeRepository admUnidadeRepository,
            PasswordEncoder passwordEncoder) {
        this.admGeralRepository = admGeralRepository;
        this.unidadeRepository = unidadeRepository;
        this.admUnidadeRepository = admUnidadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        String usuario = credenciais.get("usuario");
        String senha = credenciais.get("senha");

        return admGeralRepository.findByAdmGeralSistemaUsuario(usuario)
                .map(adm -> {
                    if (passwordEncoder.matches(senha, adm.getAdmGeralSistemaSenha())
                            && "ATIVO".equals(adm.getAdmGeralSistemaStatus())) {
                        return ResponseEntity.ok().body(Map.of("mensagem", "Login realizado com sucesso"));
                    }
                    return ResponseEntity.status(401).body(Map.of("erro", "Senha incorreta ou usuário inativo"));
                })
                .orElse(ResponseEntity.status(401).body(Map.of("erro", "Usuário não encontrado")));
    }

    // --- UNIDADES ---

    @GetMapping("/unidades")
    public List<Unidade> listarUnidades() {
        return unidadeRepository.findAll();
    }

    @PostMapping("/unidades")
    public Unidade cadastrarUnidade(@RequestBody Unidade unidade) {
        unidade.setUnidadeStatus("ATIVO");
        return unidadeRepository.save(unidade);
    }

    @PutMapping("/unidades/{id}")
    public Unidade atualizarUnidade(@PathVariable UUID id, @RequestBody Unidade unidadeAtualizada) {
        Unidade unidade = unidadeRepository.findById(id).orElseThrow();
        unidade.setUnidadeNome(unidadeAtualizada.getUnidadeNome());
        unidade.setUnidadeDescricao(unidadeAtualizada.getUnidadeDescricao());
        unidade.setUnidadeTelefone(unidadeAtualizada.getUnidadeTelefone());
        unidade.setUnidadeEmail(unidadeAtualizada.getUnidadeEmail());
        unidade.setUnidadeStatus(unidadeAtualizada.getUnidadeStatus());
        return unidadeRepository.save(unidade);
    }


    // --- ADM UNIDADES ---

    @GetMapping("/adm-unidades")
    public List<AdmUnidade> listarAdmUnidades() {
        return admUnidadeRepository.findAll();
    }

    @PostMapping("/adm-unidades")
    public ResponseEntity<?> cadastrarAdmUnidade(@RequestBody Map<String, String> body) {
        String usuario = body.get("usuario");

        // VALIDAÇÃO: Verifica se o usuário já existe em AdmUnidade ou AdmGeral
        if (admUnidadeRepository.findByAdmUnidadeUsuario(usuario).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Este nome de usuário já está em uso. Escolha outro."));
        }

        UUID unidadeId = UUID.fromString(body.get("unidadeId"));
        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        AdmUnidade adm = new AdmUnidade();
        adm.setAdmUnidadeNome(body.get("nome"));
        adm.setAdmUnidadeUsuario(usuario);
        adm.setAdmUnidadeSenha(passwordEncoder.encode(body.get("senha")));
        adm.setAdmUnidadeStatus("ATIVO");
        adm.setUnidade(unidade);

        return ResponseEntity.ok(admUnidadeRepository.save(adm));
    }

    @PutMapping("/adm-unidades/{id}")
    public ResponseEntity<?> atualizarAdmUnidade(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        AdmUnidade adm = admUnidadeRepository.findById(id).orElseThrow();
        String novoUsuario = body.get("usuario");

        // VALIDAÇÃO: Verifica se o novo usuário já existe em outro AdmUnidade ou no
        // AdmGeral
        // (Só valida se o usuário foi realmente alterado)
        if (!adm.getAdmUnidadeUsuario().equals(novoUsuario)) {
            if (admUnidadeRepository.findByAdmUnidadeUsuario(novoUsuario).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Este nome de usuário já está em uso. Escolha outro."));
            }
        }

        adm.setAdmUnidadeNome(body.get("nome"));
        adm.setAdmUnidadeUsuario(novoUsuario);

        if (body.get("senha") != null && !body.get("senha").trim().isEmpty()) {
            adm.setAdmUnidadeSenha(passwordEncoder.encode(body.get("senha")));
        }

        if (body.get("status") != null) {
            adm.setAdmUnidadeStatus(body.get("status"));
        }

        if (body.get("unidadeId") != null) {
            Unidade unidade = unidadeRepository.findById(UUID.fromString(body.get("unidadeId"))).orElseThrow();
            adm.setUnidade(unidade);
        }

        return ResponseEntity.ok(admUnidadeRepository.save(adm));
    }

}