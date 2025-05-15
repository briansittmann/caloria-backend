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

@RestController
@RequestMapping("/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final IAService iaService;
    private final DiaService diaService;
    private final UsuarioService usuarioService;
    private final CatalogoRecetasService catalogoService;
    private final ObjectMapper objectMapper;  // bean de Jackson

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

    /** Simple DTO auxiliar para poder parsear el array desde el JSON de la IA */
    public static class RecetasWrapper {
        private List<Receta> recetas;
        public List<Receta> getRecetas() { return recetas; }
        public void setRecetas(List<Receta> recetas) { this.recetas = recetas; }
    }
    
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
     * Devuelve todas las recetas guardadas en el perfil del usuario.
     */
    @GetMapping("/mis")
    public ResponseEntity<List<Receta>> misRecetas(Authentication auth) {
        String uid = auth.getName();
        List<Receta> recetas = usuarioService.obtenerRecetasUsuario(uid);
        return ResponseEntity.ok(recetas);
    }
    
    /** Elimina una receta del array del usuario */
    @DeleteMapping("/{recetaId}")
    public ResponseEntity<Void> eliminarReceta(
        @PathVariable String recetaId,
        Authentication auth) {
      String uid = auth.getName();
      usuarioService.eliminarRecetaUsuario(uid, recetaId);
      return ResponseEntity.noContent().build();
    }
}
