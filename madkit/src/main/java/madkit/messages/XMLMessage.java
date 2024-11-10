
package madkit.messages;

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

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import madkit.kernel.Message;

/** A message that carries an XML document.

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
	protected Document docContent = null;
	/** The xml content as a String (null if the content is defined as a Document) */
	protected String strcontent = null;

	/** Setup an XMLMessage with the xml document setup as a string. The string is not validated at construction
	 * @param s A valid (i.e. parseable) text XML document
	 */
	public XMLMessage(String s)
	{
		strcontent=s;
		docContent=null;
	}

	/** Setup an XMLMessage with the xml document setup as a Document
	 * @param d A well-formed DOM object
	 */
	public XMLMessage(Document d)
	{
		strcontent=null;
		docContent=d;
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
		DOMSource domSource = new DOMSource(docContent);
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
		if (docContent!=null)
			return docContent;
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
		return docContent.toString();
	}
}
