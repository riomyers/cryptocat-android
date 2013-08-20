package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

/**
 * A fragment representing a single Conversation detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link ConversationDetailActivity}
 * on handsets.
 */
public class ConversationDetailFragment extends BoundFragment implements CryptocatMessageListener
{
	private String serverId;
	private String conversationId;
	private CryptocatConversation conversation;

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
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getActivity().getActionBar().setTitle(conversationId);
	}

	@Override
	protected void onServiceBind()
	{
		conversation = service.getServer(serverId).getConversation(conversationId);
		conversationArrayAdapter = new ConversationAdapter(getActivity(), R.layout.item_message, conversation.history);
		conversationView.setAdapter(conversationArrayAdapter);

		service.post(new Runnable()
		{
			@Override
			public void run()
			{
				conversation.addMessageListener(ConversationDetailFragment.this);
			}
		});
	}

	@Override
	protected void onServiceUnbind()
	{
		service.post(new ExceptionRunnable()
		{
			@Override
			public void run() throws Exception
			{
				conversation.removeMessageListener(ConversationDetailFragment.this);
			}
		});
	}


	@Override
	public void messageReceived(final CryptocatMessage message)
	{
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				conversationArrayAdapter.notifyDataSetChanged();
			}
		});
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
						service.post(new ExceptionRunnable()
						{
							@Override
							public void run() throws Exception
							{
								conversation.sendMessage(str);
							}
						});
						text.setText("");
					}
				}
			}
		});

		return rootView;
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
