package org.crosswire.common.xml;

import org.crosswire.common.util.Logger;
import org.crosswire.jsword.book.DataPolice;
import org.xml.sax.Attributes;

/**
 * Utilities for working with SAX XML parsing.
 * 
 * <p><table border='1' cellPadding='3' cellSpacing='0'>
 * <tr><td bgColor='white' class='TableRowColor'><font size='-7'>
 *
 * Distribution Licence:<br />
 * JSword is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License,
 * version 2 as published by the Free Software Foundation.<br />
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.<br />
 * The License is available on the internet
 * <a href='http://www.gnu.org/copyleft/gpl.html'>here</a>, or by writing to:
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA<br />
 * The copyright to this program is held by it's authors.
 * </font></td></tr></table>
 * @see gnu.gpl.Licence
 * @author Joe Walker [joe at eireneh dot com]
 * @version $Id$
 */
public class XMLUtil
{
    /**
     * Prevent Instansiation
     */
    private XMLUtil()
    {
    }

    /**
     * Show the attributes of an element as debug
     */
    public static void debugSAXAttributes(Attributes attrs)
    {
        for (int i=0; i<attrs.getLength(); i++)
        {
            log.debug("attr["+i+"]: "+attrs.getQName(i)+"="+attrs.getValue(i));
        }
    }

    /**
     * The parser failed, and it might be because there are control characters
     * that are not valid in XML in the string, so this method strips them out
     */
    public static String cleanInvalidCharacters(String broken)
    {
        if (broken == null)
        {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        
        for (int i = 0; i < broken.length(); i++)
        {
            char c = broken.charAt(i);
            if (c < 32 && c != 9 && c != 10 && c != 13)
            {
                // since it is a control char, we want to see the number and not a character
                int num = c;
                DataPolice.report("disguarding illegal XML character: "+num);
            }
            else
            {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    /**
     * A parse has failed so we can try to kill the broken entities and then have
     * another go.
     */
    public static String cleanAllEntities(String broken)
    {
        if (broken == null)
        {
            return null;
        }

        String working = broken;
        int cleanfrom = 0;

        allEntities:
        while (true)
        {
            int amp = working.indexOf('&', cleanfrom);

            // If there are no more amps then we are done
            if (amp == -1)
            {
                break allEntities;
            }

            // Check for chars that should not be in an entity name
            int i = amp + 1;
            singleEntity:
            while (true)
            {
                // if we are at the end of the string the disgard from the & on
                if (i >= working.length())
                {
                    String entity = working.substring(amp);
                    String replace = guessEntity(entity);
                    DataPolice.report("replacing unterminated entity: '"+entity+"' with: '"+replace+"'");

                    working = working.substring(0, amp)+replace;
                    break singleEntity;
                }

                // if we have come to an ; then we just have an entity that isn't
                // properly declared, (or maybe it is but something else is
                // broken) so disgard it
                char c = working.charAt(i);
                if (c == ';')
                {
                    String entity = working.substring(amp, i+1);
                    String replace = guessEntity(entity);
                    DataPolice.report("replacing entity: '"+entity+"' with: '"+replace+"'");
                    
                    working = working.substring(0, amp)+replace+working.substring(i+1);
                    break singleEntity;
                }

                // XML entities are letters, numbers or -????
                // If we find something else then dump the entity
                if (!Character.isLetterOrDigit(c) && c != '-')
                {
                    String entity = working.substring(amp, i);
                    String replace = guessEntity(entity);
                    DataPolice.report("replacing invalid entity: '"+entity+"' with: '"+replace+"'");

                    working = working.substring(0, amp)+replace+working.substring(i);
                    break singleEntity;
                }

                i++;
            }

            cleanfrom = amp + 1;
        }

        return working;
    }

    /**
     * Attempt to guess what the entity should have been and fix it, or remove
     * it if there are no obvious replacements.
     */
    private static String guessEntity(String broken)
    {
        // strip any beginning & or ending ;
        if (broken.endsWith(";"))
        {
            broken = broken.substring(0, broken.length()-1);
        }
        if (broken.startsWith("&"))
        {
            broken = broken.substring(1);
        }

        // pre-defined XML entities
        if ("amp".equals(broken))
        {
            return "&#38;";
        }
        if ("lt".equals(broken))
        {
            return "&#60;";
        }
        if ("gt".equals(broken))
        {
            return "&#62;";
        }
        if ("quot".equals(broken))
        {
            return "&#34;";
        }

        // common HTML entities
        if ("nbsp".equals(broken))
        {
            return "&#160;";
        }
        if ("pound".equals(broken))
        {
            return "&#163;";
        }
        if ("yen".equals(broken))
        {
            return "&#165;";
        }
        if ("euro".equals(broken))
        {
            return "&#8364;";
        }
        if ("copy".equals(broken))
        {
            return "&#169;";
        }
        if ("para".equals(broken))
        {
            return "&#182;";
        }
        if ("lsquo".equals(broken))
        {
            return "&#8216;";
        }
        if ("rsquo".equals(broken))
        {
            return "&#8217;";
        }

        return "";
    }
    
    /**
     * XML parse failed, so we can try getting rid of all the tags and having
     * another go. We define a tag to start at a &lt; and end at the end of the
     * next word (where a word is what comes in between spaces) that does not
     * contain an = sign, or at a >, whichever is earlier.
     */
    public static String cleanAllTags(String broken)
    {
        if (broken == null)
        {
            return null;
        }

        String working = broken;

        allTags:
        while (true)
        {
            int lt = working.indexOf('<');

            // If there are no more amps then we are done
            if (lt == -1)
            {
                break allTags;
            }

            // loop to find the end of this tag
            int i = lt;
            int startattr = -1;

            singletag:
            while (true)
            {
                i++;

                // the tag can't exist past the end of the string
                if (i >= working.length())
                {
                    // go back one so we can safely chop
                    i--;
                    break singletag;
                }
                
                char c = working.charAt(i);

                // normal end of tag
                if (c == '>')
                {
                    break singletag;
                }

                // we declare end-of-tag if this 'word' is not an attribute
                if (c == ' ')
                {
                    if (startattr == -1)
                    {
                        // NOTE(joe): should we skip over consecutive spaces?
                        startattr = i;
                    }
                    else
                    {
                        // so we've already had a space indicating start of
                        // attribute, so this must be the beginning of the next
                        // NOTE(joe): no - spaces can exist in attr values
                        String value = working.substring(startattr, i);
                        if (value.indexOf("=") == -1)
                        {
                            // this 'attribute' does not contain an equals so
                            // we call it a word and end the parse
                            break singletag;
                        }
                    }
                }
            }

            // So we have the end of the tag, delete it ...
            DataPolice.report("disguarding tag: "+working.substring(lt, i+1));
            working = working.substring(0, lt)+working.substring(i+1);
        }
        
        return working;
    }

    /**
     * The log stream
     */
    protected static final Logger log = Logger.getLogger(XMLUtil.class);
}