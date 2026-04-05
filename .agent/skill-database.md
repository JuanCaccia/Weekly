# 💾 Skill: Base de Datos & Persistencia
- **Room:** Versión 2.6.1. Usar `exportSchema = false` por ahora.
- **Conversores:** `java.time.LocalDateTime` se almacena como String ISO.
- **Threading:** Todas las operaciones de escritura deben ejecutarse en un hilo secundario vía `ExecutorService` o `Coroutine`.
- **Integridad:** Validar colisiones con `CollisionDetector` antes de cada `INSERT` o `UPDATE` de actividades con hora.
