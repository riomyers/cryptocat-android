package net.dirbaio.cryptocat.protocol;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import net.dirbaio.cryptocat.ExceptionRunnable;
import net.dirbaio.cryptocat.MainActivity;
import net.dirbaio.cryptocat.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dirbaio on 8/16/13.
 */
public class CryptocatService extends Service implements CryptocatStateListener
{

	private static CryptocatService instance;

	public static CryptocatService getInstance()
	{
		return instance;
	}

	private Looper serviceLooper;
	private Handler serviceHandler;
	private Handler uiHandler;
	private final HashMap<String, CryptocatServer> servers = new HashMap<>();
	private final ArrayList<CryptocatStateListener> listeners = new ArrayList<>();

	// Binder given to clients
	private final IBinder binder = new CryptocatBinder();

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class CryptocatBinder extends Binder
	{
		public CryptocatService getService()
		{
			// Return this instance of LocalService so clients can call public methods
			return CryptocatService.this;
		}
	}

	//State listener stuff.
	public void addStateListener(CryptocatStateListener listener)
	{
		listeners.add(listener);
	}

	public void removeStateListener(CryptocatStateListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void stateChanged()
	{
		for (CryptocatStateListener l : listeners)
			l.stateChanged();
	}

	@Override
	public void onCreate()
	{
		instance = this;

		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		serviceLooper = thread.getLooper();
		serviceHandler = new Handler(serviceLooper);

		// Create a handler to run stuff on the UI thread.
		uiHandler = new Handler();
	}

	@Override
	public void onDestroy()
	{
		instance = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(this.getApplicationContext())
				.setContentTitle(getText(R.string.ticker_text))
				.setContentIntent(pendingIntent)
				.getNotification();

		startForeground(1, notification);

		return START_STICKY;
	}

	public IBinder onBind(Intent intent)
	{
		return binder;
	}

	//CryptocatServer stuff.

	public void addServer(CryptocatServer s)
	{
		servers.put(s.id, s);
		s.addStateListener(this);
		stateChanged();
	}

	public CryptocatServer getServer(String id)
	{
		return servers.get(id);
	}

	public void removeServer(String id)
	{
		if (getServer(id).getState() != CryptocatServer.State.Disconnected)
			throw new IllegalStateException("Server must be disconnected");

		CryptocatServer s = getServer(id);
		s.removeStateListener(this);
		servers.remove(id);

		stateChanged();
	}

	public void getConversationList(ArrayList<Object> list)
	{
		list.clear();
		for (CryptocatServer s : servers.values())
		{
			list.add(s);
			for (MultipartyConversation c : s.conversations.values())
			{
				list.add(c);
				for(OtrConversation o : c.privateConversations.values())
					list.add(o);
			}
		}
	}

	void post(final ExceptionRunnable r)
	{
		serviceHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					r.run();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	void post(final Runnable r)
	{
		serviceHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					r.run();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}


	void uiPost(final ExceptionRunnable r)
	{
		uiHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					r.run();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	void uiPost(final Runnable r)
	{
		uiHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					r.run();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
