package madkit.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import madkit.action.AgentAction;
import madkit.action.GUIManagerAction;
import madkit.action.KernelAction;
import madkit.action.SchedulingAction;

import org.junit.Test;

public class I18nFileTest {

	@Test
	public final void testAgentAction() throws IOException {
		testFilePresenceAndContent(AgentAction.class,"fr_FR");
	}

	@Test
	public final void testKernelAction() throws IOException {
		testFilePresenceAndContent(KernelAction.class,"fr_FR");
	}

	@Test
	public final void testGUIManagerAction() throws IOException {
		testFilePresenceAndContent(GUIManagerAction.class,"fr_FR");
	}

	@Test
	public final void testSchedulingAction() throws IOException {
		testFilePresenceAndContent(SchedulingAction.class,"fr_FR");
	}

	public <E extends Enum<E>> void testFilePresenceAndContent(Class<E> e,String... languages) throws IOException{
		EnumSet<E> set = EnumSet.allOf(e);
		testKeys(e, set,"");
		for (String lang : languages) {
			testKeys(e, set, "_"+lang);
		}
	}

	/**
	 * @param e
	 * @param set
	 * @throws IOException
	 */
	private <E extends Enum<E>> void testKeys(Class<E> e, EnumSet<E> set,String lang) throws IOException {
		System.err.println("\n----------------testing "+e+lang);
		Properties defaultConfig = new Properties();
		defaultConfig.load(getClass().getResourceAsStream("/madkit/i18n/"+e.getSimpleName()+lang+".properties"));
		assertNotNull(defaultConfig);
		assertEquals(set.size(), defaultConfig.size());
		for (E enum1 : set) {
			System.err.println(enum1.name());
			assertNotNull(defaultConfig.getProperty(enum1.name()));
		}
	}
}
