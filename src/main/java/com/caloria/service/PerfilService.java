// src/main/java/com/caloria/service/PerfilService.java
package com.caloria.service;

import com.caloria.model.Macros;
import com.caloria.model.NivelActividad;
import com.caloria.model.ObjetivoNutricional;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepo;
    private final MetabolismoService meta;

    public PerfilService(UsuarioRepository usuarioRepo,
                         MetabolismoService meta) {
        this.usuarioRepo = usuarioRepo;
        this.meta        = meta;
    }

    /**
     * Paso final: calcula y guarda calorías y macros meta.
     * El redondeo se aplica dentro de Usuario.aplicarMetas().
     */
    public Usuario completarObjetivo(String usuarioId,
                                     String nivelActStr,
                                     String objetivoStr) {

        // 1) Recuperar usuario
        Usuario u = usuarioRepo.findById(usuarioId)
            .orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, "Usuario no encontrado"));

        // 2) Calcular BMR (Mifflin–St Jeor)
        double bmr = meta.calcularBmr(
            u.getPesoKg(), u.getAlturaCm(), u.getEdad(), u.getSexo());

        // 3) Calcular TDEE = BMR × PAL
        NivelActividad nivel = NivelActividad.valueOf(nivelActStr.toUpperCase());
        double tdee = meta.calcularTdee(bmr, nivel);

        // 4) Ajustar TDEE según objetivo (cut/bulk)
        ObjetivoNutricional obj = ObjetivoNutricional.valueOf(objetivoStr.toUpperCase());
        double calObj = meta.calcularCaloriasObjetivo(tdee, obj);

        // 5) Generar distribución de macros (sin redondear aún)
        Macros macros = meta.calcularMacrosObjetivo(calObj);

        // 6) Aplicar y redondear metas en el modelo Usuario
        u.setObjetivo(objetivoStr);
        u.aplicarMetas(calObj, macros);

        // 7) Persistir cambios
        return usuarioRepo.save(u);
    }
}