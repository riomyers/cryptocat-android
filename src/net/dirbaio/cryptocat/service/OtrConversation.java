package net.dirbaio.cryptocat.service;

import net.dirbaio.cryptocat.ExceptionRunnable;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * An OTR conversation.
 * TODO: Actually use OTR. (lol)
 */
public class OtrConversation extends Conversation implements MessageListener
{
	public final String buddyNickname;
	public final MultipartyConversation parent;
	Chat chat;

	public OtrConversation(MultipartyConversation parent, String buddyNickname)
	{
		super(parent.server, parent.nickname);
		this.parent = parent;
		this.buddyNickname = buddyNickname;
		this.id = buddyNickname;
	}

	@Override
	public void join()
	{
		if (state != State.Left)
			throw new IllegalStateException("You're already joined.");
		if (server.getState() != CryptocatServer.State.Connected)
			throw new IllegalStateException("Server is not connected");
		if (parent.getState() != State.Joined)
			throw new IllegalStateException("You haven't joined the chatroom");

		state = State.Joining;

		CryptocatService.getInstance().post(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				try
				{
					server.notifyStateChanged();
					chat = parent.muc.createPrivateChat(parent.roomName +"@"+parent.server.config.conferenceServer+"/"+buddyNickname, OtrConversation.this);

					state = State.Joined;
					server.notifyStateChanged();
				}
				catch(Exception e)
				{
					e.printStackTrace();

					state = State.Error;
					server.notifyStateChanged();
				}
			}
		});
	}

	@Override
	public void leave()
	{
		if (state == State.Left)
			throw new IllegalStateException("You have not joined.");

		final Chat chatFinal = chat;
		CryptocatService.getInstance().post(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				chatFinal.removeMessageListener(OtrConversation.this);
			}
		});

		chat = null;

		state = State.Left;
		server.notifyStateChanged();
	}

	//TODO Actual OTR implementation.
	@Override
	public void sendMessage(final String msg) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException
	{
		//Check state
		if (getState() != State.Joined)
			throw new IllegalStateException("You have not joined.");

		CryptocatService.getInstance().post(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				chat.sendMessage(msg);
			}
		});

		addMessage(new CryptocatMessage(CryptocatMessage.Type.Message, nickname, msg));
	}

	@Override
	public void processMessage(Chat chat, final Message message)
	{
		CryptocatService.getInstance().uiPost(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				String txt = message.getBody();
				addMessage(new CryptocatMessage(CryptocatMessage.Type.Message, buddyNickname, txt));
			}
		});
	}

	@Override
	public String toString()
	{
		return "[" + state + "] " + parent.roomName +":"+buddyNickname;
	}

}
