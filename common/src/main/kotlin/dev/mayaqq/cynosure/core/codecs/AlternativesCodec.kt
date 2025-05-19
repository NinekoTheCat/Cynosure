package dev.mayaqq.cynosure.core.codecs

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import java.util.stream.Stream

public class AlternativesCodec<A>(
    private val codecs: List<Codec<A>>
) : Codec<A> {

    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        var result = DataResult.success(Unit)
        var output: T? = null
        for (codec in codecs) {
            val r = codec.encode(input, ops, prefix)
            result = result.apply2stable(fun(r1, _) = r1,  r)
            if (r.result().isPresent) {
                output = r.result().get()
                break
            }
        }
        return if (output != null) result.map { output } else DataResult.error { "No codecs found" }
    }


    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        var result = DataResult.error<A> { "No codecs found" }
        val errors: Stream.Builder<T> = Stream.builder()

        for (codec in codecs) {
            val r = codec.decode(ops, input)
            result = result.apply2stable({ r1, _ -> r1 }, r)
            if (r.error().isPresent) errors.accept(input) else break
        }

        return result.map { Pair.of(it, input) }
    }
}