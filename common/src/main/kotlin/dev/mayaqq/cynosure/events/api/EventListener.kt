package dev.mayaqq.cynosure.events.api

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer

internal typealias EventListeners = ArrayList<EventListener>

internal fun EventListeners.compile(event: Class<out Event>): Consumer<Any> {
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
}

internal fun <E : Event> EventListeners.addLambdaListener(clazz: Class<*>, handler: (E) -> Unit, priority: Int, receiveCancelled: Boolean) {


}

internal fun EventListeners.addMethodListener(event: Class<out Event>, method: Method, instance: Any?, priority: Int, receiveCancelled: Boolean) {
     add(EventListener(
         event,
         method.declaringClass.canonicalName.replace('.', '/'),
         method.name,
         Type.getMethodDescriptor(method),
         if (Modifier.isStatic(method.modifiers)) InvokerType.Static else InvokerType.VirtualWithInstance(instance!!),
         priority, receiveCancelled, method
    ))
}

internal data class EventListener(
    val event: Class<*>,
    val className: String,
    val methodName: String,
    val methodDesc: String,
    val type: InvokerType,
    val priority: Int,
    val receiveCancelled: Boolean,
    val ref: Any? = null
)

internal sealed interface InvokerType {

    data object Static : InvokerType

    data class VirtualWithOwner(
        val ownerClassName: String,
        val ownerFieldName: String,
        val callOpcode: Int = Opcodes.INVOKEVIRTUAL,
    ) : InvokerType

    data class VirtualWithInstance(
        val instance: Any?,
        val instruction: Int = Opcodes.INVOKEVIRTUAL
    ) : InvokerType
}
