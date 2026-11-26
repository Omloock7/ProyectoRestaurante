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

import com.saborurbano.restaurante.model.Producto;
import com.saborurbano.restaurante.repository.CategoriaRepository;
import com.saborurbano.restaurante.repository.ProductoRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private CategoriaRepository categoriaRepo;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("categorias", categoriaRepo.findByEstadoTrue());
        model.addAttribute("producto", new Producto());
        return "productos";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Producto producto, BindingResult result, RedirectAttributes flash, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("productos", productoRepo.findAll());
            model.addAttribute("categorias", categoriaRepo.findByEstadoTrue());
            model.addAttribute("error", "Verifica los datos del producto (precio > 0 y campos obligatorios).");
            return "productos";
        }
        try {
            productoRepo.save(producto);
            flash.addFlashAttribute("exito", "Producto guardado en el menú.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error: El código del producto ya está registrado.");
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Producto prod = productoRepo.findById(id).orElse(null);
        if (prod == null) {
            flash.addFlashAttribute("error", "Producto no encontrado.");
            return "redirect:/productos";
        }
        model.addAttribute("producto", prod);
        model.addAttribute("productos", productoRepo.findAll());
        model.addAttribute("categorias", categoriaRepo.findByEstadoTrue());
        return "productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            productoRepo.deleteById(id);
            flash.addFlashAttribute("exito", "Producto eliminado.");
        } catch (Exception e) {
            // Si ya está en un pedido, se marca como no disponible
            productoRepo.findById(id).ifPresent(p -> {
                p.setDisponible(false);
                productoRepo.save(p);
            });
            flash.addFlashAttribute("exito", "El producto tiene pedidos asociados; se marcó como Agotado.");
        }
        return "redirect:/productos";
    }
}