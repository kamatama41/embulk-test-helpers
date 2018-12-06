package org.embulk.test

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import org.junit.platform.commons.support.AnnotationSupport.findPublicAnnotatedFields
import java.lang.AssertionError
import kotlin.reflect.KClass

class EmbulkTestExtension : TestInstancePostProcessor, AfterEachCallback {

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val pluginClasses = findAnnotation(context.testClass, EmbulkTest::class.java)
                .orElseThrow { AssertionError("@EmbulkTest not found") }
                .value
        val embulk = createNewEmbulk(pluginClasses)
        val embulkFields = findPublicAnnotatedFields(testInstance.javaClass, ExtendedTestingEmbulk::class.java, Embulk::class.java)
        embulkFields.forEach {
            it.isAccessible = true
            it.set(testInstance, embulk)
        }
        context.getStore(NAMESPACE).put(KEY, embulk)
    }

    override fun afterEach(context: ExtensionContext) {
        var parent = context.parent
        while (parent.isPresent && parent.get() != context.root) {
            val parentContext = parent.get()
            val embulk = parentContext.getStore(NAMESPACE).remove(KEY, ExtendedTestingEmbulk::class.java)
            embulk?.destroy()

            parent = parentContext.parent
        }
    }

    private fun createNewEmbulk(pluginClasses: Array<KClass<*>>): ExtendedTestingEmbulk {
        return ExtendedTestingEmbulk.builder()
                .registerPlugins(*pluginClasses)
                .build() as ExtendedTestingEmbulk
    }

    companion object {
        @JvmStatic
        private val NAMESPACE = ExtensionContext.Namespace.create(EmbulkTestExtension::class.java)
        private val KEY = "embulk"
    }
}
