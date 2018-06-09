package org.braincopy.silbala;

import org.braincopy.gnssfinder2.R;//it should be deleted in terms of independency

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * When create this class, please use serArgments() for showing message.<BR>
 * 
 * Bundle args = new Bundle();<BR>
 * args.putString("message", "some messages ...");<BR>
 * args.putInt("index", i);<BR>
 * dialog.setArguments(args);<BR>
 * 
 * @author Hiroaki Tateshita
 * @version 0.4.6
 * 
 */
public class ARObjectDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Dialog dialog = new Dialog(getActivity());
		// Dialog dialog = new Dialog(getActivity(), R.style.DialogStyle);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		dialog.setContentView(R.layout.ar_obj_dialog);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		// dialog.setTitle("default title");
		final int index = getArguments().getInt("index");
		// if "index" is not set, getInt() method returns 0,
		// so when index set, add 1
		if (index > 0) {
			((Button) dialog.findViewById(R.id.arObjDialogButton1))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// ((ARActivity) getActivity()).touchedFlags[index -
							// 1] = false;
							((ARActivity) getActivity()).getARView()
									.getArObjs()[index - 1].setTouched(false);
							dismiss();
						}
					});
		}
		String message = getArguments().getString("message");
		if (message != null) {
			((TextView) dialog.findViewById(R.id.arObjDialogTextView1))
					.setText(message);
		}
		return dialog;
	}
}
