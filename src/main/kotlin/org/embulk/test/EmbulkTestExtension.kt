package org.embulk.test

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import org.junit.platform.commons.support.AnnotationSupport.findPublicAnnotatedFields
import java.lang.AssertionError

class EmbulkTestExtension : TestInstancePostProcessor, AfterEachCallback {

    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        val annotation = findAnnotation(context.testClass, EmbulkTest::class.java)
                .orElseThrow { AssertionError("@EmbulkTest not found") }
        val embulk = createNewEmbulk(annotation)
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

    private fun createNewEmbulk(annotation: EmbulkTest): ExtendedTestingEmbulk {
        val builder = ExtendedTestingEmbulk.builder()
        annotation.value.forEach {
            if (annotation.name.isEmpty()) {
                builder.registerPlugin(impl = it)
            } else {
                builder.registerPlugin(impl = it, name = annotation.name)
            }
        }
        return builder.build() as ExtendedTestingEmbulk
    }

    companion object {
        @JvmStatic
        private val NAMESPACE = ExtensionContext.Namespace.create(EmbulkTestExtension::class.java)
        private val KEY = "embulk"
    }
}
