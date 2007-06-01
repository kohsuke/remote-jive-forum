package org.kohsuke.remotejiveforums;

import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebLink;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.Node;
import org.xml.sax.SAXException;

/**
 * Enumerates all topics in a forum.
 *
 * @author Kohsuke Kawaguchi
 */
final class TopicIterator implements Iterator<Topic> {
    private final List<Topic> rest = new LinkedList<Topic>();
    private URL nextLink;
    private final Forum forum;

    TopicIterator(Forum forum) {
        this.forum = forum;
        try {
            nextLink = new URL(forum.siteURL +"/forum.jspa?forumID="+ forum.forumId);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);  // impossible
        }
    }

    public boolean hasNext() {
        if(rest.isEmpty())
            fetch();
        return !rest.isEmpty();
    }

    public Topic next() {
        if(rest.isEmpty())
            fetch();
        if(rest.isEmpty())
            throw new NoSuchElementException();
        return rest.remove(0);
    }

    private void fetch() {
        if(nextLink==null)
            return;

        try {
            WebResponse rsp = forum.wc.getResponse(nextLink.toExternalForm());
            Document dom = Util.getDom4j(rsp);
            for( int n=1; ; n++) {
                Node href = dom.selectSingleNode("//A[@id='jive-thread-" + n + "']/@href");
                if(href==null) {
                    // no more thread in this page
                    WebLink link = rsp.getLinkWith("Next");
                    if(link==null) {
                        nextLink = null;
                    } else {
                        nextLink = new URL(nextLink,link.getURLString());
                    }
                    return;
                }

                Matcher m = THREAD_ID_PATTERN.matcher(href.getText());
                if(!m.matches())
                    throw new RuntimeException("Unable to parse link "+href.getText());
                rest.add(new Topic(forum,Integer.parseInt(m.group(1))));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private static final Pattern THREAD_ID_PATTERN = Pattern.compile(".+threadID=([0-9]+).+");
}
