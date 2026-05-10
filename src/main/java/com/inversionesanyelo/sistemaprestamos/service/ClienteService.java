/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.inversionesanyelo.sistemaprestamos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.inversionesanyelo.sistemaprestamos.model.Cliente;
import com.inversionesanyelo.sistemaprestamos.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author fabia
 */
@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // ✅ Método para listar todos los clientes
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    // ✅ Método para guardar un cliente
    public void guardar(Cliente cliente) {

        // 🔹 Nuevo cliente
        if (cliente.getId() == null) {

            Long ultimo = clienteRepository.obtenerUltimoId();
            cliente.setId((ultimo != null ? ultimo : 0) + 1);

        } 
        // 🔹 Si escribió ID manual
        else {

            if (clienteRepository.existsById(cliente.getId())) {
                throw new RuntimeException("El ID ya existe");
            }
        }

        clienteRepository.save(cliente);
    }

    // ✅ Método para buscar un cliente por ID
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    // ✅ Método para eliminar un cliente
    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }
}
