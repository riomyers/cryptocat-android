package net.dirbaio.cryptocat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import net.dirbaio.cryptocat.service.CryptocatService;

public class BoundFragment extends Fragment
{
	//=============
	// Callback stuff


	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	protected Callbacks callbacks = sDummyCallbacks;

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks))
		{
			throw new IllegalStateException("Activity must implement fragment's callbacks.");
		}

		callbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach()
	{
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		callbacks = sDummyCallbacks;
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
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks()
	{
		@Override
		public void onItemSelected(String server, String conversation, String buddy)
		{
		}
	};


	//Title stuff
	public void updateTitle()
	{

	}

	private Context altContext;
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
