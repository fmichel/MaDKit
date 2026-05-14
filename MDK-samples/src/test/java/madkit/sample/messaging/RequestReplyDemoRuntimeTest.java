package madkit.sample.messaging;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import madkit.messages.StringMessage;
import madkit.samples.support.SamplesRuntimeTestSupport;

public class RequestReplyDemoRuntimeTest extends SamplesRuntimeTestSupport {

    @Test
    public void givenRequestReplyDemo_whenRequesterRuns_thenPongReplyIsReceived() {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<String> reply = new AtomicReference<>();

        RequestReplyDemo sample = new RequestReplyDemo() {
            @Override
            protected void onLive() {
                StringMessage response = sendWaitReply(new StringMessage("Ping!"),
                        "messaging-community", "request-reply-group", "responder", 5000);
                if (response != null) {
                    reply.set(response.getContent());
                }
                done.countDown();
            }
        };

        assertThat(launch(sample)).isNotNull();
        await(done, 6000);
        assertThat(reply.get()).isEqualTo("Pong!");
    }
}
