package mil.emp3.worldwind.feature.support;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;

import org.cmapi.primitives.IGeoPosition;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.MapInstance;
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

    // Look in BitmapCacheFactory for explanation of this
    private static boolean useWorldWindRenderCache = true;
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

        if(!iconRenderer.getBitmapCacheName().equals("NoBitmapCache")) {
            useWorldWindRenderCache = false;
        } else {
            MilStd2525.setRenderer(iconRenderer);
        }

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
    public static void setFarThreshold(double dValue) {
        FAR_THRESHOLD = dValue;
    }

    /**
     * Sets the near distance threshold; camera distances greater than this value but less that the far threshold use
     * the medium level of detail, and distances less than this value use the high level of detail.
     *
     * @param dValue camera distance threshold in meters
     */
    public static void setMidThreshold(double dValue) {
        MID_THRESHOLD = dValue;
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
        PlacemarkAttributes placemarkAttributes = placemark.getAttributes();
        int lastLevelOfDetail = milStdPlacemark.getLastLevelOfDetail();

        // Update position.
        IGeoPosition oPos = milStdPlacemark.getSymbol().getPosition();
        placemark.setPosition(Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()));

        // Determine the normal attributes based on the distance from the camera to the placemark
        if (cameraDistance > FAR_THRESHOLD) {
            // Low-fidelity: use affiliation only
            if ((lastLevelOfDetail != LOW_LEVEL_OF_DETAIL) || milStdPlacemark.isDirty()) {
                String simpleCode = "S" + armyc2.c2sd.renderer.utilities.SymbolUtilities.getAffiliation(milStdPlacemark.getSymbolCode()) + "P*------*****"; // SIDC
                placemarkAttributes = this.createPlacemarkAttributes(placemarkAttributes, simpleCode, null, null);
                milStdPlacemark.setLastLevelOfDetail(LOW_LEVEL_OF_DETAIL);
            }
        } else if (cameraDistance > MID_THRESHOLD) {
            // Medium-fidelity: use the regulation SIDC code with attributes but without modifiers
            if ((lastLevelOfDetail != MEDIUM_LEVEL_OF_DETAIL) || milStdPlacemark.isDirty()) {
                placemarkAttributes = this.createPlacemarkAttributes(placemarkAttributes, milStdPlacemark.getSymbolCode(), null, milStdPlacemark.getSymbolAttributes());
                milStdPlacemark.setLastLevelOfDetail(MEDIUM_LEVEL_OF_DETAIL);
            }
        } else {
            // High-fidelity: use the regulation SIDC code the modifiers and attributes
            if ((lastLevelOfDetail != HIGHEST_LEVEL_OF_DETAIL) || milStdPlacemark.isDirty()) {
                placemarkAttributes = this.createPlacemarkAttributes(placemarkAttributes, milStdPlacemark.getSymbolCode(), milStdPlacemark.getSymbolModifiers(), milStdPlacemark.getSymbolAttributes());
                milStdPlacemark.setLastLevelOfDetail(HIGHEST_LEVEL_OF_DETAIL);
            }
        }

        // Update the placemark's attributes bundle
        if (placemarkAttributes != null) {
            // Apply the symbols and Icon size setting scales.
            double dScale = milStdPlacemark.getMapInstance().getIconSizeSetting().getScaleFactor() * milStdPlacemark.getIconScale();
            placemarkAttributes.setImageScale(dScale);
            placemark.setAttributes(placemarkAttributes);

            if (milStdPlacemark.isDirty()) {
                milStdPlacemark.resetDirty();
            }
        }
    }

    /**
     * Processing depends on the type of cache we are using. We will keep this logic around until we make a final decision on this issue.
     * @param oPlacemarkAttributes
     * @param sSymbolCode
     * @param oModifiers
     * @param oAttr
     * @return
     */
    private PlacemarkAttributes createPlacemarkAttributes(PlacemarkAttributes oPlacemarkAttributes, String sSymbolCode, SparseArray oModifiers,
                                                          SparseArray oAttr) {
        if(useWorldWindRenderCache) {
            PlacemarkAttributes pma = MilStd2525.getPlacemarkAttributes(sSymbolCode, oModifiers, oAttr);
            pma.setDrawLeader(true);
            return pma;
        } else {
            IEmpImageInfo oImageInfo;

            oImageInfo = oMilStdIconRenderer.getMilStdIcon(sSymbolCode, oModifiers, oAttr);

            if (oImageInfo != null) {
                Rect imageBounds = oImageInfo.getImageBounds();      // The bounds of the entire image, including text
                Point centerPoint = oImageInfo.getCenterPoint();     // The center of the core symbol
                // getCenterPoint reference is top-left, WW needs bottom-left.
                Offset imageOffset = new Offset(WorldWind.OFFSET_FRACTION, ((double) centerPoint.x) / imageBounds.width(), // x offset
                    WorldWind.OFFSET_FRACTION, 1.0 - (((double) centerPoint.y) / imageBounds.height())); // y offset

                if (oPlacemarkAttributes == null) {
                    oPlacemarkAttributes = PlacemarkAttributes.createWithImage(ImageSource.fromBitmap(oImageInfo.getImage())).setImageOffset(imageOffset);
                } else {
                    oPlacemarkAttributes.setImageSource(ImageSource.fromBitmap(oImageInfo.getImage()));
                    oPlacemarkAttributes.setImageOffset(imageOffset);
                }
                oPlacemarkAttributes.setDrawLeader(true);
            }
            return oPlacemarkAttributes;
        }
    }
}
