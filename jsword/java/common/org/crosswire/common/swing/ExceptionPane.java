
package org.crosswire.common.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.crosswire.common.util.LucidException;
import org.crosswire.common.util.MsgBase;
import org.crosswire.common.util.Reporter;
import org.crosswire.common.util.StackTrace;
import org.crosswire.common.util.event.ReporterEvent;
import org.crosswire.common.util.event.ReporterListener;

/**
 * A simple way of reporting problems to the user.
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
 * @see docs.Licence
 * @author Joe Walker [joe at eireneh dot com]
 * @version $Id$
 */
public class ExceptionPane extends JPanel
{
    public static void main(String[] args)
    {
        try
        {
            die1();
        }
        catch (LucidException ex)
        {
            //showExceptionDialog(null, ex);
            showExceptionDialog(null, ex);
        }
    }

    private static void die1() throws LucidException
    {
        try
        {
            die2();
        }
        catch (NullPointerException ex)
        {
            throw new LucidException(new MsgBase("Bar"){}, ex);
        }
    }

    private static void die2()
    {
        throw new NullPointerException("Foo");
    }

    public ExceptionPane(Throwable ex)
    {
        this.ex = ex;
        jbInit();
    }

    private void jbInit()
    {
        String exmsg = "<html><font size=\"-1\">An error has occured:</font> "+ExceptionPane.getHTMLDescription(ex);
        
        // The upper pane
        message.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        message.setText(exmsg);
        message.setIcon(GuiUtil.getIcon("/toolbarButtonGraphics/general/Stop24.gif"));
        message.setIconTextGap(20);
        
        banner.setLayout(new BorderLayout());
        banner.add(message, BorderLayout.CENTER);
        list.setVisibleRowCount(6);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(courier);
        
        setDisplayedException(ex);
        
        // The buttons at the bottom
        ok.setText("OK");
        ok.setMnemonic('O');
        
        detail.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent ev)
            {
                changeDetail();
            }
        });
        detail.setText("Details");
        
        spacer.setLayout(new FlowLayout());
        spacer.add(ok);
        
        buttons.setLayout(new BorderLayout());
        buttons.add(spacer, BorderLayout.CENTER);
        buttons.add(detail, BorderLayout.WEST);
        
        upper.setLayout(new BorderLayout());
        upper.add(banner, BorderLayout.NORTH);
        upper.add(buttons, BorderLayout.CENTER);
        
        // The lower pane
        label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        label.setFont(courier);
        label.setText("No File");
        text.setEditable(false);
        text.setFont(courier);
        text_scroll.setColumnHeaderView(label);
        
        Throwable[] exs = ExceptionUtils.getThrowables(ex);
        traces.setModel(new DefaultComboBoxModel(exs));
        traces.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ev)
            {
                Throwable th = (Throwable) traces.getSelectedItem();
                setDisplayedException(th);
            }
        });
        
        heading.setLayout(new BorderLayout());
        heading.add(traces, BorderLayout.CENTER);
        
        lower.setLayout(new BorderLayout());
        lower.add(split, BorderLayout.CENTER);
        lower.add(heading, BorderLayout.NORTH);
        
        split.setOrientation(JSplitPane.VERTICAL_SPLIT);
        split.setContinuousLayout(true);
        split.setTopComponent(list_scroll);
        split.setBottomComponent(text_scroll);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setPreferredSize(new Dimension(500, 300));
        
        this.setLayout(new BorderLayout());
        this.add(upper, BorderLayout.NORTH);
    }

    /**
     * Is the detail area shown?
     */
    protected void changeDetail()
    {
        if (detail.isSelected())
        {
            ExceptionPane.this.add(lower, BorderLayout.CENTER);
        }
        else
        {
            ExceptionPane.this.remove(lower);
        }
                
        GuiUtil.getDialog(ExceptionPane.this).pack();
    }

    /**
     * Display a different nested exception
     */
    protected void setDisplayedException(Throwable ex)
    {
        StackTrace st = new StackTrace(ex);
        list.addListSelectionListener(new ExceptionPane.CustomLister(st, text, label));
        list.setModel(new StackTraceListModel(st));
    }

    /**
     * The exception we are displaying
     */
    private Throwable ex;

    // The components - contained, top to containing, bottom
    private JLabel message = new JLabel();
    private JPanel banner = new JPanel();
    private Font courier = new Font("Monospaced", Font.PLAIN, 12);
    private JList list = new JList();
    private JScrollPane list_scroll = new JScrollPane(list);
    private JPanel upper = new JPanel();
    private JLabel label = new JLabel();
    private JTextArea text = new JTextArea();
    private JScrollPane text_scroll = new JScrollPane(text);
    private JSplitPane split = new JSplitPane();
    private JButton ok = new JButton();
    private JCheckBox detail = new JCheckBox();
    private JPanel buttons = new JPanel();
    private JPanel spacer = new JPanel();
    private JPanel lower = new JPanel();
    protected JComboBox traces = new JComboBox();
    private JPanel heading = new JPanel();

    /**
     * The directories searched for source
     */
    protected static String[] source_path = new String[0];

    /**
     * The listener that pops up the ExceptionPanes
     */
    private static ExceptionPaneReporterListener li;

    /**
     * Show a dialog containing the exception
     * @param parent Something to attach the Dialog to
     * @param ex The Exception to display
     */
    public static void showExceptionDialog(Component parent, Throwable ex)
    {
        final ExceptionPane pane = new ExceptionPane(ex);

        // Setting for the whole dialog
        final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(parent));
        dialog.getRootPane().setDefaultButton(pane.ok);
        dialog.getRootPane().setLayout(new BorderLayout());
        dialog.getRootPane().setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, pane.upper.getBackground()));
        dialog.getRootPane().add(pane, BorderLayout.CENTER);
        dialog.setTitle("Error");

        pane.ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ev) { dialog.dispose(); }
        });

        //dialog.setModal(true);
    
        GuiUtil.centerWindow(dialog);
        dialog.setVisible(true);
        dialog.pack();
    
        // When it has closed
        //dialog.dispose();
        //dialog = null;
    }

    /**
     * Set the directories to search for source files.
     * @param source_path A string array of the source directories
     */
    public static void setSourcePath(String[] source_path)
    {
        ExceptionPane.source_path = source_path;
    }

    /**
     * Get the directories searched for source files.
     * @return A string array of the source directories
     */
    public static String[] getSourcePath()
    {
        return source_path;
    }

    /**
     * You must call setJoinHelpDesk() in order to start displaying
     * Exceptions sent to the Log, and in order to properly
     * close this class you must call it again (with false).
     * @param joined Are we listening to the Log
     */
    public static void setHelpDeskListener(boolean joined)
    {
        if (joined && li == null)
        {
            li = new ExceptionPaneReporterListener();
            Reporter.addReporterListener(li);
        }

        if (!joined && li != null)
        {
            Reporter.removeReporterListener(li);
            li = null;
        }
    }

    /**
     * You must call setJoinHelpDesk() in order to start displaying
     * Exceptions sent to the Log, and in order to properly
     * close this class you must call it again (with false).
     * @param joined Are we listening to the Log
     */
    public static boolean isHelpDeskListener()
    {
        return (li != null);
    }

    /**
     * Gets a short HTML description of an Exception for display in a
     * window
     */
    public static String getHTMLDescription(Throwable ex)
    {
        StringBuffer retcode = new StringBuffer();
    
        // The message in the exception
        String message = ex.getMessage();
        if (message == null || message.equals(""))
            message = "No description available";
        String orig = message;
        message = StringUtils.replace(orig, "\n", "<br>");
    
        // The name of the exception
        /*
        String classname = ex.getClass().getName();
        int lastdot = classname.lastIndexOf('.');
        if (lastdot != -1)
            classname = classname.substring(lastdot+1);
        if (classname.endsWith("Exception") && classname.length() > "Exception".length())
            classname = classname.substring(0, classname.length() - "Exception".length());
        if (classname.endsWith("Error") && classname.length() > "Error".length())
            classname = classname.substring(0, classname.length() - "Error".length());
        classname = StringUtil.createTitle(classname);
        if (classname.equals("IO")) classname = "Input / Output";
    
        retcode.append("<font size=\"-1\"><strong>");
        retcode.append(classname);
        retcode.append("</strong></font>");
        */
        retcode.append("<br>");
        retcode.append(message);
    
        // If this is a LucidException with a nested Exception
        if (ex instanceof LucidException)
        {
            Throwable nex = ((LucidException) ex).getCause();
            if (nex != null)
            {
                retcode.append("<p><br><font size=\"-1\">This was caused by: </font>");
                retcode.append(getHTMLDescription(nex));
            }
        }
    
        return retcode.toString();
    }

    /**
     * List listener to update the contents of the text area
     * whenever someone clicks in the list
     */
    static class CustomLister implements ListSelectionListener
    {
        /**
         * Initialize with the stuff we need to act on the
         * change, when the list is clicked.
         * @param st The list of elements in the exception
         * @param text The editable file
         * @param label The filename label
         */
        public CustomLister(StackTrace st, JTextArea text, JLabel label)
        {
            this.st = st;
            this.text = text;
            this.label = label;
        }

        /**
         * Update the contents of the text area and label
         * @param ev The data about the click
         */
        public void valueChanged(ListSelectionEvent ev)
        {
            if (ev.getValueIsAdjusting() == true) return;

            // Wait cursor
            SwingUtilities.getRoot(label).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Get a stack trace
            JList list = (JList) ev.getSource();
            int level = list.getSelectedIndex();
            String name = st.getClassName(level);

            if (name.indexOf('$') != -1)
                name = name.substring(0, name.indexOf('$'));
            int line_num = st.getLineNumber(level);
            String orig = name;

            // Find a file
            name = File.separator + StringUtils.replace(orig, ".", ""+File.separatorChar) + ".java";
            for (int i=0; i<source_path.length; i++)
            {
                File file = new File(source_path[i] + name);
                if (file.isFile() && file.canRead())
                {
                    // Found the file, load it into the window
                    StringBuffer data = new StringBuffer();

                    // Attempt to note the line to highlight
                    int selection_start = 0;
                    int selection_end = 0;

                    try
                    {
                        label.setText(file.getCanonicalPath());
                        LineNumberReader in = new LineNumberReader(new FileReader(file));
                        while (true)
                        {
                            String line = in.readLine();
                            if (line == null) break;
                            data.append(line).append("\n");

                            int current_line = in.getLineNumber();
                            if (current_line == line_num-1) selection_start = data.length();
                            if (current_line == line_num) selection_end = data.length()-1;
                        }
                    }
                    catch (Exception ex)
                    {
                        data.append(ex.getMessage());
                    }

                    // Actually set the text
                    text.setText(data.toString());
                    text.setSelectionStart(selection_start);
                    text.setSelectionEnd(selection_end);

                    SwingUtilities.getRoot(label).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
            }

            // If we can't find a matching file
            String error = "Can't open source for: '"+st.getClassName(level)+"' line: "+line_num+"\n";
            for (int i=0; i<source_path.length; i++)
            {
                error += "Tried: "+source_path[i]+name+"\n";
            }

            text.setText(error);
            SwingUtilities.getRoot(label).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        /** The StackTrace */
        private StackTrace st;

        /** The Text to write to */
        private JTextArea text;

        /** The Text to write to */
        private JLabel label;
    }

    /**
     * The ExceptionPane instance that we add to the Log
     */
    static class ExceptionPaneReporterListener implements ReporterListener
    {
        /**
         * Called whenever Reporter.informUser() is passed an Exception
         * @param ev The event describing the Exception
         */
        public void reportException(final ReporterEvent ev)
        {
            // This faf is to ensure that we don't break any SwingThread rules
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (ev.getSource() instanceof Component)
                    {
                        ExceptionPane.showExceptionDialog((Component) ev.getSource(),
                                                          ev.getException());
                    }
                    else
                    {
                        ExceptionPane.showExceptionDialog(null, ev.getException());
                    }
                }
            });
        }

        /**
         * Called whenever Reporter.informUser() is passed a message
         * @param ev The event describing the message
         */
        public void reportMessage(final ReporterEvent ev)
        {
            // This faf is to ensure that we don't break any SwingThread rules
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (ev.getSource() instanceof Component)
                    {
                        JOptionPane.showMessageDialog((Component) ev.getSource(),
                                                      ev.getMessage());
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(null, ev.getMessage());
                    }
                }
            });
        }
    }
}
