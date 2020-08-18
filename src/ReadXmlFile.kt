import fn.collections.List
import fn.result.Result

import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder

import java.io.File
import java.io.IOException
import java.io.StringReader

fun getXmlFilePath(): Result<String> = Result.of { "./src/file.xml" }

fun getRootElementName(): Result<String> = Result.of { "staff" }

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

// TODO: 1. Fix this function, it takes two args of the same type.
fun readXmlFile(path: () -> Result<String>,
                rootName: () -> Result<String>,
                format: Pair<String, List<String>>,
                effect: (List<String>) -> Unit): () -> Unit = {
    path()
        .flatMap { _path: String ->
            rootName()
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

fun main() {
    val program = readXmlFile(::getXmlFilePath,
                              ::getRootElementName,
                              format, ::process)

    program()
}