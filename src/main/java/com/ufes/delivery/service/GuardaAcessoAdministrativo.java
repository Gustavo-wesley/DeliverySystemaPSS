package com.ufes.delivery.service;

import com.ufes.delivery.excecao.ValidacaoException;
import java.util.Objects;

/**
 * Guard de perfil reutilizavel pelos Presenters de telas administrativas
 * (gestao de usuarios, cadastro de produto, movimentacao de estoque).
 */
public class GuardaAcessoAdministrativo {

    public static final String MSG_ACESSO_RESTRITO =
            "Funcionalidade restrita ao Administrador";

    private final SessaoService sessaoService;

    public GuardaAcessoAdministrativo(SessaoService sessaoService) {
        this.sessaoService = Objects.requireNonNull(sessaoService,
                "Serviço de sessão deve ser informado");
    }

    public boolean sessaoEhAdministrativa() {
        return sessaoService.haSessaoAtiva()
                && sessaoService.getUsuarioLogado().getPerfil().podeExecutarOperacoesAdministrativas();
    }

    public void exigirAdministrador() {
        if (!sessaoEhAdministrativa()) {
            throw new ValidacaoException(MSG_ACESSO_RESTRITO);
        }
    }
}
