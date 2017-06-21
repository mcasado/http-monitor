package exercise;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mail.DefaultJavaMailSender;
import org.apache.camel.component.mail.JavaMailSender;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.mock_javamail.Mailbox;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {Main.class},
        loader = CamelSpringDelegatingTestContextLoader.class)
public class HttpMonitorTest {

    @EndpointInject(uri = "mock:direct:cacheContentUpdate")
    protected MockEndpoint cacheUpateEndpoint;

    @Produce(uri = "direct:diff")
    protected ProducerTemplate template;

    static {
        try {
            prepareMailbox("test", "secret", "imap",
                    "localhost", 25);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @DirtiesContext
    @Test
    public void testReceivedNotificationEmail() throws Exception {
        // Mailbox.clearAll();
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(20, TimeUnit.SECONDS);
            
        List<Message> inbox = Mailbox.get("test@test.com");
        assertTrue(inbox.size() >= 1);
        String subject = inbox.get(0).getSubject();
     
    }


    private static void prepareMailbox(String user, String password, String type, String host, int port) throws Exception {

        // connect to mailbox
        Mailbox.clearAll();
        JavaMailSender sender = new DefaultJavaMailSender();
        Store store = sender.getSession().getStore(type);
        store.connect(host, port, user, password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        folder.expunge();
        folder.close(true);
    }

}

