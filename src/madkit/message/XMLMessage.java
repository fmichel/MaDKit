/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.message;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import madkit.kernel.Message;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** A message that carries a XML document.

    You can set the document either through a string or a pre-parsed
    DOM tree. This class is optimized in the sense that it will change
    representations only if needed (i.e. constructed with a String and
    used through the getDocument() accessor).

* @author Oliver Gutknecht
* @author Jacques Ferber
* @author Fabien Michel
* @version 5.0
* @since MaDKit 1.0
*
*/
public class XMLMessage extends Message
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1266801281341621595L;
	/** The xml content as a Document (null if the content is defined as a string) */
	protected Document doccontent = null;
	/** The xml content as a String (null if the content is defined as a Document) */
	protected String   strcontent = null;

	/** Setup a XMLMessage with the xml document setup as a string. The string is not validated at construction
	 * @param s A valid (i.e. parseable) text XML document
	 */
	public XMLMessage(String s)
	{
		strcontent=s;
		doccontent=null;
	}

	/** Setup a XMLMessage with the xml document setup as a Document
	 * @param d A well-formed DOM object
	 */
	public XMLMessage(Document d)
	{
		strcontent=null;
		doccontent=d;
	}

	/** Returns the XMLMessage content as a string. If the String
	 * constructor was called, this accessor just returns the initial
	 * string. If the Document constructor was used, it transforms it to a String using JDOM
	 * @return A stringified version of the message content
	 */
	public String getString()
	{
		if (strcontent!=null)
			return strcontent;
		// Serialization through Transform.
		DOMSource domSource = new DOMSource(doccontent);
		StreamResult streamResult = new StreamResult(new StringWriter());
		try {
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			serializer.transform(domSource, streamResult); 		    
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return streamResult.getWriter().toString();
	}

	/** Returns the XMLMessage content as a document. If the Document
	 * constructor was called, this accessor just returns the initial
	 * document. If the String constructor was used, it uses JDOM
	 * to parse it into a DOM tree.
	 * @return A DOM object for the message content
	 */
	public Document getDocument()
	{
		if (doccontent!=null)
			return doccontent;
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse((new InputSource(new StringReader(strcontent))));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** This method returns a string for the XML document set in this message.
	 * Warning, if the document is directly stored as a Document and not a string,
	 * this will just call the toString() method on the Document object.
	 * @return A stringified version of the document
	 */
	public String toString()
	{
		if (strcontent!=null)
			return strcontent;
		return doccontent.toString();
	}
}
