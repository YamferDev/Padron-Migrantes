package pe.edu.upeu.repository;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import pe.edu.upeu.model.Migrante;

import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public interface MigranteRepository extends CrudRepository<Migrante, Integer> {
    List<Migrante> findByNombreCompletoContainsIgnoreCase(String nombreCompleto);
}