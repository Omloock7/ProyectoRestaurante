document.addEventListener("DOMContentLoaded", () => {
    const btnAgregar = document.getElementById("btnAgregar");
    const productoSelect = document.getElementById("productoSelect");
    const cantidadInput = document.getElementById("cantidadInput");
    const tablaBody = document.querySelector("#tablaDetalle tbody");
    const totalLabel = document.getElementById("totalPedido");
    const formPedido = document.getElementById("formPedido");

    let itemsPedido = [];

    // Formatear moneda colombiana (COP)
    const formatoCOP = new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    });

    btnAgregar.addEventListener("click", () => {
        const opcion = productoSelect.options[productoSelect.selectedIndex];
        const productoId = opcion.value;
        const nombre = opcion.getAttribute("data-nombre");
        const precio = parseFloat(opcion.getAttribute("data-precio"));
        const cantidad = parseInt(cantidadInput.value);

        // Validaciones de negocio
        if (!productoId) {
            alert("Por favor, selecciona un producto.");
            return;
        }
        if (isNaN(cantidad) || cantidad <= 0) {
            alert("La cantidad debe ser mayor que cero.");
            return;
        }

        // Si el producto ya está en la tabla, suma la cantidad
        const existente = itemsPedido.find(item => item.productoId === productoId);
        if (existente) {
            existente.cantidad += cantidad;
            existente.subtotal = existente.cantidad * existente.precio;
        } else {
            itemsPedido.push({
                productoId,
                nombre,
                precio,
                cantidad,
                subtotal: precio * cantidad
            });
        }

        cantidadInput.value = 1;
        productoSelect.value = "";
        renderizarTabla();
    });

    function renderizarTabla() {
        tablaBody.innerHTML = "";
        let totalGeneral = 0;

        itemsPedido.forEach((item, index) => {
            totalGeneral += item.subtotal;

            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td>
                    ${item.nombre}
                    <input type="hidden" name="productoIds" value="${item.productoId}">
                    <input type="hidden" name="cantidades" value="${item.cantidad}">
                </td>
                <td class="text-end">${formatoCOP.format(item.precio)}</td>
                <td class="text-center">${item.cantidad}</td>
                <td class="text-end fw-semibold">${formatoCOP.format(item.subtotal)}</td>
                <td class="text-center">
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="eliminarItem(${index})">
                        <i class="bi bi-x-lg"></i>
                    </button>
                </td>
            `;
            tablaBody.appendChild(fila);
        });

        totalLabel.textContent = formatoCOP.format(totalGeneral);
    }

    // Hacer global la función para el botón de quitar fila
    window.eliminarItem = (index) => {
        itemsPedido.splice(index, 1);
        renderizarTabla();
    };

    // Validar que el pedido tenga al menos 1 producto antes de enviarlo al servidor
    formPedido.addEventListener("submit", (e) => {
        if (itemsPedido.length === 0) {
            e.preventDefault();
            alert("El pedido debe contener al menos un producto.");
        }
    });
});