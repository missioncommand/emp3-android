package mil.emp3.test.emp3vv.containers.dialogs;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class UpdateFeatureDialog extends UpdateContainerDialog {
    private static String TAG = UpdateFeatureDialog.class.getSimpleName();

    private EditText latitude;
    private EditText longitude;
    private EditText altitude;

    public UpdateFeatureDialog() {
        super();
    }
    public static UpdateFeatureDialog newInstance(IMap map, IFeature feature, IUpdateContainerDialogListener listener, boolean showOnly) {

        if ((null == listener) || (null == map) || (null == feature)) {
            throw new IllegalArgumentException("listener/map/overlay must be non-null");
        }

        UpdateFeatureDialog frag = new UpdateFeatureDialog();
        frag.init(map, feature, listener, showOnly);

        return frag;
    }

    @Override
    protected void setupAddParentList(View view) {
        addParentsList = (ListView) view.findViewById(R.id.add_parents_list);
        addParentListData = MapNamesUtility.getNames(map, false, true, true);
        addParentsListAdapter = setupMultiChoiceList("New Parents", addParentsList, addParentListData);

        Button addParentsButton = (Button) view.findViewById(R.id.add_parents);
            addParentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(addParentsList.getCheckedItemCount() == 0) {
                        ErrorDialog.showError(getContext(),"You must select at least one parent");
                        return;
                    } else {
                        Log.d(TAG, "getCheckedCount " + addParentsList.getCheckedItemCount());
                        ((IUpdateContainerDialogListener)listener).addParents(UpdateFeatureDialog.this);
                        resetListsOnDataChange();
                    }
                   }
             });
    }

    @Override
    protected void setupAddChildrenList(View view) {
        addChildrenList = (ListView) view.findViewById(R.id.add_children_list);
        addChildrenListData = MapNamesUtility.getNames(map, false, false, true);
        addChildrenListAdapter = setupMultiChoiceList("New Children", addChildrenList, addChildrenListData);

        Button addChildrenButton = (Button) view.findViewById(R.id.add_children);
        addChildrenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addChildrenList.getCheckedItemCount() == 0) {
                    ErrorDialog.showError(getContext(),"You must select at least one child");
                    return;
                } else {
                    Log.d(TAG, "getCheckedCount " + addChildrenList.getCheckedItemCount());
                    ((IUpdateContainerDialogListener)listener).addChildren(UpdateFeatureDialog.this);
                    resetListsOnDataChange();
                }
            }
        });
    }

    @Override
    protected void resetAddParentList() {
        addParentListData = MapNamesUtility.getNames(map, false, true, true);
        addParentsListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void resetAddChildrenList() {
        addChildrenListData = MapNamesUtility.getNames(map, false, false, true);
        addChildrenListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void setupOptional(View view) {

        view.findViewById(R.id.position_layout).setVisibility(View.VISIBLE);
        Button positionButton = (Button) view.findViewById(R.id.update_position);
        latitude = (EditText) view.findViewById(R.id.latitude);
        longitude = (EditText) view.findViewById(R.id.longitude);
        altitude = (EditText) view.findViewById(R.id.altitude);

        // Shows only the first position, dialog launched here will show a list of all positions that can be updated
        final IFeature feature = (IFeature) me;
        latitude.setText(String.format("%1$6.3f", feature.getPositions().get(0).getLatitude()));
        longitude.setText(String.format("%1$6.3f", feature.getPositions().get(0).getLongitude()));
        altitude.setText(String.format("%1$d", (long) feature.getPositions().get(0).getAltitude()));

        // We need a dialog to edit positions of multi point features
        if((feature.getPositions().size() > 1) || (showOnly)){
            latitude.setEnabled(false);
            longitude.setEnabled(false);
            altitude.setEnabled(false);
        }

        if(showOnly) {
            if(feature.getPositions().size() > 1) {
                // Launch a dialog with list of positions, make it a modal dialog
                positionButton.setText("Show");
                positionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch a dialog with list of positions, make it a modal dialog
                    }
                });

            } else {
                positionButton.setVisibility(View.GONE);
            }
        } else {
            positionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (feature.getPositions().size() > 1) {
                        // Launch a dialog with list of positions, make it a modal dialog
                    } else {
                        // update the position based on user input on this screen
                        try {
                            EmpGeoPosition newPosition = new EmpGeoPosition(Double.parseDouble(latitude.getText().toString()),
                                    Double.parseDouble(longitude.getText().toString()),
                                    Double.parseDouble(altitude.getText().toString()));
                            feature.getPositions().clear();
                            feature.getPositions().add(newPosition);
                            feature.apply();
                        } catch (Exception e) {
                            Log.e(TAG, "Exception processing position update ", e);
                        }
                    }
                }
            });
        }

        if(feature.getFeatureType().equals(FeatureTypeEnum.GEO_MIL_SYMBOL)) {
            view.findViewById(R.id.symbol_layout).setVisibility(View.VISIBLE);
            TextView symbolCode = (TextView) view.findViewById(R.id.symbol_code);
            final MilStdSymbol milStdSymbol = (MilStdSymbol) feature;
            symbolCode.setText(milStdSymbol.getSymbolCode());
            Button symbolButton = (Button) view.findViewById(R.id.update_symbol);
            if(!showOnly) {
                symbolButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch the symbol_properties_dialogi, might want to make it modal
                    }
                });
            } else {
                symbolButton.setVisibility(View.GONE);
            }
        } else {
            view.findViewById(R.id.symbol_layout).setVisibility(View.GONE);
        }

        if(isBufferApplicable(feature.getFeatureType())) {
            view.findViewById(R.id.buffer_layout).setVisibility(View.VISIBLE);
            final EditText bufferValue = (EditText) view.findViewById(R.id.bufferValue);
            bufferValue.setText(String.valueOf(feature.getBuffer()));

            Button bufferButton = (Button) view.findViewById(R.id.update_buffer);
            if(!showOnly) {
                bufferButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch the symbol_properties_dialogi, might want to make it modal
                        feature.setBuffer(Double.parseDouble(bufferValue.getText().toString()));
                        feature.apply();
                    }
                });
            } else {
                bufferButton.setVisibility(View.GONE);
            }
        } else {
            view.findViewById(R.id.buffer_layout).setVisibility(View.GONE);
        }
    }

    boolean hasUpdatableProperties(FeatureTypeEnum featureType) {
        switch(featureType) {
            case GEO_SQUARE:
            case GEO_RECTANGLE:
            case GEO_CIRCLE:
            case GEO_ELLIPSE:
                return true;
            default:
                return false;
        }
    }
    boolean isBufferApplicable(FeatureTypeEnum featureType) {
        switch(featureType) {
            case GEO_POLYGON:
            case GEO_SQUARE:
            case GEO_RECTANGLE:
            case GEO_CIRCLE:
            case GEO_ELLIPSE:
            case GEO_POINT:
            case GEO_PATH:
                return true;
            default:
                return false;
        }
    }

    /**
     * Setup the "Update Other Properties" Button
     * @param view
     * @param parentList
     */
    protected void setupUpdatePropertiesButton(View view, final List<String> parentList) {
        final Button updateProperties = (Button) view.findViewById(R.id.update_other_properties);
        if((me instanceof IFeature) && (!showOnly) && hasUpdatableProperties(((IFeature) me).getFeatureType())) {
            updateProperties.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateOtherProperties(parentList, me.getName(),
                            ( map.getVisibility(me) == VisibilityStateEnum.HIDDEN ? false : true));
                }
            });
        } else {
            updateProperties.setVisibility(View.GONE);
        }
    }

    /**
     * This is the callback for "Update Other Properties" button. It puts up a dialog that contains values for current properties
     * and allows the user to change them.
     * @param parentList
     * @param featureName
     * @param visible
     */
    @Override
    protected void updateOtherProperties(List<String> parentList, String featureName, boolean visible) {

        FeaturePropertiesDialog featurePropertiesDialog = null;
        if(me instanceof IFeature) {
            switch (((IFeature) me).getFeatureType()) {
                case GEO_CIRCLE:
                    featurePropertiesDialog = CirclePropertiesDialog.newInstanceForOpt("Circle Properties", map, parentList,
                            featureName, visible, new UpdateCircleProperties(getContext(), (IFeature) me), (Circle) me);
                    break;
                case GEO_ELLIPSE:
                    featurePropertiesDialog = EllipsePropertiesDialog.newInstanceForOpt("Ellipse Properties", map, parentList,
                            featureName, visible, new UpdateEllipseProperties(getContext(), (IFeature) me), (Ellipse) me);
                    break;
                case GEO_RECTANGLE:
                    featurePropertiesDialog = RectanglePropertiesDialog.newInstanceForOpt("Rectangle Properties", map, parentList,
                            featureName, visible, new UpdateRectangleProperties(getContext(), (IFeature) me), (Rectangle) me);
                    break;
                case GEO_SQUARE:
                    featurePropertiesDialog = SquarePropertiesDialog.newInstanceForOpt("Square Properties", map, parentList,
                            featureName, visible, new UpdateSquareProperties(getContext(), (IFeature) me), (Square) me);
                    break;
                default:
                    Log.e(TAG, "updateOtherProperties unsupported feature " + me.getClass().getSimpleName());
            }
        }
        if(null != featurePropertiesDialog) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            featurePropertiesDialog.show(fm, "fragment_shape_properties_dialog");
        }
    }

    /**
     * Base class used to update properties for basic shapes.
     * @param <T>
     */
    class UpdateProperties<T extends FeaturePropertiesDialog<T>>
            implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<T> {

        protected Context context;
        protected IFeature feature;
        UpdateProperties(Context context, IFeature feature) {
            this.context = context;
            this.feature = feature;
        }
        @Override
        public boolean onFeaturePropertiesSaveClick(T dialog) {
            try {
                if(isBufferApplicable(feature.getFeatureType())) {
                    feature.setBuffer(dialog.getBufferValue());
                }
                feature.apply();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "UpdateProperties.onFeaturePropertiesSaveClick", e);
                ErrorDialog.showError(context, "Failed to add Feature " + feature.getClass().getSimpleName() + " " + e.getMessage());
            }
            return false;
        }

        @Override
        public void onFeaturePropertiesCancelClick(T dialog) { }
    }

    class UpdateCircleProperties extends UpdateProperties<CirclePropertiesDialog> {

        UpdateCircleProperties(Context context, IFeature feature) {
            super(context, feature);
        }

        @Override
        public boolean onFeaturePropertiesSaveClick(CirclePropertiesDialog dialog) {
            if(feature instanceof Circle) {
                Circle circle = (Circle) feature;
                circle.setRadius(dialog.getRadiusValue());
                return (super.onFeaturePropertiesSaveClick(dialog));
            }
            return false;
        }
    }

    class UpdateEllipseProperties extends UpdateProperties<EllipsePropertiesDialog> {

        UpdateEllipseProperties(Context context, IFeature feature) {
            super(context, feature);
        }

        @Override
        public boolean onFeaturePropertiesSaveClick(EllipsePropertiesDialog dialog) {
            if(feature instanceof Ellipse) {
                Ellipse ellipse = (Ellipse) feature;
                ellipse.setSemiMajor(dialog.getSemiMajorValue());
                ellipse.setSemiMinor(dialog.getSemiMinorValue());
                ellipse.setAzimuth(dialog.getAzimuthValue());
                return (super.onFeaturePropertiesSaveClick(dialog));
            }
            return false;
        }
    }

    class UpdateRectangleProperties extends UpdateProperties<RectanglePropertiesDialog> {

        UpdateRectangleProperties(Context context, IFeature feature) {
            super(context, feature);
        }

        @Override
        public boolean onFeaturePropertiesSaveClick(RectanglePropertiesDialog dialog) {
            if(feature instanceof Rectangle) {
                Rectangle rectangle = (Rectangle) feature;
                rectangle.setAzimuth(dialog.getAzimuthValue());
                rectangle.setHeight(dialog.getHeightValue());
                rectangle.setWidth(dialog.getWidthValue());
                return (super.onFeaturePropertiesSaveClick(dialog));
            }
            return false;
        }
    }

    class UpdateSquareProperties extends UpdateProperties<SquarePropertiesDialog> {

        UpdateSquareProperties(Context context, IFeature feature) {
            super(context, feature);
        }

        @Override
        public boolean onFeaturePropertiesSaveClick(SquarePropertiesDialog dialog) {
            if(feature instanceof Square) {
                Square square = (Square) feature;
                square.setAzimuth(dialog.getAzimuthValue());
                square.setWidth(dialog.getWidthValue());
                return (super.onFeaturePropertiesSaveClick(dialog));
            }
            return false;
        }
    }
}
