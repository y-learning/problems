package properties

import fn.result.Result

import java.io.InputStream
import java.lang.invoke.MethodHandles
import java.util.Properties

class PropertyReader(configFileName: String) {
    internal val properties: Result<Properties> = Result.of {
        MethodHandles.lookup().lookupClass()
            .getResourceAsStream(configFileName)
            .use { inputStream: InputStream? ->
                Properties().let {
                    it.load(inputStream)
                    it
                }
            }
    }
}

fun main() {
    PropertyReader("/config.properties")
        .properties.forEach(
            onSuccess = { println(it) },
            onFailure = { println("Failure: $it") })
}