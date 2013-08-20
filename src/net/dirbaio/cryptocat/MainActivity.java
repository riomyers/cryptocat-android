package net.dirbaio.cryptocat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * An activity representing a list of Conversations. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ConversationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ConversationListFragment} and the item details
 * (if present) is a {@link ConversationDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ConversationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MainActivity extends SlidingFragmentActivity
		implements ConversationListFragment.Callbacks
{

	public static final String ARG_SERVER_ID = "net.dirbaio.cryptocat.SERVER_ID";
	public static final String ARG_CONVERSATION_ID = "net.dirbaio.cryptocat.CONVERSATION_ID";
	private ConversationListFragment conversationList;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Start the service if it isn't already started.
		startService(new Intent(this, CryptocatService.class));

		//Setup view stuff.
//	    setTitle("Hello World!");

		//Main content view
		setContentView(R.layout.frame_conversation_detail);

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		setSlidingActionBarEnabled(false);

		//SlidingMenu left view (conversation list)
		setBehindContentView(R.layout.frame_conversation_list);

		//SlidingMenu right view (user list)
		sm.setSecondaryMenu(R.layout.frame_buddy_list);

		//All done, now set contents on these!
		onItemSelected(null, null);
		setConversationListFragment(new ConversationListFragment());

		//TODO Move this to ConversationListFragment since it's always true.
		//FIXME PLS
		//conversationList.setActivateOnItemClick(true);

		// TODO: If exposing deep links into your app, handle intents here.
	}

	private void setFragment(int id, Fragment fragment)
	{
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		conversationList = new ConversationListFragment();
		t.replace(id, fragment);
		t.commit();
	}

	private void setConversationFragment(Fragment fragment)
	{
		setFragment(R.id.conversation_detail_container, fragment);
	}

	private void setConversationListFragment(Fragment fragment)
	{
		setFragment(R.id.conversation_list_container, fragment);
	}

	private void setBuddyListFragment(Fragment fragment)
	{
		setFragment(R.id.buddy_list_container, fragment);
	}

	/**
	 * Callback method from {@link ConversationListFragment.Callbacks}
	 * indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String server, String conversation)
	{
		// In two-pane mode, show the detail view in this activity by
		// adding or replacing the detail fragment using a
		// fragment transaction.
		Bundle arguments = new Bundle();
		arguments.putString(ARG_SERVER_ID, server);
		arguments.putString(ARG_CONVERSATION_ID, conversation);
		Fragment fragment, fragment2;

		if (server != null && conversation != null)
		{
			fragment = new ConversationDetailFragment();
			fragment2 = new BuddyListFragment();
		} else
		{
			fragment2 = new CreditsFragment();
			if (server != null)
				fragment = new ServerDetailFragment();
			else
				fragment = new JoinServerFragment();
		}

		fragment.setArguments(arguments);
		setConversationFragment(fragment);
		fragment2.setArguments(arguments);
		setBuddyListFragment(fragment2);

		getSlidingMenu().showContent();
	}
}
