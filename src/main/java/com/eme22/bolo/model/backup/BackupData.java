package com.eme22.bolo.model.backup;

import java.util.List;

public record BackupData(List<Canal> canal, String nombre, List<Permiso> permisos) {
}
