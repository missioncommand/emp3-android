package mil.emp3.api.shadows;

import android.util.SparseArray;

import org.cmapi.primitives.IGeoMilSymbol;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.utils.MilStdUtilities;

/**
 * Created by matt.miller@rgi-corp.local on 11/27/17.
 */

@Implements(MilStdUtilities.class)
public class ShadowKMLExportThread {

    //@Implementation
//    protected String getMilStdSinglePointIconURL(final MilStdSymbol feature,
//                                                 final MilStdLabelSettingEnum eLabelSetting,
//                                                 final java.util.Set<IGeoMilSymbol.Modifier> labelSet,
//                                                 final SparseArray<String> attributes) throws IOException {
//        return "https://127.0.0.1";
//    }

    @Implementation
    public static String getMilStdSinglePointIconURL(final MilStdSymbol feature,
                                                     MilStdLabelSettingEnum eLabelSetting, java.util.Set<IGeoMilSymbol.Modifier> labelSet,
                                                     SparseArray<String> attributes) {
        return "https://127.0.0.1";
    }
}
