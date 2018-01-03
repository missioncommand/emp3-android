package mil.emp3.worldwind.feature.support;

import org.cmapi.primitives.IGeoPosition;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.feature.MilStd2525SinglePoint;

/**
 * The MilStd2525LevelOfDetailSelector determines which set of Bitmaps to use for the PlacemarkAttributes of MilStd2525Placemark. A
 * MilStd2525SinglePoint creates an oInstance of this class in its constructor, and calls
 * Placemark.LevelOfDetailSelector#selectLevelOfDetail(RenderContext, Placemark, double) in its doRender() method.
 * This implementation draw all icons further than FAR_THRESHOLD distance from the camera as a dot. It draws
 * icons between FAR_THRESHOLD and MID_THRESHOLD distance as MilStd icon with not modifiers. And icons
 * closer tha MID_THRESHOLD distance as a MilStd icon with modifiers and attributes.
 */
public class MilStd2525LevelOfDetailSelector implements Placemark.LevelOfDetailSelector {
    private static final String TAG = MilStd2525LevelOfDetailSelector.class.getSimpleName();

    private static MilStd2525LevelOfDetailSelector oInstance = null;

    private static IMilStdRenderer oMilStdIconRenderer;

    protected final static int HIGHEST_LEVEL_OF_DETAIL = 0;

    protected final static int MEDIUM_LEVEL_OF_DETAIL = 1;

    protected final static int LOW_LEVEL_OF_DETAIL = 2;

    protected static double FAR_THRESHOLD = 30000;

    protected static double MID_THRESHOLD = 10000;

    private static int numFeaturesHighThreshold = 2000;
    private static int numFeaturesMidThreshold = 4000;
    private static double iconSize = IconSizeEnum.SMALL.getScaleFactor();

    /**
     * This static method initializes the oInstance.
     * @param iconRenderer
     * @return
     */
    public synchronized static MilStd2525LevelOfDetailSelector initInstance(IMilStdRenderer iconRenderer) {
        if (MilStd2525LevelOfDetailSelector.oInstance == null) {
            MilStd2525LevelOfDetailSelector.oInstance = new MilStd2525LevelOfDetailSelector();
            MilStd2525LevelOfDetailSelector.oMilStdIconRenderer = iconRenderer;
        }

        MilStd2525.setRenderer(iconRenderer);
        return MilStd2525LevelOfDetailSelector.oInstance;
    }

    public static MilStd2525LevelOfDetailSelector getInstance() {
        return MilStd2525LevelOfDetailSelector.oInstance;
    }

    /**
     * Sets the far distance threshold; camera distances greater than this value use the low level of detail, and
     * distances less than this value but greater than the near threshold use the medium level of detail.
     *
     * @param dValue camera distance threshold in meters
     */
    public static void setFarDistanceThreshold(double dValue) {
        FAR_THRESHOLD = dValue;
    }
    public static double getFarDistanceThreshold() {
        return FAR_THRESHOLD;
    }

    /**
     * Sets the near distance threshold; camera distances greater than this value but less that the far threshold use
     * the medium level of detail, and distances less than this value use the high level of detail.
     *
     * @param dValue camera distance threshold in meters
     */
    public static void setMidDistanceThreshold(double dValue) {
        MID_THRESHOLD = dValue;
    }
    public static double getMidDistanceThreshold() {
        return MID_THRESHOLD;
    }

    /**
     * Sets the mid number of MilStd symbols threshold;  Having more symbols than this value, but less
     * than that of the high detail threshold, use the medium level of details, and having a total
     * count less than this value use a high level of detail.
     * @param midDetailThreshold number of MilStd symbols threshold
     */
    public static void setMidDetailThreshold(int midDetailThreshold) {
        numFeaturesHighThreshold = midDetailThreshold;
    }
    public static int getMidDetailThreshold() {
        return numFeaturesHighThreshold;
    }

