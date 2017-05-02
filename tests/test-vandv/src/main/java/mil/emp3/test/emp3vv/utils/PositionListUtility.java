package mil.emp3.test.emp3vv.utils;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.dialogs.PositionListDialog;

/**
 * Put up a dialog that lets the user type in the positions. This is complement to PositionUtility that picks up positions
 * based on taps on the screen. This class will turn off the PositionUtility listeners.
 */
public class PositionListUtility implements PositionListDialog.IPositionListDialogListener {
    private PositionListDialog positionListDialog;
    private List<EmpGeoPosition> userPositionsList;
    final private View view;
    final private IMap map;

    public PositionListUtility(View parentView, IMap map) {
        if((null == parentView) || (null == map)) {
            throw new IllegalArgumentException("parentView and map must be non-null");
        }
        this.view = parentView;
        this.map = map;
    }
    public void onCreateView(final FragmentManager fm, final PositionUtility positionUtility) {
        // Allows user to type in positions. Once this button is selected, position utility is turned off, i.e.
        // Tapping on the map will have no consequences to position list.

        final Button positionsList = (Button) view.findViewById(R.id.positionsList);
        positionsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null == userPositionsList) {
                    userPositionsList = new ArrayList<>();
                    userPositionsList.add(new EmpGeoPosition());
                }
                if(null != positionUtility) {
                    positionUtility.switchToManual(userPositionsList);
                }
                positionListDialog = PositionListDialog.newInstanceForOptItem("Positions List", PositionListUtility.this, map, userPositionsList, 1);
                positionListDialog.show(fm, "position_list_fragment");
            }
        });
    }

    public void stop() {
        if(null != positionListDialog) {
            positionListDialog.dismiss();
        }
    }

    @Override
    public void positionsSet(PositionListDialog positionListDialog) {
        if ((userPositionsList != null) && (userPositionsList.size() > 0)){
            TextView tv = (TextView) view.findViewById(R.id.position);
            tv.setText(String.format(Locale.US, "Lat: %1$6.6f Lon: %2$6.6f", userPositionsList.get(0).getLatitude(), userPositionsList.get(0).getLongitude()));
        }
    }
}
