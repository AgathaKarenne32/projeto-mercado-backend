package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.dto.request.CreateRascunhoRequest;
import com.prati.projetomercado.dto.request.UpdateRascunhoRequest;
import com.prati.projetomercado.dto.response.RascunhoResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Rascunho;
import com.prati.projetomercado.repository.AuthUserRepository; // Import que faltava
import com.prati.projetomercado.repository.RascunhoRepository;
import com.prati.projetomercado.service.RascunhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RascunhoServiceImpl implements RascunhoService {

    private final RascunhoRepository rascunhoRepository;
    private final AuthUserRepository authUserRepository; // Adicionamos este repositório

    private RascunhoResponse paraRascunhoResponse(Rascunho rascunho) {
        return new RascunhoResponse(
                rascunho.getId(),
                rascunho.getMercado(),
                rascunho.getConteudo(),
                rascunho.getCreatedAt(),
                rascunho.getUpdatedAt()
        );
    }

    private AuthUser getUsuarioAutenticado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return authUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado no banco de dados."));
    }

    @Override
    public RascunhoResponse criarRascunho(CreateRascunhoRequest createRascunhoRequest) {
        AuthUser usuarioLogado = getUsuarioAutenticado();
        Rascunho novoRascunho = Rascunho.builder()
                .mercado(createRascunhoRequest.mercado())
                .conteudo(createRascunhoRequest.conteudo())
                .user(usuarioLogado)
                .build();
        Rascunho rascunhoSalvo = rascunhoRepository.save(novoRascunho);
        return paraRascunhoResponse(rascunhoSalvo);
    }

    @Override
    public List<RascunhoResponse> buscarRascunhosDoUsuario() {
        AuthUser usuarioLogado = getUsuarioAutenticado();
        List<Rascunho> rascunhos = rascunhoRepository.findByUser(usuarioLogado);
        return rascunhos.stream()
                .map(this::paraRascunhoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RascunhoResponse buscarRascunhoPorId(Long rascunhoId) {
        AuthUser usuarioLogado = getUsuarioAutenticado();
        Rascunho rascunho = rascunhoRepository.findRascunhoByUserAndId(usuarioLogado, rascunhoId)
                .orElseThrow(() -> new RuntimeException("Rascunho não encontrado"));
        return paraRascunhoResponse(rascunho);
    }

    @Override
    public RascunhoResponse atualizarRascunho(Long rascunhoId, UpdateRascunhoRequest updateRascunhoRequest) {
        AuthUser usuarioLogado = getUsuarioAutenticado();
        Rascunho rascunhoExistente = rascunhoRepository.findRascunhoByUserAndId(usuarioLogado, rascunhoId)
                .orElseThrow(() -> new RuntimeException("Rascunho não encontrado"));
        rascunhoExistente.setMercado(updateRascunhoRequest.mercado());
        rascunhoExistente.setConteudo(updateRascunhoRequest.conteudo());
        Rascunho rascunhoAtualizado = rascunhoRepository.save(rascunhoExistente);
        return paraRascunhoResponse(rascunhoAtualizado);
    }

    @Override
    public void apagarRascunho(Long rascunhoId) {
        System.out.println("pegando rascunho");
        var rascunho = rascunhoRepository.getRascunhoByUserAndId(getUsuarioAutenticado(), rascunhoId);
        System.out.println("rascunho pegod");
        rascunhoRepository.delete(rascunho.getFirst());
//        rascunhoRepository.deleteRascunhoByUserAndId(getUsuarioAutenticado(), rascunhoId);
    }
}