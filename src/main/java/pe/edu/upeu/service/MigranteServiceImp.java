package pe.edu.upeu.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pe.edu.upeu.model.Migrante;
import pe.edu.upeu.repository.MigranteRepository;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MigranteServiceImp implements MigranteServiceInter {

    @Inject
    MigranteRepository repository;

    @Override
    public void agregarMigrante(Migrante m) {
        repository.save(m);
    }

    @Override
    public List<Migrante> listarMigrantes() {
        List<Migrante> lista = new ArrayList<>();
        repository.findAll().forEach(lista::add);
        return lista;
    }

    @Override
    public void actualizarMigrante(Migrante m, int idReal) {
        m.setId(idReal);
        repository.update(m);
    }

    @Override
    public void eliminarMigrante(int idReal) {
        repository.deleteById(idReal);
    }

    @Override
    public Migrante buscarPorId(int idReal) {
        return repository.findById(idReal).orElse(null);
    }
}
