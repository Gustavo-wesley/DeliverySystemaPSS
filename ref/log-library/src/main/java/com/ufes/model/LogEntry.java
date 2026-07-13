package com.ufes.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class LogEntry {

    private final String nomeUsuario;
    private final LocalDate data;
    private final LocalTime hora;
    private final String codigoPedido;
    private final String nomeOperacao;
    private final String nomeCliente;
    private final String mensagem;

    public LogEntry(
            String nomeUsuario,
            LocalDate data,
            LocalTime hora,
            String codigoPedido,
            String nomeOperacao,
            String nomeCliente,
            String mensagem) {

        if (nomeUsuario == null || nomeUsuario.isBlank()) {
            throw new IllegalArgumentException("Nome do usuario deve ser informado");
        }

        if (data == null) {
            throw new IllegalArgumentException("Data deve ser informada");
        }

        if (hora == null) {
            throw new IllegalArgumentException("Hora deve ser informada");
        }

        if (codigoPedido == null || codigoPedido.isBlank()) {
            throw new IllegalArgumentException("Codigo do pedido deve ser informado");
        }

        if (nomeOperacao == null || nomeOperacao.isBlank()) {
            throw new IllegalArgumentException("Nome da operacao deve ser informado");
        }

        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente deve ser informado");
        }

        this.nomeUsuario = nomeUsuario;
        this.data = data;
        this.hora = hora;
        this.codigoPedido = codigoPedido;
        this.nomeOperacao = nomeOperacao;
        this.nomeCliente = nomeCliente;
        this.mensagem = mensagem;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }

    public String getNomeOperacao() {
        return nomeOperacao;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getMensagem() {
        return mensagem;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "nomeUsuario='" + nomeUsuario + '\'' +
                ", data=" + data +
                ", hora=" + hora +
                ", codigoPedido='" + codigoPedido + '\'' +
                ", nomeOperacao='" + nomeOperacao + '\'' +
                ", nomeCliente='" + nomeCliente + '\'' +
                ", mensagem='" + mensagem + '\'' +
                '}';
    }
}