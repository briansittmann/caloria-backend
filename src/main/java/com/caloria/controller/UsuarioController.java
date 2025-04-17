package com.caloria.controller;

import com.caloria.model.Macros;
import com.caloria.model.Usuario;
import com.caloria.repository.UsuarioRepository;
import com.caloria.dto.UsuarioRequestDTO;
import com.caloria.service.UsuarioService;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
	
    @Autowired
    private UsuarioService usuarioService;  // Inyección automática

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/crear")
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crearUsuario(@RequestBody @Valid UsuarioRequestDTO usuarioDTO) {
        return usuarioService.crearUsuario(usuarioDTO);
    }
    
    @PostMapping("/test")
    public Usuario guardarUsuarioTest() {
        Usuario u = new Usuario();
        u.setNombre("Brian");
        u.setEmail("brian@gmail.com");
        u.setEdad(27);
        u.setSexo("masculino");
        u.setAlturaCm(169);
        u.setPesoKg(64);
        u.setNivelActividad("activa");
        u.setObjetivo("bajar_grasa");
        u.setHoraInicioDia("04:00");

        Macros macros = new Macros();
        macros.setProteinasG(120);
        macros.setCarbohidratosG(200);
        macros.setGrasasG(60);
        u.setMacrosObjetivo(macros);

        u.setCaloriasObjetivo(2100);

        return usuarioRepository.save(u);
    }
    
    @GetMapping("/usuarios")
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioService.obtenerTodosUsuarios();
    }
}