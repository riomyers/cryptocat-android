package net.dirbaio.cryptocat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import net.dirbaio.cryptocat.service.CryptocatService;

public class MainActivity extends SherlockFragmentActivity
		implements ConversationListFragment.Callbacks
{

	public static final String ARG_SERVER_ID = "net.dirbaio.cryptocat.SERVER_ID";
	public static final String ARG_CONVERSATION_ID = "net.dirbaio.cryptocat.CONVERSATION_ID";
	public static final String ARG_BUDDY_ID = "net.dirbaio.cryptocat.BUDDY_ID";
	public static final String STATE_MENU_SHOWING = "net.dirbaio.cryptocat.MENU_SHOWING";
	public static final String STATE_SECONDARY_MENU_SHOWING = "net.dirbaio.cryptocat.SECONDARY_MENU_SHOWING";
	public static final String STATE_SELECTED_SERVER = "net.dirbaio.cryptocat.SELECTED_SERVER";
	public static final String STATE_SELECTED_CONVERSATION = "net.dirbaio.cryptocat.SELECTED_CONVERSATION";
	public static final String STATE_SELECTED_BUDDY = "net.dirbaio.cryptocat.SELECTED_BUDDY";

	private SlidingMenu sm;

	public BoundFragment currFragment;
	private ConversationListFragment conversationList;
	private String selectedServer, selectedConversation, selectedBuddy;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Start the service if it isn't already started.
		startService(new Intent(this, CryptocatService.class));

		//Main content view
		setContentView(R.layout.frame_conversation_detail);

		// customize the SlidingMenu
		sm = new SlidingMenu(this);

		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setSecondaryShadowDrawable(R.drawable.shadowright);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setMode(SlidingMenu.LEFT_RIGHT);

		sm.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);

		//SlidingMenu left view (conversation list)
		sm.setMenu(R.layout.frame_conversation_list);

		//SlidingMenu right view (user list)
		sm.setSecondaryMenu(R.layout.frame_buddy_list);

		//Restore instance state.
		if(savedInstanceState != null)
		{
			if(savedInstanceState.getBoolean(STATE_SECONDARY_MENU_SHOWING))
				sm.showSecondaryMenu(false);
			else if(savedInstanceState.getBoolean(STATE_MENU_SHOWING))
				sm.showMenu(false);

			selectedServer = savedInstanceState.getString(STATE_SELECTED_SERVER);
			selectedConversation = savedInstanceState.getString(STATE_SELECTED_CONVERSATION);
			selectedBuddy = savedInstanceState.getString(STATE_SELECTED_BUDDY);
		}

		//Create conversation list
		conversationList = new ConversationListFragment();
		setConversationListFragment(conversationList);

		getSupportActionBar().setTitle("Cryptocat");

		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		//All done, now set contents!
		selectItem(selectedServer, selectedConversation, selectedBuddy);
	}

	private void setFragment(int id, Fragment fragment)
	{
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
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

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_SECONDARY_MENU_SHOWING, sm.isSecondaryMenuShowing());
		outState.putBoolean(STATE_MENU_SHOWING, sm.isMenuShowing());
		outState.putString(STATE_SELECTED_SERVER, selectedServer);
		outState.putString(STATE_SELECTED_CONVERSATION, selectedConversation);
		outState.putString(STATE_SELECTED_BUDDY, selectedBuddy);
	}

	/**
	 * Callback method from {@link ConversationListFragment.Callbacks}
	 * indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String server, String conversation, String buddy)
	{
		selectItem(server, conversation, buddy);
		sm.showContent();
	}

	public void selectItem(String server, String conversation, String buddy)
	{
		selectedServer = server;
		selectedConversation = conversation;
		selectedBuddy = buddy;

		conversationList.setSelectedItem(server, conversation, buddy);

		// In two-pane mode, show the detail view in this activity by
		// adding or replacing the detail fragment using a
		// fragment transaction.
		Bundle arguments = new Bundle();
		arguments.putString(ARG_SERVER_ID, server);
		arguments.putString(ARG_CONVERSATION_ID, conversation);
		arguments.putString(ARG_BUDDY_ID, buddy);
		BoundFragment fragment;
		Fragment fragment2;

		if (server != null && conversation != null)
		{
			fragment = new ConversationDetailFragment();
			fragment2 = new BuddyListFragment();
		}
		else
		{
			fragment2 = new CreditsFragment();
			if (server != null)
				fragment = new ServerDetailFragment();
			else
				fragment = new JoinServerFragment();
		}

		currFragment = fragment;
		fragment.setArguments(arguments);
		setConversationFragment(fragment);

		fragment2.setArguments(arguments);
		setBuddyListFragment(fragment2);
	}
}
