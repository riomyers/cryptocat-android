/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dirbaio.cryptocat;

import android.os.Build;
import org.jivesoftware.smack.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class CryptocatServer
{
	public final String id;
	public final String server, conferenceServer;
	public final int port;

	private final HashMap<String, CryptocatConversation> conversations = new HashMap<>();
	private final ArrayList<CryptocatStateListener> listeners = new ArrayList<>();

	private String username, password;
	Connection con;

	private State state;

	public enum State
	{
		Disconnected,
		Connecting,
		Connected,
	}

	public State getState()
	{
		return state;
	}

	public CryptocatServer(String server, String conferenceServer, int port)
	{
		this.id = server;
		this.server = server;
		this.conferenceServer = conferenceServer;
		this.port = port;
		this.state = State.Disconnected;
	}

	public void connect() throws XMPPException
	{
		if (state == State.Connected)
			throw new IllegalStateException("You're already connected to this server.");

		try
		{
			//Done!
			state = State.Connecting;
			notifyStateChanged();

			//No idea wtf this is
			SmackConfiguration.setLocalSocks5ProxyEnabled(false);

			//Setup connection
			ConnectionConfiguration config = new ConnectionConfiguration(server, port);

			//Android trust store shenaniagans
			//This is still broken :(
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				config.setTruststoreType("AndroidCAStore");
				config.setTruststorePassword(null);
				config.setTruststorePath(null);
			} else
			{
				config.setTruststoreType("BKS");
				String path = System.getProperty("javax.net.ssl.trustStore");
				if (path == null)
					path = System.getProperty("java.home") + File.separator + "etc"
							+ File.separator + "security" + File.separator
							+ "cacerts.bks";
				System.err.println("Trust path: " + path);
				config.setTruststorePath(path);
			}

			// Connect to the server
			con = new XMPPConnection(config);


			con.connect();
			con.addConnectionListener(new ConnectionListener()
			{
				@Override
				public void connectionClosed()
				{
					//TODO dejoin conversations
					state = State.Disconnected;
					notifyStateChanged();
				}

				@Override
				public void connectionClosedOnError(Exception e)
				{
					//TODO dejoin conversations
					state = State.Disconnected;
					notifyStateChanged();
				}

				@Override
				public void reconnectingIn(int seconds)
				{
				}

				@Override
				public void reconnectionSuccessful()
				{
					state = State.Connected;
					notifyStateChanged();
				}

				@Override
				public void reconnectionFailed(Exception e)
				{
				}
			});


			//Register
			username = Utils.randomString();
			password = Utils.randomString();
			AccountManager man = new AccountManager(con);
			man.createAccount(username, password);

			//Login
			con.login(username, password);

			//Done!
			state = State.Connected;
			notifyStateChanged();
		} catch (XMPPException e)
		{
			state = State.Disconnected;
			notifyStateChanged();
			throw e;
		}
	}

	public void disconnect()
	{
		if (state == State.Disconnected)
			throw new IllegalStateException("You're not connected to this server.");

		//Leave all conversations
		for (CryptocatConversation c : conversations.values())
			c.leave();

		con.disconnect();
		con = null;
		username = null;
		password = null;

		state = State.Disconnected;
		notifyStateChanged();
	}

	public void addStateListener(CryptocatStateListener listener)
	{
		listeners.add(listener);
	}

	public void removeStateListener(CryptocatStateListener listener)
	{
		listeners.remove(listener);
	}

	void notifyStateChanged()
	{
		for (CryptocatStateListener l : listeners)
			l.stateChanged();
	}

	public CryptocatConversation createConversation(String name, String nickname) throws XMPPException
	{
		CryptocatConversation cc = new CryptocatConversation(this, name, nickname);
		conversations.put(cc.id, cc);

		notifyStateChanged();
		return cc;
	}

	public CryptocatConversation getConversation(String id)
	{
		return conversations.get(id);
	}

	public void removeConversation(String id)
	{
		if (getConversation(id).getState() != CryptocatConversation.State.NotJoined)
			throw new IllegalStateException("Conversation must be disconnected");

		conversations.remove(id);
	}

	public void getConversationList(ArrayList<Object> list)
	{
		for (CryptocatConversation c : conversations.values())
			list.add(c);
	}

	@Override
	public String toString()
	{
		return "[" + state + "] " + id;
	}
}
