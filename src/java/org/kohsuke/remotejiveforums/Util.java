package org.kohsuke.remotejiveforums;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import java.util.Collection;
import java.util.Iterator;

/**
 * Utility code.
 *
 * @author
 *      Kohsuke Kawaguchi (kk@kohsuke.org)
 */
class Util {
    /** Finds the first element child, or null if not found. */
    static Element getFirstElementChild( Element parent ) {
        for( Node n = parent.getFirstChild(); n!=null; n=n.getNextSibling() ) {
            if(n.getNodeType()==Node.ELEMENT_NODE)
                return (Element)n;
        }
        return null;
    }

    /**
     * Gets the value for an item in a combo box.
     */
    static String getOptionValueFor( WebForm form, String parameter, String displayString ) throws ProcessingException {
        String[] options = form.getOptions(parameter);
        for (int i = 0; i < options.length; i++) {
            if( options[i].replace((char)0xA0,' ').equals(displayString) ) {
                return form.getOptionValues(parameter)[i];
            }
        }
        throw new ProcessingException("No such option:"+displayString);
    }

    /**
     * Finds a hyper-link that has the specified text and whose
     * target URL starts with the given prefix.
     */
    static WebLink findLink( WebConversation wc, String text, String urlPrefix ) throws ProcessingException, SAXException {
        WebLink[] links = wc.getCurrentPage().getLinks();
        for( int i=0; i<links.length; i++ ) {
            if( links[i].asText().indexOf(text)==-1 )
                continue;
            if( !links[i].getURLString().startsWith(urlPrefix))
                continue;

            return links[i];
        }

        throw new ProcessingException("no link found for '"+text+'\'');
    }

    /**
     * Obtains the HTML of the response as a dom4j document.
     */
    static Document getDom4j( WebResponse wr ) throws SAXException {
        return new DOMReader().read(wr.getDOM());
    }

    /**
     * Format all strings in the collection by using the specified separator.
     */
    static String toList(Collection addresses, char separator) {
        StringBuffer buf = new StringBuffer();
        for (Iterator itr = addresses.iterator(); itr.hasNext();) {
            String address = (String) itr.next();
            if(buf.length()>0)  buf.append(separator);
            buf.append(address);
        }
        return buf.toString();
    }

    /**
     * Gets the form with a specified action, or throw an error
     */
    public static WebForm getFormWithAction(WebResponse response, String actionName) throws ProcessingException, SAXException {
        WebForm[] forms = response.getForms();
        for( int i=0; i<forms.length; i++ ) {
            WebForm form = forms[i];

            if(form.getAction().equals(actionName))
                return form;
        }

        throw new ProcessingException("Unable to find a form with action="+actionName);
    }

    /**
     * Collapses whitespace
     */
    public static String collapse(String text) {
        int len = text.length();

        // most of the texts are already in the collapsed form.
        // so look for the first whitespace in the hope that we will
        // never see it.
        int s=0;
        while(s<len) {
            if(isWhiteSpace(text.charAt(s)))
                break;
            s++;
        }
        if(s==len)
            // the input happens to be already collapsed.
            return text;

        // we now know that the input contains spaces.
        // let's sit down and do the collapsing normally.

        StringBuilder result = new StringBuilder(len /*allocate enough size to avoid re-allocation*/ );

        if(s!=0) {
            for( int i=0; i<s; i++ )
                result.append(text.charAt(i));
            result.append(' ');
        }

        boolean inStripMode = true;
        for (int i = s+1; i < len; i++) {
            char ch = text.charAt(i);
            boolean b = isWhiteSpace(ch);
            if (inStripMode && b)
                continue; // skip this character

            inStripMode = b;
            if (inStripMode)
                result.append(' ');
            else
                result.append(ch);
        }

        // remove trailing whitespaces
        len = result.length();
        if (len > 0 && result.charAt(len - 1) == ' ')
            result.setLength(len - 1);
        // whitespaces are already collapsed,
        // so all we have to do is to remove the last one character
        // if it's a whitespace.

        return result.toString();
    }

    public static final boolean isWhiteSpace(char ch) {
        return ch == 0x9 || ch == 0xA || ch == 0xD || ch == 0x20;
    }
}
