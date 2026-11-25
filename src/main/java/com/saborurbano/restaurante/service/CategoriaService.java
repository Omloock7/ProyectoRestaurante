package com.saborurbano.restaurante.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.saborurbano.restaurante.model.Categoria;
import com.saborurbano.restaurante.repository.CategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public List<Categoria> listarActivas() {
        return categoriaRepository.findByEstadoTrue();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria guardar(Categoria categoria) {
        // Si es creación (id == null), validar que el código no exista
        if (categoria.getId() == null && categoriaRepository.existsByCodigo(categoria.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una categoría con el código: " + categoria.getCodigo());
        }
        return categoriaRepository.save(categoria);
    }

    public void cambiarEstado(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        categoria.setEstado(!categoria.getEstado());
        categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}