package com.caloria.service;

import com.caloria.model.CatalogoAlimento;
import com.caloria.model.Alimento;
import com.caloria.repository.CatalogoAlimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogoAlimentoService {

    private final CatalogoAlimentoRepository repository;

    /**
     * Busca un alimento normalizado por nombre (ignora mayúsculas/minúsculas).
     */
    public Optional<CatalogoAlimento> obtenerPorNombre(String nombre) {
        return repository.findByNombreIgnoreCase(nombre);
    }

    /**
     * Guarda en el catálogo un nuevo alimento normalizado (por 100 g),
     * si no existía ya uno con ese nombre.
     */
    public CatalogoAlimento guardarSiNoExiste(Alimento a) {
        return repository.findByNombreIgnoreCase(a.getNombre())
                .orElseGet(() -> {
                    CatalogoAlimento nuevo = CatalogoAlimento.from(a);
                    return repository.save(nuevo);
                });
    }
    
    /**
     * Guarda un objeto CatalogoAlimento si no existía ya uno con ese nombre.
     */
    public CatalogoAlimento guardarSiNoExisteCatalogo(CatalogoAlimento ca) {
        return repository.findByNombreIgnoreCase(ca.getNombre())
                         .orElseGet(() -> repository.save(ca));
    }
    
    /**
     * Lista todos los alimentos del catálogo.
     */
    public List<CatalogoAlimento> listarTodos() {
        return repository.findAll();
    }

    /**
     * Fuerza la actualización de un alimento en el catálogo.
     */
    public CatalogoAlimento actualizar(CatalogoAlimento ca) {
        return repository.save(ca);
    }
}