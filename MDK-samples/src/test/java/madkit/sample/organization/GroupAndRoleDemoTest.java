package madkit.sample.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class GroupAndRoleDemoTest {

    @Test
    public void givenGroupAndRoleDemo_whenInspectingClass_thenActivationAndMainArePresent() throws Exception {
        Method activation = GroupAndRoleDemo.class.getDeclaredMethod("onActivation");
        Method main = GroupAndRoleDemo.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(main).isNotNull();
    }
}
