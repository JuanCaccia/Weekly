# 🎨 Skill: UI & UX Weekly
- **Timeline Engine:** 1 hora = 100dp. El escalado debe hacerse siempre con `TypedValue.applyDimension`.
- **Custom View:** El fondo del timeline se dibuja en `TimelineBackgroundView.onDraw`. No añadir sub-vistas para las líneas horarias.
- **Animaciones:** Usar `TransitionManager` con `AccelerateDecelerateInterpolator` para acordeones. Duración máxima: 400ms.
- **Interacción:** El FAB es contextual al día expandido. Los bloques de tareas deben tener un "Touch Target" mínimo de 48dp de altura.
