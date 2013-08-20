package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BuddyListFragment extends BoundListFragment implements CryptocatBuddyListener
{
	private String serverId;
	private String conversationId;
	private CryptocatConversation conversation;

	private ArrayAdapter<CryptocatConversation.Buddy> buddyArrayAdapter;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public BuddyListFragment()
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
		buddyArrayAdapter = new BuddyAdapter(getActivity(), R.layout.item_buddy, conversation.buddies);
		setListAdapter(buddyArrayAdapter);

		service.post(new Runnable()
		{
			@Override
			public void run()
			{
				conversation.addBuddyListener(BuddyListFragment.this);
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
				conversation.removeBuddyListener(BuddyListFragment.this);
			}
		});
	}

	@Override
	public void buddyListChanged()
	{
		handler.post(new Runnable()
		{
			@Override
			public void run()
			{
				buddyArrayAdapter.notifyDataSetChanged();
			}
		});
	}


	private class BuddyAdapter extends ArrayAdapter<CryptocatConversation.Buddy>
	{

		private Context context;

		public BuddyAdapter(Context context, int textViewResourceId, ArrayList<CryptocatConversation.Buddy> items)
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
				view = inflater.inflate(R.layout.item_conversation, null);
			}

			CryptocatConversation.Buddy item = getItem(position);
			TextView textView = (TextView) view.findViewById(R.id.text);
			textView.setText(item.toString());

			return view;
		}
	}
}
