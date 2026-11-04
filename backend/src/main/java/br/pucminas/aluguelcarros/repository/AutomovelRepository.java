package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Automovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    List<Automovel> findAllByOrderByMarcaAscModeloAsc();

    List<Automovel> findByDisponivelTrueOrderByMarcaAscModeloAsc();

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByMatriculaIgnoreCase(String matricula);
}
