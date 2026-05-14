package madkit.sample.organization.secured;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class SecuredGroupDemoTest {

    @Test
    public void givenSecuredGroupDemo_whenInspected_thenActivationAndMainMethodsExist() throws Exception {
        assertThat(SecuredGroupDemo.class.getDeclaredMethod("onActivation")).isNotNull();
        Method main = SecuredGroupDemo.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
