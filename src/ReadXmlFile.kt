import fn.collections.List
import fn.result.Result

import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder

import java.io.File
import java.io.IOException
import java.io.StringReader

data class FilePath private constructor(val value: Result<String>) {

    companion object {

        private fun isValidPath(filePath: String): Boolean =
            filePath.isNotEmpty()

        operator fun invoke(value: String): FilePath =
            FilePath(Result.of({ isValidPath(it) }, value,
                               "Invalid file path: $value"))
    }
}

data class ElementName private constructor(val value: Result<String>) {

    companion object {

        private fun isValidName(filePath: String): Boolean =
            filePath.isNotEmpty()

        operator fun invoke(value: String): ElementName =
            ElementName(Result.of({ isValidName(it) }, value,
                                  "Invalid element name: $value"))
    }
}

fun readFileToString(path: String): Result<String> =
    Result.of { File(path).readText() }

fun readDocument(xml: String, rootElementName: String): Result<List<Element>> =
    SAXBuilder().let { builder ->
        try {
            val doc = builder.build(StringReader(xml))
            val root = doc.rootElement
            Result(List(*root.getChildren(rootElementName).toTypedArray()))
        } catch (e: IOException) {
            Result.failure("Invalid root element name `$rootElementName`" +
                                   " or XML data $xml: ${e.message}")
        } catch (e: JDOMException) {
            Result.failure("Invalid root element name `$rootElementName`" +
                                   " or XML data $xml: ${e.message}")
        } catch (e: Exception) {
            Result.failure("Unexpected error while reading xml data " +
                                   "$xml: ${e.message}")
        }
    }

val format: Pair<String, List<String>> = Pair("""
    First Name : %s
    Last Name : %s
    Email : %s
    Salary : %s
    """, List("firstName", "lastName", "email", "salary"))

// TODO: 2. Fix the null result when using a wrong element name
// TODO: 3. Fix the exception thrown when you forget one of the element names
//  in the list
fun processElement(element: Element,
                   format: Pair<String, List<String>>): String =
    format.let { (format, listChildren): Pair<String, List<String>> ->
        val parameters = listChildren.map { element.getChildText(it) }
        String.format(format, *parameters.toArrayList().toArray())
    }

fun toStringList(elements: List<Element>,
                 format: Pair<String, List<String>>): List<String> =
    elements.map { processElement(it, format) }

fun <A> process(list: List<A>): Unit = list.forEach(::println)

fun readXmlFile(path: () -> FilePath,
                rootName: () -> ElementName,
                format: Pair<String, List<String>>,
                effect: (List<String>) -> Unit): () -> Unit = {
    path().value
        .flatMap { _path: String ->
            rootName().value
                .flatMap { _rootName: String ->
                    readFileToString(_path)
                        .flatMap { xmlDoc: String ->
                            readDocument(xmlDoc, _rootName)
                                .map { list: List<Element> ->
                                    toStringList(list, format)
                                }
                        }
                }
        }.forEach(onSuccess = effect, onFailure = { it.printStackTrace() })
}

fun getXmlFilePath(): FilePath = FilePath("./src/file.xml")

fun getRootElementName(): ElementName = ElementName("staff")

fun main() {
    val program = readXmlFile(::getXmlFilePath,
                              ::getRootElementName,
                              format, ::process)

    program()
}