package net.dirbaio.cryptocat.protocol;

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

public class OtrConversation extends Conversation implements MessageListener
{
	public final String buddyNickname;
	public final MultipartyConversation parent;
	Chat chat;

	public OtrConversation(MultipartyConversation parent, String buddyNickname) throws XMPPException
	{
		super(parent.server, parent.nickname);
		this.parent = parent;
		this.buddyNickname = buddyNickname;
	}

	@Override
	public void join() throws XMPPException
	{
		if (state == State.Joined)
			throw new IllegalStateException("You're already joined.");
		if (server.getState() == CryptocatServer.State.Disconnected)
			throw new IllegalStateException("Server is not connected");
		if (parent.getState() == State.NotJoined)
			throw new IllegalStateException("You haven't joined the chatroom");

		chat = parent.muc.createPrivateChat(parent.roomName +"@"+parent.server.conferenceServer+"/"+buddyNickname, this);

		state = State.Joined;
		server.notifyStateChanged();
	}

	@Override
	public void leave()
	{
		if (state == State.NotJoined)
			throw new IllegalStateException("You have not joined.");

		chat.removeMessageListener(this);
		chat = null;

		state = State.NotJoined;
		server.notifyStateChanged();
	}

	//TODO Actual OTR implementation.
	@Override
	public void sendMessage(String msg) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, XMPPException, NoSuchPaddingException
	{
		//Check state
		if (getState() == State.NotJoined)
			throw new IllegalStateException("You have not joined.");

		chat.sendMessage(msg);

		addMessage(new CryptocatMessage(CryptocatMessage.Type.Message, nickname, msg));
	}

	@Override
	public void processMessage(Chat chat, Message message)
	{
		String txt = message.getBody();
		addMessage(new CryptocatMessage(CryptocatMessage.Type.Message, buddyNickname, txt));
	}

	@Override
	public String toString()
	{
		return "[" + state + "] " + parent.roomName +":"+buddyNickname;
	}

}
