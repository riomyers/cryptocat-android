/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.dirbaio.cryptocat.service;

import android.os.Build;
import net.dirbaio.cryptocat.ExceptionRunnable;
import org.jivesoftware.smack.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class CryptocatServer
{
	public final String id;
	public final CryptocatServerConfig config;

	public final HashMap<String, MultipartyConversation> conversations = new HashMap<>();
	private final ArrayList<CryptocatStateListener> listeners = new ArrayList<>();

	private String username, password;
	Connection con;

	private State state;

	public enum State
	{
		Disconnected,
		Connected,
		Disconnecting,
		Connecting,
		Error,
	}

	public State getState()
	{
		return state;
	}

	public CryptocatServer(CryptocatServerConfig config)
	{
		this.id = config.server;
		this.config = config;
		this.state = State.Disconnected;
	}

	public void connect()
	{
		if (state != State.Disconnected && state != State.Error)
			throw new IllegalStateException("You're already connected to this server.");

		state = State.Connecting;
		notifyStateChanged();

		//No idea wtf this is
		SmackConfiguration.setLocalSocks5ProxyEnabled(false);

		ConnectionConfiguration conConfig;
		if(config.useBosh)
		{
			//Setup connection
			URI uri = null;
			try
			{
				uri = new URI(config.boshRelay);
			}
			catch (URISyntaxException e)
			{
				throw new IllegalArgumentException(e);
			}
	
			int defaultPort = -1;
			if(uri.getScheme().equals("https"))
				defaultPort = 443;
			else if(uri.getScheme().equals("http"))
				defaultPort = 80;
			else
				throw new IllegalArgumentException("BOSH relay must be HTTP or HTTPS.");
	
			int port = uri.getPort();
			if(port == -1)
				port = defaultPort;
	
			BOSHConfiguration boshConfig = new BOSHConfiguration(true, uri.getHost(), port, uri.getPath(), config.server);
			boshConfig.setUsedHostAddress(boshConfig.getHostAddresses().get(0)); //I have no idea what this is either.
			
			conConfig = boshConfig;
		}
		else
			conConfig = new ConnectionConfiguration(config.server, config.port);
		
		
		//Android trust store shenaniagans
		//This is still broken :(
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			conConfig.setTruststoreType("AndroidCAStore");
			conConfig.setTruststorePassword(null);
			conConfig.setTruststorePath(null);
		}
		else
		{
			conConfig.setTruststoreType("BKS");
			String path = System.getProperty("javax.net.ssl.trustStore");
			if (path == null)
				path = System.getProperty("java.home") + File.separator + "etc"
						+ File.separator + "security" + File.separator
						+ "cacerts.bks";

			conConfig.setTruststorePath(path);
		}

        //Require TLS to connect to the server.
        conConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.required);

        //Don't allow self signed certificates.
        conConfig.setSelfSignedCertificateEnabled(false);

		final ConnectionConfiguration conConfigFinal = conConfig;

		CryptocatService.getInstance().post(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				try
				{
					// Connect to the server
					if(config.useBosh)
						con = new BOSHConnection((BOSHConfiguration)conConfigFinal);
					else
						con = new XMPPConnection(conConfigFinal);
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
							state = State.Error;
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
				}
				catch (XMPPException e)
				{
					e.printStackTrace();

					state = State.Error;
					notifyStateChanged();
				}
			}
		});
	}

	public void disconnect()
	{
		if (state != State.Connected)
			throw new IllegalStateException("You're not connected to this server.");

		//Leave all conversations
		for (MultipartyConversation c : conversations.values())
			c.leave();

		final Connection con2 = con;

		CryptocatService.getInstance().post(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				con2.disconnect();
			}
		});

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
		CryptocatService.getInstance().uiPost(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				for (CryptocatStateListener l : listeners)
					l.stateChanged();
			}
		});
	}

	public MultipartyConversation createConversation(String name, String nickname) throws XMPPException
	{
		MultipartyConversation cc = new MultipartyConversation(this, name, nickname);
		conversations.put(cc.id, cc);

		notifyStateChanged();
		return cc;
	}

	public MultipartyConversation getConversation(String id)
	{
		return conversations.get(id);
	}

	public void removeConversation(String id)
	{
		if (getConversation(id).getState() != MultipartyConversation.State.Left)
			throw new IllegalStateException("Conversation must be disconnected");

		conversations.remove(id);
	}

	@Override
	public String toString()
	{
		return "[" + state + "] " + id;
	}
}
