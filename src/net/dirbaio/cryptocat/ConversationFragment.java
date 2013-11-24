package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.dirbaio.cryptocat.service.*;

import java.util.ArrayList;

/**
 * A fragment representing a single Conversation screen.
 * This fragment is contained in a {@link MainActivity}
 */
public class ConversationFragment extends BaseFragment implements CryptocatMessageListener, CryptocatBuddyListener
{
	private String serverId;
	private String conversationId;
	private String buddyId;
	private Conversation conversation;

	private ArrayAdapter<CryptocatMessage> conversationArrayAdapter;
	private ConversationListView conversationView;
	private View rootView;
	private ImageButton sendButton;
	private EditText text;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		serverId = getArguments().getString(MainActivity.ARG_SERVER_ID);
		conversationId = getArguments().getString(MainActivity.ARG_CONVERSATION_ID);
		buddyId = getArguments().getString(MainActivity.ARG_BUDDY_ID);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.conversation_menu, menu);
	}

	@Override
	protected void onMustUpdateTitle(ActionBar ab)
	{
		if(conversation == null) return;

		if(conversation instanceof MultipartyConversation)
		{
			MultipartyConversation mp = (MultipartyConversation) conversation;

			String subtitle = "";
			for(MultipartyConversation.Buddy b : mp.buddies)
			{
				if(!subtitle.isEmpty())
					subtitle += ", ";
				subtitle += b.nickname;
			}
			ab.setTitle(mp.roomName);
			ab.setSubtitle(subtitle);
		}
		else if(conversation instanceof OtrConversation)
		{
			OtrConversation priv = (OtrConversation) conversation;

			ab.setTitle(priv.parent.roomName);
			ab.setSubtitle(priv.buddyNickname);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		conversation = getService().getServer(serverId).getConversation(conversationId);
		if(buddyId != null)
			conversation = ((MultipartyConversation)conversation).getPrivateConversation(buddyId);

		conversationArrayAdapter = new ConversationAdapter(getActivity(), conversation.history);
		conversationView.setAdapter(conversationArrayAdapter);

		conversation.addMessageListener(ConversationFragment.this);
		if(conversation instanceof MultipartyConversation)
			((MultipartyConversation)conversation).addBuddyListener(ConversationFragment.this);

		updateTitle();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		conversation.removeMessageListener(ConversationFragment.this);
	}

	@Override
	public void messageReceived(final CryptocatMessage message)
	{
		conversationArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.fragment_conversation, container, false);

		conversationView = (ConversationListView) rootView.findViewById(R.id.conversation);
		conversationView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

		sendButton = (ImageButton) rootView.findViewById(R.id.send);
		text = (EditText) rootView.findViewById(R.id.text);
		sendButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				sendMessage(text.getText().toString());
			}
		});
		text.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_SEND)
				{
					sendMessage(text.getText().toString());
					handled = true;
				}
				return handled;
			}
		});

		return rootView;
	}

	private void sendMessage(String str)
	{
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

	@Override
	public void buddyListChanged()
	{
		updateTitle();
	}

	private class ConversationAdapter extends ArrayAdapter<CryptocatMessage>
	{

		private Context context;

		public ConversationAdapter(Context context, ArrayList<CryptocatMessage> items)
		{
			super(context, 0, items);
			this.context = context;
		}

        @Override
        public int getViewTypeCount() {
            return CryptocatMessage.Type.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).type.ordinal();
        }

        public View getView(int position, View view, ViewGroup parent)
		{
            CryptocatMessage msg = getItem(position);

			if (view == null)
			{
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                int id;

                switch (msg.type)
                {
                    case Message:       id = R.layout.message; break;
                    case MessageMine:   id = R.layout.message_mine; break;
                    case Join:          id = R.layout.message_join; break;
                    case Leave:         id = R.layout.message_leave; break;
                    case File:          id = R.layout.message_error; break;
                    case Error:         id = R.layout.message_error; break;
                    default: throw new IllegalStateException("Unknown item type");
                }

                view = inflater.inflate(id, null);
			}

            boolean showNick = true;

            if(msg.type == CryptocatMessage.Type.Message && position != 0)
            {
                CryptocatMessage prevMsg = getItem(position-1);
                if(prevMsg.type == CryptocatMessage.Type.Message && prevMsg.nickname.equals(msg.nickname))
                    showNick = false;
            }

            if(msg.type != CryptocatMessage.Type.Error && msg.type != CryptocatMessage.Type.MessageMine)
            {
                TextView nickView = (TextView) view.findViewById(R.id.nickname);
                nickView.setVisibility(showNick?View.VISIBLE:View.GONE);
                nickView.setText(msg.nickname);
            }

            if(msg.type != CryptocatMessage.Type.Join && msg.type != CryptocatMessage.Type.Leave)
            {
                TextView textView = (TextView) view.findViewById(R.id.text);
                textView.setText(msg.text);
            }


			return view;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.buddies:
				callbacks.showSecondaryMenu();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
