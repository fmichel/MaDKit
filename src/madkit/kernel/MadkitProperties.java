/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import madkit.i18n.ErrorMessages;
import madkit.util.XMLUtilities;

/**
 * The properties object used within MaDKit.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.91
 */
public class MadkitProperties extends Properties {

    /**
     * 
     */
    private static final long serialVersionUID = 1964226720362899440L;

    private static final Logger logger = Logger.getLogger(MadkitProperties.class.getName());

    /**
     * Shortcut for System.getProperty("javawebstart.version") != null;
     */
    public static final boolean JAVAWS_IS_ON = System.getProperty("javawebstart.version") != null;

    
    
    MadkitProperties() {
    }
 
    /**
     * Loads properties from an XML file.
     * 
     * @param filePath
     *            can be absolute or relative
     * @throws IOException
     */
    public void loadPropertiesFromMaDKitXML(final String filePath) throws IOException {
	try (InputStream is = getInputStream(filePath)) {
	    NodeList madkitOptionNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement()
		    .getElementsByTagName(XMLUtilities.MADKIT_PROPERTIES);
	    for (int i = 0; i < madkitOptionNodes.getLength(); i++) {
		org.w3c.dom.NamedNodeMap options = madkitOptionNodes.item(i).getAttributes();
		for (int j = 0; j < options.getLength(); j++) {
		    put(options.item(j).getNodeName(), options.item(j).getNodeValue());
		}
	    }
	    logger.fine(() -> "** Config file " + filePath + " successfully loaded **\n");
	}
	catch(SAXException | ParserConfigurationException e) {
	    logger.log(Level.WARNING, ErrorMessages.CANT_LOAD + "configuration " + filePath, e);
	}
    }

    /**
     * Loads properties from a regular properties formatted file.
     * 
     * @param pathname
     * @throws IOException
     */
    public void loadPropertiesFromPropertiesFile(final String pathname) throws IOException {
	try (InputStream is = getInputStream(pathname)) {
	    load(is);
	}
    }

    /**
     * Loads properties from a properties file (classic or XML).
     * 
     * @param pathname
     * @throws IOException
     */
    public void loadPropertiesFromFile(final String pathname) throws IOException {
	if (pathname.endsWith(".xml")) {
	    loadPropertiesFromMaDKitXML(pathname);
	}
	else {
	    loadPropertiesFromPropertiesFile(pathname);
	}
    }

    /**
     * Return an <code>InputStream</code> on a file. pathname could be relative to (1) the actual MaDKit class path, which
     * is preferable considering jar export, or (2) the user.dir, or it could be an absolute path. The returned input stream
     * should be closed once done.
     * 
     * @param pathname
     *            A pathname string If the <code>pathname</code> argument is <code>null</code>
     * @return an <code>InputStream</code> by opening a connection to an actual file, or <code>null</code> if the file is
     *         not found.
     * @throws NullPointerException
     * @throws IOException
     */
    public static InputStream getInputStream(final String pathname) throws IOException {
	final File f = new File(pathname);
	InputStream is = f.exists() ? new FileInputStream(f) : MadkitClassLoader.getLoader().getResourceAsStream(pathname);// NOSONAR MDK CL must not be closed
	if (is == null)
	    throw new FileNotFoundException(pathname);
	return is;
    }
    
    @Override
    public synchronized String toString() {
	String message = "MaDKit current configuration is\n\n";
	message += "\t--- MaDKit regular options ---\n";
	for (String option : Madkit.DEFAULT_CONFIG.stringPropertyNames()) {
	    message += "\t" + String.format("%-" + 30 + "s", option) + getProperty(option) + "\n";
	}
	Set<Object> tmp = new HashSet<>(keySet());
	tmp.removeAll(Madkit.DEFAULT_CONFIG.keySet());
	if (tmp.size() > 0) {
	    message += "\n\t--- Additional non MaDKit options ---\n";
	    for (Object o : tmp)
		message += "\t" + String.format("%-" + 25 + "s", o) + get(o) + "\n";
	}
        return message;
    }
    
}
