import fn.collections.List
import fn.collections.sequence
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

fun <T> readXmlFile(path: () -> FilePath,
                    rootName: () -> ElementName,
                    presenter: (Element) -> Result<T>,
                    effect: (List<T>) -> Unit): () -> Unit = {
    path().value
        .flatMap { _path: String ->
            rootName().value
                .flatMap { _rootName: String ->
                    readFileToString(_path)
                        .flatMap { xmlDoc: String ->
                            readDocument(xmlDoc, _rootName)
                                .flatMap { elements: List<Element> ->
                                    sequence(elements.map(presenter))
                                }
                        }
                }
        }.forEach(onSuccess = effect, onFailure = { it.printStackTrace() })
}

private fun getChildText(element: Element, name: String): String =
    element.getChildText(name)
        ?: "The element name `$name` is not a child of ${element.name}"

val processElement: (List<String>) -> (String) -> (Element) -> Result<String> =
    { elementNames: List<String> ->
        { format: String ->
            { element: Element ->
                try {
                    Result(String.format(format, *elementNames.map {
                        getChildText(element, it)
                    }.toArrayList()
                        .toArray()))
                } catch (e: java.util.MissingFormatArgumentException) {
                    Result.failure("Error while formatting an element." +
                                           " There is probably a missing" +
                                           " element name in the list of " +
                                           "element names $elementNames")
                } catch (e: Exception) {
                    Result.failure("Error while formatting an element. " +
                                           "${e.message}")
                }
            }
        }
    }

fun main() {
    val format = """First Name : %s
    Last Name : %s
    Email : %s
    Salary : %s
    """

    val elementNames = List("firstName", "lastName", "email", "salary")

    fun getXmlFilePath(): FilePath = FilePath("./src/file.xml")

    fun getRootElementName(): ElementName = ElementName("staff")

    fun <A> printList(list: List<A>): Unit = list.forEach(::println)

    val program = readXmlFile(::getXmlFilePath,
                              ::getRootElementName,
                              processElement(elementNames)(format),
                              ::printList)

    try {
        program()
    } catch (e: Exception) {
        println("A ${e.javaClass.name} occurred: ${e.message}")
    }

    print("Finished.")
}
