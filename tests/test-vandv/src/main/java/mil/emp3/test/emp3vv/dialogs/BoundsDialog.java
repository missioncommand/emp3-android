package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class BoundsDialog extends Emp3TesterDialogBase {
    private static String TAG = BoundsDialog.class.getSimpleName();

    private EditText north;
    private EditText east;
    private EditText west;
    private EditText south;

    public interface IBoundsDialogListener extends IEmp3TesterDialogBaseListener {
        boolean boundsSet(BoundsDialog dialog);
    }

    public BoundsDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static BoundsDialog newInstance(String title, IMap map, IBoundsDialogListener listener) {

        BoundsDialog frag = new BoundsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        return frag;
    }

    public static BoundsDialog newInstanceForOptItem(String title, IMap map, IBoundsDialogListener listener) {

        BoundsDialog frag = new BoundsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        return frag;
    }

    public IGeoBounds getBounds() {
        IGeoBounds bounds = new GeoBounds();

        bounds.setNorth(Double.valueOf(north.getText().toString()));
        bounds.setEast(Double.valueOf(east.getText().toString()));
        bounds.setWest(Double.valueOf(west.getText().toString()));
        bounds.setSouth(Double.valueOf(south.getText().toString()));

        return bounds;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bounds_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        north = (EditText) view.findViewById(R.id.north);
        east = (EditText) view.findViewById(R.id.east);
        west = (EditText) view.findViewById(R.id.west);
        south = (EditText) view.findViewById(R.id.south);

        IGeoBounds bounds = map.getBounds();

        north.setText(String.format("%1$6.3f", bounds.getNorth()));
        east.setText(String.format("%1$6.3f", bounds.getEast()));
        west.setText(String.format("%1$6.3f", bounds.getWest()));
        south.setText(String.format("%1$6.3f", bounds.getSouth()));

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BoundsDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != BoundsDialog.this.listener) {
                    IGeoBounds bounds = getBounds();
                    if(bounds.getNorth() > 90.0 || (bounds.getNorth() < -90.0)) {
                        ErrorDialog.showError(getContext(), "-90.0 <= North <= 90.0");
                        return;
                    }
                    if(bounds.getEast() > 180.0 || (bounds.getEast() < -180.0)) {
                        ErrorDialog.showError(getContext(), "-180.0 <= East <= 180.0");
                        return;
                    }
                    if(bounds.getWest() > 180.0 || (bounds.getWest() < -180.0)) {
                        ErrorDialog.showError(getContext(), "-180.0 <= West <= 180.0");
                        return;
                    }
                    if(bounds.getSouth() > 90.0 || (bounds.getSouth() < -90.0)) {
                        ErrorDialog.showError(getContext(), "-90.0 <= South <= 90.0");
                        return;
                    }
                    ((IBoundsDialogListener)BoundsDialog.this.listener).boundsSet(BoundsDialog.this);
                }
            }
        });
    }
}
