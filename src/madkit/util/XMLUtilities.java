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
package madkit.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitProperties;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

/**
 * XML shortcuts as static methods
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 */
public class XMLUtilities {

    /**
     * return the DOM from an xml file.
     * 
     * @param xmlFile
     * @return the DOM from an xml file or <code>null</code> if not found or invalid
     */
    public static Document getDOM(String xmlFile) throws SAXException, IOException, ParserConfigurationException {
	try (final InputStream is = MadkitProperties.getInputStream(xmlFile)) {// for closing the stream
	    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
	}
    }

    public static String nodeToString(Node node) {
	String s = node.getNodeName() + "( ";
	NamedNodeMap map = node.getAttributes();
	if (map != null) {
	    for (int i = 0; i < map.getLength(); i++) {
		s += map.item(i).getNodeName() + "=" + map.item(i).getNodeValue() + " ";
	    }
	}
	return s += " )";
    }

    /**
     * Valid attribute of an agent node : {@value} in a MDK xml config file
     */
    public static final String NB_OF_INSTANCES = "nbOfInstances";

    /**
     * Valid attribute of an agent node : {@value} in a MDK xml config file
     */
    public static final String GUI = "GUI";

    /**
     * The name of the child node of {@link #AGENT} containing attributes that have to be set on the agent: {@value}
     */
    public static final String ATTRIBUTES = "Attributes";

    /**
     * Valid attribute of an agent node determining the agent class to launch, value is {@value}
     */
    public static final String CLASS = "class";

    /**
     * The name of a node containing a launch configuration: {@value}
     */
    public static final String AGENT = "Agent";

    /**
     * The name of a node containing properties for MaDKit : {@value}
     * 
     * @see Option
     * @see LevelOption
     * @see BooleanOption
     */
    public static final String MADKIT_PROPERTIES = "MadkitProperties";

    /**
     * Valid attribute of an agent node determining the agent log level to be set, the name of the attribute is {@value}
     */
    public static final String LOG_LEVEL = "logLevel";

    /**
     * Name of the root node of MaDKit xml fles, value is {@value}
     */
    public static final String MDK = "MDK";

    /**
     * Valid attribute of an agent node determining if the launch should be done using the bucket mode. For instance :
     * 
     * <pre>
     * {@code
     *    	<Agent class="madkit.xml.XMLBenchTestAgent" nbOfInstances="100000" bucketMode="true">
     * 		<Attributes speed="3" i="5" s="blabla"/>
     * </Agent>
     * }
     * 
     * 
     * If the {@link #BUCKET_MODE_ROLE} tag is used, bucketMode is automatically 
     * added and set to <code>true</code>.
     * </pre>
     * 
     * see {@link AbstractAgent#launchAgentBucket(java.util.List, int, String...)}
     */
    public static final String BUCKET_MODE = "bucketMode";

    /**
     * The name of the child node of {@link #AGENT} containing the roles which should be set when using the bucket mode.
     * e.g.
     * 
     * <pre>
     * {@code
     *    	<Agent class="madkit.xml.XMLBenchTestAgent" nbOfInstances="100000" bucketMode="true">
     *    
     * 		<Attributes speed="3" i="5" s="blabla"/>
     * 
     * 		<bucketModeRole community="Tcommunity" group="Tgroup" role="Trole"/>
     * </Agent>
     * }
     * </pre>
     */
    public static final String BUCKET_MODE_ROLE = "bucketModeRole";

}
