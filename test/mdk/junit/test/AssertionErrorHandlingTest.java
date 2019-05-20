package mdk.junit.test;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAgent;

public class AssertionErrorHandlingTest extends JunitMadkit{

    class ExpectedFailure implements TestRule {
        public Statement apply(Statement base, Description description) {
            return statement(base, description);
        }

        private Statement statement(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } catch (Throwable e) {
                        if (description.getAnnotation(Deprecated.class) != null) {
                            System.err.println("test failed, but that's ok:");
                        } else {
                            throw e;
                        }
                    }
                }
            };
        }
    }
    
    @Rule public ExpectedFailure expectedFailure = new ExpectedFailure();

    
    @Deprecated
    @Test
    public void assertionErrorHandlingTestActivate() {
	launchTestV2(new NormalAgent() {
	    protected void activate() {
		assertTrue(false);
	    }
	});
    }

    @Test
    @Deprecated
    public void assertionErrorHandlingTestLive() {
	launchTestV2(new NormalAgent() {
	    protected void live() {
		assertTrue(false);
	    }
	});
    }


    @Test
    @Deprecated
    public void assertionErrorHandlingTestV2() {
	launchTestV2(new NormalAgent() {
	    protected void live() {
		launchAgent(new AbstractAgent() {
		    @Override
		    protected void activate() {
			super.activate();
			assertTrue(false);
		    }
		});
		assertTrue(false);
	    }
	});
    }


    @Test
    public void assertionErrorHandlingAATest() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertTrue(true);
	    }
	});
    }

    @Test
    @Deprecated
    public void assertionErrorHandlingAATestFail() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertTrue(false);
	    }
	});
    }

    @Test
    @Deprecated
    public void assertionErrorHandlingAAEndTest() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		killAgent(this);
	    }

	    @Override
	    protected void end() {
		assertTrue(false);
	    }
	});
    }
}
