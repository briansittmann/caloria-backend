package com.caloria.service;

import com.caloria.model.Receta;
import com.caloria.repository.CatalogoRecetasRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;


/**
 * Servicio que gestiona el catálogo de recetas disponibles para los usuarios.
 * Permite guardar recetas únicas, parsear respuestas desde IA y recuperar por ID.
 */
@Service
@RequiredArgsConstructor
public class CatalogoRecetasService {

    private final CatalogoRecetasRepository repo;
    private final ObjectMapper mapper;

    /**
     * Parsea una cadena JSON que contiene recetas bajo el nodo "recetas"
     * y las guarda en el catálogo si no estaban previamente.
     *
     * @param json Cadena JSON con el campo "recetas"
     * @return Lista de recetas persistidas (sin duplicados)
     * @throws JsonProcessingException Si el JSON es inválido
     */
    public List<Receta> parseYGuardar(String json) throws JsonProcessingException {
        JsonNode root = mapper.readTree(json).get("recetas");
        List<Receta> lista = new ArrayList<>();
        for (JsonNode node : root) {
            Receta r = mapper.treeToValue(node, Receta.class);
            // Usa tu saveIfNotExists para evitar duplicados:
            Receta guardada = saveIfNotExists(r);
            lista.add(guardada);
        }
        return lista;
    }
    
    
    /**
     * Inserta una receta en la base de datos sólo si no existe otra con el mismo título (ignora mayúsculas).
     *
     * @param receta Receta a insertar
     * @return Receta existente o nueva
     */
    public Receta saveIfNotExists(Receta receta) {
        return repo.findByTituloIgnoreCase(receta.getTitulo())
                   .orElseGet(() -> repo.save(receta));
    }
    
    /**
     * Recupera una lista de recetas a partir de sus IDs.
     *
     * @param ids Lista de IDs de recetas
     * @return Recetas correspondientes
     */
    public List<Receta> findAllByIds(List<String> ids) {
      return repo.findAllById(ids);
    }
    
}