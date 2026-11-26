package com.saborurbano.restaurante.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.saborurbano.restaurante.model.Cliente;
import com.saborurbano.restaurante.model.DetallePedido;
import com.saborurbano.restaurante.model.EstadoPedido;
import com.saborurbano.restaurante.model.Pedido;
import com.saborurbano.restaurante.model.Producto;
import com.saborurbano.restaurante.service.ClienteService;
import com.saborurbano.restaurante.service.PedidoService;
import com.saborurbano.restaurante.service.ProductoService;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("estados", EstadoPedido.values());
        return "pedidos-lista";
    }

    @GetMapping("/nuevo")
    public String nuevoPedido(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("productosDisponibles", productoService.listarDisponibles());
        return "pedidos-form";
    }

    @PostMapping("/guardar")
    public String guardarPedido(
            @RequestParam("clienteId") Long clienteId,
            @RequestParam(value = "productoIds", required = false) List<Long> productoIds,
            @RequestParam(value = "cantidades", required = false) List<Integer> cantidades,
            RedirectAttributes flash) {
        try {
            if (productoIds == null || productoIds.isEmpty()) {
                throw new IllegalArgumentException("El pedido debe contener al menos un producto.");
            }

            Pedido pedido = new Pedido();
            Cliente cliente = new Cliente();
            cliente.setId(clienteId);
            pedido.setCliente(cliente);

            List<DetallePedido> detalles = new ArrayList<>();
            for (int i = 0; i < productoIds.size(); i++) {
                DetallePedido detalle = new DetallePedido();
                Producto prod = new Producto();
                prod.setId(productoIds.get(i));
                detalle.setProducto(prod);
                detalle.setCantidad(cantidades.get(i));
                detalles.add(detalle);
            }
            pedido.setDetalles(detalles);

            Pedido guardado = pedidoService.registrarPedido(pedido);
            flash.addFlashAttribute("exito", "Pedido " + guardado.getNumeroPedido() + " registrado correctamente.");
            return "redirect:/pedidos";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/nuevo";
        }
    }

    @PostMapping("/actualizar-estado/{id}")
    public String actualizarEstado(@PathVariable Long id, @RequestParam("estado") EstadoPedido estado, RedirectAttributes flash) {
        try {
            pedidoService.actualizarEstado(id, estado);
            flash.addFlashAttribute("exito", "Estado del pedido actualizado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos";
    }
}