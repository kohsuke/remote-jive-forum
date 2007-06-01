
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.cookies.CookieProperties;
import org.kohsuke.remotejiveforums.Forum;
import org.kohsuke.remotejiveforums.Topic;
import org.kohsuke.remotejiveforums.ProcessingException;
import org.kohsuke.remotejiveforums.Post;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * @author Kohsuke Kawaguchi
 */
public class Main {
    public static void main(String[] args) throws Exception {
        WebConversation wc = new WebConversation();

        // set up for java.net
        HttpUnitOptions.setScriptingEnabled(false);
        CookieProperties.setDomainMatchingStrict(false);

        // first login
        WebResponse r = wc.getResponse("https://www.dev.java.net/servlets/TLogin");
        WebForm form = r.getForms()[1];
        form.setParameter("loginID","kohsuke_agent");
        form.setParameter("password","kohsuke");
        r = form.submit();

        Forum forum = Forum.get(new URL("http://forums.java.net/jive/"),49,wc);
        // post(forum,wc);

        Topic topic = new Topic(forum, 2597);
        Post pt = topic.getPost();
        System.out.println(pt);
    }

    private static Topic post(WebConversation wc, Forum forum) throws MalformedURLException, ProcessingException {
        Topic topic = forum.createTopic("test","testing. please ignore");
        System.out.println(topic.getTopicURL());
        return topic;
    }
}
