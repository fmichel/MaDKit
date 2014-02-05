/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import madkit.i18n.ErrorMessages;
import madkit.kernel.MadkitClassLoader;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * The properties object used within MaDKit.
 * 
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 * 
 */
public class MadkitProperties extends Properties {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1964226720362899440L;
	
	private final static Logger	logger = Logger.getLogger(MadkitProperties.class.getName());

	/**
	 * Loads properties from an XML file.
	 * 
	 * @param pathname
	 * @throws IOException
	 */
	public void loadPropertiesFromMaDKitXML(final String pathname) throws IOException{
		try (InputStream is = getInputStream(pathname)) {
					NodeList madkitOptionNodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement()
							.getElementsByTagName(XMLUtilities.MADKIT_PROPERTIES);
					for (int i = 0; i < madkitOptionNodes.getLength(); i++) {
						org.w3c.dom.NamedNodeMap options = madkitOptionNodes.item(i).getAttributes();
						for (int j = 0; j < options.getLength(); j++) {
							put(options.item(j).getNodeName(), options.item(j).getNodeValue());
						}
					}
					logLoadSuccess(pathname);
		} catch (SAXException | ParserConfigurationException e) {
			logLoadFailure(pathname, e);
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
		} catch (IOException e) {
			throw e;
		}
	}
	
	/**
	 * Loads properties from a properties file (classic or XML).
	 * 
	 * @param pathname
	 * @throws IOException
	 */
	public void loadPropertiesFromFile(final String pathname) throws IOException {
		if(pathname.endsWith(".xml")){
			loadPropertiesFromMaDKitXML(pathname);
		}
		else{
			loadPropertiesFromPropertiesFile(pathname);
		}
	}
	
	/**
	 * Return an <code>InputStream</code> on a file.
	 * pathname could be relative to (1) the actual MaDKit class path, which is
	 * preferable considering jar export, or (2) the user.dir, or it could be 
	 * an absolute path. The returned input stream should be closed once done.
	 * 
     * @param   pathname  A pathname string
     * @throws  NullPointerException
     *          If the <code>pathname</code> argument is <code>null</code>
	 * @return an <code>InputStream</code> by
     * opening a connection to an actual file, or <code>null</code>
     * if the file is not found.
     * 
	 * @throws IOException
	 */
	public static InputStream getInputStream(final String pathname) throws IOException{
		final File f = new File(pathname);
		@SuppressWarnings("resource") //closed when used
		InputStream is = f.exists() ? new FileInputStream(f) : MadkitClassLoader.getLoader().getResourceAsStream(pathname);
		if(is == null)
			throw new FileNotFoundException(pathname);
		return is;
	}
	
	/**
	 * @param fileName
	 * @param e
	 */
	static private void logLoadFailure(final String fileName, Exception e) {
		logger.log(Level.WARNING, ErrorMessages.CANT_LOAD + "configuration " + fileName, e);
	}
	
	/**
	 * @param fileName
	 */
	static private void logLoadSuccess(final String fileName) {
		logger.fine("** Config file " + fileName + " successfully loaded **\n");
	}

//	static private boolean checkInputStream(InputStream is,String fileName){
//		if(is == null){
//			logger.warning(ErrorMessages.CANT_FIND + "configuration " + fileName);
//			return false;
//		}
//		return true;
//	}


}
