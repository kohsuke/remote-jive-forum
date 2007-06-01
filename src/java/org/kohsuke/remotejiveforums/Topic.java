package org.kohsuke.remotejiveforums;

import com.meterware.httpunit.WebResponse;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
    private Post post;

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
            public Void scrape() throws IOException, SAXException {
                WebResponse rsp = forum.wc.getResponse(getTopicURL().toExternalForm());
                Document doc = Util.getDom4j(rsp);
                Node pageTitle = doc.selectSingleNode("//P[@class='jive-page-title']");
                if(pageTitle==null)
                    throw new ProcessingException("Unable to find the title of the topic");
                title = pageTitle.getText().trim();
                if(title.startsWith("Thread: ")) {
                    // it's always supposed to start with 'Thread:' but be defensive
                    title = title.substring("Thread: ".length());
                }

                Node firstLast = doc.selectSingleNode("//TH[@class='jive-first-last']//NOBR");

                String str = firstLast.getText().trim();

                int s = str.indexOf("Replies:")+8;
                if(s==-1)
                    throw new ProcessingException("Unable to find the reply count of the topic");
                replies = Integer.parseInt(str.substring(s).trim());

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

    public Post getPost() throws ProcessingException {
        if(post==null) {
            post = new Scraper<Post>("Parsing posts") {
                public Post scrape() throws IOException, SAXException {
                    WebResponse rsp = forum.wc.getResponse(getTopicURL().toExternalForm());
                    Document doc = Util.getDom4j(rsp);
                    List<Node> posts = doc.selectNodes("//DIV[@class='jive-messagebox']");

                    Map<Integer,Post> byIds = new HashMap<Integer,Post>();
                    Post rootPost = null;

                    for (Node post : posts) {
                        String postId = post.selectSingleNode("TABLE//TR[starts-with(@id,'jive-message-')]/@id").getText().trim().substring("jive-message-".length());
                        String poster = post.selectSingleNode(".//NOBR/A[starts-with(@href,'profile.jspa')]").getText().trim();
                        Node descriptionNode = post.selectSingleNode(".//TD[@class='jive-last']//SPAN[@class='jive-description']");
                        String date = Util.collapse(descriptionNode.getText().trim());
                        Matcher m = POSTED_DATE_PATTERN.matcher(date);
                        if(!m.matches())
                            throw new ProcessingException("Failed to parse "+date);
                        date = m.group(1);

                        String parent = null;
                        Node parentLink = descriptionNode.selectSingleNode("NOBR/A[IMG]/@href");
                        if(parentLink!=null) {
                            parent = parentLink.getText();
                            parent = parent.substring(parent.lastIndexOf('#')+1);
                        }

                        Element body = (Element) post.selectSingleNode(".//TD[@class='jive-last']/TABLE//TR/TD[@colspan='4']");

                        Post p = new Post(Integer.parseInt(postId),poster,new Date(date),
                            parent==null?null: byIds.get(Integer.parseInt(parent)), body);
                        byIds.put(p.getId(),p);

                        if(rootPost==null)
                            rootPost = p;
                    }

                    return rootPost;
                }
            }.run();
        }
        return post;
    }

    private static final Pattern POSTED_DATE_PATTERN = Pattern.compile("Posted: (.+ [AP]M).*");
}
