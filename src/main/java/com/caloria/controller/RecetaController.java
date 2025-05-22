package com.caloria.controller;

import com.caloria.model.Receta;
import com.caloria.model.Usuario;
import com.caloria.dto.MacrosDTO;
import com.caloria.dto.ResumenDiaDTO;
import com.caloria.service.CatalogoRecetasService;
import com.caloria.service.DiaService;
import com.caloria.service.IAService;
import com.caloria.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Controlador REST encargado de la generación, guardado y recuperación
 * de recetas personalizadas dentro del perfil del usuario.
 *
 * Se comunica con IAService para obtener recetas nutricionalmente ajustadas,
 * y con UsuarioService y CatalogoRecetasService para almacenarlas o eliminarlas.
 *
 * Prefijo base: `/recetas`
 */
@RestController
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final IAService iaService;
    private final DiaService diaService;
    private final UsuarioService usuarioService;
    private final CatalogoRecetasService catalogoService;
    private final ObjectMapper objectMapper;  // bean de Jackson
    
    
    /**
     * Genera recetas personalizadas a través de un asistente de IA,
     * utilizando las preferencias del usuario y sus macronutrientes restantes.
     *
     * El JSON devuelto por la IA se parsea a objetos Receta,
     * que luego se guardan en el catálogo (si no existen).
     *
     * @param numComidas Número de recetas deseadas (1–4)
     * @param auth Token JWT que contiene el ID del usuario
     * @return Lista de recetas generadas y guardadas
     * @throws Exception si falla el parseo del JSON o la llamada a IA
     */
    @PostMapping("/generar")
    public ResponseEntity<List<Receta>> generarYGuardarRecetas(
            @RequestParam int numComidas,
            Authentication auth) throws Exception {

        String uid = auth.getName();

        // 1) Obtener resumen
        ResumenDiaDTO resumen = diaService.getResumenDelDia(uid);
        MacrosDTO macrosDto = new MacrosDTO(
            resumen.getMacrosRestantes().getProteinasG(),
            resumen.getMacrosRestantes().getCarbohidratosG(),
            resumen.getMacrosRestantes().getGrasasG(),
            resumen.getCaloriasRestantes()
        );

        // 2) Preferencias y alergias
        Usuario usr = usuarioService.obtenerPerfil(uid);
        List<String> prefs = usr.getPreferencias();
        List<String> alerg = usr.getAlergias();

        // 3) Llamar a la IA y obtener el JSON crudo
        String jsonRecetas = iaService.generarRecetas(prefs, alerg, macrosDto, numComidas);

        // 4) Parsear ese JSON a List<Receta>
        //    suponiendo que el JSON tiene la forma { "recetas":[ {...}, {...} ] }
        RecetasWrapper wrapper = objectMapper
            .readValue(jsonRecetas, RecetasWrapper.class);
        List<Receta> recetasIA = wrapper.getRecetas();

        // 5) Guardar cada receta en el catálogo (si no existía)
        List<Receta> guardadas = recetasIA.stream()
            .map(catalogoService::saveIfNotExists)
            .collect(Collectors.toList());
        
        System.out.println("/// RECETAS PARA FRONT (usuario=" + uid + "):");
        guardadas.forEach(r -> System.out.println("  * " + r));
        
        return ResponseEntity.ok(guardadas);
    }

    /**
     * DTO auxiliar usado para parsear la estructura de respuesta
     * de la IA (JSON con clave "recetas").
     */
    public static class RecetasWrapper {
        private List<Receta> recetas;
        public List<Receta> getRecetas() { return recetas; }
        public void setRecetas(List<Receta> recetas) { this.recetas = recetas; }
    }
    
    
    /**
     * Asocia una o más recetas a la lista personal del usuario.
     * Se utiliza principalmente para guardar recetas generadas previamente por IA.
     *
     * @param recetasIA Lista de recetas completas
     * @param auth Token JWT con el ID del usuario
     * @return Lista de recetas efectivamente guardadas
     */
    @PostMapping("/guardar")
    public ResponseEntity<List<Receta>> guardarRecetasIA(
        @RequestBody List<Receta> recetasIA,
        Authentication auth) {

      // Aquí tomas el UID directamente del token validado
      String uid = auth.getName();

      List<Receta> asociadas = usuarioService.guardarRecetasUsuario(uid, recetasIA);
      
      System.out.println("/// RECETAS GUARDADAS POR usuario=" + uid + ": " +
              asociadas.stream().map(Receta::getId).collect(Collectors.toList()));
      
      return ResponseEntity.ok(asociadas);
    }
    
    /**
     * Devuelve todas las recetas que el usuario ha guardado en su perfil.
     *
     * @param auth Token JWT del usuario
     * @return Lista de recetas asociadas al usuario
     */
    @GetMapping("/mis")
    public ResponseEntity<List<Receta>> misRecetas(Authentication auth) {
        String uid = auth.getName();
        List<Receta> recetas = usuarioService.obtenerRecetasUsuario(uid);
        return ResponseEntity.ok(recetas);
    }
    
    /**
     * Elimina una receta previamente guardada en el perfil del usuario.
     *
     * @param recetaId ID de la receta a eliminar
     * @param auth Token JWT con el ID del usuario
     * @return HTTP 204 si la eliminación fue exitosa
     */
    @DeleteMapping("/{recetaId}")
    public ResponseEntity<Void> eliminarReceta(
        @PathVariable String recetaId,
        Authentication auth) {
      String uid = auth.getName();
      usuarioService.eliminarRecetaUsuario(uid, recetaId);
      return ResponseEntity.noContent().build();
    }
}
