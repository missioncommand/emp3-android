package mil.emp3.api;


import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IImageLayer;
import mil.emp3.api.utils.EmpBoundingBox;

/**
 * This class implements an image map service layer. It super imposes an image on the map at a specified location
 * bounded by the provided bounding box.
 */

public class ImageLayer extends MapService implements IImageLayer {
    private final IGeoBounds boundingBox;

    /**
     * This constructor creates an Image Layer.
     * @param urlString the URL the image is located at.
     * @param bBox The bounding box where to locate the image on the map.
     * @throws MalformedURLException If the URL is invalid.
     */
    public ImageLayer(String urlString, IGeoBounds bBox)
            throws MalformedURLException {
        super(urlString);
        this.boundingBox = new GeoBounds();
        this.boundingBox.setNorth(bBox.getNorth());
        this.boundingBox.setSouth(bBox.getSouth());
        this.boundingBox.setEast(bBox.getEast());
        this.boundingBox.setWest(bBox.getWest());
    }

    @Override
    public IGeoBounds getBoundingBox() {
        return this.boundingBox;
    }
}
