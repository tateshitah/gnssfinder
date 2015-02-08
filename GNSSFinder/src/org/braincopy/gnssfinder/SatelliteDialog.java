package org.braincopy.gnssfinder;

import org.braincopy.silbala.ARActivity;
import org.braincopy.silbala.ARObjectDialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author Hiroaki Tateshita
 * @version 0.7.2
 * 
 */
public class SatelliteDialog extends ARObjectDialog {
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Dialog dialog = new Dialog(getActivity());
		// Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		dialog.setContentView(R.layout.satellite_dialog);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		final int index = getArguments().getInt("index");
		// if "index" is not set, getInt() method returns 0,
		// so when index set, add 1
		if (index > 0) {
			((Button) dialog.findViewById(R.id.arObjDialogButton1))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							((ARActivity) getActivity()).getARView()
									.getArObjs()[index - 1].setTouched(false);
							dismiss();
						}
					});
		}
		String catNo = getArguments().getString("catNo");
		if (catNo != null) {
			((TextView) dialog.findViewById(R.id.catNoTextView))
					.setText("NORAD CAT NO: " + catNo);
		}
		String message = getArguments().getString("message");
		if (message != null) {
			((TextView) dialog.findViewById(R.id.arObjDialogTextView1))
					.setText(message);
		}
		String gnssString = getArguments().getString("gnssStr");
		if (gnssString != null) {
			((ImageView) dialog.findViewById(R.id.satelliteImageView1))
					.setImageBitmap(Satellite.getGNSSImage(gnssString,
							getResources()));
		}
		return dialog;
	}
}
