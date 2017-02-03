package mil.emp3.api.utils;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;

/**
 * Builds MilStdSymbol which is used by all the test case.
 */
public class BasicUtilities {
    public static MilStdSymbol generateMilStdSymbol(String description, UUID uuid, double latitude, double longitude) {
        java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
        IGeoPosition oPosition = new GeoPosition();
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        oPositionList.add(oPosition);
        MilStdSymbol oSPSymbol = null;
        try {
            oSPSymbol = new MilStdSymbol(
                    IGeoMilSymbol.SymbolStandard.MIL_STD_2525C,
                    "SFAPMFF--------");
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        oSPSymbol.getPositions().clear();
        oSPSymbol.getPositions().addAll(oPositionList);
        oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "My First Icon");
        oSPSymbol.setName(description);
        oSPSymbol.setDescription(description);
        return oSPSymbol;
    }

    public static MilStdSymbol generateMilStdSymbol(String description, UUID uuid, double latitude, double longitude,
                                                    double altitude) {
        MilStdSymbol feature = generateMilStdSymbol(description, uuid, latitude, longitude);
        List<IGeoPosition> positions = feature.getPositions();
        positions.get(0).setAltitude(altitude);
        return feature;
    }

    public static List<IFeature> generateMilStdSymbolList(int howMany, double latitude, double longitude) {
        long startCount = 1000;
        List<IFeature> list = new ArrayList<>();
        for(int ii = 0; ii < howMany; ii++) {
            list.add(generateMilStdSymbol(String.valueOf(ii), new java.util.UUID(startCount, startCount++), latitude + (ii * .01), longitude + (ii * .01)));
        }
        return list;
    }

    public static IFeature updateMilStdSymbolAltitude( IFeature feature, double altitude) {
        List<IGeoPosition> positions = feature.getPositions();
        positions.get(0).setAltitude(altitude);
        return feature;
    }

    public static IFeature updateMilStdSymbolLatLong( IFeature feature, double latitude, double longitude) {
        List<IGeoPosition> positions = feature.getPositions();
        positions.get(0).setLatitude(latitude);
        positions.get(0).setLongitude(longitude);
        return feature;
    }
}
