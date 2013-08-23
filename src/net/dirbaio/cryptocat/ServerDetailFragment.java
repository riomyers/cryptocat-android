package net.dirbaio.cryptocat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import net.dirbaio.cryptocat.protocol.MultipartyConversation;
import net.dirbaio.cryptocat.protocol.CryptocatServer;
import net.dirbaio.cryptocat.protocol.CryptocatStateListener;
import org.jivesoftware.smack.XMPPException;

public class ServerDetailFragment extends BoundFragment implements CryptocatStateListener
{

	private String serverId;
	private CryptocatServer server;

	private View rootView;

	@Override
	public void stateChanged()
	{
		//TODO Add some indication the server is connected or disconnected in this fragment
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

		final Button button = (Button) rootView.findViewById(R.id.button);
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

		return rootView;
	}
}