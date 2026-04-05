# 🏗️ Skill: Arquitectura Hexagonal
- **Módulo Domain:** Java puro. Prohibido importar `android.*`. Solo lógica de negocio y definiciones de interfaz (Repositorios).
- **Módulo App:** Implementación de infraestructura (Room, Hilt, Framework Android).
- **Data Flow:** UI -> ViewModel -> UseCase (Domain) -> Repository Implementation (App) -> Room.
- **DI:** Hilt gestiona el grafo. Los módulos se definen en el paquete `di`.
