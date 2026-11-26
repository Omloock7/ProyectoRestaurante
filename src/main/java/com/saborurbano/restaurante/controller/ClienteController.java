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

import com.saborurbano.restaurante.model.Cliente;
import com.saborurbano.restaurante.service.ClienteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listarClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("cliente", new Cliente());
        return "cliente";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Cliente cliente, BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("error", "Verifica que todos los campos obligatorios estén correctos.");
            return "cliente";
        }
        try {
            clienteService.guardar(cliente);
            flash.addFlashAttribute("exito", "Cliente guardado correctamente.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Cliente cli = clienteService.buscarPorId(id).orElse(null);
        if (cli == null) {
            flash.addFlashAttribute("error", "Cliente no encontrado.");
            return "redirect:/clientes";
        }
        model.addAttribute("cliente", cli);
        model.addAttribute("clientes", clienteService.listarTodos());
        return "cliente";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clienteService.eliminar(id);
            flash.addFlashAttribute("exito", "Cliente eliminado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "No se puede eliminar el cliente porque tiene pedidos registrados.");
        }
        return "redirect:/clientes";
    }
}