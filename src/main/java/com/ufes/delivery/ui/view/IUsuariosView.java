package com.ufes.delivery.ui.view;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * View passiva da gestao de usuarios e autorizacoes (US03, Figura 2).
 * O Presenter entrega as linhas prontas e recebe os ids selecionados.
 */
public interface IUsuariosView {

    /**
     * Linha da tabela de usuarios, ja formatada para exibicao.
     */
    record LinhaUsuario(
            Long id,
            String username,
            String nome,
            boolean autorizado,
            String perfil,
            String situacao) {
    }

    String getNomeBusca();

    void mostrarUsuarios(List<LinhaUsuario> linhas);

    List<Long> getIdsSelecionados();

    void setPerfisDisponiveis(List<String> perfis);

    boolean confirmarExclusao(int quantidade);

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoBuscar(Runnable acao);

    void setAoAutorizar(Runnable acao);

    void setAoDesautorizar(Runnable acao);

    void setAoExcluir(Runnable acao);

    void setAoNovo(Runnable acao);

    void setAoFechar(Runnable acao);

    /**
     * Disparado quando o perfil de uma linha e trocado na caixa de
     * combinacao: (id do usuario, nome do novo perfil).
     */
    void setAoAlterarPerfil(BiConsumer<Long, String> acao);
}
