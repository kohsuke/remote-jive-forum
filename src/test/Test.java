
import com.meterware.httpunit.WebConversation;
import org.kohsuke.remotejiveforums.Forum;
import org.kohsuke.remotejiveforums.Topic;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class Test {
    public static void main(String[] args) throws Exception {
        WebConversation wc = new WebConversation();
        wc.setProxyServer("webcache.sfbay.sun.com",8080);
        Forum f = Forum.get(new URL("http://www.jivesoftware.com/jive/"),5,wc);
        Topic topic = f.createTopic("test","testing. please ignore");
        System.out.println(topic.getTopicURL());
    }
}
