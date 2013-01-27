package madkit.action;

import java.io.File;
import java.net.MalformedURLException;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;


public class KernelActionTest extends JunitMadkit{

	@Test
	public final void test() {//TODO no test here...
		launchTest(new AbstractAgent(){
			/**
			 * 
			 */
			private static final long	serialVersionUID	= -2571329103697172436L;

			@Override
			protected void activate() {
				try {
					KernelAction.LOAD_JAR_FILE.getActionFor(this,new File("test.jar").toURI().toURL()).actionPerformed(null);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				pause(100);
			}
		});
	}

}
