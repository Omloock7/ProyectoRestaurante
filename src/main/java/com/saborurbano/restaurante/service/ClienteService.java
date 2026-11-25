package com.saborurbano.restaurante.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saborurbano.restaurante.model.Cliente;
import com.saborurbano.restaurante.repository.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarPorIdentificacion(String identificacion) {
        return clienteRepository.findByIdentificacion(identificacion);
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() == null) {
            if (clienteRepository.existsByIdentificacion(cliente.getIdentificacion())) {
                throw new IllegalArgumentException("Ya existe un cliente con la identificación: " + cliente.getIdentificacion());
            }
            if (clienteRepository.existsByCorreo(cliente.getCorreo())) {
                throw new IllegalArgumentException("Ya existe un cliente con el correo: " + cliente.getCorreo());
            }
        }
        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }
}