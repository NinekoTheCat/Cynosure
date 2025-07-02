package dev.mayaqq.cynosure.events.api

import dev.mayaqq.cynosure.utils.asm.descriptorToClassName
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.function.Consumer
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
        //listeners.values.forEach { it.removeListener(callback) }
    }

    public fun post(
        event: Event,
        context: Any? = null,
        onError: ((Throwable) -> Unit)? = null
    ): Boolean = TODO()

    @Suppress("UNCHECKED_CAST")
    internal fun registerASMMethod(className: String, methodName: String, methodDesc: String, options: Map<String, Any?>, instanceFieldName: String?, instanceFieldOwner: String?) {

        val priority = options["priority"] as? Int ?: 0
        val receiveCancelled = options["receiveCancelled"] as? Boolean ?: false

        val event = Class.forName(methodDesc.substringAfter("(").substringBefore(")").descriptorToClassName())
        if (!Event::class.java.isAssignableFrom(event)) return
        unregisterHandler(event)
        listeners.getOrPut(event as Class<Event>, ::EventListeners)
            .addASMListener(className, methodName, methodDesc, instanceFieldName, instanceFieldOwner, event as Class<out Event>, priority, receiveCancelled)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun registerMethod(method: Method, instance: Any? = null) {
        if (method.parameterCount != 1) return
        val options = method.getAnnotation(Subscription::class.java) ?: return

        val event = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(event)) return
        unregisterHandler(event)
        listeners.getOrPut(event as Class<Event>, ::EventListeners)
            .addMethodListener(method, instance, options.priority, options.receiveCancelled)
    }

    internal fun unregisterMethod(method: Method) {
        if (method.parameterCount != 1) return
        method.getAnnotation(Subscription::class.java) ?: return

        val event = method.parameterTypes[0]
        if (!Event::class.java.isAssignableFrom(event)) return
        unregisterHandler(event)
        listeners.values.forEach { it.removeListener(method) }
    }

    private fun <T : Event> getHandler(event: Class<T>): Consumer<Any> = handlers.getOrPut(event) { listeners[event]!!.compile() }

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