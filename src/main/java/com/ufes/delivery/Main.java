package com.ufes.delivery;

import com.ufes.delivery.ui.AplicacaoDelivery;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do POC Delivery.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // segue com o look and feel padrao mesmo
            }

            try {
                new AplicacaoDelivery().iniciar();
            } catch (Exception e) {
                // sem banco ou sem auditoria a aplicacao nao pode operar
                JOptionPane.showMessageDialog(null,
                        "Falha ao iniciar a aplicação: " + e.getMessage(),
                        "Delivery", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
