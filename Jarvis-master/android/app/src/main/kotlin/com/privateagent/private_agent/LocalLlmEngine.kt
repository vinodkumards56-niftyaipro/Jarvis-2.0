package com.privateagent.private_agent

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Owns the LiteRT-LM Engine and Conversation on one dedicated thread.
 * Engine initialization and generation are deliberately kept off Android's UI thread.
 */
class LocalLlmEngine {
    private val owner: ExecutorService = Executors.newSingleThreadExecutor()
    private var engine: Engine? = null
    private var modelPath: String? = null

    fun initialize(path: String, callback: (Result<Unit>) -> Unit) {
        owner.execute {
            try {
                val model = File(path)
                require(model.isFile && model.canRead()) { "Local model file is missing or unreadable." }
                closeOwnedResources()
                val candidate = Engine(EngineConfig(modelPath = model.absolutePath, backend = Backend.CPU()))
                // Do not retain the engine unless initialization completed successfully.
                candidate.initialize()
                engine = candidate
                modelPath = model.absolutePath
                callback(Result.success(Unit))
            } catch (error: Throwable) {
                callback(Result.failure(error))
            }
        }
    }

    fun generate(prompt: String, callback: (Result<String>) -> Unit) {
        owner.execute {
            try {
                require(prompt.isNotBlank()) { "Prompt cannot be empty." }
                val activeEngine = checkNotNull(engine) { "Local model is not loaded." }
                val response = activeEngine.createConversation().use { conversation ->
                    conversation.sendMessage(prompt).toString()
                }
                callback(Result.success(response))
            } catch (error: Throwable) {
                callback(Result.failure(error))
            }
        }
    }

    fun status(callback: (Map<String, Any?>) -> Unit) {
        owner.execute {
            callback(
                mapOf(
                    "loaded" to (engine != null),
                    "modelPath" to modelPath
                )
            )
        }
    }

    fun unload(callback: (Result<Unit>) -> Unit = {}) {
        owner.execute {
            try {
                closeOwnedResources()
                callback(Result.success(Unit))
            } catch (error: Throwable) {
                callback(Result.failure(error))
            }
        }
    }

    private fun closeOwnedResources() {
        val current = engine
        engine = null
        modelPath = null
        current?.close()
    }

    fun shutdown() {
        owner.execute { closeOwnedResources() }
        owner.shutdown()
    }
}
