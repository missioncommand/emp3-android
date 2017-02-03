package mil.emp3.api.interfaces;


import org.cmapi.primitives.IGeoBounds;

import mil.emp3.api.utils.EmpBoundingBox;

/**
 * This class defines the interface to an image layer map service.
 */

public interface IImageLayer extends IMapService {
    /**
     * This method retrieves the bounding box defined for the image layer.
     * @return {@link IGeoBounds}
     */
    IGeoBounds getBoundingBox();
}
