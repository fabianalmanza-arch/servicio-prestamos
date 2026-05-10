/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.security;

import com.inversionesanyelo.sistemaprestamos.model.Empleado;
import com.inversionesanyelo.sistemaprestamos.repository.EmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 *
 * @author Fabian
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {

        if (!correo.contains("@")) {
            correo = correo + "@gmail.com";
        }

        Empleado empleado = empleadoRepository.findByCorreo(correo);

        if (empleado == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }

        if (!empleado.getTipoEmpleado().equalsIgnoreCase("admin")) {
            throw new UsernameNotFoundException("Acceso solo para administradores");
        }

        return User.builder()
                .username(empleado.getCorreo())
                .password(empleado.getPassword())
                .roles(empleado.getTipoEmpleado().toUpperCase())
                .build();
    }
}
