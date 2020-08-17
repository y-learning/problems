package properties

import fn.result.Result

import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.util.Properties

class PropertyReader(configFileName: String) {
    private val properties: Result<Properties> = Result.of {
        MethodHandles.lookup().lookupClass()
            .getResourceAsStream(configFileName)
            .use { inputStream: InputStream? ->
                Properties().let {
                    it.load(inputStream)
                    it
                }
            }
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