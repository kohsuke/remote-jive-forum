package org.kohsuke.remotejiveforums;

import org.dom4j.Element;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class Post {
    private final int id;
    private final String poster;
    private final Date date;
    private final Post parent;
    private final List<Post> replies = new ArrayList<Post>();
    private final Element body;

    public Post(int id, String poster, Date date, Post parent, Element body) {
        this.id = id;
        this.poster = poster;
        this.date = date;
        this.parent = parent;
        if(parent!=null)
            parent.replies.add(this);
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public String getPoster() {
        return poster;
    }

    public Date getDate() {
        return date;
    }

    public Post getParent() {
        return parent;
    }

    public List<Post> getReplies() {
        return Collections.unmodifiableList(replies);
    }

    public Element getBody() {
        return body;
    }
    
    public String getBodyString() {
        return Util.collapse(body.getText()).trim();
    }
}
