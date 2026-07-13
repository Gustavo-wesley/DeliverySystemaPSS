package com.ufes.delivery.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Preenche os parametros de um PreparedStatement.
 */
@FunctionalInterface
interface PreparadorSql {

    void preparar(PreparedStatement ps) throws SQLException;
}
