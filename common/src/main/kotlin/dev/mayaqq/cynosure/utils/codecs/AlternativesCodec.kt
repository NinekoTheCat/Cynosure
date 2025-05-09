package dev.mayaqq.cynosure.utils.codecs

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import java.util.stream.Stream

public class AlternativesCodec<A>(
    private val codecs: List<Codec<A>>
) : Codec<A> {

    override fun <T : Any> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        var result = DataResult.error<T> { "No codecs found" }
        for (codec in codecs) {
            result = result.apply2stable({r1, _ -> r1},  codec.encode(input, ops, prefix))
            if (result.result().isPresent) break
        }
        return result
    }


    override fun <T : Any> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        var result = DataResult.error<A> { "No codecs found" }
        val errors: Stream.Builder<T> = Stream.builder()

        for (codec in codecs) {
            val r = codec.decode(ops, input)
            result = result.apply2stable({ r1, _ -> r1 }, r)
            if (r.error().isPresent) errors.accept(input) else break
        }

        return result.map { Pair.of(it, ops.createList(errors.build())) }
    }
}