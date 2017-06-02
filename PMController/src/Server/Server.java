/**
 * @author  Christian Schönweiß, University Freiburg
 * @since   09.02.2015
 * 
 */

package Server;

import gui.MainActivity;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.util.Log;

import Server.CommunicationThread;

/**
 * <h1>Server (Server Socket)</h1>
 * Every Server holds a Server Thread and a CommunicationThread
 * to communicate over TCP and JSON with its out()-Method to a Monitor
 * 
 */
public class Server {

	private ServerSocket serverSocket;
	private Thread serverThread = null;
	private CommunicationThread commThread;

	// List of CommunicationThreads
	private ArrayList<CommunicationThread> commThreads = new ArrayList<CommunicationThread>();

	// Different debugging tags for LogCat
	private final String tagST = "ServerThread";
	private final String tagS = "Server";
	private final String tagNC = "Network";

	// Manager for Android Network Discovery on Controller
	private NsdManager mNsdManager;
	private RegistrationListener mRegistrationListener;
	private String serviceName;
	private NsdServiceInfo mServiceInfo;

	// Hardcoded controller port because of usability (Task of network admin to
	// open port 3000
	private int controllerPort = 3000;

	// -- GETTER / SETTER --

	public NsdManager getmNsdManager() {
		return mNsdManager;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getControllerPort() {
		return controllerPort;
	}

	// -- End GETTER / SETTER --

	/**
	 * 
	 * Creates a Server object to set Service name of NSD Manager and starts the
	 * Server Thread to register Service under the given Service name
	 * 
	 * @param serviceName
	 *            User input: Service Name was set by user before
	 */
	public Server(String serviceName) {
		this.serviceName = serviceName;
		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();
	}

	/**
	 * Interrupts Thread and closes Server Socket connection
	 */
	public void tearDownServer() {
		serverThread.interrupt();
		try {
			serverSocket.close();
		} catch (IOException ioe) {
			Log.e(tagS, "Error when closing server socket.");
		}
	}

	/**
	 * 
	 * Sends JSONstring to every Monitor over its socket connection
	 * (CommunicationThread)
	 * 
	 * @param jsonString
	 *            JSON-String created by an event before sending
	 * 
	 */
	public void out(String jsonString) {
		for (CommunicationThread commThread : commThreads) {
			try {
				if (commThread == null) {
					Log.d(tagS, "No Communiction Handler initialized");
					return;
				}
				Log.d(tagS, "Controller sends: " + jsonString);
				commThread.out(jsonString);
			} catch (IOException e) {
				System.err.println("Caught IOException: " + e.getMessage());
			}
		}
	}

	/**
	 * <h1>Server Thread</h1> The Server Thread establishs a ServerSocket on a
	 * hardcoded Port (because of usability for user) and registers a Network
	 * Service via NSD Manager in its network Ever new Client connecting to
	 * Server gets its own Thread to communicate with the Server Socket via
	 * Socket connection
	 * 
	 */
	private class ServerThread implements Runnable {

		public void run() {
			try {
				// Initialize a server socket on port: controllerPort
				serverSocket = new ServerSocket(controllerPort);
			} catch (Exception e) {
				System.err.println("Caught IOException: " + e.getMessage());
			}
			// Register Service in the Network over NSDManager
			registerService(controllerPort);
			Log.d(tagST, "Server running on port: " + getControllerPort());

			while (!Thread.currentThread().isInterrupted()) {
				try {
					// starts Communication Thread for every new client
					// connecting to Server
					commThread = (new CommunicationThread(serverSocket.accept()));
					Log.d(tagST, "Client connected");
					commThreads.add(commThread);
					commThread.start();
					Log.d(tagST, "Connection Handler started");
				} catch (IOException e1) {
					System.err
							.println("Caught IOException: " + e1.getMessage());
				} catch (NullPointerException e2) {
					System.err.println("Caught NullPointerException: "
							+ e2.getMessage());
				}
			}
		}
	}

	/**
	 * Gets controllerPort from ServerSocket to register Service in the Network
	 * over NsdServiceInfo uses HTTP over TCP
	 */
	private void registerService(int port) {
		mServiceInfo = new NsdServiceInfo();
		mServiceInfo.setServiceName(serviceName);
		mServiceInfo.setServiceType("_http._tcp.");
		mServiceInfo.setPort(port);

		mNsdManager = (NsdManager) MainActivity.getAppContext()
				.getSystemService(Context.NSD_SERVICE);

		initializeRegistrationListener();

		// registers Service with given parameters
		mNsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD,
				mRegistrationListener);
	}

	/**
	 * Listener for events on registration of service
	 */
	private void initializeRegistrationListener() {
		mRegistrationListener = new NsdManager.RegistrationListener() {

			@Override
			public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
				serviceName = NsdServiceInfo.getServiceName();
				Log.d(tagNC, "Service name: " + serviceName);
				Log.d(tagNC, "Port number: " + controllerPort);
			}

			@Override
			public void onRegistrationFailed(NsdServiceInfo serviceInfo,
					int errorCode) {
				Log.d(tagNC, "Registration Failed! Error code: " + errorCode);
			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo arg0) {
				Log.d(tagNC, "NSD-Service unregistered.");
			}

			@Override
			public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
					int errorCode) {
				Log.d(tagNC, "NSD-Unregistration failed!" + errorCode);
			}
		};
	}

	/**
	 * unregisters Service in network to reuse Session Name onDestroy()
	 */
	public void tearDownNSD() {
		mNsdManager.unregisterService(mRegistrationListener);
	}

}
