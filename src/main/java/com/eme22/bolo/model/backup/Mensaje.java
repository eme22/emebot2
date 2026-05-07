package com.eme22.bolo.model.backup;

import java.util.List;

public record Mensaje(String autor, String contenido, long fecha, List<Adjunto> adjuntos) {
}
