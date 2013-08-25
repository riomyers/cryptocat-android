package net.dirbaio.cryptocat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import net.dirbaio.cryptocat.service.CryptocatServer;

public class JoinServerFragment extends BoundFragment
{

	private View rootView;

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.fragment_join_server, container, false);

		final Button button = (Button) rootView.findViewById(R.id.connect_default);
		final EditText text = (EditText) rootView.findViewById(R.id.text);
		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (bound)
				{
					//TODO Error if already connected to server
					CryptocatServer server = service.createServer("crypto.cat", "conference.crypto.cat", 5222);
					server.connect();
					callbacks.onItemSelected(server.id, null, null);
				}
			}
		});

		return rootView;
	}

}
