package com.saborurbano.restaurante.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.saborurbano.restaurante.model.Categoria;
import com.saborurbano.restaurante.repository.CategoriaRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("categoria", new Categoria()); // Objeto vacío para el formulario
        return "categorias";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Categoria categoria, BindingResult result, RedirectAttributes flash, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepo.findAll());
            model.addAttribute("error", "Revisa los campos obligatorios de la categoría.");
            return "categorias";
        }
        try {
            categoriaRepo.save(categoria);
            flash.addFlashAttribute("exito", "Categoría guardada correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: El código de categoría ya existe.");
        }
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Categoria cat = categoriaRepo.findById(id).orElse(null);
        if (cat == null) {
            flash.addFlashAttribute("error", "Categoría no encontrada.");
            return "redirect:/categorias";
        }
        model.addAttribute("categoria", cat);
        model.addAttribute("categorias", categoriaRepo.findAll());
        return "categorias";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            categoriaRepo.deleteById(id);
            flash.addFlashAttribute("exito", "Categoría eliminada.");
        } catch (Exception e) {
            // Si tiene productos asociados, la desactivamos en lugar de borrarla (Regla Épica 1)
            categoriaRepo.findById(id).ifPresent(c -> {
                c.setEstado(false);
                categoriaRepo.save(c);
            });
            flash.addFlashAttribute("exito", "La categoría tiene productos asociados, por lo que fue desactivada.");
        }
        return "redirect:/categorias";
    }
}