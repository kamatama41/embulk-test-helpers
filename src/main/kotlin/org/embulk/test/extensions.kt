package org.embulk.test

import org.embulk.config.DataSource
import kotlin.reflect.KClass

fun <T1 : Any, T2: Any> TestingEmbulk.Builder.registerPlugin(iface: KClass<T1>, name: String, impl: KClass<T2>)
        : TestingEmbulk.Builder {
    return registerPlugin(iface.java, name, impl.java)
}

fun <T: DataSource> T.set(vararg keyValues: Pair<String, Any>): T {
    keyValues.forEach { this.set(it.first, it.second) }
    return this
}
