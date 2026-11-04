package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteEContratoTest {

    @Test
    void clienteAceitaAteTresRendimentos() {
        Cliente c = new Cliente("joao", "x", "MG1", "111.111.111-11", "João", "Rua A", "Dev");
        c.definirRendimentos(List.of(
                new Rendimento("A", new BigDecimal("1000")),
                new Rendimento("B", new BigDecimal("2000")),
                new Rendimento("C", new BigDecimal("500"))));
        assertEquals(3, c.getRendimentos().size());
        assertEquals(0, new BigDecimal("3500").compareTo(c.getRendaTotal()));
        c.getRendimentos().forEach(r -> assertSame(c, r.getCliente()));
    }

    @Test
    void clienteRejeitaQuatroRendimentos() {
        Cliente c = new Cliente("joao", "x", "MG1", "111.111.111-11", "João", "Rua A", "Dev");
        List<Rendimento> quatro = List.of(
                new Rendimento("A", BigDecimal.ONE), new Rendimento("B", BigDecimal.ONE),
                new Rendimento("C", BigDecimal.ONE), new Rendimento("D", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> c.definirRendimentos(quatro));
    }

    @Test
    void parcelaPelaTabelaPrice() {
        Agente banco = new Agente("b", "x", "Banco", "1", TipoAgente.BANCO);
        ContratoCredito credito = new ContratoCredito(banco, new BigDecimal("10000.00"), new BigDecimal("1.00"), 12);
        // PMT(1%, 12, 10000) = 888.49
        assertEquals(new BigDecimal("888.49"), credito.getValorParcela());
    }

    @Test
    void parcelaSemJuros() {
        Agente banco = new Agente("b", "x", "Banco", "1", TipoAgente.BANCO);
        ContratoCredito credito = new ContratoCredito(banco, new BigDecimal("1200.00"), BigDecimal.ZERO, 12);
        assertEquals(new BigDecimal("100.00"), credito.getValorParcela());
    }
}
