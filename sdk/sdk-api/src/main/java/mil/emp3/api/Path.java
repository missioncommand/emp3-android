package mil.emp3.api;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpGeoPosition;

import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoRenderable;

/**
 * This class implements the Path (Multi segment line) feature. It accepts a list of coordinates
 * where each adjacent pair of coordinates identifies a line segment.
 */
public class Path extends Feature<IGeoRenderable> implements IFeature<IGeoRenderable> {
    /**
     * This constructor creates a default path feature.
     */
    public Path() {
        super(new GeoRenderable(), FeatureTypeEnum.GEO_PATH);
    }

    /**
     * This constructor creates a default path feature with the position list provided.
     * @param oPositionList
     */
    public Path(final List<IGeoPosition> oPositionList) {
        super(new GeoRenderable(), FeatureTypeEnum.GEO_PATH);
        if (null == oPositionList) {
            throw new IllegalArgumentException("The coordinate parameter can NOT be null");
        }
        for (final IGeoPosition geoPosition : oPositionList) {
            if (!EmpGeoPosition.validate(geoPosition)) {
                throw new IllegalArgumentException("GeoPosition is invalid");
            }
        }
        this.getRenderable().getPositions().addAll(oPositionList);
    }

    /**
     * This constructor creates a path feature from the renderable provided.
     * @param oRenderable
     */
    public Path(final IGeoRenderable oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_PATH);
    }

    /**
     *
     * @return String gives path parameters
     */

    @Override
    public String toString() {
        String str =  "Path ";
        if (getPositions() != null && !getPositions().isEmpty()) {
            str += "with " + getPositions().size() + " points\n";
            for (IGeoPosition position : getPositions()) {
                str += "\tlatitude: " + position.getLatitude() + ", " +
                        "\tlongitude: " + position.getLongitude() + ", " +
                        "\taltitude: " + position.getAltitude() + "\n";
            }
        } else {
            str +="with zero points";
        }
        return str;
    }

}
