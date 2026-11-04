package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.enums.TipoAgente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgenteRepository extends JpaRepository<Agente, Long> {

    boolean existsByCnpj(String cnpj);

    List<Agente> findByTipoOrderByNomeAsc(TipoAgente tipo);

    List<Agente> findAllByOrderByNomeAsc();
}
