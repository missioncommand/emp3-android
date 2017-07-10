package mil.emp3.test.emp3vv.dialogs.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mil.emp3.test.emp3vv.R;

public class EnumerationSelection<E extends Enum<E>> {

    private static String TAG = EnumerationSelection.class.getSimpleName();
    TextView tvSelected;
    Button enumerationButton;

    E userSelected;
    final Class<E> clazz;
    final IEnumerationSelectionListener listener;

    public interface IEnumerationSelectionListener<E extends Enum<E>> {
        void onValueChanged(E newValue);
    }

    public EnumerationSelection(Class<E> clazz, E userSelected) {
        this(clazz, userSelected, null);
    }

    public EnumerationSelection(Class<E> clazz, E userSelected, IEnumerationSelectionListener listener) {
        this.clazz = clazz;
        this.userSelected = userSelected;
        this.listener = listener;
    }
    public E getValue() {
        return userSelected;
    }

    public void onViewCreated(final Activity activity, View view, @Nullable Bundle savedInstanceState) {
        tvSelected = (TextView) view.findViewById(R.id.selected_enumeration);
        tvSelected.setText(userSelected.toString());
        enumerationButton = (Button) view.findViewById(R.id.enumeration);
        enumerationButton.setText(clazz.getSimpleName());
        enumerationButton.setOnClickListener(v -> activity.runOnUiThread(() -> {
            final List<String> enumerations = new ArrayList<>();
            int index = 0;
            int selection = index;
            for (E ise : EnumSet.allOf(clazz)) {
                enumerations.add(ise.toString());
                if(ise.equals(userSelected)) {
                    selection = index;
                }
                index++;
            }
            Log.d(TAG, "onViewCreated selection " + selection + " userSelected " + userSelected.toString());

            final ArrayAdapter<String> adapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_checked, enumerations);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle("Choose " + clazz.getSimpleName())
                    .setSingleChoiceItems(adapter, selection, (dialog, which) -> {
                        for (E ise : EnumSet.allOf(clazz)) {
                            if(ise.toString().equals(enumerations.get(which)) ) {
                                userSelected = ise;
                                if(null != listener) {
                                    listener.onValueChanged(userSelected);
                                }
                            }
                        }
                        tvSelected.setText(enumerations.get(which));
                        dialog.cancel();
                    });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }));
    }
}
