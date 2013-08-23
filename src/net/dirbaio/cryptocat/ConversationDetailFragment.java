package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import net.dirbaio.cryptocat.protocol.*;
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

/**
 * A fragment representing a single Conversation screen.
 * This fragment is contained in a {@link MainActivity}
 */
public class ConversationDetailFragment extends BoundFragment implements CryptocatMessageListener, CryptocatBuddyListener
{
	private String serverId;
	private String conversationId;
	private String buddyId;
	private Conversation conversation;

	private ArrayAdapter<CryptocatMessage> conversationArrayAdapter;
	private ListView conversationView;
	private View rootView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationDetailFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		serverId = getArguments().getString(MainActivity.ARG_SERVER_ID);
		conversationId = getArguments().getString(MainActivity.ARG_CONVERSATION_ID);
		buddyId = getArguments().getString(MainActivity.ARG_BUDDY_ID);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

    public void updateTitle()
	{
		if(conversation == null) return;

		if(conversation instanceof MultipartyConversation)
		{
			MultipartyConversation mp = (MultipartyConversation) conversation;

			String subtitle = "";
			for(MultipartyConversation.Buddy b : mp.buddies)
				if(!subtitle.isEmpty())
				{
					subtitle += ", ";
					subtitle += b.nickname;
				}
			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setTitle(mp.roomName);
			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setSubtitle(subtitle);
		}
		else if(conversation instanceof OtrConversation)
		{
			OtrConversation priv = (OtrConversation) conversation;

			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setTitle(priv.parent.roomName);
			((SherlockFragmentActivity)getActivity()).getSupportActionBar().setSubtitle(priv.buddyNickname);
		}
	}

	@Override
	protected void onServiceBind()
	{
		conversation = service.getServer(serverId).getConversation(conversationId);
		if(buddyId != null)
			conversation = ((MultipartyConversation)conversation).getPrivateConversation(buddyId);

		conversationArrayAdapter = new ConversationAdapter(getActivity(), R.layout.item_message, conversation.history);
		conversationView.setAdapter(conversationArrayAdapter);

		conversation.addMessageListener(ConversationDetailFragment.this);
		if(conversation instanceof MultipartyConversation)
			((MultipartyConversation)conversation).addBuddyListener(ConversationDetailFragment.this);

		updateTitle();
	}

	@Override
	protected void onServiceUnbind()
	{
		conversation.removeMessageListener(ConversationDetailFragment.this);
	}

	@Override
	public void messageReceived(final CryptocatMessage message)
	{
		conversationArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.fragment_conversation_detail, container, false);

		conversationView = (ListView) rootView.findViewById(R.id.conversation);
		conversationView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		final Button button = (Button) rootView.findViewById(R.id.send);
		final EditText text = (EditText) rootView.findViewById(R.id.text);
		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (bound)
				{
					final String str = text.getText().toString();
					if (!str.isEmpty())
					{
						try
						{
							conversation.sendMessage(str);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						text.setText("");
					}
				}
			}
		});

		return rootView;
	}

	@Override
	public void buddyListChanged()
	{
		updateTitle();
	}

	private class ConversationAdapter extends ArrayAdapter<CryptocatMessage>
	{

		private Context context;

		public ConversationAdapter(Context context, int textViewResourceId, ArrayList<CryptocatMessage> items)
		{
			super(context, textViewResourceId, items);
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.item_message, null);
			}

			CryptocatMessage item = getItem(position);

			if (item != null)
			{
				TextView nickView = (TextView) view.findViewById(R.id.nickname);
				nickView.setText(item.nickname);
				if (position != 0)
				{
					CryptocatMessage itemold = getItem(position - 1);
					if (itemold.t == item.t && itemold.nickname.equals(item.nickname))
						nickView.setVisibility(View.GONE);
					else
						nickView.setVisibility(View.VISIBLE);
				}
				else
					nickView.setVisibility(View.VISIBLE);
				TextView textView = (TextView) view.findViewById(R.id.text);
				String txt = item.text;
				if (item.t == CryptocatMessage.Type.Join) txt = "- Joined -";
				if (item.t == CryptocatMessage.Type.Leave) txt = "- Left -";
				textView.setText(txt);
			}

			return view;
		}
	}
}
