/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel;

import java.io.*;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.distrimind.madkit.gui.AgentFrame;
import com.distrimind.madkit.gui.ConsoleAgent;
import com.distrimind.madkit.gui.MDKDesktopFrame;
import com.distrimind.madkit.i18n.ErrorMessages;
import com.distrimind.madkit.i18n.SuccessMessages;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.util.MultiFormatPropertiesObjectParser;
import com.distrimind.madkit.util.XMLUtilities;
import com.distrimind.ood.database.DatabaseFactory;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.ood.database.Table;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SecureRandomType;
import com.distrimind.util.properties.MultiFormatProperties;
import com.distrimind.util.properties.PropertiesParseException;
import com.distrimind.util.version.Version;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * The properties object used within MaDKit.
 * 
 * 
 * @author Jasopn Mahdjoub
 * @since MadKitLanEdition 1.0
 * @version 1.1
 * 
 */
@SuppressWarnings("UnusedReturnValue")
public class MadkitProperties extends MultiFormatProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2186060810090339476L;

	private final static Logger logger = Logger.getLogger(MadkitProperties.class.getName());

	/**
	 * Represents the current madkit version.
	 */
	public final Version madkitVersion = Madkit.getVersion();

	/**
	 * Represents the minimum MadkitLanEdition version that this instance can
	 * accept. If a distant peer try to connect with a MadkitLanEdition version
	 * lower than this one, than the connection will be rejected.
	 */
	public Version minimumMadkitVersion;

	public Class<?> madkitMainClass = Madkit.class;

	public URL madkitWeb;

	public URL madkitRepositoryURL = null;

	/**
	 * Tells if kernel address must be secured. If set to true, the MAC hardware
	 * address from which is derived the kernel address will be encrypted.
	 */
	public boolean isKernelAddressSecured = true;

	public boolean isKernelAddressSecured() {
		return isKernelAddressSecured && networkProperties.network;
	}

	/**
	 * the desktop frame class which should be used, default is
	 * {@link MDKDesktopFrame}
	 */
	public Class<?> desktopFrameClass = MDKDesktopFrame.class;

	/**
	 * Tells if MadkitLanEdition can be launched with the desktop GUI
	 */
	public boolean desktop = false;

	/**
	 * Tells if {@link #desktop} value is predominant than the Madkit choice.
	 */
	public boolean forceDesktop = false;

	/**
	 * the agent frame class which should be used by the GUI manager, default is
	 * {@link AgentFrame}
	 */
	public Class<?> agentFrameClass = AgentFrame.class;

	/**
	 * the directory containing the MDK i18n files
	 */
	public File i18nDirectory = new File("com/distrimind/madkit/i18n/");

	/**
	 * Only useful for kernel developers
	 */
	public Level kernelLogLevel = Level.OFF;

	/**
	 * Only useful for kernel developers
	 */
	public Level guiLogLevel = Level.OFF;

	/**
	 * Can be used to make MaDKit quiet
	 */
	public Level madkitLogLevel = Level.INFO;

	/**
	 * Can be used to specify multiple properties at once, using regular properties
	 * files
	 */
	public ArrayList<File> configFiles = null;
	
	/**
	 * If set to true, this MadKit properties will be saved into all files given by the <code>configFiles</code> list
	 */
	public boolean savePropertiesAfterKernelKill=false;

	public Level platformLogLevel = Level.INFO;

	/**
	 * Connect to the MaDKit repository on startup. Default value is "false".
	 */
	public boolean autoConnectMadkitWebsite = false;

	/**
	 * Loads all the jar files which are in the demos directory on startup. Default
	 * value is "false".
	 */
	public boolean loadLocalDemos = false;

	/**
	 * Launches the {@link ConsoleAgent} before any other.
	 */
	public boolean console = false;

	public boolean noMadkitConsoleLog = false;

	public File MadkitLogFile = null;

	public Level orgLogLevel = Level.OFF;

	public boolean noOrgConsolLog = false;

	/**
	 * Option defining the default agent log level for newly launched agents.
	 * Default value is "INFO". This value could be overridden individually by
	 * agents using {@link AbstractAgent#setLogLevel(Level)}.
	 * <p>
	 * Example:
	 * <ul>
	 * <li>--agentLogLevel OFF</li>
	 * <li>--agentLogLevel ALL</li>
	 * <li>--agentLogLevel FINE</li>
	 * </ul>
	 * 
	 * @see AbstractAgent#logger
	 * @see java.util.logging.Logger
	 * @see AbstractAgent#getMadkitConfig()
	 */
	public Level agentLogLevel = Level.INFO;

	/**
	 * Defines if agent logging should be quiet in the default console. Default
	 * value is "false".
	 */
	public boolean noAgentConsoleLog = false;

	/**
	 * If activated, MaDKit will create a log file for every agent which has a log
	 * level greater than {@link Level#OFF}. Default value is "false".
	 * 
	 * @see #logDirectory
	 */
	public boolean createLogFiles = false;

	/**
	 * Used to specify the directory wherein the logs should be done when the
	 * {@link #createLogFiles} is activated.
	 * 
	 * <pre>
	 * SYNOPSIS
	 * </pre>
	 * 
	 * <code><b>--logDirectory</b></code> DIRECTORY_NAME
	 * 
	 * <pre>
	 * DESCRIPTION
	 * </pre>
	 * 
	 * Specify the desired directory. It could be an absolute or a relative path. At
	 * runtime, a log directory named with the current date (second precision) will
	 * be created in the log directory for each MaDKit session. E.g.
	 * /home/neo/madkit_5/logs/2012.02.23.16.23.53
	 * 
	 * <pre>
	 * DEFAULT VALUE
	 * </pre>
	 * 
	 * Default value is <i>"logs"</i>, so that a directory named "logs" will be
	 * created in the application working directory.
	 * 
	 * <pre>
	 * EXAMPLES
	 * </pre>
	 * <ul>
	 * <li>--logDirectory bin</li>
	 * <li>--logDirectory /home/neo/madkit_logs</li>
	 * </ul>
	 * 
	 * @see #createLogFiles
	 */
	public File logDirectory = new File("logs");

	/**
	 * Option defining the default warning log level for newly launched agents.
	 * Default value is "FINE". This value could be changed individually by the
	 * agents using {@link AgentLogger#setWarningLogLevel(Level)} on their personal
	 * logger.
	 * <p>
	 * Example:
	 * <ul>
	 * <li>--warningLogLevel OFF</li>
	 * <li>--warningLogLevel ALL</li>
	 * <li>--warningLogLevel FINE</li>
	 * </ul>
	 * 
	 * @see AbstractAgent#logger
	 * @see java.util.logging.Logger
	 * @see AbstractAgent#getMadkitConfig()
	 * @since MaDKit 5
	 */
	public Level warningLogLevel = Level.FINE;

	private DatabaseFactory databaseFactory;

	public void setDatabaseFactory(DatabaseFactory df) throws DatabaseException {
		if (databaseFactory != null && databaseFactory != df
				&& !databaseFactory.getDatabaseWrapperSingleton().isClosed())
			databaseFactory.getDatabaseWrapperSingleton().close();

		databaseFactory = df;
	}

	public boolean isDatatabaseEnabled() {
		return databaseFactory != null;
	}

	/**
	 * Get the database wrapper (OOD) used by MadkitLanEdition. This wrapper
	 * represents files storing several database. One of them is used by
	 * MadkitLanEdition. It is possible to use this wrapper to create new database
	 * that will be stored into the same file/directory. Moreover, it is not
	 * recommended to use several database wrappers into the same program. We
	 * recommend to use this wrapper to create all your database.
	 * 
	 * @return the database wrapper used by MadkitLanEdition.
	 * @throws DatabaseException if a problem occurs during the database connection
	 * @see DatabaseWrapper 
	 * @see Table
	 */
	public DatabaseWrapper getDatabaseWrapper() throws DatabaseException {
		if (databaseFactory == null)
			return null;
		return databaseFactory.getDatabaseWrapperSingleton();
	}

	public ArrayList<AgentToLaunch> launchAgents;

	public NetworkProperties networkProperties = new NetworkProperties();

	public ArrayList<MultiFormatProperties> freeProperties = null;

	public static final String defaultProjectCodeName = "Unknown Project Name";

	/**
	 * Represents the current project version.
	 */
	public Version projectVersion = null;

	/**
	 * Represents the minimum project version that this instance can accept. If a
	 * distant peer try to connect with a project version lower than this one, than
	 * the connection will be rejected.
	 */
	public Version minimumProjectVersion = null;

	public MadkitProperties() {
		super(new MultiFormatPropertiesObjectParser());
		this.minimumMadkitVersion=new Version(madkitVersion.getProgramName(), madkitVersion.getShortProgramName(), (short)1, (short)7, (short)6, Version.Type.Stable, (short)0, madkitVersion.getProjectStartDate(), madkitVersion.getProjectEndDate());
		this.minimumMadkitVersion.setBuildNumber(100);
		try {
			madkitWeb = new URL("https://github.com/JazZ51/MaDKitLanEdition");
			madkitRepositoryURL = new URL("https://github.com/JazZ51/MaDKitLanEdition");// new
																						// URL(madkitWeb.toString()+"/repository/"+madkitVersion.getFileHeadName()+"/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public void addProperty(MultiFormatProperties _property) {
		freeProperties.add(_property);
	}

	/**
	 * Loads properties from an XML file.
	 * 
	 * @param xml_file
	 *            can be absolute or relative
	 * @throws IOException if a problem occurs during the XML file loading
	 */
	@Override
	public void loadXML(File xml_file) throws IOException {
		try {
			super.loadXML(xml_file);
			logger.fine(String.format(SuccessMessages.CONFIG_LOAD_SUCCESS.toString(), xml_file.toString()));
		} catch (PropertiesParseException e) {
			logger.log(Level.WARNING,
					String.format(ErrorMessages.CANT_LOAD_CONFIG_FILE.toString(), xml_file.toString()), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadXML(Document document) {
		try {
			super.loadXML(document);
			logger.fine(String.format(SuccessMessages.CONFIG_LOAD_SUCCESS.toString(), document.toString()));
		} catch (PropertiesParseException e) {
			logger.log(Level.WARNING,
					String.format(ErrorMessages.CANT_LOAD_CONFIG_FILE.toString(), document.toString()), e);
		}
	}
	
	/**
	 * Loads properties from an YAML file.
	 * 
	 * @param yaml_file
	 *            can be absolute or relative
	 * @throws IOException if a problem occurs during the XML file loading
	 */
	@Override
	public void loadYAML(File yaml_file) throws IOException {
		super.loadYAML(yaml_file);
		logger.fine(String.format(SuccessMessages.CONFIG_LOAD_SUCCESS.toString(), yaml_file.toString()));
	}



	public void load(File file) throws IOException {
		if (file.getName().endsWith(".xml"))
			loadXML(file);
		else if (file.getName().endsWith(".yaml"))
			loadYAML(file);
		else if (file.getName().endsWith(".properties")) {
			try(FileReader fr=new FileReader(file);BufferedReader br=new BufferedReader(fr)) {
				Properties p=new Properties();
				p.load(br);
				loadFromProperties(p);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveXML(File xml_file) {
		saveXML(xml_file, null);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveXML(File xml_file, MultiFormatProperties referenceProperties) {
		try {
			super.saveXML(xml_file, referenceProperties);
			logger.fine(String.format(SuccessMessages.CONFIG_SAVE_SUCCESS.toString(), xml_file.toString()));
		} catch (PropertiesParseException e) {
			logger.log(Level.WARNING,
					String.format(ErrorMessages.CANT_SAVE_CONFIG_FILE.toString(), xml_file.toString()), e);
		}
	}
	public void save(File file) throws IOException, PropertiesParseException {
		save(file, null);
	}
	public void save(File file, MultiFormatProperties referenceProperties) throws IOException, PropertiesParseException {
		if (file.getName().endsWith(".xml"))
			saveXML(file, referenceProperties);
		else if (file.getName().endsWith(".yaml"))
			saveYAML(file, referenceProperties);
		else if (file.getName().endsWith(".properties")) {
			try(FileWriter fw=new FileWriter(file);BufferedWriter bw=new BufferedWriter(fw)) {
				convertToStringProperties(reference).store(bw, "Madkit properties");
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveYAML(File yaml_file) {
		saveYAML(yaml_file, null);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveYAML(File yaml_file, MultiFormatProperties referenceProperties) {
		try {
			super.saveYAML(yaml_file, referenceProperties);
			logger.fine(String.format(SuccessMessages.CONFIG_SAVE_SUCCESS.toString(), yaml_file.toString()));
		} catch (IOException e) {
			logger.log(Level.WARNING,
					String.format(ErrorMessages.CANT_SAVE_CONFIG_FILE.toString(), yaml_file.toString()), e);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveXML(Document doc) {
		saveXML(doc, null);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveXML(Document doc, MultiFormatProperties referenceProperties) {
		try {
			super.saveXML(doc, referenceProperties);
			logger.fine(String.format(SuccessMessages.CONFIG_SAVE_SUCCESS.toString(), doc.toString()));
		} catch (PropertiesParseException e) {
			logger.log(Level.WARNING, String.format(ErrorMessages.CANT_SAVE_CONFIG_FILE.toString(), doc.toString()), e);
		}

	}

	@Override
	public MadkitProperties clone() {
		try {
			return (MadkitProperties) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 *
	 * Loads properties from an XML file.
	 * 
	 * @param filePath
	 *            can be absolute or relative
	 * @throws IOException
	 */
	/*
	 * public void loadPropertiesFromMaDKitXML(final String filePath) throws
	 * IOException { try (InputStream is = getInputStream(filePath)) { NodeList
	 * madkitOptionNodes =
	 * DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).
	 * getDocumentElement() .getElementsByTagName(XMLUtilities.MADKIT_PROPERTIES);
	 * for (int i = 0; i < madkitOptionNodes.getLength(); i++) {
	 * org.w3c.dom.NamedNodeMap options = madkitOptionNodes.item(i).getAttributes();
	 * for (int j = 0; j < options.getLength(); j++) {
	 * put(options.item(j).getNodeName(), options.item(j).getNodeValue()); } }
	 * logger.fine("** Config file " + filePath + " successfully loaded **\n"); }
	 * catch (SAXException | ParserConfigurationException e) {
	 * logger.log(Level.WARNING, ErrorMessages.CANT_LOAD + "configuration " +
	 * filePath, e); } }
	 */

	/*
	 * Loads properties from a regular properties formatted file.
	 * 
	 * @param pathname
	 * @throws IOException
	 */
	/*
	 * public void loadPropertiesFromPropertiesFile(final String pathname) throws
	 * IOException { try (InputStream is = getInputStream(pathname)) { load(is); }
	 * catch (IOException e) { throw e; } }
	 */

	/*
	 * Loads properties from a properties file (classic or XML).
	 * 
	 * @param pathname
	 * @throws IOException
	 */
	/*
	 * public void loadPropertiesFromFile(final String pathname) throws IOException
	 * { if (pathname.endsWith(".xml")) { loadPropertiesFromMaDKitXML(pathname); }
	 * else { loadPropertiesFromPropertiesFile(pathname); } }
	 */

	/**
	 * Return an <code>InputStream</code> on a file. pathname could be relative to
	 * (1) the actual MaDKit class path, which is preferable considering jar export,
	 * or (2) the user.dir, or it could be an absolute path. The returned input
	 * stream should be closed once done.
	 * 
	 * @param file
	 *            A pathname string If the <code>pathname</code> argument is
	 *            <code>null</code>
	 * @return an <code>InputStream</code> by opening a connection to an actual
	 *         file, or <code>null</code> if the file is not found.
	 * 
	 * @throws IOException if IO problem occurs
	 */
	public static InputStream getInputStream(final File file) throws IOException {
		// closed when used
		InputStream is = file.exists() ? new FileInputStream(file)
				: MadkitClassLoader.getLoader().getResourceAsStream(file.toString());
		if (is == null)
			throw new FileNotFoundException(file.toString());
		return is;
	}

	@Override
	public Node getRootNode(Document _document) {
		for (int i = 0; i < _document.getChildNodes().getLength(); i++) {
			Node n = _document.getChildNodes().item(i);
			if (n.getNodeName().equals(XMLUtilities.MDK))
				return n;
		}
		return null;
	}

	@Override
	public Node createOrGetRootNode(Document _document) {
		Node res = getRootNode(_document);
		if (res == null) {
			res = _document.createElement(XMLUtilities.MDK);
			_document.appendChild(res);
		}
		return res;
	}

	public static void main(String args[]) {
		MadkitProperties mp = new MadkitProperties();
		mp.saveYAML(new File("madkit.yaml"));
	}
	
	

	boolean prepareCurrentRandomSeedsForBackup()
	{
		boolean change=false;
		if (approvedRandom!=null)
		{
			savedRandomSeedForApprovedRandom=Base64.encodeBase64URLSafeString(approvedRandom.generateSeed(55));
			change=true;
		}
		if (approvedRandomForKeys!=null)
		{
			savedRandomSeedForApprovedRandomForKeys=Base64.encodeBase64URLSafeString(approvedRandomForKeys.generateSeed(55));
			change=true;
		}
		return change;
	}
	
	private String savedRandomSeedForApprovedRandom=null, savedRandomSeedForApprovedRandomForKeys=null;
	private SecureRandomType approvedRandomType=SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED;
	private SecureRandomType approvedRandomTypeForKeys=SecureRandomType.FORTUNA_WITH_BC_FIPS_APPROVED_FOR_KEYS;
	private volatile AbstractSecureRandom approvedRandom=null;
	private volatile AbstractSecureRandom approvedRandomForKeys=null;
	private String nonceForApprovedRandom="Au courant de l'amour lorsque je m'abandonne,\n" + 
			"Dans le torrent divin quand je plonge enivré,\n" + 
			"Et presse éperdument sur mon sein qui frissonne\n" + 
			"Un être idolâtre.\n" + 
			"\n" + 
			"Je sais que je n'étreins qu'une forme fragile,\n" + 
			"Qu'elle peut à l'instant se glacer sous ma main,\n" + 
			"Que ce cœur tout à moi, fait de flamme et d'argile,\n" + 
			"Sera cendre demain ;\n" + 
			"\n" + 
			"Qu'il n'en sortira rien, rien, pas une étincelle\n" + 
			"Qui s'élance et remonte à son foyer lointain :\n" + 
			"Un peu de terre en hâte, une pierre qu'on scelle,\n" + 
			"Et tout est bien éteint.\n" + 
			"\n" + 
			"Et l'on viendrait serein, à cette heure dernière,\n" + 
			"Quand des restes humains le souffle a déserté,\n" + 
			"Devant ces froids débris, devant cette poussière\n" + 
			"Parler d'éternité !\n";
	private String nonceForApprovedRandomForKeys="\n" + 
			"\n" + 
			"    L ‘Emmuré.\n" + 
			"\n" + 
			"    S’il respire il pense à l’encoche\n" + 
			"    Dans la tendre chaux confidente\n" + 
			"    Où ses mains du soir étendent ton corps.\n" + 
			"\n" + 
			"    Le laurier l’épuise,\n" + 
			"    La privation le consolide.\n" + 
			"\n" + 
			"    O toi, la monotone absente,\n" + 
			"    La fileuse de salpêtre,\n" + 
			"    Derrière des épaisseurs fixes\n" + 
			"    Une échelle sans âge déploie ton voile !\n" + 
			"\n" + 
			"    Tu vas nue, constellée d’échardes,\n" + 
			"    Secrète, tiède et disponible,\n" + 
			"    Attachée au sol indolent,\n" + 
			"    Mais l’intime de l’homme abrupt dans sa prison.\n" + 
			"\n" + 
			"    A te mordre les jours grandissent,\n" + 
			"    Plus arides, plus imprenables que les nuages qui se déchirent au fond des os.\n" + 
			"    J’ai pesé de tout mon désir\n" + 
			"    Sur ta beauté matinale\n" + 
			"    Pour qu’elle éclate et se sauve.\n" + 
			"\n" + 
			"    L’ont suivie l’alcool sans rois mages,\n" + 
			"    Le battement de ton triangle,\n" + 
			"    La main-d’oeuvre de tes yeux\n" + 
			"    Et le gravier debout sur l’algue.\n" + 
			"\n" + 
			"    Un parfum d’insolation\n" + 
			"    Protège ce qui va éclore.\n" + 
			"\n" + 
			"";
	private String parameterForApprovedRandom="J'ai rencontré trois escargots\n" + 
			"Qui s'en allaient cartable au dos\n" + 
			"Et dans le pré trois limaçons\n" + 
			"Qui disaient par cœur leur leçon.\n" + 
			"Puis dans un champ, quatre lézards\n" + 
			"Qui écrivaient un long devoir.\n" + 
			"\n" + 
			"Où peut se trouver leur école ?\n" + 
			"Au milieu des avoines folles ?\n" + 
			"Et leur maître est-il ce corbeau\n" + 
			"Que je vois dessiner là-haut\n" + 
			"De belles lettres au tableau ?";
	private String parameterForApprovedRandomForKeys="Eh bien ! reprends-le donc ce peu de fange obscure\n" + 
			"Qui pour quelques instants s'anima sous ta main ;\n" + 
			"Dans ton dédain superbe, implacable Nature,\n" + 
			"Brise à jamais le moule humain.\n" + 
			"\n" + 
			"De ces tristes débris quand tu verrais, ravie,\n" + 
			"D'autres créations éclore à grands essaims,\n" + 
			"Ton Idée éclater en des formes de vie\n" + 
			"Plus dociles à tes desseins,\n" + 
			"\n" + 
			"Est-ce à dire que Lui, ton espoir, ta chimère,\n" + 
			"Parce qu'il fut rêvé, puisse un jour exister ?\n" + 
			"Tu crois avoir conçu, tu voudrais être mère ;\n" + 
			"A l'œuvre ! il s'agit d'enfanter.\n" + 
			"\n" + 
			"Change en réalité ton attente sublime.\n" + 
			"Mais quoi ! pour les franchir, malgré tous tes élans,\n" + 
			"La distance est trop grande et trop profond l'abîme\n" + 
			"Entre ta pensée et tes flancs.\n";
	
	
	public SecureRandomType getApprovedRandomType() {
		return approvedRandomType;
	}

	public void setApprovedRandomType(SecureRandomType approvedRandomType) {
		this.approvedRandomType = approvedRandomType;
		approvedRandom=null;
	}

	public SecureRandomType getApprovedRandomTypeForKeys() {
		return approvedRandomTypeForKeys;
	}

	public void setApprovedRandomTypeForKeys(SecureRandomType approvedRandomTypeForKeys) {
		this.approvedRandomTypeForKeys = approvedRandomTypeForKeys;
    }
	
	private static long getMacAddress()
	{
		long result = 0;
		long result2=0;
		try {
			final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                final NetworkInterface ni = e.nextElement();


                if (!ni.isLoopback()) {

                    long val = getHardwareAddress(ni.getHardwareAddress());
                    if (val != 0 && val != 224)
                    {
                        if (ni.isPointToPoint()) {
                            result2=val;
                        }
                        else {
                            result = val;
                            break;
                        }
                    }
                }
            }
        }
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if (result==0)
			result=result2;
		return result;
		
	}
	private static long getHardwareAddress(byte hardwareAddress[]) {
		long result = 0;
		if (hardwareAddress != null) {
			for (final byte value : hardwareAddress) {
				result <<= 8;
				result |= value & 255;
			}
		}
		return result;
	}
	
	
	public AbstractSecureRandom getApprovedSecureRandom() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		if (approvedRandom==null)
		{
			synchronized(this)
			{
				if (approvedRandom==null)
				{
					approvedRandom=approvedRandomType.getSingleton(((nonceForApprovedRandom+System.currentTimeMillis()+(savedRandomSeedForApprovedRandom==null?"":savedRandomSeedForApprovedRandom))+getMacAddress()).getBytes(), parameterForApprovedRandom.getBytes(), true);
				}
			}
		}
		return approvedRandom;
	}
	public AbstractSecureRandom getApprovedSecureRandomForKeys() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		if (approvedRandomForKeys==null)
		{
			synchronized(this)
			{
				if (approvedRandomForKeys==null)
				{
					approvedRandomForKeys=approvedRandomTypeForKeys.getSingleton(((nonceForApprovedRandomForKeys+System.currentTimeMillis()+(savedRandomSeedForApprovedRandomForKeys==null?"":savedRandomSeedForApprovedRandomForKeys))+getMacAddress()).getBytes(), parameterForApprovedRandomForKeys.getBytes(), true);
				}
			}
		}
		return approvedRandomForKeys;
	}

	public String getNonceForApprovedRandom() {
		return nonceForApprovedRandom;
	}

	public void setNonceForApprovedRandom(String nonceForApprovedRandom) {
		this.nonceForApprovedRandom = nonceForApprovedRandom;
		approvedRandom=null;
	}

	public String getNonceForApprovedRandomForKeys() {
		return nonceForApprovedRandomForKeys;
	}

	public void setNonceForApprovedRandomForKeys(String nonceForApprovedRandomForKeys) {
		this.nonceForApprovedRandomForKeys = nonceForApprovedRandomForKeys;
		approvedRandomForKeys=null;
	}

	public String getParameterForApprovedRandom() {
		return parameterForApprovedRandom;
	}

	public void setParameterForApprovedRandom(String parameterForApprovedRandom) {
		this.parameterForApprovedRandom = parameterForApprovedRandom;
		approvedRandom=null;
	}

	public String getParameterForApprovedRandomKeys() {
		return parameterForApprovedRandomForKeys;
	}

	public void setParameterForApprovedRandomKeys(String parameterForApprovedRandomKeys) {
		this.parameterForApprovedRandomForKeys = parameterForApprovedRandomKeys;
		approvedRandomForKeys=null;
	}
	
	private transient MadkitProperties reference=null;

	void setReference(MadkitProperties reference) {
		this.reference = reference;
	}

	public MadkitProperties getReference() {
		return reference;
	}
    /**
     * Save MadKit config into the given file.
     * Save only the differences between the current MaDKit config and the reference MaDKit configuration, given at the MaDKit construction
     * @see #getReference()
     * @param file the file path
     * @throws IOException if a problem occurs
     * @throws PropertiesParseException if the property file parsing failed
     */
	public void saveConfiguration(File file) throws IOException, PropertiesParseException {
		assert file!=null;
        prepareCurrentRandomSeedsForBackup();
		save(file, reference);
	}

    /**
     * Save MadKit config into the file given by {@link #configFiles}.
     * Save only the differences between the current MaDKit config and the reference MaDKit configuration, given at the MaDKit construction
     * @see #getReference()
     * @throws IOException if a problem occurs
     * @throws PropertiesParseException if the property file parsing failed
     */
    public void saveConfiguration() throws IOException, PropertiesParseException {
	    if (configFiles==null)
	        return;
        prepareCurrentRandomSeedsForBackup();
	    for (File f : configFiles)
            save(f, reference);
    }
}
