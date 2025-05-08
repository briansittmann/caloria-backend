package com.caloria.repository;

import com.caloria.model.Receta;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CatalogoRecetasRepository extends MongoRepository<Receta, String> {
  Optional<Receta> findByTituloIgnoreCase(String titulo);
}