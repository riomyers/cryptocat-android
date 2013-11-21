package net.dirbaio.cryptocat.service;

import net.dirbaio.cryptocat.ExceptionRunnable;
import net.java.otr4j.OtrEngineHost;
import net.java.otr4j.OtrPolicy;
import net.java.otr4j.session.SessionID;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * An OTR conversation.
 * TODO: Actually use OTR. (lol)
 */
public class OtrConversation extends Conversation implements MessageListener, OtrEngineHost
{
	public final String buddyNickname;
	public final MultipartyConversation parent;
	Chat chat;

    private OtrPolicy policy;

	public OtrConversation(MultipartyConversation parent, String buddyNickname) throws XMPPException
	{
		super(parent.server, parent.nickname);
		this.parent = parent;
		this.buddyNickname = buddyNickname;
		this.id = buddyNickname;
	}

	@Override
	public void join() throws XMPPException
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
	public void sendMessage(final String msg) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, XMPPException, NoSuchPaddingException
	{
		//Check state
		if (getState() != State.Joined)
			throw new IllegalStateException("You have not joined.");

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

    public void sendRawMessage(final String msg)
    {
        CryptocatService.getInstance().post(new ExceptionRunnable()
        {
            @Override
            public void run() throws Exception
            {
                chat.sendMessage(msg);
            }
        });
    }


    @Override
    public void injectMessage(SessionID sessionID, String s) {
        sendRawMessage(s);
    }

    @Override
    public void showWarning(SessionID sessionID, String s) {
        System.err.println("OTR WARNING: "+s);
    }

    @Override
    public void showError(SessionID sessionID, String s) {
        System.err.println("OTR ERROR: "+s);
    }

    @Override
    public OtrPolicy getSessionPolicy(SessionID sessionID) {
        return policy;
    }

    @Override
    public KeyPair getKeyPair(SessionID sessionID) {
        KeyPairGenerator kg;
        try {
            kg = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        return kg.genKeyPair();
    }
}
