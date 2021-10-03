package caha42.mmt.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import caha42.mmt.R;
import caha42.mmt.io.CalendarController;

public class AddTrackerDialogFragement extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_addtracker, null))
                    // Add action buttons
                    .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView trackerNameView = AddTrackerDialogFragement.this.getDialog().findViewById(R.id.tracker_name);
                            CalendarController.createCalender(getActivity().getContentResolver(), trackerNameView.getText().toString());
                            // TODO MainActivity should be recreated.
                            getActivity().recreate();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AddTrackerDialogFragement.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

}
