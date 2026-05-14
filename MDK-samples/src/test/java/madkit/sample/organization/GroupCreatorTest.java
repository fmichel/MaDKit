package madkit.sample.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class GroupCreatorTest {

    @Test
    public void givenGroupCreator_whenInspectingOrganizationMethods_thenActivationAndMainArePresent() throws Exception {
        Method activation = GroupCreator.class.getDeclaredMethod("onActivation");
        Method main = GroupCreator.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(main).isNotNull();
    }
}
