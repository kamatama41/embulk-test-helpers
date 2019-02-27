package org.embulk.test

import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(EmbulkTestExtension::class)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Embulk
