/**
 * @author  Christian Schönweiß, University Freiburg
 * @since   09.02.2015
 * 
 */

package Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <h1>Communication Thread</h1> Every Monitor connecting to Server gets its own
 * Communication Thread to communicate to Controller and vice versa using
 * out()-method or (not used yet) in()-method
 * 
 */
class CommunicationThread extends Thread {

	// given socket connection of Server
	private final Socket socket;

	// if new message is ready to send
	private boolean newMessage = true;
	// not used yet, but if communication to Monitor should be vice versa
	private String incomeMessage = "";

	// -- GETTER / SETTER --

	public Socket getSocket() {
		return socket;
	}

	public String getIncomeMessage() {
		return incomeMessage;
	}

	public void setIncomeMessage(String incomeMessage) {
		this.incomeMessage = incomeMessage;
	}

	public boolean isNewMessage() {
		return newMessage;
	}

	public void setNewMessage(boolean newMessage) {
		this.newMessage = newMessage;
	}

	// -- END GETTER / SETTER --

	/**
	 * 
	 * CONSTRUCTOR Creates a Communication Thread for every connected Client
	 * based on the given Socket connection
	 * 
	 * @param socket
	 *            Socket (Client) given after Monitor connects to Server
	 * 
	 */
	CommunicationThread(Socket socket) {
		this.socket = socket;
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
		}
	}

	/**
	 * @return Message from other device (Monitor) as string
	 */
	/*
	 * public String in() { String inStr = ""; StringBuilder completeString =
	 * new StringBuilder(); try { this.inMessage = new BufferedReader(new
	 * InputStreamReader( this.socket.getInputStream()));
	 * 
	 * while ((inStr = inMessage.readLine()) != null) {
	 * completeString.append(inStr); } } catch (IOException e) {
	 * e.printStackTrace(); } return completeString.toString(); }
	 */

	/**
	 * 
	 * Sends JSON-String to Monitor using BufferedWriter and given Socket
	 * connection
	 * 
	 * @param jsonString
	 *            JSON-String is build by GSON Builder, send to Monitor
	 * @throws IOException
	 *             exceptions for Socket, flush or writer Errors
	 * 
	 */
	void out(String jsonString) throws IOException {
		try {

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			writer.write(jsonString + "\n");
			writer.flush();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
