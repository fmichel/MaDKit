/**
 * 
 */
package madkit.api.abstractAgent;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import madkit.kernel.JunitMadKit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



@RunWith(Suite.class)
@SuiteClasses(value={
		CreateGroupTest.class
})

public class AllTests {
		
//	static{
//		JunitMadKit.addMadkitArgs("--network");
//	}
//		@Test
//		public void testing2(){
//			new JUnit4TestAdapter(AllTests.class).run(new TestResult());
//		}
}

