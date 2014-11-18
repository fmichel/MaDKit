package madkit.gui.menu;

import static org.junit.Assert.assertEquals;

import java.awt.Component;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JMenuItem;

import madkit.i18n.Words;
import madkit.kernel.Madkit;

import org.junit.Test;

public class HelpMenuTest {

	@Test
	public final void testUrls() throws IOException {
		// HttpURLConnection.setFollowRedirects(false);
		HelpMenu name = new HelpMenu();
		for (Component item : name.getMenuComponents()) {
			final String actionCommand = ((JMenuItem) item).getActionCommand();
			URL url = new URL(Madkit.WEB + "/" + actionCommand);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("HEAD");
			if (!actionCommand.endsWith(Words.ABOUT.toString())) {
				System.err.println(url);
				assertEquals(HttpURLConnection.HTTP_OK, huc.getResponseCode());
			}

			// for (ActionListener l : item.getListeners(ActionListener.class)) {
			// l.actionPerformed(new ActionEvent(item,1,(((JMenuItem)item).getActionCommand())));
			// }
		}
	}

}
