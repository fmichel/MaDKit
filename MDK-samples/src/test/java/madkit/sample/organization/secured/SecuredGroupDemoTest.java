package madkit.sample.organization.secured;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;

public class SecuredGroupDemoTest extends MadkitConcurrentTestCase {

    @Test
    public void givenSecuredGroupDemo_whenInspected_thenActivationAndMainMethodsExist() throws Exception {
        assertThat(SecuredGroupDemo.class.getDeclaredMethod("onActivation")).isNotNull();
        Method main = SecuredGroupDemo.class.getMethod("main", String[].class);
        assertThat(main).isNotNull();
    }

    @Test
    public void givenSecuredGroup_whenAgentsRequestRoles_thenPasswordControlsAdmission() {
        // Given / When / Then
        runTest(new DefaultTestAgent() {
            @Override
            public void behaviorInActivate() {
                assertThat(createGroup("secured-community", "vip-group", false,
                        (agentNetworkID, roleName, memberCard) -> "secret-password".equals(memberCard)))
                                .isEqualTo(ReturnCode.SUCCESS);
                assertThat(requestRole("secured-community", "vip-group", "manager", "secret-password"))
                                .isEqualTo(ReturnCode.ROLE_ALREADY_HANDLED);

                DefaultTestAgent authorizedAgent = new DefaultTestAgent() {
                    @Override
                    public void behaviorInActivate() {
                        assertThat(requestRole("secured-community", "vip-group", "member", "secret-password"))
                                        .isEqualTo(ReturnCode.SUCCESS);
                    }
                };
                authorizedAgent.setMadkitConcurrentTestCase(SecuredGroupDemoTest.this);
                launchAgent(authorizedAgent);

                DefaultTestAgent unauthorizedAgent = new DefaultTestAgent() {
                    @Override
                    public void behaviorInActivate() {
                        assertThat(requestRole("secured-community", "vip-group", "member", "wrong-password"))
                                        .isEqualTo(ReturnCode.ACCESS_DENIED);
                    }
                };
                unauthorizedAgent.setMadkitConcurrentTestCase(SecuredGroupDemoTest.this);
                launchAgent(unauthorizedAgent);
                resume();
            }
        });
    }
}