package jFx2.core.capabilities

interface LogScope {
    fun debug(msg: () -> String) {}
    fun warn(msg: () -> String) {}
    fun error(msg: () -> String, t: Throwable? = null) {}
}

object NoopLogScope : LogScope