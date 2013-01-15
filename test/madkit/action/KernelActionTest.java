package madkit.action;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;


public class KernelActionTest extends JunitMadkit{

	@Test
	public final void test() {
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				try {
					KernelAction.LOAD_JAR_FILE.getActionFor(this,new File("test.jar").toURL()).actionPerformed(null);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pause(1000);
			}
		});
	}

}
