package org.kohsuke.remotejiveforums;

import com.meterware.httpunit.WebResponse;
import org.dom4j.Document;
import org.dom4j.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents one topic in a forum like
 * <a href="http://forums.java.net/jive/thread.jspa?threadID=432&tstart=0">this</a>.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Topic {
    private final Forum forum;
    private final int threadId;

    private int replies;
    private String title;

    public Topic(Forum forum, int threadId) {
        this.forum = forum;
        this.threadId = threadId;
    }

    /**
     * Gets the URL of this topic.
     */
    public URL getTopicURL() {
        try {
            String url = forum.siteURL.toExternalForm();
            if(!url.endsWith("/"))
                url += '/';
            return new URL(url+"thread.jspa?threadID="+threadId);
        } catch (MalformedURLException e) {
            // impossible
            throw new Error(e);
        }
    }

    /**
     * Parses the page title that contains several metadata about the topic.
     */
    private void parsePageTitle() throws ProcessingException {
        if(title!=null)     return;

        new Scraper<Void>("Parsing the page title") {
            public Void scrape() throws IOException, SAXException, ProcessingException {
                WebResponse rsp = forum.wc.getResponse(getTopicURL().toExternalForm());
                Document doc = Util.getDom4j(rsp);
                Node pageTitle = doc.selectSingleNode("//P/SPAN[@class='jive-page-title']");
                if(pageTitle==null)
                    throw new ProcessingException("Unable to find the title of the topic");
                title = pageTitle.getText().trim();

                String str = Util.collapse(pageTitle.getParent().getText());

                int s = str.indexOf("Replies: ")+9;
                int e = str.indexOf(' ',s);
                if(s==-1 || e==-1 || s>=e)
                    throw new ProcessingException("Unable to find the reply count of the topic");
                replies = Integer.parseInt(str.substring(s,e));

                return null;
            }
        }.run();
    }

    /**
     * Gets the number of replies in this topic.
     */
    public int getReplies() throws ProcessingException {
        parsePageTitle();
        return replies;
    }

    public String getTitle() throws ProcessingException {
        parsePageTitle();
        return title;
    }
}
