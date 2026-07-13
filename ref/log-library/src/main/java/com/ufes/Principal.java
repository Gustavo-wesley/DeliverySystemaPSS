package com.ufes;

import com.ufes.logger.ILogger;
import com.ufes.logger.LoggerFactory;
import com.ufes.logger.TipoLogger;
import com.ufes.service.LogService;

public class Principal {

    public static void main(String[] args) {

        try {

            ILogger logger =
                    LoggerFactory.criarLogger(
                            TipoLogger.JSONL,
                            "audit.jsonl");

            LogService logService =
                    new LogService(logger);

            logService.registrarOperacao(
                    "PED001",
                    "Calculo do valor total do pedido",
                    "Maria",
                    "Operacao realizada com sucesso"
            );

            logService.registrarOperacao(
                    "PED002",
                    "Aplicacao de cupom",
                    "Joao",
                    "Cupom aplicado com sucesso"
            );

            try {

                throw new RuntimeException(
                        "Erro de teste");

            } catch (Exception ex) {

                logService.registrarErro(
                        "PED003",
                        "Aplicacao de cupom",
                        "Carlos",
                        ex
                );
            }

            System.out.println(
                    "Logs registrados com sucesso.");

        } catch (Exception ex) {

            System.err.println(
                    "Erro ao executar sistema: "
                            + ex.getMessage());
        }
    }
}