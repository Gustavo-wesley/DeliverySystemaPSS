package com.ufes.delivery.ui.view;

import java.util.List;

/**
 * View passiva do cadastro de cliente (US06, Figura 5): dados do cliente
 * e ate tres enderecos de entrega, com um padrao.
 */
public interface IClienteView {

    /**
     * Linha da tabela de enderecos, exatamente como digitada na tela.
     */
    record LinhaEndereco(
            boolean padrao,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            String cep) {

        /**
         * Uma linha em branco nao vira endereco.
         */
        public boolean estaVazia() {
            return vazio(logradouro) && vazio(numero) && vazio(complemento)
                    && vazio(bairro) && vazio(cidade) && vazio(uf) && vazio(cep);
        }

        private static boolean vazio(String valor) {
            return valor == null || valor.isBlank();
        }
    }

    String getNome();

    String getCpf();

    List<LinhaEndereco> getEnderecos();

    void setNome(String nome);

    void setCpf(String cpf);

    void setEnderecos(List<LinhaEndereco> linhas);

    void mostrarErro(String mensagem);

    void mostrarInformacao(String mensagem);

    void abrir();

    void fechar();

    void setAoSalvar(Runnable acao);

    void setAoCancelar(Runnable acao);
}
