/**
 * 
 */
package com.cryptomountain.sweepfreeq;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.app.Fragment;

public final class AboutFragment extends Fragment {
	
	public static String version = "V1.0.0.0";
	
	public void versionInfo(){
		PackageInfo pInfo = new PackageInfo();
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		version = pInfo.versionName;
	}
}
