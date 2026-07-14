package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IPagamentoView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Resultado simulado do pagamento (Figura 12): faixas de destaque no
 * topo, secoes Resumo do Pedido, Informacoes do Pagamento e Entrega,
 * tudo somente leitura; o unico comando e Fechar.
 */
public class PagamentoDialog extends JDialog implements IPagamentoView {

    private static final Color VERDE_ESCURO = new Color(0, 110, 0);
    private static final Color VERDE_CLARO = new Color(223, 240, 216);
    private static final Color VERMELHO_ESCURO = new Color(160, 0, 0);
    private static final Color VERMELHO_CLARO = new Color(242, 222, 222);

    private final JLabel faixaResultado = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel faixaSituacao = new JLabel(" ", SwingConstants.CENTER);

    private final JLabel valorCodigoPedido = new JLabel(" ");
    private final JLabel valorCliente = new JLabel(" ");
    private final JLabel valorEndereco = new JLabel(" ");
    private final JLabel valorTotalPedido = new JLabel(" ");

    private final JLabel valorSituacaoPagamento = new JLabel(" ");
    private final JLabel valorForma = new JLabel(" ");
    private final JLabel valorDataHora = new JLabel(" ");
    private final JLabel valorIdTransacao = new JLabel(" ");
    private final JLabel rotuloValorPago = new JLabel("Valor pago:");
    private final JLabel valorPago = new JLabel(" ");
    private final JPanel linhaValorPago = new JPanel(new BorderLayout());

    private final JLabel valorSituacaoPedido = new JLabel(" ");
    private final JLabel valorPrazo = new JLabel(" ");
    private final JLabel valorObservacao = new JLabel(" ");

    private final JButton botaoFechar = new JButton("Fechar");

    public PagamentoDialog() {
        setTitle("Pagamento");
        setModal(true);
        montarTela();
    }

    private void montarTela() {
        faixaResultado.setFont(faixaResultado.getFont().deriveFont(Font.BOLD, 18f));
        faixaResultado.setOpaque(true);
        faixaResultado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        faixaSituacao.setFont(faixaSituacao.getFont().deriveFont(Font.BOLD, 16f));
        faixaSituacao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JPanel painelFaixas = new JPanel(new GridBagLayout());
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(6, 12, 3, 12);
        f.gridx = 0;
        f.gridy = 0;
        f.fill = GridBagConstraints.HORIZONTAL;
        f.weightx = 1;
        painelFaixas.add(faixaResultado, f);
        f.gridy = 1;
        painelFaixas.add(faixaSituacao, f);

        // ---- Resumo do Pedido ----
        JPanel painelResumo = new JPanel(new GridBagLayout());
        painelResumo.setBorder(BorderFactory.createTitledBorder("Resumo do Pedido"));
        int linha = 0;
        adicionarLinha(painelResumo, linha++, "Pedido:", valorCodigoPedido);
        adicionarLinha(painelResumo, linha++, "Cliente:", valorCliente);
        adicionarLinha(painelResumo, linha++, "Endereço de entrega:", valorEndereco);
        valorTotalPedido.setFont(valorTotalPedido.getFont().deriveFont(Font.BOLD, 14f));
        adicionarLinha(painelResumo, linha++, "Total do pedido:", valorTotalPedido);

        // ---- Informacoes do Pagamento ----
        JPanel painelPagamento = new JPanel(new GridBagLayout());
        painelPagamento.setBorder(BorderFactory.createTitledBorder("Informações do Pagamento"));
        valorSituacaoPagamento.setFont(valorSituacaoPagamento.getFont().deriveFont(Font.BOLD));
        linha = 0;
        adicionarLinha(painelPagamento, linha++, "Situação do pagamento:", valorSituacaoPagamento);
        adicionarLinha(painelPagamento, linha++, "Forma de pagamento:", valorForma);
        adicionarLinha(painelPagamento, linha++, "Data e hora do pagamento:", valorDataHora);
        adicionarLinha(painelPagamento, linha++, "Identificador da transação:", valorIdTransacao);

        // o valor financeiro efetivado ganha uma linha destacada
        rotuloValorPago.setFont(rotuloValorPago.getFont().deriveFont(Font.BOLD));
        valorPago.setFont(valorPago.getFont().deriveFont(Font.BOLD, 14f));
        linhaValorPago.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        linhaValorPago.add(rotuloValorPago, BorderLayout.WEST);
        JPanel envelopeValor = new JPanel(new BorderLayout());
        envelopeValor.setOpaque(false);
        envelopeValor.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        envelopeValor.add(valorPago, BorderLayout.WEST);
        linhaValorPago.add(envelopeValor, BorderLayout.CENTER);
        GridBagConstraints v = new GridBagConstraints();
        v.gridx = 0;
        v.gridy = linha;
        v.gridwidth = 2;
        v.fill = GridBagConstraints.HORIZONTAL;
        v.weightx = 1;
        v.insets = new Insets(3, 6, 3, 6);
        painelPagamento.add(linhaValorPago, v);

        // ---- Entrega ----
        JPanel painelEntrega = new JPanel(new GridBagLayout());
        painelEntrega.setBorder(BorderFactory.createTitledBorder("Entrega"));
        valorSituacaoPedido.setFont(valorSituacaoPedido.getFont().deriveFont(Font.BOLD));
        valorPrazo.setFont(valorPrazo.getFont().deriveFont(Font.BOLD, 14f));
        valorObservacao.setFont(valorObservacao.getFont().deriveFont(Font.BOLD));
        linha = 0;
        adicionarLinha(painelEntrega, linha++, "Situação do pedido:", valorSituacaoPedido);
        adicionarLinha(painelEntrega, linha++, "Prazo estimado de entrega:", valorPrazo);
        adicionarLinha(painelEntrega, linha++, "Observação:", valorObservacao);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        centro.add(painelResumo);
        centro.add(painelPagamento);
        centro.add(painelEntrega);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        botoes.add(botaoFechar);

        setLayout(new BorderLayout(6, 6));
        add(painelFaixas, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(botaoFechar);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 560);
        setLocationRelativeTo(null);
    }

