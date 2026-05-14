package madkit.sample.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class OrganizationExplorerTest {

    @Test
    public void givenOrganizationExplorer_whenInspectingOrganizationMethods_thenActivationAndMainArePresent() throws Exception {
        Method activation = OrganizationExplorer.class.getDeclaredMethod("onActivation");
        Method main = OrganizationExplorer.class.getMethod("main", String[].class);
        assertThat(activation).isNotNull();
        assertThat(main).isNotNull();
    }
}
