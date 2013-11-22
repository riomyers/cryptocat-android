package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.dirbaio.cryptocat.service.*;

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
	private ImageButton sendButton;
	private EditText text;

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

		conversationArrayAdapter = new ConversationAdapter(getActivity(), R.layout.item_message, conversation.history);
		conversationView.setAdapter(conversationArrayAdapter);

		conversation.addMessageListener(ConversationDetailFragment.this);
		if(conversation instanceof MultipartyConversation)
			((MultipartyConversation)conversation).addBuddyListener(ConversationDetailFragment.this);

		updateTitle();
	}

	@Override
	public void onPause()
	{
		super.onPause();
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
				boolean me = item.nickname.equals(conversation.nickname);

				boolean showNick = false;

				if(!me &&
						item.t == CryptocatMessage.Type.Message &&
						conversation instanceof MultipartyConversation &&
						(position == 0 || !getItem(position-1).nickname.equals(item.nickname)))
					showNick = true;

				TextView nickView = (TextView) view.findViewById(R.id.nickname);
				if(showNick)
				{
					nickView.setText(item.nickname);
					nickView.setVisibility(View.VISIBLE);
				}
				else
					nickView.setVisibility(View.GONE);


				String txt;
				int background;
				int gravity;

				switch (item.t)
				{
					case Message:
						txt = item.text;
						background = me?R.drawable.bubble_rev:R.drawable.bubble;
						gravity = me ? Gravity.RIGHT:Gravity.LEFT;
						break;
					case Join:
						txt = item.nickname + " joined";
						background = R.drawable.bubble_notif;
						gravity = Gravity.CENTER;
						break;
                    case Leave:
                        txt = item.nickname + " left";
                        background = R.drawable.bubble_notif;
                        gravity = Gravity.CENTER;
                        break;
                    case File:
                        txt = item.nickname + " sent a file";
                        background = R.drawable.bubble_notif;
                        gravity = Gravity.CENTER;
                        break;
                    case Error:
                        txt = "ERROR: "+item.text;
                        background = R.drawable.bubble_notif;
                        gravity = Gravity.CENTER;
                        break;
					default:
						throw new IllegalStateException("Unknown item type");
				}

				TextView textView = (TextView) view.findViewById(R.id.text);
				textView.setText(txt);

				View bubbleView = view.findViewById(R.id.bubble);
				bubbleView.setBackgroundResource(background);

				FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				params.gravity = gravity;

				bubbleView.setLayoutParams(params);
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
