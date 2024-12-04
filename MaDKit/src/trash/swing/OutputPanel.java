package madkit.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import madkit.kernel.Agent;
import madkit.kernel.AgentFrame;
import madkit.kernel.AgentLogger;

/**
 * A scrollable panel that prints all the agent's logging activity.
 * This component is the default panel which is used for the frames assigned to agents that do not define their own GUI
 * and which are launched using <code>true</code> for the <code>createFrame</code> parameter.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 1
 * @see Agent#setupGUI()
 */
public class OutputPanel extends JPanel {

    private static final long serialVersionUID = 602152712654986449L;

    private final JTextArea textArea;

    private transient StreamHandler handler;

    /**
     * Builds the panel for the agent
     * 
     * @param agent
     */
    public OutputPanel(final Agent agent) {
	textArea = new JTextArea(5, 32);
	setLayout(new BorderLayout());

	textArea.setEditable(false);
	setPreferredSize(new Dimension(250, 100));
	setBackground(Color.WHITE);

	initHandler(agent);

	add(BorderLayout.CENTER, new JScrollPane(textArea));

	final JButton b = new JButton("clear");// TODO i18n
	b.addActionListener((evt) -> clearOutput());
	add(BorderLayout.SOUTH, b);
	setBackground(Color.WHITE);
    }

    public void writeToTextArea(String text) {
	if (EventQueue.isDispatchThread()) {
	    textArea.append(text);
	    textArea.setCaretPosition(textArea.getText().length());
	}
	else {
	    EventQueue.invokeLater(() -> writeToTextArea(text));
	}
    }


    /**
     * Remove all the contained text.
     */
    public void clearOutput() {
	textArea.setText(null);
    }

    @Override
    public void setBackground(Color bg) {
	if (textArea != null) {
	    textArea.setBackground(bg);
	}
	super.setBackground(bg);
    }

    private void initHandler(Agent agent) {
        handler = new StreamHandler() {
    
            @Override
            public synchronized void publish(LogRecord record) {
        	if (!isLoggable(record)) {
        	    return;
        	}
        	String msg;
        	try {
        	    msg = getFormatter().format(record);
        	}
        	catch(Exception ex) {
        	    // We don't want to throw an exception here, but we
        	    // report the exception to any registered ErrorManager.
        	    reportError(null, ex, ErrorManager.FORMAT_FAILURE);
        	    return;
        	}
        	writeToTextArea(msg);
    
            }
    
            @Override
            public boolean isLoggable(LogRecord record) {
        	final int levelValue = getLevel().intValue();
        	if (record.getLevel().intValue() < levelValue || levelValue == Level.OFF.intValue()) {
        	    return false;
        	}
        	final Filter filter = getFilter();
        	if (filter == null) {
        	    return true;
        	}
        	return filter.isLoggable(record);
            }
        };
        handler.setFormatter(AgentLogger.AGENT_FILE_FORMATTER);
        agent.getLogger().addHandler(getHandler());
    }

    /**
     * Returns the handler which has been created for the agent
     * 
     * @return the handler associated with this panel
     */
    public StreamHandler getHandler() {
	return handler;
    }

}
