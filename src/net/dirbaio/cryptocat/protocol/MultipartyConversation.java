package net.dirbaio.cryptocat.protocol;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.*;

public class MultipartyConversation extends Conversation
{

	public static final String PUBLIC_KEY = "publicKey";
	public static final String PUBLIC_KEY_REQUEST = "publicKeyRequest";
	public static final String MESSAGE = "message";

	MultiUserChat muc;
	public final String roomName;

	public byte[] privateKey;
	public byte[] publicKey;
	public final Map<String, Buddy> buddiesByName = new HashMap<>();
	public final ArrayList<Buddy> buddies = new ArrayList<>();
	public final HashMap<String, OtrConversation> privateConversations = new HashMap<>();
	private final HashMap<String, OtrConversation> privateConversationsByNick = new HashMap<>();
	private final ArrayList<CryptocatBuddyListener> buddyListeners = new ArrayList<>();

	public MultipartyConversation(CryptocatServer server, String roomName, String nickname) throws XMPPException
	{
		super(server, nickname);
		this.roomName = roomName;
	}

	public void join() throws XMPPException
	{
		if (state == State.Joined)
			throw new IllegalStateException("You're already joined.");
		if (server.getState() == CryptocatServer.State.Disconnected)
			throw new IllegalStateException("Server is not connected");

		//Random cleaning
		buddiesByName.clear();

		//Generate keypair
		privateKey = new byte[32];
		publicKey = new byte[32];
		Utils.random.nextBytes(privateKey);
		Curve25519.keygen(publicKey, null, privateKey);

		//Setup MUC chat
		muc = new MultiUserChat(server.con, roomName + "@" + server.conferenceServer);

		muc.addMessageListener(new PacketListener()
		{
			@Override
			public void processPacket(Packet packet)
			{
				try
				{
					if (packet instanceof Message)
					{
						Message m = (Message) packet;
						receivedMessage(getNickname(m.getFrom()), m.getBody());
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		muc.addParticipantListener(new PacketListener()
		{
			@Override
			public void processPacket(Packet packet)
			{
				try
				{
					if (packet instanceof Presence)
					{
						Presence p = (Presence) packet;
						String from = getNickname(p.getFrom());
						System.out.println("Presence: " + p.getFrom() + " " + p.getType() + " " + p.getMode());

						if (p.getType() == Presence.Type.available)
							sendPublicKey(from);
						if (p.getType() == Presence.Type.unavailable)
						{
							//FIXME: This is broken, it doesn't get called. aSmack bug?
							Buddy b = buddiesByName.remove(from);
							buddies.remove(b);
							notifyBuddyListChange();
							System.out.println("LEAVE!");
							addMessage(new CryptocatMessage(CryptocatMessage.Type.Leave, from, null));
						}
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		//Request no history from the server.
		DiscussionHistory history = new DiscussionHistory();
		history.setMaxStanzas(0);

		muc.join(nickname, "", history, SmackConfiguration.getPacketReplyTimeout());

		state = State.Joined;
		server.notifyStateChanged();
	}

	public void leave()
	{
		if (state == State.NotJoined)
			throw new IllegalStateException("You have not joined.");

		if (server.getState() != CryptocatServer.State.Disconnected)
			muc.leave();

		privateKey = null;
		publicKey = null;
		buddiesByName.clear();

		state = State.NotJoined;
		server.notifyStateChanged();
	}

	public void addBuddyListener(CryptocatBuddyListener l)
	{
		buddyListeners.add(l);
	}

	public void removeBuddyListener(CryptocatBuddyListener l)
	{
		buddyListeners.remove(l);
	}

	public OtrConversation startPrivateConversation(String buddyNickname) throws XMPPException
	{
		if (state != State.Joined)
			throw new IllegalStateException("You're not joined to the chatroom.");
		if (server.getState() == CryptocatServer.State.Disconnected)
			throw new IllegalStateException("Server is not connected");

		//Don't create the conversation again.
		OtrConversation conv = privateConversationsByNick.get(buddyNickname);
		if(conv != null)
			return conv;

		conv = new OtrConversation(this, buddyNickname);
		privateConversations.put(conv.id, conv);
		privateConversationsByNick.put(buddyNickname, conv);

		conv.join();

		server.notifyStateChanged();
		return conv;
	}

	public OtrConversation getPrivateConversation(String id)
	{
		return privateConversations.get(id);
	}

	@Override
	public String toString()
	{
		return "[" + state + "] " + roomName;
	}

	private void sendJsonMessage(JsonMessage m) throws XMPPException
	{
		String send = GsonHelper.customGson.toJson(m);
		muc.sendMessage(send);
	}

	private void sendPublicKey(String to) throws XMPPException
	{

		JsonMessage m = new JsonMessage();
		JsonMessageEntry e = new JsonMessageEntry();
		e.message = publicKey;
		m.type = PUBLIC_KEY;
		m.text.put(to, e);
		sendJsonMessage(m);
	}

	private void receivedMessage(String from, String body) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, XMPPException, NoSuchPaddingException
	{
		if (from.equals(nickname))
			return;

		//Decode JSON
		JsonMessage m;
		m = GsonHelper.customGson.fromJson(body, JsonMessage.class);

		//For typing notification messages and others, body is empty.
		if (m == null)
			return;

		//Get my item_message
		JsonMessageEntry myMessage = m.text.get(nickname);

		//No item_message for me
		if (myMessage == null || m.type == null)
			return;

		switch (m.type)
		{
			case PUBLIC_KEY:
			{
				if (buddiesByName.containsKey(from))
					return;

				byte[] hisPublicKey = myMessage.message;
				Buddy b = new Buddy(from, hisPublicKey);
				buddiesByName.put(from, b);
				buddies.add(b);
				notifyBuddyListChange();
				addMessage(new CryptocatMessage(CryptocatMessage.Type.Join, from, null));
				break;
			}
			case PUBLIC_KEY_REQUEST:
			{
				sendPublicKey(from);
				break;
			}
			case MESSAGE:
			{
				//Real item_message
				Buddy b = buddiesByName.get(from);

				//Sort recipients
				ArrayList<String> sortedRecipients = new ArrayList<>(m.text.keySet());
				Collections.sort(sortedRecipients);

				//Check HMAC
				byte[] ciphertext = myMessage.message;
				SecretKeySpec secretKey = new SecretKeySpec(b.hmacSecret, "HmacSHA512");
				Mac mac = Mac.getInstance("HmacSHA512", "BC");
				mac.init(secretKey);
				for (String recipient : sortedRecipients)
				{
					JsonMessageEntry msg2 = m.text.get(recipient);
					mac.update(msg2.message);
					mac.update(msg2.iv);
				}

				byte[] hmac = mac.doFinal();
				byte[] messageHmac = myMessage.hmac;

				if (!Arrays.equals(hmac, messageHmac))
					throw new RuntimeException("Bad HMAC");

				//Decrypt
				byte[] iv = new byte[16];
				System.arraycopy(myMessage.iv, 0, iv, 0, 12);
				byte[] plaintext = b.decryptAes(ciphertext, iv);

				//Check tag
				MessageDigest tagDigest = MessageDigest.getInstance("SHA-512", "BC");
				tagDigest.update(plaintext);
				for (String recipient : sortedRecipients)
				{
					JsonMessageEntry msg2 = m.text.get(recipient);
					tagDigest.update(msg2.hmac);
				}
				byte[] tag = tagDigest.digest();
				for (int i = 0; i < 7; i++)
					tag = tagDigest.digest(tag);

				if (!Arrays.equals(tag, m.tag))
					throw new RuntimeException("Bad item_message tag");

				//Remove the 64bytes of random padding
				if (plaintext.length < 64)
					throw new RuntimeException("Message is too short");
				plaintext = Arrays.copyOf(plaintext, plaintext.length - 64);

				//Convert to string
				String plaintextString = new String(plaintext, "UTF-8");

				CryptocatMessage msg = new CryptocatMessage(CryptocatMessage.Type.Message, from, plaintextString);

				//And we are done!

				//Send the item_message to all listeners and save it in history.
				addMessage(msg);
				break;
			}
		}
	}


	private void notifyBuddyListChange()
	{
		for (CryptocatBuddyListener l : buddyListeners)
			l.buddyListChanged();
	}

	public void sendMessage(String message) throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, XMPPException, NoSuchPaddingException
	{
		//Check state
		if (state == State.NotJoined)
			throw new IllegalStateException("You have not joined.");

		// Append 64 random bytes to the string.
		// This is used to prevent bruteforcing the contents of the item_message
		// using the tag value.

		byte[] plaintext = message.getBytes("UTF-8");
		byte[] randomPad = new byte[64];
		Utils.random.nextBytes(randomPad);
		plaintext = Arrays.copyOf(plaintext, plaintext.length + 64);
		System.arraycopy(randomPad, 0, plaintext, plaintext.length - 64, 64);

		//Encrypt
		JsonMessage m = new JsonMessage();

		for (Buddy b : buddiesByName.values())
		{
			//Create IV
			byte[] iv2 = new byte[12];
			Utils.random.nextBytes(iv2);

			//Create 16byte IV
			byte[] iv = new byte[16];
			System.arraycopy(iv2, 0, iv, 0, iv2.length);
			byte[] ciphertext = b.encryptAes(plaintext, iv);

			JsonMessageEntry e = new JsonMessageEntry();
			e.message = ciphertext;
			e.iv = iv2;
			m.text.put(b.nickname, e);
		}

		//Sort recipients
		ArrayList<String> sortedRecipients = new ArrayList<>(buddiesByName.keySet());
		Collections.sort(sortedRecipients);

		//HMAC
		for (Buddy b : buddiesByName.values())
		{
			SecretKeySpec secretKey = new SecretKeySpec(b.hmacSecret, "HmacSHA512");
			Mac mac = Mac.getInstance("HmacSHA512");
			mac.init(secretKey);
			for (String recipient : sortedRecipients)
			{
				JsonMessageEntry msg2 = m.text.get(recipient);
				mac.update(msg2.message);
				mac.update(msg2.iv);
			}

			m.text.get(b.nickname).hmac = mac.doFinal();
		}

		//Message tag
		MessageDigest tagDigest = MessageDigest.getInstance("SHA-512", "BC");
		tagDigest.update(plaintext);
		for (String recipient : sortedRecipients)
		{
			JsonMessageEntry msg2 = m.text.get(recipient);
			tagDigest.update(msg2.hmac);
		}
		byte[] tag = tagDigest.digest();
		for (int i = 0; i < 7; i++)
			tag = tagDigest.digest(tag);

		m.tag = tag;

		//And that's it!
		m.type = MESSAGE;
		sendJsonMessage(m);

		//Once done, send to listeners and save to history
		CryptocatMessage msg = new CryptocatMessage(CryptocatMessage.Type.Message, nickname, message);

		addMessage(msg);
	}


	public class Buddy
	{

		public final String nickname;
		private final byte[] publicKey;
		private final byte[] messageSecret, hmacSecret;

		private Buddy(String nickname, byte[] publicKey) throws InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchProviderException
		{
			this.nickname = nickname;
			this.publicKey = publicKey;

			//Gen shared secret
			byte[] curve = new byte[32];
			Curve25519.curve(curve, privateKey, publicKey);

			//Gen secrets
			MessageDigest mda = MessageDigest.getInstance("SHA-512", "BC");
			byte[] digest = mda.digest(curve);

			messageSecret = new byte[32];
			hmacSecret = new byte[32];

			System.arraycopy(digest, 0, messageSecret, 0, 32);
			System.arraycopy(digest, 32, hmacSecret, 0, 32);
			System.out.println("Connected: " + nickname);
		}

		private byte[] encryptAes(byte[] plaintext, byte[] iv) throws InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
		{
			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

			SecretKeySpec key = new SecretKeySpec(messageSecret, "AES");
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);

			byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
			int ctLength = cipher.update(plaintext, 0, plaintext.length, ciphertext, 0);
			ctLength += cipher.doFinal(ciphertext, ctLength);

			return ciphertext;
		}

		private byte[] decryptAes(byte[] plaintext, byte[] iv) throws InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException
		{
			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

			SecretKeySpec key = new SecretKeySpec(messageSecret, "AES");
			IvParameterSpec ivParam = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ivParam);

			byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
			int ctLength = cipher.update(plaintext, 0, plaintext.length, ciphertext, 0);
			ctLength += cipher.doFinal(ciphertext, ctLength);

			return ciphertext;
		}

		@Override
		public String toString()
		{
			return nickname;
		}
	}

	private String getNickname(String full)
	{
		return full.substring(full.indexOf('/') + 1);
	}

	private static class JsonMessage
	{
		String type;
		HashMap<String, JsonMessageEntry> text = new HashMap<>();
		byte[] tag;
	}

	private static class JsonMessageEntry
	{
		byte[] message;
		byte[] iv;
		byte[] hmac;

		@Override
		public String toString()
		{
			return "JsonMessageEntry{item_message=" + Utils.toBase64(message) + ", iv=" + Utils.toBase64(iv) + ", hmac=" + Utils.toBase64(hmac) + '}';
		}
	}
}
