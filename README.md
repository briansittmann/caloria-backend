# CalorIA – Backend

**CalorIA** es una API REST desarrollada en Java con Spring Boot que actúa como backend para una app móvil de coaching nutricional. Permite a los usuarios registrar su perfil, definir objetivos alimentarios, analizar comidas y generar recetas personalizadas usando IA.

---

## 🚀 Instalación

1. Cloná este repositorio:

```bash
git clone https://github.com/tu-usuario/caloria-backend.git
cd caloria-backend
## Requisitos


2.	Asegurate de tener configuradas las siguientes variables de entorno (en tu IDE o .env):

•	OPENAI_API_KEY
•	FOOD_AI
•	RECIPES_AI

3.	Ejecutá el proyecto con Maven:
./mvnw spring-boot:run


📦 Requisitos
	•	Java 11 o superior
	•	Maven 3+
	•	MongoDB (Atlas o local)
	•	Conexión a Internet (para consultas a OpenAI)

🧪 Endpoints y Documentación Swagger

Una vez levantado el servidor, accedé a la documentación interactiva de la API en:

🛠️ Tecnologías Usadas
	•	Spring Boot – Framework principal para desarrollo backend
	•	MongoDB Atlas – Base de datos NoSQL en la nube
	•	Spring Security + JWT – Autenticación segura y sin sesiones
	•	OpenAI Assistants v2 – Análisis nutricional y generación de recetas mediante IA
	•	Lombok – Reducción de código repetitivo
	•	Maven – Compilación y gestión de dependencias


📁 Estructura del Proyecto
src/
├── controller/      # Controladores REST
├── service/         # Lógica de negocio
├── model/           # Entidades y documentos MongoDB
├── dto/             # Objetos de transferencia de datos
├── config/          # Seguridad, JWT, Jackson, Swagger
└── repository/      # Repositorios de acceso a datos

👤 Autor
Brian Sittmann – @briansittmann

📄 Licencia

MIT License – Libre para usar, modificar y distribuir.
