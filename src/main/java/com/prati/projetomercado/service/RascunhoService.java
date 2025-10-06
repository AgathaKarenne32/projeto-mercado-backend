package com.prati.projetomercado.service;

import com.prati.projetomercado.dto.request.CreateRascunhoRequest;
import com.prati.projetomercado.dto.request.UpdateRascunhoRequest;
import com.prati.projetomercado.dto.response.RascunhoResponse;

import java.util.List;

public interface RascunhoService {

    /**
     * Cria um novo rascunho para o usuário autenticado.
     * @param createRascunhoRequest DTO com os dados para a criação.
     * @return O rascunho recém-criado, formatado como um DTO de resposta.
     */
    RascunhoResponse criarRascunho(CreateRascunhoRequest createRascunhoRequest);

    /**
     * Busca todos os rascunhos pertencentes ao usuário autenticado.
     * @return Uma lista de rascunhos, formatada como DTOs de resposta.
     */
    List<RascunhoResponse> buscarRascunhosDoUsuario();

    /**
     * Busca um rascunho específico pelo seu ID.
     * @param rascunhoId O ID do rascunho a ser buscado.
     * @return O rascunho encontrado, formatado como DTO de resposta.
     */
    RascunhoResponse buscarRascunhoPorId(Long rascunhoId);

    /**
     * Atualiza um rascunho existente.
     * @param rascunhoId O ID do rascunho a ser atualizado.
     * @param updateRascunhoRequest DTO com os novos dados.
     * @return O rascunho atualizado, formatado como DTO de resposta.
     */
    RascunhoResponse atualizarRascunho(Long rascunhoId, UpdateRascunhoRequest updateRascunhoRequest);

    /**
     * Apaga um rascunho pelo seu ID.
     * @param rascunhoId O ID do rascunho a ser apagado.
     */
    void apagarRascunho(Long rascunhoId);
}