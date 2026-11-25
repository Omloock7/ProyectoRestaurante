package com.saborurbano.restaurante.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saborurbano.restaurante.model.EstadoPedido;
import com.saborurbano.restaurante.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(EstadoPedido estado);
    List<Pedido> findAllByOrderByFechaDesc();
    boolean existsByNumeroPedido(String numeroPedido);
}