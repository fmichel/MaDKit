package madkit.action;

import java.io.File;
import java.net.MalformedURLException;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.MadkitClassLoader;

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
					MadkitClassLoader.loadUrl(new File("test.jar").toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				pause(100);
			}
		});
	}

}
