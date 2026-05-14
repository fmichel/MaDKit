package madkit.sample.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent.ReturnCode;
import madkit.samples.support.SamplesRuntimeTestSupport;

public class GroupCreatorRuntimeTest extends SamplesRuntimeTestSupport {

	@Test
	public void givenGroupCreator_whenLaunched_thenCreateGroupAndRequestRoleSucceed() {
		CountDownLatch done = new CountDownLatch(1);
		AtomicReference<ReturnCode> createRc = new AtomicReference<>();
		AtomicReference<ReturnCode> roleRc = new AtomicReference<>();

		GroupCreator sample = new GroupCreator() {
			@Override
			protected void onActivation() {
				createRc.set(createGroup("sample-community", "sample-group"));
				roleRc.set(requestRole("sample-community", "sample-group", "sample-role"));
				done.countDown();
			}
		};

		assertThat(launch(sample)).isNotNull();
		await(done, 2000);
		assertThat(createRc.get()).isEqualTo(SUCCESS);
		assertThat(roleRc.get()).isEqualTo(SUCCESS);
	}
}
