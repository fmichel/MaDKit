package madkit.reflection;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ReflectionUtilsTest {

	enum A_TEST_ENUM {
		A_TEST_VALUE;
	}

	@Test
	public void enumToMethodNameTest() {
		String actual = ReflectionUtils.enumToMethodName(A_TEST_ENUM.A_TEST_VALUE);
		assertEquals(actual, "aTestValue");
	}

	@Test
	public void given_MethodWithArgs_whenGetSignature_thenSignatureIsReturned() {
		Class<?>[] paramTypes = { String.class, int.class };
		String result = ReflectionUtils.getSignature("test", paramTypes);
		assertEquals(result, "testjava.lang.Stringjava.lang.Integer");
	}

	@Test
	public void given_NoArgMethod_whenGetSignature_thenSignatureIsReturned() {
		String result = ReflectionUtils.getSignature("test");
		assertEquals(result, "test");
	}
}
