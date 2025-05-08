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

@Service
@RequiredArgsConstructor
public class CatalogoRecetasService {

    private final CatalogoRecetasRepository repo;
    private final ObjectMapper mapper;

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

    public Receta saveIfNotExists(Receta receta) {
        return repo.findByTituloIgnoreCase(receta.getTitulo())
                   .orElseGet(() -> repo.save(receta));
    }
}