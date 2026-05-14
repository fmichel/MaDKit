package madkit.samples;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.sample.organization.GroupCreator;
import madkit.sample.organization.RoleRequester;

public class AgentCGRTest {

    @Test
    public void givenOrganizationSamples_whenInspectingClasses_thenCgrRelatedEntryPointsExist() throws Exception {
        Method groupCreatorActivation = GroupCreator.class.getDeclaredMethod("onActivation");
        Method roleRequesterActivation = RoleRequester.class.getDeclaredMethod("onActivation");
        assertThat(groupCreatorActivation).isNotNull();
        assertThat(roleRequesterActivation).isNotNull();
    }
}
