/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.gui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.scene.Scene;
import madkit.kernel.Agent;
import madkit.kernel.FXAgentStage;

/**
 * This agent displays standard out and err prints in its GUI. This agent is
 * useful when the application is not launched from a command line or an IDE so
 * that the console is not directly visible.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 6.0
 */
class ConsoleAgent extends Agent {

	private static final PrintStream systemOut = System.out;// NOSONAR
	private static final PrintStream systemErr = System.err;// NOSONAR

	@Override
	protected void onActivation() {
		setupDefaultGUI();
		getLogger().info("hello");
		System.err.println("test");
	}

	@Override
	public void setupDefaultGUI() {
		FXExecutor.runAndWait(() -> {
			FXAgentStage stage = new FXAgentStage(this);
			FXOutputPane outP = new FXOutputPane(this);
			Scene scene = new Scene(outP);
			System.setOut(new PrintStream(new StreamCapturer(outP, systemOut)));
			System.setErr(new PrintStream(new StreamCapturer(outP, systemErr)));
//			scene.setSize(800, 500);
			stage.setScene(scene);
			stage.show();
		});
	}

	@Override
	protected void onEnd() {
		System.setErr(systemErr);
		System.setOut(systemOut);
	}

	public static void main(String[] args) {
		executeThisAgent();
	}

}

class StreamCapturer extends OutputStream {

	private FXOutputPane panel;
	private PrintStream capturedStream;
	private ByteArrayOutputStream buffer;

	public StreamCapturer(FXOutputPane consumer, PrintStream old) {
		this.capturedStream = old;
		this.panel = consumer;
		this.buffer = new ByteArrayOutputStream(128);
	}

	@Override
	public void write(int b) throws IOException {
		capturedStream.write(b);
		buffer.write(b);
		if ((char) b == '\n') {
			byte[] byteArray = buffer.toByteArray();
			panel.writeToTextArea(new String(byteArray));
			buffer.reset();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		capturedStream.write(b, off, len);
		buffer.write(b, off, len);
		if (new String(b, off, len).contains("\n")) {
			byte[] byteArray = buffer.toByteArray();
			panel.writeToTextArea(new String(byteArray));
			buffer.reset();
		}
	}

	@Override
	public void flush() throws IOException {
		capturedStream.flush();
	}

	@Override
	public void close() throws IOException {
		capturedStream.close();
	}
}