package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoAluguelRepository extends JpaRepository<PedidoAluguel, Long> {

    List<PedidoAluguel> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    List<PedidoAluguel> findAllByOrderByCriadoEmDesc();

    List<PedidoAluguel> findByStatusOrderByCriadoEmDesc(StatusPedido status);

    boolean existsByClienteId(Long clienteId);

    boolean existsByAutomovelId(Long automovelId);
}
