package org.embulk.test

import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

@ExtendWith(EmbulkTestExtension::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmbulkTest(val value: Array<KClass<*>> = [], val name: String = "")
