package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.RegraNegocioException;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PedidoAluguelTest {

    private PedidoAluguel pedido;
    private Agente banco;

    @BeforeEach
    void setUp() {
        pedido = new PedidoAluguel();
        pedido.setDataInicio(LocalDate.of(2026, 12, 1));
        pedido.setDataFim(LocalDate.of(2026, 12, 10));
        banco = new Agente("banco", "x", "Banco", "00.000.000/0001-00", TipoAgente.BANCO);
    }

    @Test
    void novoPedidoComecaPendente() {
        assertEquals(StatusPedido.PENDENTE, pedido.getStatus());
        assertEquals(10, pedido.getQuantidadeDias());
    }

    @Test
    void periodoInvalidoLancaExcecao() {
        pedido.setDataFim(LocalDate.of(2026, 11, 30));
        assertThrows(RegraNegocioException.class, pedido::validarPeriodo);
    }

    @Test
    void parecerPositivoAprovaEPermiteContrato() {
        pedido.avaliar(banco, true, "Renda compatível");
        assertEquals(StatusPedido.APROVADO, pedido.getStatus());
        assertEquals("Renda compatível", pedido.getParecer());

        pedido.marcarComoContratado();
        assertEquals(StatusPedido.CONTRATADO, pedido.getStatus());
    }

    @Test
    void parecerNegativoReprovaENaoPermiteContrato() {
        pedido.avaliar(banco, false, "Renda insuficiente");
        assertEquals(StatusPedido.REPROVADO, pedido.getStatus());
        assertThrows(RegraNegocioException.class, pedido::marcarComoContratado);
    }

    @Test
    void naoPermiteAvaliarDuasVezes() {
        pedido.avaliar(banco, true, "ok");
        assertThrows(RegraNegocioException.class, () -> pedido.avaliar(banco, false, "de novo"));
    }

    @Test
    void clienteSoModificaPedidoPendente() {
        assertDoesNotThrow(pedido::garantirModificavelPeloCliente);
        pedido.avaliar(banco, true, "ok");
        assertThrows(RegraNegocioException.class, pedido::garantirModificavelPeloCliente);
        assertDoesNotThrow(pedido::garantirModificavelPeloAgente);
    }

    @Test
    void cancelamento() {
        pedido.cancelar();
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertThrows(RegraNegocioException.class, pedido::cancelar);
    }
}
