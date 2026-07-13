package com.ufes.delivery.repository.sqlite;

import java.sql.Connection;

/**
 * Operacao executada dentro de uma transacao unica gerenciada
 * pelo {@link DatabaseManager}.
 */
@FunctionalInterface
public interface OperacaoTransacional<T> {

    T executar(Connection conexao) throws Exception;
}
