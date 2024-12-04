package madkit.kernel;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class CommunityTest {

	@Test
	public void givenNewCommunity_whenCreated_thenNameIsSet() {
		Community community = new Community("TestCommunity", new Organization(new KernelAgent()));
		assertEquals(community.getName(), "TestCommunity");
	}

	@Test
	public void givenNewCommunity_whenCreated_thenLoggerIsNotNull() {
		Community community = new Community("TestCommunity", new Organization(new KernelAgent()));
		assertNotNull(community.getLogger());
	}

	@Test(expectedExceptions = CGRNotAvailable.class)
	public void givenCommunityWithoutGroup_whenGetGroup_thenThrowsException() {
		Community community = new Community("TestCommunity", new Organization(new KernelAgent()));
		community.getGroup("NonExistentGroup");
	}

	@Test
	public void givenCommunityWithoutGroups_whenCheckExists_thenReturnsFalse() {
		Community community = new Community("TestCommunity", new Organization(new KernelAgent()));
		assertFalse(community.exists());
	}
}
