package com.ufes.delivery.service.pagamento;

public class PixQrCode implements IFormaPagamento {

    @Override
    public String getNome() {
        return "PIX QR Code";
    }

    @Override
    public String toString() {
        return getNome();
    }
}
