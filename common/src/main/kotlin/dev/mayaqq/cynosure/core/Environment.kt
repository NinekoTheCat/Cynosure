package dev.mayaqq.cynosure.core

public val currentEnvironment: Environment
    get() = PlatformHooks.environment

public enum class Environment {
    CLIENT, SERVER;
}