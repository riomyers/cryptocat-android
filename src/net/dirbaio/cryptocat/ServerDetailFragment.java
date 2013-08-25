package net.dirbaio.cryptocat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import net.dirbaio.cryptocat.service.MultipartyConversation;
import net.dirbaio.cryptocat.service.CryptocatServer;
import net.dirbaio.cryptocat.service.CryptocatStateListener;
import org.jivesoftware.smack.XMPPException;

public class ServerDetailFragment extends BoundFragment implements CryptocatStateListener
{

	private String serverId;
	private CryptocatServer server;

	private View rootView;
	private int oldVisible = -1;

	@Override
	public void stateChanged()
	{
		int visible = R.id.not_connected;
		if(server.getState() == CryptocatServer.State.Connected)
			visible = R.id.join;
		if(server.getState() == CryptocatServer.State.Connecting)
			visible = R.id.connecting;

		if(oldVisible != visible)
		{
			rootView.findViewById(R.id.not_connected).setVisibility(visible==R.id.not_connected?View.VISIBLE:View.GONE);
			rootView.findViewById(R.id.join).setVisibility(visible==R.id.join?View.VISIBLE:View.GONE);
			rootView.findViewById(R.id.connecting).setVisibility(visible==R.id.connecting?View.VISIBLE:View.GONE);

			oldVisible = visible;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		serverId = getArguments().getString(MainActivity.ARG_SERVER_ID);
	}

	@Override
	protected void onServiceBind()
	{
		server = service.getServer(serverId);
		server.addStateListener(this);
		stateChanged();
	}

	@Override
	protected void onServiceUnbind()
	{
		server.removeStateListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.fragment_server_detail, container, false);

		final Button button = (Button) rootView.findViewById(R.id.join_button);
		final EditText roomNameText = (EditText) rootView.findViewById(R.id.name);
		final EditText nicknameText = (EditText) rootView.findViewById(R.id.nickname);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (bound)
				{
					String roomName = roomNameText.getText().toString();
					String nickname = nicknameText.getText().toString();
					try
					{
						MultipartyConversation c;
						c = server.createConversation(roomName, nickname);
						c.join();
						callbacks.onItemSelected(serverId, c.id, null);
					}
					catch (XMPPException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		final Button button2 = (Button) rootView.findViewById(R.id.reconnect_button);
		button2.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(bound)
				{
					server.connect();
				}
			}
		});
		return rootView;
	}
}