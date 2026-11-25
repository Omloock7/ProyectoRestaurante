package com.saborurbano.restaurante.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saborurbano.restaurante.model.Cliente;
import com.saborurbano.restaurante.model.DetallePedido;
import com.saborurbano.restaurante.model.EstadoPedido;
import com.saborurbano.restaurante.model.Pedido;
import com.saborurbano.restaurante.model.Producto;
import com.saborurbano.restaurante.repository.ClienteRepository;
import com.saborurbano.restaurante.repository.PedidoRepository;
import com.saborurbano.restaurante.repository.ProductoRepository;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaDesc();
    }

    public List<Pedido> listarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Pedido registrarPedido(Pedido pedido) {
        // Validar que el cliente exista
        if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente para el pedido");
        }
        Cliente cliente = clienteRepository.findById(pedido.getCliente().getId())
                .orElseThrow(() -> new IllegalArgumentException("El cliente seleccionado no existe"));
        pedido.setCliente(cliente);

        // Validar que el pedido tenga al menos un producto
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe contener al menos un producto");
        }

        //  Generar número de pedido automático si no viene asignado
        if (pedido.getNumeroPedido() == null || pedido.getNumeroPedido().isBlank()) {
            String codigoGenerado = "PED-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            pedido.setNumeroPedido(codigoGenerado);
        } else if (pedidoRepository.existsByNumeroPedido(pedido.getNumeroPedido())) {
            throw new IllegalArgumentException("Ya existe un pedido con el número: " + pedido.getNumeroPedido());
        }

        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal totalPedido = BigDecimal.ZERO;

        // 4Validar cada línea de detalle y recalcular precios/subtotales en el servidor
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                throw new IllegalArgumentException("Cada línea del pedido debe tener un producto válido");
            }

            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            // Regla: No agregar productos no disponibles
            if (Boolean.FALSE.equals(producto.getDisponible())) {
                throw new IllegalArgumentException("El producto '" + producto.getNombre() + "' no está disponible");
            }

            // Regla: Cantidad mayor que cero
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad para '" + producto.getNombre() + "' debe ser mayor que cero");
            }

            // Regla: Precio unitario mayor que cero (tomado directamente de la BD)
            if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El precio de '" + producto.getNombre() + "' debe ser mayor que cero");
            }

            detalle.setProducto(producto);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.calcularSubtotal();
            detalle.setPedido(pedido);

            totalPedido = totalPedido.add(detalle.getSubtotal());
        }

        pedido.setTotal(totalPedido);

        // Guarda cabecera y detalles en una sola transacción (gracias a CascadeType.ALL)
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalArgumentException("No se puede cambiar el estado de un pedido ya " + pedido.getEstado().getDescripcion().toLowerCase());
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }
}