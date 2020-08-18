package properties

import fn.collections.List
import fn.collections.List.Companion.fromSeparated
import fn.result.Result

import java.io.IOException
import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.util.Properties

class PropertyReader(configFileName: String) {
    private val properties: Result<Properties> = try {
        MethodHandles.lookup().lookupClass()
            .getResourceAsStream(configFileName)
            .use { inputStream: InputStream? ->
                when (inputStream) {
                    null -> Result.failure(
                        "File $configFileName  is not found in classpath")
                    else -> Properties().let {
                        it.load(inputStream)
                        Result(it)
                    }
                }
            }
    } catch (e: IOException) {
        Result.failure("IOException reading classpath resource $configFileName")
    } catch (e: Exception) {
        Result.failure(
            "Exception: ${e.message} " +
                    "while reading classpath resource $configFileName")
    }

    fun readAsString(name: String): Result<String> = properties.flatMap {
        Result.of {
            it.getProperty(name)
        }.mapFailure("Property `$name` not found")
    }

    fun readAsInt(name: String): Result<Int> =
        readAsString(name).flatMap {
            Result.of(
                { it.toInt() },
                "Invalid value while parsing property `$name` to Int: `$it`")
        }

    fun <T> readAsList(name: String, f: (String) -> T): Result<List<T>> =
        readAsString(name).flatMap {
            Result.of(
                { fromSeparated(it, ",").map(f) },
                "Invalid value while parsing property `$name` to List: `$it`")
        }

    fun readAsListOfInt(name: String): Result<List<Int>> =
        readAsList(name, String::toInt)

    fun readAsListOfDouble(name: String): Result<List<Double>> =
        readAsList(name, String::toDouble)

    fun readAsListOfBoolean(name: String): Result<List<Boolean>> =
        readAsList(name, String::toBoolean)

    fun <T> readAsType(name: String, f: (String) -> Result<T>): Result<T> =
        readAsString(name).flatMap {
            try {
                f(it)
            } catch (e: Exception) {
                Result.failure<T>(
                    "Invalid value `$it`" +
                            " while parsing property `$name`")
            }
        }

    inline
    fun <reified T : Enum<T>> readAsEnum(name: String,
                                         enumClass: Class<T>): Result<T> =
        readAsType(name) { s: String ->
            Result.of(
                { enumValueOf<T>(s) }, "Error parsing property `$name`: " +
                        "value `$s` can't be parsed tp ${enumClass.name}")
        }
}

data class Person(val id: Int, val firstName: String, val lastName: String)

enum class Type { SERIAL, PARALLEL }

fun main() {
    val propertyReader = PropertyReader("/config.properties")

    propertyReader.readAsString("host")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })

    propertyReader.readAsString("name")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })

    propertyReader.readAsString("why")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })

    val person = propertyReader.readAsInt("id")
        .flatMap { id: Int ->
            propertyReader.readAsString("firstName")
                .flatMap { firstName: String ->
                    propertyReader.readAsString("lastName")
                        .map { lastName ->
                            Person(id, firstName, lastName)
                        }
                }
        }

    person.forEach(onSuccess = { println(it) }, onFailure = { println(it) })

    propertyReader.readAsListOfInt("list")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })

    propertyReader.readAsEnum("type", Type::class.java)
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })
}