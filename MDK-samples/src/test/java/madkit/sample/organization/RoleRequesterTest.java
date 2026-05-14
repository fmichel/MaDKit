package madkit.sample.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class RoleRequesterTest {

    @Test
    public void givenRoleRequester_whenInspectingOrganizationMethods_thenActivationAndMainArePresent() throws Exception {
        Method activation = RoleRequester.class.getDeclaredMethod("onActivation");
        Method main = RoleRequester.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(main).isNotNull();
    }
}
