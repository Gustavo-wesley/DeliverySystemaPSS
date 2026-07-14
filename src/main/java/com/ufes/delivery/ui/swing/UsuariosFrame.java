package com.ufes.delivery.ui.swing;

import com.ufes.delivery.ui.view.IUsuariosView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Gestao de usuarios e autorizacoes (Figura 2). A tabela tem a coluna
 * "Sel." para marcar as linhas das operacoes em lote e a coluna
 * "Perfil" editavel por caixa de combinacao.
 */
public class UsuariosFrame extends JFrame implements IUsuariosView {

    private static final int COLUNA_SEL = 0;
    private static final int COLUNA_USERNAME = 1;
    private static final int COLUNA_NOME = 2;
    private static final int COLUNA_AUTORIZADO = 3;
    private static final int COLUNA_PERFIL = 4;
    private static final int COLUNA_SITUACAO = 5;

    private final JTextField campoNome = new JTextField(28);
    private final JButton botaoBuscar = new JButton("Buscar");
    private final JButton botaoAutorizar = new JButton("Autorizar");
    private final JButton botaoDesautorizar = new JButton("Desautorizar");
    private final JButton botaoExcluir = new JButton("Excluir");
    private final JButton botaoNovo = new JButton("Novo");
    private final JButton botaoFechar = new JButton("Fechar");

    private final DefaultTableModel modelo;
    private final JTable tabela;
    private final JComboBox<String> comboPerfil = new JComboBox<>();

    // ids na mesma ordem das linhas da tabela
    private final List<Long> idsDasLinhas = new ArrayList<>();
    // evita disparar o evento de perfil enquanto a tabela e recarregada
    private boolean carregando = false;

    private BiConsumer<Long, String> aoAlterarPerfil;

    public UsuariosFrame() {
        super("Usuários");

        modelo = new DefaultTableModel(
                new Object[]{"Sel.", "Nome de usuário", "Nome", "Autorizado", "Perfil", "Situação"}, 0) {
            @Override
            public Class<?> getColumnClass(int coluna) {
                if (coluna == COLUNA_SEL || coluna == COLUNA_AUTORIZADO) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int linha, int coluna) {
                // so a selecao e o perfil sao editaveis na tela
                return coluna == COLUNA_SEL || coluna == COLUNA_PERFIL;
            }
        };
        tabela = new JTable(modelo);
        montarTela();
    }

    private void montarTela() {
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Busca de Usuários"));
        painelBusca.add(new JLabel("Nome"));
        painelBusca.add(campoNome);
        painelBusca.add(botaoBuscar);

        tabela.getColumnModel().getColumn(COLUNA_SEL).setMaxWidth(45);
        tabela.getColumnModel().getColumn(COLUNA_AUTORIZADO).setMaxWidth(90);
        tabela.getColumnModel().getColumn(COLUNA_PERFIL)
                .setCellEditor(new DefaultCellEditor(comboPerfil));

        modelo.addTableModelListener(e -> {
            if (carregando || e.getColumn() != COLUNA_PERFIL || e.getFirstRow() < 0) {
                return;
            }
            if (aoAlterarPerfil != null) {
                Long id = idsDasLinhas.get(e.getFirstRow());
                String perfil = (String) modelo.getValueAt(e.getFirstRow(), COLUNA_PERFIL);
                aoAlterarPerfil.accept(id, perfil);
            }
        });

        JPanel painelUsuarios = new JPanel(new BorderLayout());
        painelUsuarios.setBorder(BorderFactory.createTitledBorder("Usuários"));
        painelUsuarios.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        painelBotoes.add(botaoAutorizar);
        painelBotoes.add(botaoDesautorizar);
        painelBotoes.add(botaoExcluir);
        painelBotoes.add(botaoNovo);
        painelBotoes.add(botaoFechar);

        setLayout(new BorderLayout(8, 8));
        add(painelBusca, BorderLayout.NORTH);
        add(painelUsuarios, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680, 420);
        setLocationRelativeTo(null);
    }

    @Override
    public String getNomeBusca() {
        return campoNome.getText().trim();
    }

    @Override
    public void mostrarUsuarios(List<LinhaUsuario> linhas) {
        carregando = true;
        try {
            // interrompe uma edicao pendente antes de trocar as linhas
            if (tabela.isEditing()) {
                tabela.getCellEditor().cancelCellEditing();
            }
            modelo.setRowCount(0);
            idsDasLinhas.clear();
            for (LinhaUsuario linha : linhas) {
                idsDasLinhas.add(linha.id());
                modelo.addRow(new Object[]{
                    Boolean.FALSE,
                    linha.username(),
                    linha.nome(),
                    linha.autorizado(),
                    linha.perfil(),
                    linha.situacao()
                });
            }
        } finally {
            carregando = false;
        }
    }

    @Override
    public List<Long> getIdsSelecionados() {
        List<Long> selecionados = new ArrayList<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (Boolean.TRUE.equals(modelo.getValueAt(i, COLUNA_SEL))) {
                selecionados.add(idsDasLinhas.get(i));
            }
        }
        return selecionados;
    }

    @Override
    public void setPerfisDisponiveis(List<String> perfis) {
        comboPerfil.removeAllItems();
        for (String perfil : perfis) {
            comboPerfil.addItem(perfil);
        }
    }

    @Override
    public boolean confirmarExclusao(int quantidade) {
        int resposta = JOptionPane.showConfirmDialog(this,
                "Confirma a exclusão de " + quantidade + " usuário(s)?",
                "Usuários", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return resposta == JOptionPane.YES_OPTION;
    }

    @Override
    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Usuários", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void mostrarInformacao(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Usuários",
                JOptionPane.INFORMATION_MESSAGE);
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
    public void setAoBuscar(Runnable acao) {
        botaoBuscar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoAutorizar(Runnable acao) {
        botaoAutorizar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoDesautorizar(Runnable acao) {
        botaoDesautorizar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoExcluir(Runnable acao) {
        botaoExcluir.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoNovo(Runnable acao) {
        botaoNovo.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoFechar(Runnable acao) {
        botaoFechar.addActionListener(e -> acao.run());
    }

    @Override
    public void setAoAlterarPerfil(BiConsumer<Long, String> acao) {
        this.aoAlterarPerfil = acao;
    }
}
