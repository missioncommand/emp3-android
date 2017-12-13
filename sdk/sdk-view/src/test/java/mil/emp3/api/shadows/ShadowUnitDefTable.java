package mil.emp3.api.shadows;

import android.graphics.Typeface;
import android.util.Log;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import armyc2.c2sd.renderer.utilities.XMLParser;
import armyc2.c2sd.renderer.utilities.XMLUtil;


/**
 * Shadows UnitDefTable
 * UnitDefTable uses internal aar resources and the classloader to load data in.
 * These resources cannot be accessed by our tests here because of this so we can include them
 * in our test resources and actually test our code against this class to prevent null pointers and index
 * out of bounds exceptions.
 *
 * @author James Rummel
 *
 */
@Implements(UnitDefTable.class)
public class ShadowUnitDefTable
{
    // TODO - Due to reasons listed above these files have been included locally in our test resources.
    // TODO - They will need to be kept up to date with the version of the renderer we're using.
    // https://github.com/missioncommand/mil-sym-android/tree/master/renderer/src/main/res/raw
    private static final String UNIT_CONSTANTS_B = "unitconstantsb.xml";
    private static final String UNIT_CONSTANTS_C = "unitconstantsc.xml";

    private String TAG = "ShadowUnitDefTable";
    private static Boolean _initCalled = false;
    private static UnitDefTable _instance = null;
    private static Map<String, UnitDef> _UnitDefinitionsB = null;
    private static ArrayList<UnitDef> _UnitDefDupsB = null;

    private static Map<String, UnitDef> _UnitDefinitionsC = null;
    private static ArrayList<UnitDef> _UnitDefDupsC = null;

    @Implementation
    public final synchronized void init()
    {
        if (_initCalled == false) {
            String[] xml = new String[2];
            xml[0] = getXML(UNIT_CONSTANTS_B);
            xml[1] = getXML(UNIT_CONSTANTS_C);

            init(xml);
        }
    }

    /**
     * must be called first
     */
    @Implementation
    public synchronized void init(String[] unitConstantsXML)
    {
        if (_initCalled == false) {

            _UnitDefinitionsB = new HashMap<>();
            _UnitDefDupsB = new ArrayList<>();

            _UnitDefinitionsC = new HashMap<>();
            _UnitDefDupsC = new ArrayList<>();

            String lookupXmlB = unitConstantsXML[0];// FileHandler.InputStreamToString(xmlStreamB);
            String lookupXmlC = unitConstantsXML[1];

            populateLookup(lookupXmlB, RendererSettings.Symbology_2525B);
            populateLookup(lookupXmlC, RendererSettings.Symbology_2525C);

            _initCalled = true;
        }
    }

    /**
     * @name getSymbolDef
     *
     * @desc Returns a SymbolDef from the SymbolDefTable that matches the passed in Symbol Id
     *
     * @param basicSymbolID - IN - A 15 character MilStd code
     * @return SymbolDef whose Symbol Id matches what is passed in
     */
    @Implementation
    public UnitDef getUnitDef(String basicSymbolID, int symStd)
    {
        UnitDef returnVal = null;
        try {
            if (symStd == RendererSettings.Symbology_2525B) {
                returnVal = _UnitDefinitionsB.get(basicSymbolID);
            } else if (symStd == RendererSettings.Symbology_2525C) {
                returnVal = _UnitDefinitionsC.get(basicSymbolID);
            }
        } catch (Exception exc) {
            Log.e("UnitDefTable", exc.getMessage(), exc);
        } catch (Throwable thrown) {
            Log.wtf("UnitDefTable", thrown.getMessage(), thrown);
        }
        return returnVal;
    }

    /**
     *
     * @return
     */
    @Implementation
    public Map<String, UnitDef> getAllUnitDefs(int symStd)
    {
        if (symStd == RendererSettings.Symbology_2525B)
            return _UnitDefinitionsB;
        else
            return _UnitDefinitionsC;
    }

    @Implementation
    public ArrayList<UnitDef> getUnitDefDups(int symStd)
    {
        if (symStd == RendererSettings.Symbology_2525B)
            return _UnitDefDupsB;
        else
            return _UnitDefDupsC;
    }

    /**
     *
     * @param basicSymbolID
     * @return
     */
    @Implementation
    public Boolean hasUnitDef(String basicSymbolID, int symStd)
    {
        if (basicSymbolID != null && basicSymbolID.length() == 15) {
            if (symStd == RendererSettings.Symbology_2525B)
                return _UnitDefinitionsB.containsKey(basicSymbolID);
            else if (symStd == RendererSettings.Symbology_2525C)
                return _UnitDefinitionsC.containsKey(basicSymbolID);
            else
                return false;
        } else
            return false;
    }




        /*
     * public String[] searchByHierarchy(String hierarchy) { for(UnitDef foo : _UnitDefinitions.values() ) {
     * if(foo.getHierarchy().equalsIgnoreCase(hierarchy)) { return } } }
     */

    private String getXML(String xmlName)
    {
        String xml = null;
        Typeface tf = null;
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(xmlName);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader r = new BufferedReader(isr);
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                xml = total.toString();

                // cleanup
                r.close();
                isr.close();
                is.close();
                r = null;
                isr = null;
                is = null;
                total = null;
            }
        } catch (Exception exc) {
            Log.e(TAG, exc.getMessage(), exc);
        }
        return xml;
    }

    private static void populateLookup(String xml, int symStd)
    {
        UnitDef ud = null;

        Document document = XMLParser.getDomElement(xml);

        NodeList symbols = XMLUtil.getItemList(document, "SYMBOL");
        for (int i = 0; i < symbols.getLength(); i++) {
            Node node = symbols.item(i);

            String symbolID = XMLUtil.parseTagValue(node, "SYMBOLID");
            String description = XMLUtil.parseTagValue(node, "DESCRIPTION");
            description = description.replaceAll("&amp;", "&");
            String drawCategory = XMLUtil.parseTagValue(node, "DRAWCATEGORY");
            String hierarchy = XMLUtil.parseTagValue(node, "HIERARCHY");
            String alphaHierarchy = XMLUtil.parseTagValue(node, "ALPHAHIERARCHY");
            String path = XMLUtil.parseTagValue(node, "PATH");

            if (SymbolUtilities.isInstallation(symbolID))
                symbolID = symbolID.substring(0, 10) + "H****";

            int idc = 0;
            if (drawCategory != null || drawCategory.equals("") == false)
                idc = Integer.valueOf(drawCategory);

            ud = new UnitDef(symbolID, description, idc, hierarchy, path);

            boolean isMCSSpecificFE = SymbolUtilities.isMCSSpecificForceElement(ud);

            if (symStd == RendererSettings.Symbology_2525B) {
                if (_UnitDefinitionsB.containsKey(symbolID) == false && isMCSSpecificFE == false)
                    _UnitDefinitionsB.put(symbolID, ud);// EMS have dupe symbols with same code
                else if (isMCSSpecificFE == false)
                    _UnitDefDupsB.add(ud);
            } else {
                if (_UnitDefinitionsC.containsKey(symbolID) == false && isMCSSpecificFE == false)
                    _UnitDefinitionsC.put(symbolID, ud);// EMS have dupe symbols with same code
                else if (isMCSSpecificFE == false)
                    _UnitDefDupsC.add(ud);
            }
        }
    }// end populateLookup




}
