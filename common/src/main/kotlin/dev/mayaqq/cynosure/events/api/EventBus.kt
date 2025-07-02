package dev.mayaqq.cynosure.events.api

import dev.mayaqq.cynosure.CynosureInternal
import dev.mayaqq.cynosure.utils.asm.descriptorToClassName
import dev.mayaqq.cynosure.utils.asm.mappedValues
import org.objectweb.asm.tree.ClassNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer
import kotlin.metadata.ClassKind
import kotlin.metadata.jvm.KotlinClassMetadata
import kotlin.metadata.jvm.Metadata
import kotlin.metadata.kind
import kotlin.reflect.KClass

/**
 * Main event bus where all minecraft events are run
 */
public object MainBus : EventBus() {
    override fun toString(): String = "main"
}

/**
 * Subscribe to an event bus. If the receiver is an instance or object any instance method marked with [Subscription]
 * will be added to the event bus. If the receiver is a [KClass] or [Class] instance, any static methods of the class
 * will be added to the event bus
 */
public fun Any.subscribeTo(bus: EventBus) {
    val thing = if(this is KClass<*>) java else this
    if(thing is Class<*>) {
        thing.declaredMethods.filter { Modifier.isStatic(it.modifiers) }
            .forEach { bus.registerMethod(it) }
    } else {
        javaClass.declaredMethods.filter { !Modifier.isStatic(it.modifiers) }
            .forEach { bus.registerMethod(it, thing) }
    }
}

/**
 * Unsubscribe from an event bus
 */
public fun Any.unsubscribeFrom(bus: EventBus) {
    val thing = if(this is KClass<*>) java else this
    if(thing is Class<*>) {
        thing.declaredMethods.filter { Modifier.isStatic(it.modifiers) }
            .forEach { bus.unregisterMethod(it) }
    } else {
        javaClass.declaredMethods.filter { !Modifier.isStatic(it.modifiers) }
            .forEach { bus.unregisterMethod(it) }
    }
}


public open class EventBus {

    private val listeners: MutableMap<Class<*>, EventListeners> = mutableMapOf()
    private val handlers: MutableMap<Class<*>, Consumer<Any>> = mutableMapOf()

    public inline fun <reified E : Event> register(priority: Int = 0, receiveCancelled: Boolean = false, noinline handler: (E) -> Unit) {
        register(E::class.java, priority, receiveCancelled, handler)
    }

    public fun <E : Event> register(clazz: Class<E>, priority: Int = 0, receiveCancelled: Boolean = false, handler: (E) -> Unit) {
        unregisterHandler(clazz)
        listeners.getOrPut(clazz, ::EventListeners).addLambdaListener(clazz, handler, priority, receiveCancelled)
    }

    public inline fun <reified E : Event> unregister(noinline callback: (E) -> Unit) {
        unregister(E::class.java, callback = callback)
    }

    public fun <E : Event> unregister(type: Class<E>, callback: (E) -> Unit) {
        unregisterHandler(type)
        listeners.values.forEach { it.removeListener(callback) }
    }

    public fun post(
        event: Event,
        context: Any? = null,
        onError: ((Throwable) -> Unit)? = null
    ): Boolean {
        getHandler(event.javaClass).accept(event)
        return event.isCancelled
    }

    @CynosureInternal
    public fun registerClassNode(classNode: ClassNode) {

        val metadata = classNode.visibleAnnotations
            ?.find { it.desc.descriptorToClassName() == Metadata::class.qualifiedName }
            ?.mappedValues
            ?.let {
                Metadata(
                    kind = it["k"] as? Int,
                    metadataVersion = (it["mv"] as? List<Int>)?.toIntArray(),
                    data1 = (it["d1"] as? List<String>)?.toTypedArray(),
                    data2 = (it["d2"] as? List<String>)?.toTypedArray(),
                    extraString = it["xs"] as? String,
                    packageName = it["pn"] as? String,
                    extraInt = it["xi"] as? Int
                )
            }
            ?.let(KotlinClassMetadata::readLenient)

        val instanceFieldName: String?
        val instanceFieldOwner: String?

        if (metadata is KotlinClassMetadata.Class) {
            val klass = metadata.kmClass
            when (klass.kind) {
                ClassKind.OBJECT -> {
                    instanceFieldOwner = classNode.name
                    instanceFieldName = "INSTANCE"
                }
                ClassKind.COMPANION_OBJECT -> {
                    instanceFieldOwner = classNode.name.substringBeforeLast('\$')
                    instanceFieldName = classNode.name.substringAfterLast('\$')
                }
                else ->  {
                    instanceFieldOwner = null
                    instanceFieldName = null
                }
            }
        } else {
            instanceFieldName = null
            instanceFieldOwner = null
        }

        for (method in classNode.methods) {

            if (method.parameters != null && method.parameters.size != 1) continue

            val options = method.visibleAnnotations
                ?.find { it.desc.descriptorToClassName() == Subscription::class.java.canonicalName }
                ?.mappedValues
                ?: continue

            registerASMMethod(classNode.name, method.name, method.desc, options, instanceFieldName, instanceFieldOwner)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerASMMethod(className: String, methodName: String, methodDesc: String, options: Map<String, Any?>, instanceFieldName: String?, instanceFieldOwner: String?) {

        val priority = options["priority"] as? Int ?: 0
        val receiveCancelled = options["receiveCancelled"] as? Boolean ?: false

        val event = Class.forName(methodDesc.substringAfter("(").substringBefore(")").descriptorToClassName())
        if (!Event::class.java.isAssignableFrom(event)) return
        unregisterHandler(event)
        listeners.getOrPut(event as Class<Event>, ::EventListeners).add(EventListener(
            event, className, methodName, methodDesc,
            instanceFieldName?.let { InvokerType.VirtualWithOwner(it, instanceFieldOwner!!) } ?: InvokerType.Static,
            priority, receiveCancelled, "$className.$methodName$methodDesc"
        ))
    }

    @Suppress("UNCHECKED_CAST")
    internal fun registerMethod(method: Method, instance: Any? = null) {
        if (method.parameterCount != 1) return
        val options = method.getAnnotation(Subscription::class.java) ?: return

        val event = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(event)) return
        unregisterHandler(event)
        listeners.getOrPut(event as Class<Event>, ::EventListeners)
            .addMethodListener(event, method, instance, options.priority, options.receiveCancelled)
    }

    internal fun unregisterMethod(method: Method) {
        if (method.parameterCount != 1) return
        method.getAnnotation(Subscription::class.java) ?: return

        val event = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(event)) return
        unregisterHandler(event)
        listeners.values.forEach { it.removeListener(method) }
    }

    private fun <T : Event> getHandler(event: Class<T>): Consumer<Any> = handlers.getOrPut(event) {
        getEventClasses(event).flatMapTo(EventListeners()) { listeners[it] ?: emptyList() }.createHandler(event)
    }

    private fun unregisterHandler(clazz: Class<*>) = this.handlers.keys
        .filter { it.isAssignableFrom(clazz) }
        .forEach(this.handlers::remove)


    private fun getEventClasses(clazz: Class<*>): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()
        classes.add(clazz)

        var current = clazz
        while (current.superclass != null) {
            val superClass = current.superclass
            if (superClass == Any::class.java
                || superClass.getAnnotation(RootEventClass::class.java) != null) break
            classes.add(superClass)
            current = superClass
        }
        return classes
    }
}

@CynosureInternal
public val CynosureEventLogger: Logger = LoggerFactory.getLogger("Cynosure Event Registration")