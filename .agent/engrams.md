Weekly Engrams: Lecciones Aprendidas
001: El Problema de la Medianoche (00:00) en el Timeline
Problema: Al asignar una hora de fin a las 00:00 (medianoche), el cálculo de duración (end - start) resulta en un número negativo o cero, haciendo que el bloque desaparezca del Timeline o lance una excepción.

Causa: java.time.LocalTime reinicia a 00:00 al final del día, lo cual es lógicamente menor que, por ejemplo, las 22:00.

Solución Técnica: En el TimelineMapper, si endTime es 00:00, se debe tratar aritméticamente como 24:00 para el cálculo de altura.

Regla: long durationMinutes = (end.equals(LocalTime.MIN)) ? (1440 - startToMinutes) : (endToMinutes - startToMinutes);.

002: Conflicto de Scroll Anidado
Problema: El scroll vertical del Timeline no funciona dentro del acordeón porque el RecyclerView principal consume el evento táctil.

Solución: Habilitar nestedScrollingEnabled="true" en el ScrollView interno y asegurar que el contenedor de bloques tenga una altura fija calculada (24 * HOUR_HEIGHT).

003: Persistencia de UI y Reciclaje
Problema: Al marcar una tarea como completada, el estado visual se perdía o se replicaba en otros días debido al reciclaje de vistas.

Solución: El estado visual (alpha, strike-through) debe resetearse explícitamente en el onBind de cada bloque basándose exclusivamente en el modelo de datos.