    private void adicionarLinha(JPanel painel, int linha, String rotulo, JLabel valor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.weightx = 0;
        painel.add(new JLabel(rotulo), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(3, 40, 3, 6);
        painel.add(valor, gbc);
    }

    @Override
    public void setResultado(String texto, boolean aprovado) {
        faixaResultado.setText(texto);
        faixaResultado.setForeground(aprovado ? VERDE_ESCURO : VERMELHO_ESCURO);
        faixaResultado.setBackground(aprovado ? VERDE_CLARO : VERMELHO_CLARO);
    }

    @Override
    public void setSituacaoOperacional(String texto, boolean destaque) {
        faixaSituacao.setText(texto);
        faixaSituacao.setForeground(destaque ? VERDE_ESCURO : VERMELHO_ESCURO);
    }

    @Override
    public void setCodigoPedido(String texto) {
        valorCodigoPedido.setText(texto);
    }

    @Override
    public void setNomeCliente(String texto) {
        valorCliente.setText(texto);
    }

    @Override
    public void setEnderecoEntrega(String texto) {
        valorEndereco.setText(texto);
    }

    @Override
    public void setTotalPedido(String texto) {
        valorTotalPedido.setText(texto);
    }

    @Override
    public void setSituacaoPagamento(String texto, boolean aprovado) {
        valorSituacaoPagamento.setText(texto);
        valorSituacaoPagamento.setForeground(aprovado ? VERDE_ESCURO : VERMELHO_ESCURO);
    }

    @Override
    public void setForma(String texto) {
        valorForma.setText(texto);
    }

    @Override
    public void setDataHoraPagamento(String texto) {
        valorDataHora.setText(texto);
    }

    @Override
    public void setIdTransacao(String texto) {
        valorIdTransacao.setText(texto);
    }

    @Override
    public void setValorPago(String texto) {
        valorPago.setText(texto);
        boolean temValor = !"-".equals(texto);
        valorPago.setForeground(temValor ? VERDE_ESCURO : Color.DARK_GRAY);
        linhaValorPago.setBackground(temValor ? VERDE_CLARO : getBackground());
    }

    @Override
    public void setSituacaoPedido(String texto, boolean destaque) {
        valorSituacaoPedido.setText(texto);
        valorSituacaoPedido.setForeground(destaque ? VERDE_ESCURO : Color.DARK_GRAY);
    }

    @Override
    public void setPrazoEntrega(String texto) {
        valorPrazo.setText(texto);
        valorPrazo.setForeground("-".equals(texto) ? Color.DARK_GRAY : VERDE_ESCURO);
    }

    @Override
    public void setObservacao(String texto) {
        valorObservacao.setText(texto);
    }

    @Override
    public void abrir() {
        setVisible(true);
    }

    @Override
    public void fechar() {
        dispose();
    }

    @Override
    public void setAoFechar(Runnable acao) {
        botaoFechar.addActionListener(e -> acao.run());
    }
}
