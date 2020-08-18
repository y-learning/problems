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
                        "File $configFileName" +
                                " is not found in classpath")
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

    fun readProperty(name: String): Result<String> = properties.flatMap {
        Result.of { it.getProperty(name) }
    }
}

fun main() {
    val propertyReader = PropertyReader("/config.properties")

    propertyReader.readProperty("host")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })

    propertyReader.readProperty("name")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })

    propertyReader.readProperty("why")
        .forEach(
            onSuccess = { println(it) },
            onFailure = { println(it) })
}