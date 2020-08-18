package properties

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
}

data class Person(val id: Int, val firstName: String, val lastName: String)

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
}