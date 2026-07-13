package com.ufes.delivery.service;

import com.ufes.delivery.model.Usuario;
import java.time.LocalDateTime;

/**
 * Guarda o usuario autenticado da execucao (US01) e substitui,
 * do lado do Delivery, o mock UsuarioLogadoService da lib de log.
 * Criado uma unica vez na composicao da aplicacao e injetado
 * por construtor onde for necessario.
 */
public class SessaoService {

    private Usuario usuarioLogado;
    private LocalDateTime dataHoraLogin;

    public void iniciarSessao(Usuario usuario, LocalDateTime dataHoraLogin) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário da sessão deve ser informado");
        }
        if (dataHoraLogin == null) {
            throw new IllegalArgumentException("Data e hora do login devem ser informadas");
        }
        this.usuarioLogado = usuario;
        this.dataHoraLogin = dataHoraLogin;
    }

    public void encerrarSessao() {
        this.usuarioLogado = null;
        this.dataHoraLogin = null;
    }

    public boolean haSessaoAtiva() {
        return usuarioLogado != null;
    }

    public Usuario getUsuarioLogado() {
        if (usuarioLogado == null) {
            throw new IllegalStateException("Não há sessão ativa");
        }
        return usuarioLogado;
    }

    public LocalDateTime getDataHoraLogin() {
        if (dataHoraLogin == null) {
            throw new IllegalStateException("Não há sessão ativa");
        }
        return dataHoraLogin;
    }

    /**
     * Nome do usuario para os registros de auditoria.
     */
    public String getNomeUsuario() {
        return getUsuarioLogado().getUsername();
    }
}
