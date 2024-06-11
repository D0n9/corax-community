/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.feysh.corax.config.general.utils

import mu.KotlinLogging
import org.sonarsource.analyzer.commons.xml.XmlFile
import org.sonarsource.analyzer.commons.xml.XmlTextRange
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.SAXException
import java.io.IOException
import java.nio.file.Path
import kotlin.jvm.optionals.getOrNull

object PositionalXMLReader {
    val logger = KotlinLogging.logger {  }
//    const val LINE_NUMBER_KEY_NAME = "lineNumber"
//    const val COLUMN_NUMBER_KEY_NAME = "columnNumber"

    @Throws(IOException::class, SAXException::class)
    fun readXMLUnsafe(file: Path): Document? {
        return try {
            readXML(file)
        } catch (e: Exception) {
            logger.warn { "Exception while reading xml document: $file. e: ${e.message}" }
            logger.debug(e) { "Exception while reading xml document: $file. e: ${e.message}" }
            null
        }
    }

    @Throws(SAXException::class)
    fun readXMLUnsafe(xml: String, path: String): Document? {
        return try {
            readXML(xml)
        } catch (e: Exception) {
            logger.warn { "Exception while reading xml document: $path. e: ${e.message}" }
            logger.debug(e) { "Exception while reading xml document: $path. e: ${e.message}" }
            null
        }
    }

    @Throws(IOException::class, SAXException::class)
    fun readXML(file: Path): Document {
        return SonarFactory.readPositionalXML(file).namespaceUnawareDocument
    }

    @Throws(SAXException::class)
    fun readXML(xml: String): Document {
        return SonarFactory.readPositionalXML(xml).namespaceUnawareDocument
    }

//    fun readXML(`is`: InputStream?): Document {
//        val doc: Document
//        val parser: SAXParser
//        try {
//            val factory = SAX_FACTORY
//            parser = factory.newSAXParser()
//            val dbf = DocumentBuilderFactory.newInstance()
//            val docBuilder = dbf.newDocumentBuilder()
//            doc = docBuilder.newDocument()
//        } catch (e: ParserConfigurationException) {
//            throw RuntimeException(
//                "Can't create SAX parser / DOM builder.", e
//            )
//        }
//        val elementStack = Stack<Element>()
//        val textBuffer = StringBuilder()
//        val handler: DefaultHandler = object : DefaultHandler() {
//            private var locator: Locator? = null
//            override fun setDocumentLocator(locator: Locator) {
//                this.locator = locator // Save the locator, so that it can be
//                // used later for line tracking when
//                // traversing nodes.
//            }
//
//            override fun startElement(
//                uri: String, localName: String,
//                qName: String, attributes: Attributes
//            ) {
//                addTextIfNeeded()
//                val el = doc.createElement(qName)
//                for (i in 0 until attributes.length) {
//                    el.setAttribute(
//                        attributes.getQName(i),
//                        attributes.getValue(i)
//                    )
//                }
//                locator?.let {
//                    el.setUserData(LINE_NUMBER_KEY_NAME, it.lineNumber, null)
//                    el.setUserData(COLUMN_NUMBER_KEY_NAME, it.columnNumber, null)
//                }
//                elementStack.push(el)
//            }
//
//            override fun endElement(
//                uri: String, localName: String,
//                qName: String
//            ) {
//                addTextIfNeeded()
//                val closedEl = elementStack.pop()
//                if (elementStack.isEmpty()) { // Is this the root element?
//                    doc.appendChild(closedEl)
//                } else {
//                    val parentEl = elementStack.peek()
//                    parentEl.appendChild(closedEl)
//                }
//            }
//
//            override fun characters(
//                ch: CharArray, start: Int,
//                length: Int
//            ) {
//                textBuffer.append(ch, start, length)
//            }
//
//            // Outputs text accumulated under the current node
//            private fun addTextIfNeeded() {
//                if (textBuffer.isNotEmpty()) {
//                    val el = elementStack.peek()
//                    val textNode: Node = doc.createTextNode(
//                        textBuffer
//                            .toString()
//                    )
//                    el.appendChild(textNode)
//                    textBuffer.delete(0, textBuffer.length)
//                }
//            }
//        }
//        parser.parse(`is`, handler)
//        return doc
//    }
}

val Node.range: XmlTextRange? get() = XmlFile.getRange(this, XmlFile.Location.NODE).getOrNull()
val Node.lineNumber: Int get() = range?.startLine ?: -1
val Node.columnNumber: Int get() = range?.startColumn ?: -1
val Node.endLineNumber: Int get() = range?.endLine ?: -1
val Node.endColumnNumber: Int get() = range?.endColumn ?: -1
