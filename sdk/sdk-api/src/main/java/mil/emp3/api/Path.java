package mil.emp3.api;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.interfaces.IFeature;
import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoRenderable;

/**
 * This class implements the Path (Multi segment line) feature. It accepts a list of coordinates
 * where each adjacent pair of coordinates identifies a line segment.
 */
public class Path extends Feature implements IFeature {
    public Path() {
        super(new GeoRenderable(), FeatureTypeEnum.GEO_PATH);
    }

    public Path(List<IGeoPosition> oCoordinates) {
        super(new GeoRenderable(), FeatureTypeEnum.GEO_PATH);
        this.setPositions(oCoordinates);
    }

    public Path(IGeoRenderable oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_PATH);
    }
}
