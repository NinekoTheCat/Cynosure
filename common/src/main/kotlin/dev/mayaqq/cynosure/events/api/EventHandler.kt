package dev.mayaqq.cynosure.events.api

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.io.File
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Consumer

internal fun EventListeners.createHandler(event: Class<out Event>): Consumer<Any> {
    val cw = ClassWriter(0)
    val eventClass = event.canonicalName.replace('.', '/')

    val thisClass = "dev/mayaqq/cynosure/events/api/EventHandler$${eventClass.replace('/', '$')}"
    cw.visit(
        V17, ACC_PUBLIC or ACC_FINAL, thisClass,
        null, "java/lang/Object", arrayOf("java/util/function/Consumer")
    )

    val instances = map(EventListener::invokerType)
        .filterIsInstance<InvokerType.VirtualWithInstance>()
        .toCollection(ObjectLinkedOpenHashSet())

    val init = cw.visitMethod(
        ACC_PRIVATE, "<init>",
        "(${instances.joinToString("") { Type.getDescriptor(it.clazz) }})V",
        null, null
    )
    init.visitCode()
    init.visitVarInsn(ALOAD, 0)
    init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

    instances.forEachIndexed { index, instance ->
        val desc = Type.getDescriptor(instance.clazz)
        cw.visitField(ACC_PRIVATE or ACC_FINAL, "instance$$index", desc, null, null).visitEnd()
        init.visitVarInsn(ALOAD, 0)
        init.visitVarInsn(ALOAD, index + 1)
        init.visitFieldInsn(PUTFIELD, thisClass, "instance$$index", desc)
    }

    init.visitInsn(RETURN)
    init.visitMaxs(2, instances.size + 1)
    init.visitEnd()


    val accept = cw.visitMethod(ACC_PUBLIC, "accept", "(Ljava/lang/Object;)V", null, null)
    accept.visitCode()
    accept.visitVarInsn(ALOAD, 1)
    accept.visitTypeInsn(CHECKCAST, eventClass)
    accept.visitVarInsn(ASTORE, 1)

    var maxMarker = false
    var nextLabel: Label? = null

    forEach { listener ->
        nextLabel?.let {
            accept.visitLabel(it)
            accept.visitFrame(F_FULL, 2, arrayOf<Any>(thisClass, eventClass), 0, emptyArray<Any>())
        }

        val lbl = Label()
        if (!listener.receiveCancelled) {
            accept.visitVarInsn(ALOAD, 1)
            accept.visitMethodInsn(INVOKEVIRTUAL, eventClass, "isCancelled", "()Z", false)
            accept.visitFrame(F_FULL, 2, arrayOf<Any>(thisClass, eventClass), 1, arrayOf<Any>(INTEGER))
            accept.visitJumpInsn(IFNE, lbl)
        }
        nextLabel = lbl

        when (val invoker = listener.invokerType) {
            InvokerType.Static -> {
                accept.visitVarInsn(ALOAD, 1)
                if (listener.event != event) accept.visitTypeInsn(CHECKCAST, Type.getDescriptor(event))
                accept.visitMethodInsn(INVOKESTATIC, listener.className, listener.methodName, listener.methodDesc, false)
            }
            is InvokerType.VirtualWithInstance -> {
                accept.visitVarInsn(ALOAD, 0)
                accept.visitFieldInsn(GETFIELD, thisClass, "instance$${instances.indexOf(invoker)}", "L${listener.className};")
                accept.visitVarInsn(ALOAD, 1)
                if (listener.event != event) accept.visitTypeInsn(CHECKCAST, Type.getDescriptor(event))
                accept.visitMethodInsn(invoker.opcode, listener.className, listener.methodName, listener.methodDesc, invoker.clazz.isInterface)
                maxMarker = true
            }
            is InvokerType.VirtualWithOwner -> {
                accept.visitFieldInsn(GETSTATIC, invoker.ownerClassName, invoker.ownerFieldName, "L${listener.className};")
                accept.visitVarInsn(ALOAD, 1)
                if (listener.event != event) accept.visitTypeInsn(CHECKCAST, Type.getDescriptor(event))
                accept.visitMethodInsn(invoker.opcode, listener.className, listener.methodName, listener.methodDesc, false)
                maxMarker = true
            }
        }
    }

    nextLabel?.let {
        accept.visitLabel(it)
    }

    accept.visitFrame(F_FULL, 2, arrayOf<Any>(thisClass, eventClass), 0, emptyArray<Any>())
    accept.visitInsn(RETURN)
    accept.visitMaxs(if (maxMarker) 3 else 2, 2)
    accept.visitEnd()
    cw.visitEnd()

    val lookup = MethodHandles.lookup().defineHiddenClass(cw.toByteArray(), true)
    val ctor = lookup.findConstructor(
        lookup.lookupClass(),
        MethodType.methodType(Nothing::class.javaPrimitiveType, instances.map(InvokerType.VirtualWithInstance::clazz))
    )

    return ctor.invokeWithArguments(instances.map(InvokerType.VirtualWithInstance::instance)) as Consumer<Any>
}