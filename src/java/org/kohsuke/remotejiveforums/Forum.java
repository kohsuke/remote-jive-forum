package org.kohsuke.remotejiveforums;

import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Represents one JIVE forum like
 * <a href="http://forums.java.net/jive/forum.jspa?forumID=46">this</a>.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Forum {
    /**
     * Creates a new {@link Forum}.
     *
     * @param site
     *      An URL like "http://forums.java.net/jive" -- the root dir of the JIVE installation
     *      on the server.
     * @param forumId
     *      Forum ID to connect. Check the URL of the forum to find the forum ID.
     * @param wc
     *      This conversation is expected to be already logged in.
     *      This object will be owned by the newly created {@link Forum} object.
     */
    public static Forum get( URL site, int forumId, WebConversation wc ) {
        return new Forum(site,forumId,wc);
    }



    final URL siteURL;
    final int forumId;
    final WebConversation wc;
    private final Map<Integer/*threadId*/,Topic> topics = new WeakHashMap<Integer, Topic>();


    public Forum(URL site, int forumId, WebConversation wc) {
        this.siteURL = site;
        this.forumId = forumId;
        this.wc = wc;
    }

    /**
     * Creates a new topic in this forum.
     */
    public Topic createTopic( final String subject, final String body ) throws ProcessingException {
        return new Scraper<Topic>("Creating a new topic: "+subject) {
            public Topic scrape() throws IOException, SAXException, ProcessingException, ParseException {
                WebResponse rsp = wc.getResponse(siteURL+"/post!default.jspa?forumID="+forumId);
                WebForm form = rsp.getFormWithName("postform");
                form.setParameter("subject",subject);
                form.setParameter("body",body);
                SubmitButton submit = form.getSubmitButton("doPost");
                rsp = form.submit(submit);

                // extract the topic id
                String url = rsp.getURL().toString();
                int idx = url.lastIndexOf("threadID=");
                if(idx==-1)
                    throw new ProcessingException("unable to extract thread ID from "+url);

                return getTopic(Integer.parseInt(url.substring(idx+9)));
            }
        }.run();
    }

    public Topic getTopic(int threadId) {
        Topic topic = topics.get(threadId);
        if(topic==null) {
            topic = new Topic(this,threadId);
            topics.put(threadId,topic);
        }
        return topic;
    }
}
