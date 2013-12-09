package net.dirbaio.cryptocat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import net.dirbaio.cryptocat.service.CryptocatServer;
import net.dirbaio.cryptocat.service.CryptocatStateListener;
import net.dirbaio.cryptocat.service.MultipartyConversation;
import net.dirbaio.cryptocat.service.OtrConversation;

import java.util.ArrayList;

/**
 * A list fragment representing a list of Conversations. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ConversationFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationListFragment extends BaseListFragment implements CryptocatStateListener
{

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int activatedPosition = ListView.INVALID_POSITION;
	private final ArrayList<Object> conversations = new ArrayList<Object>();
	private ArrayAdapter<Object> conversationArrayAdapter;

	private MultipartyConversation conversation;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationListFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		if(getArguments() != null)
		{
			String serverId = getArguments().getString(MainActivity.ARG_SERVER_ID);
			String conversationId = getArguments().getString(MainActivity.ARG_CONVERSATION_ID);

			if(serverId != null)
				conversation = getService().getServer(serverId).getConversation(conversationId);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.conversation_list_menu, menu);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getService().addStateListener(this);
		conversationArrayAdapter = new ConversationAdapter(getAltContext(), R.layout.item_conversation, conversations);
		setListAdapter(conversationArrayAdapter);

		getService().addStateListener(this);
		stateChanged(); //Fire initial update.

		setActivateOnItemClick(true);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		getService().removeStateListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION))
			setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
	}

	@Override
	public void stateChanged()
	{
		if(conversation != null)
			conversation.getPrivateConversationList(conversations);
		else
			getService().getConversationList(conversations);

		conversationArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id)
	{
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		Object o = conversations.get(position);
		if (o instanceof OtrConversation)
		{
			OtrConversation conv = (OtrConversation) o;
			callbacks.onItemSelected(conv.server.id, conv.parent.id, conv.id);
		}
		else if (o instanceof MultipartyConversation)
		{
			MultipartyConversation conv = (MultipartyConversation) o;
			callbacks.onItemSelected(conv.server.id, conv.id, null);
		}
		else if (o instanceof CryptocatServer)
		{
			CryptocatServer srv = (CryptocatServer) o;
			callbacks.onItemSelected(srv.id, null, null);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if (activatedPosition != ListView.INVALID_POSITION)
		{
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick)
	{
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(activateOnItemClick
				? ListView.CHOICE_MODE_SINGLE
				: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position)
	{
		if (position == ListView.INVALID_POSITION)
		{
			getListView().setItemChecked(activatedPosition, false);
		} else
		{
			getListView().setItemChecked(position, true);
		}

		activatedPosition = position;
	}

	private int getPositionFor(String server, String conversation, String buddy)
	{
		int i = 0;
		for(Object o : conversations)
		{
			if(o instanceof CryptocatServer && conversation == null && buddy == null)
			{
				CryptocatServer s = (CryptocatServer) o;
				if(s.id.equals(server))
					return i;
			}
			if(o instanceof MultipartyConversation && buddy == null)
			{
				MultipartyConversation c = (MultipartyConversation) o;
				if(c.server.id.equals(server) && c.id.equals(conversation))
					return i;
			}
			if(o instanceof OtrConversation)
			{
				OtrConversation c = (OtrConversation) o;
				if(c.server.id.equals(server) && c.parent.id.equals(conversation) && c.id.equals(buddy))
					return i;
			}
			i++;
		}
		return -1;
	}
	public void setSelectedItem(String server, String conversation, String buddy)
	{
		if(getView() != null)
			setSelection(getPositionFor(server, conversation, buddy));
	}

	private class ConversationAdapter extends ArrayAdapter<Object>
	{

		private Context context;

		public ConversationAdapter(Context context, int textViewResourceId, ArrayList<Object> items)
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

			Object item = getItem(position);
			TextView textView = (TextView) view.findViewById(R.id.text);
            String str = item.toString();
            textView.setText(str);

			return view;
		}
	}

	@Override
	protected void onMustUpdateTitle(ActionBar ab)
	{
		ab.setTitle("Cryptocat");
		ab.setSubtitle(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.newserver:
				callbacks.onItemSelected(null, null, null);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

