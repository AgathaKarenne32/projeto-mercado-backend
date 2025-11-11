package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.dto.request.CreateRascunhoRequest;
import com.prati.projetomercado.dto.request.RascunhoFilterRequest;
import com.prati.projetomercado.dto.request.UpdateRascunhoRequest;
import com.prati.projetomercado.dto.response.RascunhoResponse;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.Rascunho;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.repository.RascunhoRepository;
import com.prati.projetomercado.repository.spec.RascunhoSpecification;
import com.prati.projetomercado.service.RascunhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                rascunho.getTotalPrice(),
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
                .totalPrice(createRascunhoRequest.totalPrice())
                .user(usuarioLogado)
                .build();
        Rascunho rascunhoSalvo = rascunhoRepository.save(novoRascunho);
        return paraRascunhoResponse(rascunhoSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RascunhoResponse> buscarRascunhosDoUsuario(int page, int size) {
        AuthUser usuarioLogado = getUsuarioAutenticado();

        Pageable pageable = PageRequest.of(page, size);

        Page<Rascunho> rascunhosPage = rascunhoRepository.findByUser(usuarioLogado, pageable);
        return rascunhosPage.map(this::paraRascunhoResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RascunhoResponse buscarRascunhoPorId(Long rascunhoId) {
        AuthUser usuarioLogado = getUsuarioAutenticado();
        Rascunho rascunho = rascunhoRepository.findRascunhoByUserAndId(usuarioLogado, rascunhoId)
                .orElseThrow(() -> new RuntimeException("Rascunho não encontrado"));
        return paraRascunhoResponse(rascunho);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RascunhoResponse> searchRascunhos(RascunhoFilterRequest filter, int page, int size) {
        AuthUser user = getUsuarioAutenticado();
        Pageable pageable = PageRequest.of(page, size);

        Specification<Rascunho> spec = Specification.allOf(
                RascunhoSpecification.belongsToUser(user),
                RascunhoSpecification.hasMercado(filter.mercado()),
                RascunhoSpecification.hasTotalBetween(filter.minTotal(), filter.maxTotal()),
                RascunhoSpecification.hasCreatedDate(filter.date())
        );

        Page<Rascunho> rascunhosPage = rascunhoRepository.findAll(spec, pageable);
        return rascunhosPage.map(this::paraRascunhoResponse);
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
