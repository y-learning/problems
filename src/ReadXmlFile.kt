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

const val format =
    """
    First Name : %s
    Last Name : %s
    Email : %s
    Salary : %s
    """

fun processElement(element: Element, format: String): String =
    String.format(format, element.getChildText("firstName"),
        element.getChildText("lastName"),
        element.getChildText("email"),
        element.getChildText("salary"))

fun toStringList(elements: List<Element>, format: String): List<String> =
    elements.map { processElement(it, format) }

fun <A> process(list: List<A>): Unit = list.forEach(::println)

fun main() {
    getXmlFilePath().flatMap { path: String ->
        getRootElementName().flatMap { rootElementName ->
            readFileToString(path).flatMap { xmlFile: String ->
                readDocument(xmlFile, rootElementName)
                    .map { list: List<Element> ->
                        toStringList(list, format)
                    }
            }
        }
    }.forEach(onSuccess = { process(it) },
        onFailure = { it.printStackTrace() })
}