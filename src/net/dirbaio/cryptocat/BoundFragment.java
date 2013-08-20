package net.dirbaio.cryptocat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;

public class BoundFragment extends Fragment
{
	//==============
	// Service binding stuff

	protected Handler handler;
	protected CryptocatService service;
	protected boolean bound = false;

	@Override
	public void onStart()
	{
		super.onStart();

		handler = new Handler();

		// Bind to LocalService
		Intent intent = new Intent(this.getActivity(), CryptocatService.class);
		getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		if (bound)
		{
			onServiceUnbind();
			getActivity().unbindService(connection);
			bound = false;
		}
	}

	protected void onServiceBind()
	{

	}

	protected void onServiceUnbind()
	{

	}


	/**
	 * Defines callbacks for service binding, passed to bindService()
	 */
	private final ServiceConnection connection = new ServiceConnection()
	{

		@Override
		public void onServiceConnected(ComponentName className,
		                               IBinder service)
		{
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			CryptocatService.CryptocatBinder binder = (CryptocatService.CryptocatBinder) service;
			BoundFragment.this.service = binder.getService();
			bound = true;

			onServiceBind();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0)
		{
			bound = false;
		}
	};


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
}
