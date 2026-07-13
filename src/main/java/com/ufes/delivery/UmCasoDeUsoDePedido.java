package com.ufes.delivery;

import com.ufes.delivery.desconto.pedido.AplicadorCupomPedidoService;
import com.ufes.delivery.desconto.taxa.entrega.CalculadoraTaxaDescontoPedidoService;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Endereco;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.repository.CupomRepositoryEmMemoria;
import java.time.LocalDateTime;

public class UmCasoDeUsoDePedido {

    public static void main(String[] args) {
        Cliente cliente = new Cliente(null, "Maria", "111.111.111-11", "Ouro", 1);
        cliente.adicionarEndereco(new Endereco(null, "Limoeiro", "100", "",
                "Cidade Maravilhosa", "Castelo", "ES", "29000-000", true));

        LocalDateTime dataPedido = LocalDateTime.now();
        Pedido pedido = new Pedido(dataPedido, cliente);

        Item item1 = new Item("Caderno", 2, 10.50, "Educacao");
        Item item2 = new Item("Borracha", 5, 4.25, "Educacao");
        Item item3 = new Item("Biscoito", 4, 5.80, "Alimentacao");
        Item item4 = new Item("Pao", 2, 1.50, "Alimentacao");
        Item item5 = new Item("Livro", 2, 40.20, "Lazer");
        Item item6 = new Item("Jogo", 1, 45.90, "Lazer");

        pedido.adicionarItem(item1);
        pedido.adicionarItem(item2);
        pedido.adicionarItem(item3);
        pedido.adicionarItem(item4);
        pedido.adicionarItem(item5);
        pedido.adicionarItem(item6);

        CalculadoraTaxaDescontoPedidoService calculadoraDeDesconto = new CalculadoraTaxaDescontoPedidoService();
        calculadoraDeDesconto.calcularDesconto(pedido);

        CupomRepositoryEmMemoria cupomRepository = new CupomRepositoryEmMemoria();
        cupomRepository.adicionarCupom(
                new CupomDescontoPedido("VALIDOHOJE", 15.0, dataPedido.minusDays(1), dataPedido.plusDays(1)));

        AplicadorCupomPedidoService aplicadorCupomService = new AplicadorCupomPedidoService(cupomRepository);

        LocalDateTime dataHoraAplicacaoCupom = LocalDateTime.now();

        aplicadorCupomService.aplicarCupom(pedido, "VALIDOHOJE", dataHoraAplicacaoCupom);

        try {
            aplicadorCupomService.aplicarCupom(pedido, "CUPOMINEXISTENTE", dataHoraAplicacaoCupom);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
        }

        pedido.calcularValorTotal();

        System.out.println(pedido);

    }
}
