package com.caloria.service;

import com.caloria.model.CatalogoAlimento;
import com.caloria.model.Alimento;
import com.caloria.repository.CatalogoAlimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * Servicio responsable de acceder y mantener el catálogo de alimentos normalizados.
 * Estos alimentos representan valores nutricionales por cada 100 gramos.
 *
 * Permite búsquedas por nombre, inserciones seguras (sin duplicados)
 * y actualizaciones explícitas.
 */
@Service
@RequiredArgsConstructor
public class CatalogoAlimentoService {

    private final CatalogoAlimentoRepository repository;

    /**
     * Busca un alimento en el catálogo por nombre (sin distinción de mayúsculas).
     *
     * @param nombre Nombre del alimento a buscar
     * @return Optional con el alimento encontrado, o vacío si no existe
     */
    public Optional<CatalogoAlimento> obtenerPorNombre(String nombre) {
        return repository.findByNombreIgnoreCase(nombre);
    }


    /**
     * Guarda en el catálogo un nuevo alimento (basado en un {@link Alimento}),
     * sólo si aún no existe uno con ese nombre.
     *
     * @param a Objeto Alimento desde el cual se construye el catálogo
     * @return CatalogoAlimento guardado o existente
     */
    public CatalogoAlimento guardarSiNoExiste(Alimento a) {
        return repository.findByNombreIgnoreCase(a.getNombre())
                .orElseGet(() -> {
                    CatalogoAlimento nuevo = CatalogoAlimento.from(a);
                    return repository.save(nuevo);
                });
    }
    
    
    /**
     * Guarda un alimento del catálogo sólo si no hay uno con el mismo nombre.
     *
     * @param ca Objeto CatalogoAlimento
     * @return El objeto existente o el nuevo guardado
     */
    public CatalogoAlimento guardarSiNoExisteCatalogo(CatalogoAlimento ca) {
        return repository.findByNombreIgnoreCase(ca.getNombre())
                         .orElseGet(() -> repository.save(ca));
    }
    

    /**
     * Devuelve la lista completa de alimentos del catálogo.
     *
     * @return Lista de CatalogoAlimento
     */
    public List<CatalogoAlimento> listarTodos() {
        return repository.findAll();
    }

    /**
     * Fuerza la actualización (o inserción) de un alimento en el catálogo.
     *
     * @param ca Alimento actualizado
     * @return Alimento persistido
     */
    public CatalogoAlimento actualizar(CatalogoAlimento ca) {
        return repository.save(ca);
    }
}