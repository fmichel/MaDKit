package madkit.sample.messaging.enumdispatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

public class EnumDispatchDemoTest {

    @Test
    public void givenEnumDispatchDemo_whenInspected_thenActivationLiveAndMainMethodsExist() throws Exception {
        assertThat(EnumDispatchDemo.class.getDeclaredMethod("onActivation")).isNotNull();
        assertThat(EnumDispatchDemo.class.getDeclaredMethod("onLive")).isNotNull();
        Method main = EnumDispatchDemo.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }
}
