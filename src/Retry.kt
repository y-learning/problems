import fn.result.Result
import java.lang.IllegalStateException
import java.util.Random

/**
 * @param times The number of tries before the function ends.
 * @param delay THe period of time between each try in milliseconds.
 */
fun <A, B> retry(times: Int, delay: Long = 10, f: (A) -> B): (A) -> Result<B> {
    fun retry(a: A, result: Result<B>, e: Result<B>, count: Int): Result<B> =
        result.orElse {
            when (count) {
                0 -> e
                else -> {
                    Thread.sleep(delay)
                    retry(a, Result.of { f(a) }, result, count - 1)
                }
            }
        }

    return { a: A -> retry(a, Result.of { f(a) }, Result(), times - 1) }
}

fun show(message: String) = Random().nextInt(10).let {
    when {
        it < 8 -> throw IllegalStateException("Failure !!!")
        else -> println("Show: $message")
    }
}

fun main() {
    retry(3, 20, ::show)("Hello, Kotlin!")
        .forEach(onFailure = { println(it.message) })
}