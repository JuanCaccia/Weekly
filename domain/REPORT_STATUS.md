# Reporte de Estado del Proyecto: Weekly

Este documento resume el estado actual de la aplicación tras las últimas actualizaciones de UI, reactividad y lógica de negocio.

## 1. Núcleo (Domain)
- **Task.java**: 
  - Se han implementado métodos `equals()` y `hashCode()` robustos que comparan: `id`, `titulo`, `prioridad`, `horaInicio`, `horaFin` e `isCompleted`.
  - Se añadió el método `copy()` para facilitar la inmutabilidad durante la edición y evitar mutaciones accidentales en el hilo de UI.
- **Prioridades**: Definidas con colores específicos:
  - Alta: `#E6A4A4`
  - Media: `#F5C6A5`
  - Baja: `#9ED9B3`

## 2. Interfaz de Usuario (UI/UX)
- **Diseño Técnico**:
  - Se redujo el `cornerRadius` global de 24dp/28dp a un rango de **4dp a 8dp**, logrando un aspecto más profesional y estructurado.
  - El fondo de los bloques de tarea en el Timeline es ahora blanco puro para contrastar con el fondo del contenedor.
- **TimelineBackgroundView**:
  - Se eliminó la línea de tiempo horizontal que cruzaba la pantalla.
  - Se implementó un **indicador de hora actual dinámico**: un recuadro redondeado violeta (`#BFA8E6`) con texto blanco en el margen lateral que se desplaza en tiempo real.
  - El contenedor del Timeline ahora tiene un fondo grisáceo tenue (`#ECEAF3`).

## 3. Reactividad y Datos (DiffUtil & Room)
- **Sincronización Inmediata**:
  - Se corrigió el problema de refresco de la UI mediante el uso de copias de objetos (`t.copy()`) en el diálogo de edición.
  - El `MainViewModel` ahora emite una nueva instancia de la lista (`new ArrayList<>`) en cada actualización, asegurando que `DiffUtil` detecte los cambios de contenido.
  - Se centralizó la lógica en el ViewModel compartido (`requireActivity()`) para que el diálogo y la actividad principal estén siempre en sintonía.
- **Validación de Colisiones**:
  - Se mantiene la lógica que evita solapamientos horários al guardar o editar actividades.

## 4. Componentes de Adaptación
- **DayAdapter**: 
  - Optimizado para usar `Objects.equals()` en la comparación de listas de tareas.
  - Barra lateral de los bloques vinculada visualmente a la **Prioridad** de la tarea.
- **AddTaskDialogFragment**: 
  - Soporta creación y edición completa de Tareas y Eventos.
  - Integración con selectores de hora y categorías.

## Próximos Pasos Sugeridos
- Implementación de notificaciones para recordatorios de tareas.
- Refinamiento de la vista de estadísticas/heatmap basada en la densidad de tareas completadas.
- Sincronización en la nube (opcional).

---
*Reporte generado automáticamente - [Fecha Actual]*
