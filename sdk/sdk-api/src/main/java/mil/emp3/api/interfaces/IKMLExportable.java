package mil.emp3.api.interfaces;


import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This class defines the interface for all objects that can be exported to KML.
 */

public interface IKMLExportable extends IContainer {

    /**
     * This method generates a KML document string that contains the exportable object and all of its content.
     * @return A kml 2.2 document string or an empty string.
     * @throws IOException An IO exception is raised during the export.
     * @throws EMP_Exception If the EMP object is not exportable to KML.
     */
    String exportToKML() throws IOException, EMP_Exception;

    /**
     * This method is called to allow the exportable to serialize its styles.
     * @param xmlSerializer
     * @throws IOException
     */
    void exportStylesToKML(XmlSerializer xmlSerializer) throws IOException;

    /**
     * This method is called to allow the exportable to serialize itself.
     * @param xmlSerializer
     * @throws IOException
     */
    void exportEmpObjectToKML(XmlSerializer xmlSerializer) throws IOException;
}
