# emebot

Emebot is a supersonic, subatomic Discord administration and entertainment bot built with the modern **Quarkus** framework for Java. It offers highly responsive event processing, database-driven configuration, Lavalink integration, and a sophisticated, resilient, tool-capable AI chatbot subsystem.

---

## 🚀 Key Features

### 1. 🤖 Advanced AI Chatbot & Agent Subsystem (New!)
A fully persistent, tool-capable AI companion integrated directly into Discord.
*   **Session-Based Conversations:** Complete DB-backed persistence (`AIChatMessage`) capturing user messages, assistant responses, and intermediate tool execution logs for context-aware multi-turn conversations.
*   **Decoupled Tool Registry (`AIToolRegistry`):** Grants the LLM real-time agentic capabilities. The AI can dynamically invoke bot actions such as managing channels/roles, executing anime actions, looking up birthdays, retrieving text channel history, and controlling music playback.
*   **Secure Execution:** Real-time verification of user permissions through Discord JDA prior to executing sensitive tools.
*   **Multi-Tier Config Fallback & Resilience:** High availability via multi-tier API keys. If a primary endpoint fails, the bot seamlessly traverses:
    1. Server-specific API configuration
    2. Server backup slots (Indices 1–10)
    3. Global system backup slots (Indices 1–10)
    4. Global default configurations
*   **Smart Routing:** Remembers the last successful configuration index (`lastSuccessfulConfigIndex`) to bypass failing endpoints and optimize response latency.
*   **Recursive Tool Resolution:** Supports up to `5` consecutive tool calls in a single turn, enabling the AI to solve multi-step problems autonomously.
*   **Dynamic System Prompts:** Personalized system instructions supporting variable interpolation (`{botName}`, `{serverName}`, `{userName}`).
*   **Localized Error Handling:** Translates standard API exceptions (e.g., `401 Unauthorized`, `429 Rate Limited`, `504 Timeout`) into clear, helpful, or sassy Spanish feedback.

### 2. 🗄️ Database-Driven Settings & Refactoring
*   **Refactored Persistence (`SettingsManager`):** Migrated from traditional configuration-file management to a robust database-backed system utilizing JPA, Hibernate, and the Active Record/Repository pattern via **Panache**.
*   **Clean Database Operations:** Replaced stateful persists with clean `merge` calls in JPA repositories, ensuring thread-safe concurrency during server and configuration updates.
*   **Security & Environment Refactoring:** Eradicated hardcoded tokens and secrets across the entire codebase. Configurations are externalized into environment variables (e.g., `DISCORD_TOKEN`, `DB_PASSWORD`, `LAVALINK_PASSWORD`, `OPENAI_API_KEY`).
*   **Multi-Profile Configuration:** Features segmented profiles for seamless operations:
    *   `%dev`: Dynamic MariaDB database and local nodes.
    *   `%prod`: Production-grade MariaDB persistence and remote nodes.
    *   `%test`: In-memory H2 database integration for lightning-fast test isolation.

### 3. 🎵 Lavalink Audio Streaming
High-performance audio streaming with search capability, queues, custom repeat modes, volume controls, and audio effects.

---

## 🛠️ Command Reference (AI Subsystem)

### Server Administration Commands (`/ai`)
*   `enable` / `disable`: Toggles the chatbot on the server.
*   `status`: Displays detailed active settings, exclusive mode, custom model configurations, and backup slots.
*   `channel [canal-id]`: Designates an exclusive channel for AI chat. If empty, removes exclusivity.
*   `exclusive [true/false]`: Enforces whether the AI should ONLY respond in the designated channel.
*   `setup [options]`: Saves server-specific configurations. Supports overriding keys, base URLs, models, and assigning backup slots `1-10` (`/ai setup api-key=... base-url=... modelo-ia=... indice=1`).
*   `reset`: Resets session token and wipes channel chat history database logs.

### Global Owner Commands (`/aiowner`)
*   `setup [options]`: Modifies the primary global AI credentials or specifies global backup slots `1-10`.
*   `prompt [text]`: Modifies the global base system prompt. Supports dynamic templates.

---

## 💻 Developer & Deployment Guide

This project is built on **Quarkus**.

### Prerequisites
Make sure you have configured your environment variables in your local shell or system properties:
*   `DISCORD_TOKEN`: Discord Bot Token.
*   `CLIENT_TOKEN`: Discord client token/application ID.
*   `DB_PASSWORD`: Password for the MariaDB database.
*   `LAVALINK_PASSWORD`: Password for your Lavalink nodes.
*   `OPENAI_API_KEY`: API Key for the OpenAI or DeepSeek provider.

### Running the application in Dev Mode
Start the application in Quarkus Dev Mode with live-coding enabled:
```shell script
./mvnw quarkus:dev
```
> **Note:** Access the Quarkus Dev UI in dev mode at <http://localhost:8080/q/dev/>.

### Packaging the Application
To compile and package the application:
```shell script
./mvnw package
```
*   Produces `quarkus-run.jar` in `target/quarkus-app/` directory along with copied dependencies in `lib/`.
*   Run the compiled artifact using: `java -jar target/quarkus-app/quarkus-run.jar`.

To build a standalone **Über-Jar**:
```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```
*   Runnable as `java -jar target/*-runner.jar`.

### Building a Native Executable
Build a highly optimized native executable using GraalVM:
```shell script
./mvnw package -Dnative
```
Or build a containerized native executable without local GraalVM:
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```
Run the native executable:
```shell script
./target/emebot-1.0-SNAPSHOT-runner
```
