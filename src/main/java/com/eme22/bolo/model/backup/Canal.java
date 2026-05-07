package com.eme22.bolo.model.backup;

import java.util.List;

public record Canal(String nombre, String tipo, List<Mensaje> mensajes) {
}
