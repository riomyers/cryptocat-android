package net.dirbaio.cryptocat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends BaseFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		inflater = getAltInflater(inflater);
		View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView versionText = (TextView) rootView.findViewById(R.id.versionText);

        Context context = getAltContext();
        String version = "?";
        try
        {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {}

        versionText.setText(getString(R.string.version)+" "+version);
        return rootView;
	}

}
