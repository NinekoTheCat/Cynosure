package dev.mayaqq.cynosure.events.internal

import dev.mayaqq.cynosure.events.api.Subscription
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.function.Consumer

private val METADATA: String = Metadata::class.qualifiedName!!

private val SUBSCRIPTION: String = Subscription::class.qualifiedName!!


@Suppress("UNCHECKED_CAST")
internal fun generateASMEventListener(className: String, methodName: String, methodDesc: String, instanceFieldName: String?, instanceFieldOwnerName: String?): Consumer<Any> {

    val event = Type.getType(methodDesc.substringAfter('(').substringBefore(')'))
    val instanceFieldOwnerName = instanceFieldOwnerName ?: className
    val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

    cw.visit(
        V17, ACC_PUBLIC or ACC_FINAL,
        "dev/mayaqq/cynosure/events/internal/${className.replace('/', '_')}\$EventListener$$methodName$${event.hashCode().toString(16)}",
        null, "java/lang/Object", arrayOf("java/util/function/Consumer")
    )

    val init = cw.visitMethod(ACC_PRIVATE, "<init>", "()V", null, null)
    init.visitCode()
    init.visitVarInsn(ALOAD, 0)
    init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
    init.visitInsn(RETURN)
    init.visitMaxs(1, 1)
    init.visitEnd()

    val accept = cw.visitMethod(ACC_PUBLIC, "accept", "(Ljava/lang/Object;)V", null, null)
    accept.visitCode()
    if (instanceFieldName == null) {
        accept.visitVarInsn(ALOAD, 1)
        accept.visitTypeInsn(CHECKCAST, event.internalName)
        accept.visitMethodInsn(INVOKESTATIC, className,  methodName, methodDesc, false)
        accept.visitInsn(RETURN)
        accept.visitMaxs(1, 2)
    } else {
        accept.visitFieldInsn(GETSTATIC, instanceFieldOwnerName, instanceFieldName, "L$className;")
        accept.visitVarInsn(ALOAD, 1)
        accept.visitTypeInsn(CHECKCAST, event.internalName)
        accept.visitMethodInsn(INVOKEVIRTUAL, className, methodName, methodDesc, false)
        accept.visitInsn(RETURN)
        accept.visitMaxs(2, 2)
    }

    accept.visitEnd()
    cw.visitEnd()

    val lookup = MethodHandles.lookup().defineHiddenClass(cw.toByteArray(), true)
    val ctor = lookup.findConstructor(lookup.lookupClass(), MethodType.methodType(Nothing::class.javaPrimitiveType))
    return ctor.invoke() as Consumer<Any>
}