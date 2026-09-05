# 🏛️ Codebase Architect

> Autonomous AI-powered codebase visualizer and interactive architecture explorer. Transform raw GitHub repositories into navigable, bi-directional architectural flowcharts in seconds.

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.3+](https://img.shields.io/badge/Spring%20Boot-3.3+-green.svg)](https://spring.io/projects/spring-boot)
[![React 18](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![React Flow](https://img.shields.io/badge/React%20Flow-@xyflow/react-ff4081.svg)](https://reactflow.dev/)
[![Gemini 1.5](https://img.shields.io/badge/AI-Google%20Gemini%201.5-8e44ad.svg)](https://ai.google.dev/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📌 Overview

Understanding legacy systems, unfamiliar microservices, or large open-source repositories often requires hours of tedious file browsing. Static architectural diagrams in Notion or Confluence decay almost immediately after code commits.

**Codebase Architect** automates code comprehension:
1. Performs an ephemeral shallow clone of any public or private GitHub repository.
2. Filters out build noise and extracts structural AST signatures (controllers, services, database models, interfaces).
3. Leverages **Google Gemini 1.5** via strict JSON Schema mode to map high-level architecture into structured node and edge graphs.
4. Renders an interactive **React Flow** canvas paired with an embedded **Monaco Editor** for bi-directional code navigation and animated execution flow tracing.

---

## ✨ Key Features

* **Instant Visual Blueprints:** Converts complex repositories into auto-laid-out layered graphs (Controllers $\to$ Business Logic $\to$ Data Persistence $\to$ External APIs).
* **Bi-directional Code Deep-Linking:** Click any architectural node on the canvas to instantly open the source file in Monaco Editor scrolled directly to the relevant line number.
* **Conversational Flow Tracing:** Ask natural language questions (e.g., *"How does the user registration and payment flow work?"*) and watch the execution path illuminate across animated edges and nodes.
* **Ephemeral & Secure Ingestion:** Repositories are processed in containerized scratch space and purged immediately after metadata extraction.
* **Export Options:** Export architectural diagrams to **Mermaid.js**, SVG, or high-res PNG for documentation and pull requests.

---

## 🏗️ System Architecture