    /**
     * Sets the high number of MilStd symbols threshold;  Having more symbols than this value, results
     * in the symbols being rendered with a low level of detail,, and having a total smaller than this
     * value but more than the mid detail threshold, use a medium level of details.
     * @param highDetailThreshold number of MilStd symbols threshold
     */
    public static void setHighDetailThreshold(int highDetailThreshold) {
        numFeaturesMidThreshold = highDetailThreshold;
    }
    public static int getHighDetailThreshold() {
        return numFeaturesMidThreshold;
    }

    /**
     * Set the size that MilStd symbols should be rendered at
     * @param size the size that MilStd symbols should be rendered at
     */
    public static void setIconSize(IconSizeEnum size) {
        iconSize = size.getScaleFactor();
    }

    private MilStd2525LevelOfDetailSelector() {
    }

    /**
     * Gets the active attributes for the current distance to the camera and highlighted state.
     *
     * @param rc             The current render context
     * @param placemark      The placemark needing a level of detail selection
     * @param cameraDistance The distance from the placemark to the camera (meters)
     */
    @Override
    public void selectLevelOfDetail(RenderContext rc, Placemark placemark, double cameraDistance) {
        if (!(placemark instanceof MilStd2525SinglePoint.EMPPlacemark)) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "MilStd2525LevelOfDetailSelector", "selectLevelOfDetail",
                            "The placemark is not a MilStd2525SinglePoint"));
        }
        MilStd2525SinglePoint milStdPlacemark = ((MilStd2525SinglePoint.EMPPlacemark) placemark).featureMapper;
        PlacemarkAttributes placemarkAttributes = null;//placemark.getAttributes();
        int lastLevelOfDetail = milStdPlacemark.getLastLevelOfDetail();
        int featureCount = milStdPlacemark.getMapInstance().getFeatureHash().size();
        // Update position.
        IGeoPosition oPos = milStdPlacemark.getSymbol().getPosition();
        placemark.getPosition().set(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude());

        // Determine the normal attributes based on the distance from the camera to the placemark
        if (cameraDistance > FAR_THRESHOLD || featureCount > numFeaturesMidThreshold) {
            // Low-fidelity: use affiliation only
            if ((lastLevelOfDetail != LOW_LEVEL_OF_DETAIL) || milStdPlacemark.isDirty()) {
                milStdPlacemark.setLastLevelOfDetail(LOW_LEVEL_OF_DETAIL);
                String simpleCode = "S" + armyc2.c2sd.renderer.utilities.SymbolUtilities.getAffiliation(milStdPlacemark.getSymbolCode()) + "P*------*****"; // SIDC
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(simpleCode);
                placemarkAttributes.setDrawLeader(true);
            }
        } else if (cameraDistance > MID_THRESHOLD || featureCount > numFeaturesHighThreshold) {
            // Medium-fidelity: use the regulation SIDC code with attributes but without modifiers
            if ((lastLevelOfDetail != MEDIUM_LEVEL_OF_DETAIL) || milStdPlacemark.isDirty()) {
                milStdPlacemark.setLastLevelOfDetail(MEDIUM_LEVEL_OF_DETAIL);
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(milStdPlacemark);
                placemarkAttributes.setDrawLeader(true);
            }
        } else {
            // High-fidelity: use the regulation SIDC code the modifiers and attributes
            if ((lastLevelOfDetail != HIGHEST_LEVEL_OF_DETAIL) || milStdPlacemark.isDirty()) {
                milStdPlacemark.setLastLevelOfDetail(HIGHEST_LEVEL_OF_DETAIL);
                placemarkAttributes = MilStd2525.getPlacemarkAttributes(milStdPlacemark);
                placemarkAttributes.setDrawLeader(true);
            }
        }

        // Update the placemark's attributes bundle
        if (placemarkAttributes != null) {
            // Apply the symbols and Icon size setting scales.
            double dScale = iconSize * milStdPlacemark.getIconScale();
            placemarkAttributes.setImageScale(dScale);
            placemark.setAttributes(placemarkAttributes);

            if (milStdPlacemark.isDirty()) {
                milStdPlacemark.resetDirty();
            }
        }
    }
}
