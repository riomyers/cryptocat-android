package net.dirbaio.cryptocat;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import net.dirbaio.cryptocat.service.CryptocatService;

/**
 * Base Fragment class used throughout the app. Adds useful methods to communicate with the activity and service.
 */
public class BaseFragment extends SherlockFragment
{
	//=============
	// Callback stuff

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	protected Callbacks callbacks = dummyCallbacks;

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks))
			throw new IllegalStateException("Activity must implement fragment's callbacks.");

		callbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		callbacks = dummyCallbacks;
	}

	protected CryptocatService getService()
	{
		return CryptocatService.getInstance();
	}


	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks
	{
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String server, String conversation, String buddy);

		public void showMenu();
		public void showSecondaryMenu();
		public void showContent();
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks dummyCallbacks = new Callbacks()
	{
		@Override
		public void onItemSelected(String server, String conversation, String buddy)
		{
		}

		@Override
		public void showMenu()
		{
		}

		@Override
		public void showSecondaryMenu()
		{
		}

		@Override
		public void showContent()
		{
		}
	};


	//Title and menu stuff
	//============

	private boolean selected = false;
	public void setSelected(boolean selected)
	{
		this.selected = selected;
		setMenuVisibility(selected);
		updateTitle();
	}

	protected final void updateTitle()
	{
		if(selected && getActivity() != null)
		{
			ActionBar ab = ((SherlockFragmentActivity)getActivity()).getSupportActionBar();
			if(ab != null)
				onMustUpdateTitle(ab);
		}
	}

	protected void onMustUpdateTitle(ActionBar ab)
	{

	}

    // Alternate Context
    // (Used to make the side fragments have the dark theme
    //================

	private static Context altContext;
	protected Context getAltContext()
	{
		if(altContext == null)
		{
			// create ContextThemeWrapper from the original Activity Context with the custom theme
			altContext = new ContextThemeWrapper(getActivity(), R.style.SlidingMenu);
		}

		return altContext;
	}

	protected LayoutInflater getAltInflater(LayoutInflater inflater)
	{
		// clone the inflater using the ContextThemeWrapper
		return inflater.cloneInContext(getAltContext());
	}
}
