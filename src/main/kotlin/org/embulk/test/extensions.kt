package org.embulk.test

import com.google.common.base.CaseFormat.UPPER_CAMEL
import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import org.embulk.config.DataSource
import kotlin.reflect.KClass

fun ExtendedTestingEmbulk.Builder.registerPlugin(
        impl: KClass<*>, name: String? = null, iface: KClass<*>? = null): ExtendedTestingEmbulk.Builder {
    return registerPlugin(impl.java, name ?: guessName(impl.java), iface?.java ?: guessInterface(impl.java))
}

fun <T : DataSource> T.set(vararg keyValues: Pair<String, Any>): T {
    keyValues.forEach { this.set(it.first, it.second) }
    return this
}

fun String.toSnakeCase(): String = UPPER_CAMEL.to(LOWER_UNDERSCORE, this)
