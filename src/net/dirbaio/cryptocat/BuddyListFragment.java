package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import net.dirbaio.cryptocat.service.CryptocatBuddyListener;
import net.dirbaio.cryptocat.service.MultipartyConversation;
import net.dirbaio.cryptocat.service.OtrConversation;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;

public class BuddyListFragment extends BoundListFragment implements CryptocatBuddyListener
{
	private String serverId;
	private String conversationId;
	private MultipartyConversation conversation;

	private ArrayAdapter<MultipartyConversation.Buddy> buddyArrayAdapter;

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
	}

	@Override
	public void onResume()
	{
		super.onResume();
		conversation = getService().getServer(serverId).getConversation(conversationId);
		buddyArrayAdapter = new BuddyAdapter(getAltContext(), R.layout.item_buddy, conversation.buddies);
		setListAdapter(buddyArrayAdapter);

		conversation.addBuddyListener(BuddyListFragment.this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		conversation.removeBuddyListener(BuddyListFragment.this);
	}

	@Override
	public void buddyListChanged()
	{
		buddyArrayAdapter.notifyDataSetChanged();

	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id)
	{
		super.onListItemClick(listView, view, position, id);

		MultipartyConversation.Buddy b = buddyArrayAdapter.getItem(position);

		try
		{
			OtrConversation o = conversation.startPrivateConversation(b.nickname);
			callbacks.onItemSelected(serverId, conversationId, o.id);
		}
		catch (XMPPException e)
		{
			e.printStackTrace();
		}
	}

	private class BuddyAdapter extends ArrayAdapter<MultipartyConversation.Buddy>
	{

		private Context context;

		public BuddyAdapter(Context context, int textViewResourceId, ArrayList<MultipartyConversation.Buddy> items)
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

			MultipartyConversation.Buddy item = getItem(position);
			TextView textView = (TextView) view.findViewById(R.id.text);
			textView.setText(item.toString());

			return view;
		}
	}
}
