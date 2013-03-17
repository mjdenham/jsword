/**
 * Distribution License:
 * JSword is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, version 2.1 or later
 * as published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The License is available on the internet at:
 *       http://www.gnu.org/copyleft/lgpl.html
 * or by writing to:
 *      Free Software Foundation, Inc.
 *      59 Temple Place - Suite 330
 *      Boston, MA 02111-1307, USA
 *
 * Copyright: 2005 - 2013
 *     The copyright to this program is held by it's authors.
 *
 */
package org.crosswire.jsword.versification;

import junit.framework.TestCase;

import org.crosswire.jsword.book.CaseType;
import org.crosswire.jsword.passage.NoSuchVerseException;
import org.crosswire.jsword.passage.Verse;
import org.crosswire.jsword.versification.system.SystemMT;
import org.crosswire.jsword.versification.system.Versifications;

/**
 * JUnit Test.
 * 
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's authors.
 * @author Joe Walker [joe at eireneh dot com]
 */
public class VersificationParentTst extends TestCase {
    private Versification v11n;
    private CaseType storedCase;

    public VersificationParentTst(String s, String v11nName) {
        super(s);
        this.v11n = Versifications.instance().getVersification(v11nName);
    }

    @Override
    protected void setUp() {
        storedCase = BookName.getDefaultCase();
    }

    @Override
    protected void tearDown() {
        BookName.setCase(storedCase);
    }

    public void testIn() throws Exception {
        // Counts using loops
        int verseCount = 0;

        // For all the books
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            // Continue the verse counts for the whole Bible
            for (int c = 0; c <= v11n.getLastChapter(b); c++) {
                verseCount += v11n.getLastVerse(b, c) + 1;
            }
        }

        assertEquals(verseCount, v11n.maximumOrdinal() + 1);
        assertEquals(verseCount, v11n.getCount(null));
    }

    public void testOrdinal() throws Exception {
        int ordinal = 0;
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            int cib = v11n.getLastChapter(b);
            for (int c = 0; c <= cib; c++) {
                int vic = v11n.getLastVerse(b, c);
                for (int v = 0; v <= vic; v++) {
                    Verse verse = new Verse(v11n, b, c, v);
                    assertEquals(verse.getOsisID(), ordinal++, v11n.getOrdinal(verse));
                }
            }
        }
    }

    public void testDecodeOrdinal() throws Exception {
        int ordinal = 0;
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            int cib = v11n.getLastChapter(b);
            for (int c = 0; c <= cib; c++) {
                int vic = v11n.getLastVerse(b, c);
                for (int v = 0; v <= vic; v++) {
                    Verse verse = new Verse(v11n, b, c, v);
                    assertEquals(verse.getOsisID(), verse, v11n.decodeOrdinal(ordinal++));
                }
            }
        }
    }

    public void testValidate() throws Exception {
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            // Test that negative chapters are invalid
            try {
                v11n.validate(b, -1, 0);
                fail();
            } catch (NoSuchVerseException ex) {
            }

            for (int c = 0; c <= v11n.getLastChapter(b); c++) {
                // Test that negative verse are invalid
                try {
                    v11n.validate(b, c, -1);
                    fail();
                } catch (NoSuchVerseException ex) {
                }

                // test that every verse, including introductions are valid
                for (int v = 0; v <= v11n.getLastVerse(b, c); v++) {
                    v11n.validate(b, c, v);
                }

                // test that every verses past the end are invalid
                try {
                    v11n.validate(b, c, v11n.getLastVerse(b, c) + 1);
                    fail();
                } catch (NoSuchVerseException ex) {
                }
            }

            // test that every chapters past the end are invalid
            try {
                v11n.validate(b, v11n.getLastChapter(b) + 1, 0);
                fail();
            } catch (NoSuchVerseException ex) {
            }
        }
    }

    public void testPatch() throws Exception {

        int all = 0;
        // For all the books
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            int cib = v11n.getLastChapter(b);
            for (int c = 0; c <= cib; c++) {
                int vic = v11n.getLastVerse(b, c);
                for (int v = 0; v <= vic; v++) {
                    Verse pv = v11n.patch(BibleBook.INTRO_BIBLE, 0, all);
                    assertEquals(pv.getName(), b, pv.getBook());
                    assertEquals(pv.getName(), c, pv.getChapter());
                    assertEquals(pv.getName(), v, pv.getVerse());
                    all++;
                }
            }
        }

        Verse gen11 = new Verse(v11n, BibleBook.GEN, 1, 1);
        assertEquals(gen11, v11n.patch(BibleBook.GEN, 1, 1));
        assertEquals(gen11, v11n.patch(BibleBook.GEN, 0, 2));
        assertEquals(gen11, v11n.patch(null, 3, 1));
        assertEquals(gen11, v11n.patch(null, 0, 4));
    }

    public void testVerseCount() throws Exception {
        int count_up = 0;
        Verse firstVerse = new Verse(v11n, v11n.getFirstBook(), 0, 0);
        BibleBook lastBook = v11n.getLastBook();
        int lastChapter = v11n.getLastChapter(lastBook);
        Verse lastVerse = new Verse(v11n, lastBook, lastChapter, v11n.getLastVerse(lastBook, lastChapter));
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            int cib = v11n.getLastChapter(b);
            for (int c = 0; c <= cib; c++) {
                int vic = v11n.getLastVerse(b, c);
                for (int v = 0; v <= vic; v++) {
                    Verse curVerse = new Verse(v11n, b, c, v);
                    int up = v11n.distance(firstVerse, curVerse) + 1;
                    assertEquals(++count_up, up);
//                    assertEquals(verseCountSlow(gen00, curVerse), up);
                }
            }

        }
        int count_down = v11n.maximumOrdinal();
        assertEquals(v11n.getOrdinal(lastVerse), count_down);
        for (BibleBook b = v11n.getFirstBook(); b != null; b = v11n.getNextBook(b)) {
            int cib = v11n.getLastChapter(b);
            for (int c = 0; c <= cib; c++) {
                int vic = v11n.getLastVerse(b, c);
                for (int v = 0; v <= vic; v++) {
                    Verse curVerse = new Verse(v11n, b, c, v);
                    int down = v11n.distance(curVerse, lastVerse);
                    assertEquals(count_down--, down);
                }
            }

        }
    }

    public void testNames() {
        // AV11N(DMS): Is this right?
        assertEquals(2, BibleBook.GEN.ordinal());
        assertEquals(68, BibleBook.REV.ordinal());
    }

    public void testMTSystem() {
        // The MT v11n is OT only and had a problem where this would have failed.
        // At this time all versifications have an OT and Gen 1:1
        Versification v11nMT = Versifications.instance().getVersification(SystemMT.V11N_NAME);
        Verse verse = new Verse(v11n, BibleBook.GEN, 1, 1);
        int index = verse.getOrdinal();
        Testament testament = v11nMT.getTestament(index);
        assertEquals("Gen 1:1 is old testament", Testament.OLD, testament);
        assertEquals("ordinals in OT do not change", index, v11n.getTestamentOrdinal(index));
    }

}