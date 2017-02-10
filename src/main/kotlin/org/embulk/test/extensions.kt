package org.embulk.test

import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import org.embulk.config.DataSource
import kotlin.reflect.KClass

fun <T1 : Any, T2 : Any> TestingEmbulk.Builder.registerPlugin(iface: KClass<T1>, name: String, impl: KClass<T2>)
        : TestingEmbulk.Builder {
    return registerPlugin(iface.java, name, impl.java)
}

fun <T : DataSource> T.set(vararg keyValues: Pair<String, Any>): T {
    keyValues.forEach { this.set(it.first, it.second) }
    return this
}

fun listOf(vararg plugins: KClass<*>): List<Class<*>> {
    return plugins.map { it.java }
}

fun String.toSnakeCase(): String = UPPER_CAMEL.to(LOWER_UNDERSCORE, this)
