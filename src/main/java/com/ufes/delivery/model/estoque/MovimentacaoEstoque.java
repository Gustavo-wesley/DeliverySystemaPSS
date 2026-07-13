package com.ufes.delivery.model.estoque;

import com.ufes.delivery.model.Produto;
import com.ufes.delivery.model.Usuario;
import java.time.LocalDate;

/**
 * Registro de movimentacao de estoque confirmada (US08).
 */
public class MovimentacaoEstoque {

    private Long id;
    private final Produto produto;
    private final ITipoMovimentacao tipo;
    private final int quantidade;
    private final LocalDate data;
    private final String motivo;
    private final String notaFiscal;
    private final Usuario responsavel;

    public MovimentacaoEstoque(Long id, Produto produto, ITipoMovimentacao tipo,
            int quantidade, LocalDate data, String motivo, String notaFiscal,
            Usuario responsavel) {

        if (produto == null) {
            throw new IllegalArgumentException("Produto deve ser informado");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de movimentação deve ser informado");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data da movimentação deve ser informada");
        }
        if (responsavel == null) {
            throw new IllegalArgumentException("Responsável pela movimentação deve ser informado");
        }

        tipo.validar(quantidade, motivo, notaFiscal);

        this.id = id;
        this.produto = produto;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.data = data;
        this.motivo = motivo == null ? "" : motivo.trim();
        this.notaFiscal = notaFiscal == null ? "" : notaFiscal.trim();
        this.responsavel = responsavel;
    }

    public int variacaoDeEstoque() {
        return tipo.variacaoDeEstoque(quantidade);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public ITipoMovimentacao getTipo() {
        return tipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public LocalDate getData() {
        return data;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getNotaFiscal() {
        return notaFiscal;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }
}
