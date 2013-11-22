package net.dirbaio.cryptocat.service;

import net.java.otr4j.OtrException;
import org.jivesoftware.smack.XMPPException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;

public abstract class Conversation
{
	private final ArrayList<CryptocatMessageListener> msgListeners = new ArrayList<>();
	public CryptocatServer server;
	public final String nickname;
	public String id;
	public final ArrayList<CryptocatMessage> history = new ArrayList<>();

	protected State state;

	public enum State
	{
		Left,
		Joined,
		Leaving,
		Joining,
		Error
	}

	public Conversation(CryptocatServer server, String nickname) throws XMPPException
	{
		Utils.assertUiThread();
		this.server = server;
		this.nickname = nickname;
		this.state = State.Left;
	}

	public final State getState()
	{
		Utils.assertUiThread();
		return state;
	}

	public abstract void join() throws XMPPException;
	public abstract void leave();
	public abstract void sendMessage(String msg) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, XMPPException, NoSuchPaddingException, OtrException;


	public final void addMessageListener(CryptocatMessageListener l)
	{
		Utils.assertUiThread();
		msgListeners.add(l);
	}

	public final void removeMessageListener(CryptocatMessageListener l)
	{
		Utils.assertUiThread();
		msgListeners.remove(l);
	}

	protected void addMessage(CryptocatMessage msg)
	{
		Utils.assertUiThread();

		history.add(msg);

		for (CryptocatMessageListener l : msgListeners)
			l.messageReceived(msg);
	}

}

