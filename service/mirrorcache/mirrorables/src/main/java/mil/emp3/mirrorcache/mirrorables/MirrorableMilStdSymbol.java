package mil.emp3.mirrorcache.mirrorables;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.mirrorcache.api.IMirrorable;

public class MirrorableMilStdSymbol extends MilStdSymbol implements IMirrorable {

    static final private int FLAG_NULL     = 0;
    static final private int FLAG_NOT_NULL = 1;

    // describes the selected state of this feature
    private boolean isSelected;

    private String mirrorKey;

    public MirrorableMilStdSymbol() throws EMP_Exception {
        this(new GeoMilSymbol());
        setStrokeStyle(null);
        setFillStyle(null);
        setLabelStyle(null);
    }
    public MirrorableMilStdSymbol(IGeoMilSymbol s) throws EMP_Exception {
        super(s);
    }

    @Override
    public void readFromByteArray(ByteBuffer in) {

        //geoId
        byte[] bytes = new byte[in.getInt()];
        in.get(bytes);
        setGeoId(UUID.fromString(new String(bytes)));

        // name
        bytes = new byte[in.getInt()];
        in.get(bytes);
        setName(new String(bytes));

        //symbolCode
        bytes = new byte[in.getInt()];
        in.get(bytes);
        setSymbolCode(new String(bytes));

        // symbolStandard
        bytes = new byte[in.getInt()];
        in.get(bytes);
        setSymbolStandard(SymbolStandard.valueOf(new String(bytes)));

        // altitudeMode
        if (FLAG_NOT_NULL == in.getInt()) {
            bytes = new byte[in.getInt()];
            in.get(bytes);
            setAltitudeMode(AltitudeMode.valueOf(new String(bytes)));
        }

        // positions
        final List<IGeoPosition> positions = new ArrayList<>();
        for (int i = 0, positionCount = in.getInt(); i < positionCount; i++) {
            final IGeoPosition position = new GeoPosition();
            position.setLatitude(in.getDouble());
            position.setLongitude(in.getDouble());
            position.setAltitude(in.getDouble());

            positions.add(position);
        }
        getPositions().clear();
        getPositions().addAll(positions);

        // modifiers
        for (int i = 0, modifierCount = in.getInt(); i < modifierCount; i++) {
            bytes = new byte[in.getInt()];
            in.get(bytes);
            final Modifier m = Modifier.valueOf(new String(bytes));

            bytes = new byte[in.getInt()];
            in.get(bytes);
            final String value = new String(bytes);

            setModifier(m, value);
        }

        // fillStyle
        if (FLAG_NOT_NULL == in.getInt()) {
            setFillStyle(new GeoFillStyle());

            // fillColor
            if (FLAG_NOT_NULL == in.getInt()) {
                getFillStyle().setFillColor(new EmpGeoColor(in.getDouble(), in.getInt(), in.getInt(), in.getInt()));
            }

            // fillPattern
            if (FLAG_NOT_NULL == in.getInt()) {
                bytes = new byte[in.getInt()];
                in.get(bytes);
                getFillStyle().setFillPattern(IGeoFillStyle.FillPattern.valueOf(new String(bytes)));
            }

            // description
            if (FLAG_NOT_NULL == in.getInt()) {
                bytes = new byte[in.getInt()];
                in.get(bytes);
                getFillStyle().setDescription(new String(bytes));
            }
        }

        // strokeStyle
        if (FLAG_NOT_NULL == in.getInt()) {
            setStrokeStyle(new GeoStrokeStyle());

            // strokeColor
            if (FLAG_NOT_NULL == in.getInt()) {
                getStrokeStyle().setStrokeColor(new EmpGeoColor(in.getDouble(), in.getInt(), in.getInt(), in.getInt()));
            }

            // stipplingPattern
            getStrokeStyle().setStipplingPattern(in.getShort());

            // StipplingFactor
            getStrokeStyle().setStipplingFactor(in.getInt());

            // strokeWidth
            getStrokeStyle().setStrokeWidth(in.getDouble());
        }

        // labelStyle
        if (FLAG_NOT_NULL == in.getInt()) {
            setLabelStyle(new GeoLabelStyle());

            // color
            if (FLAG_NOT_NULL == in.getInt()) {
                getLabelStyle().setColor(new EmpGeoColor(in.getDouble(), in.getInt(), in.getInt(), in.getInt()));
            }

            // outline color
            if (FLAG_NOT_NULL == in.getInt()) {
                getLabelStyle().setOutlineColor(new EmpGeoColor(in.getDouble(), in.getInt(), in.getInt(), in.getInt()));
            }

            // justification
            if (FLAG_NOT_NULL == in.getInt()) {
                bytes = new byte[in.getInt()];
                in.get(bytes);
                getLabelStyle().setJustification(IGeoLabelStyle.Justification.valueOf(new String(bytes)));
            }

            // Typeface
            if (FLAG_NOT_NULL == in.getInt()) {
                bytes = new byte[in.getInt()];
                in.get(bytes);
                getLabelStyle().setTypeface(IGeoLabelStyle.Typeface.valueOf(new String(bytes)));
            }

            // FontFamily
            if (FLAG_NOT_NULL == in.getInt()) {
                bytes = new byte[in.getInt()];
                in.get(bytes);
                getLabelStyle().setFontFamily(new String(bytes));
            }

            // size
            getLabelStyle().setSize(in.getDouble());
        }

        // isSelected
        setIsSelected(in.getShort() == 1);
    }

    @Override
    public void writeToByteArray(ByteBuffer out) {

        // geoId
        out.putInt(getGeoId().toString().length());
        out.put(getGeoId().toString().getBytes());

        // name
        out.putInt(getName().length());
        out.put(getName().getBytes());

        // symbolCode
        out.putInt(getSymbolCode().length());
        out.put(getSymbolCode().getBytes());

        // symbolStandard
        out.putInt(getSymbolStandard().toString().length());
        out.put(getSymbolStandard().toString().getBytes());

        // altitudeMode
        if (getAltitudeMode() != null) {
            out.putInt(FLAG_NOT_NULL);
            out.putInt(getAltitudeMode().toString().length());
            out.put(getAltitudeMode().toString().getBytes());

        } else {
            out.putInt(FLAG_NULL);
        }

        // positions
        out.putInt(getPositions().size());
        for (IGeoPosition position : getPositions()) {
            out.putDouble(position.getLatitude());
            out.putDouble(position.getLongitude());
            out.putDouble(position.getAltitude());
        }

        // modifiers
        out.putInt(getModifiers().size());
        for (Map.Entry entry : getModifiers().entrySet()) {
            out.putInt(entry.getKey().toString().length());   // key
            out.put(entry.getKey().toString().getBytes());

            out.putInt(entry.getValue().toString().length()); // value
            out.put(entry.getValue().toString().getBytes());
        }

        // fillStyle
        if (getFillStyle() != null) {
            out.putInt(FLAG_NOT_NULL);

            // fillColor
            if (getFillStyle().getFillColor() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putDouble(getFillStyle().getFillColor().getAlpha());
                out.putInt(getFillStyle().getFillColor().getRed());
                out.putInt(getFillStyle().getFillColor().getGreen());
                out.putInt(getFillStyle().getFillColor().getBlue());

            } else {
                out.putInt(FLAG_NULL);
            }

            // fillPattern
            if (getFillStyle().getFillPattern() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putInt(getFillStyle().getFillPattern().toString().length());
                out.put(getFillStyle().getFillPattern().toString().getBytes());

            } else {
                out.putInt(FLAG_NULL);
            }

            // description
            if (getFillStyle().getDescription() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putInt(getFillStyle().getDescription().length());
                out.put(getFillStyle().getDescription().getBytes());

            } else {
                out.putInt(FLAG_NULL);
            }

        } else {
            out.putInt(FLAG_NULL);
        }

        // strokeStyle
        if (getStrokeStyle() != null) {
            out.putInt(FLAG_NOT_NULL);

            // strokeColor
            if (getStrokeStyle().getStrokeColor() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putDouble(getStrokeStyle().getStrokeColor().getAlpha());
                out.putInt(getStrokeStyle().getStrokeColor().getRed());
                out.putInt(getStrokeStyle().getStrokeColor().getGreen());
                out.putInt(getStrokeStyle().getStrokeColor().getBlue());

            } else {
                out.putInt(FLAG_NULL);
            }

            // StipplingPattern
            out.putShort(getStrokeStyle().getStipplingPattern());

            // StipplingFactor
            out.putInt(getStrokeStyle().getStipplingFactor());

            // strokeWidth
            out.putDouble(getStrokeStyle().getStrokeWidth());

        } else {
            out.putInt(FLAG_NULL);
        }

        // labelStyle
        if (getLabelStyle() != null) {
            out.putInt(FLAG_NOT_NULL);

            // color
            if (getLabelStyle().getColor() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putDouble(getLabelStyle().getColor().getAlpha());
                out.putInt(getLabelStyle().getColor().getRed());
                out.putInt(getLabelStyle().getColor().getGreen());
                out.putInt(getLabelStyle().getColor().getBlue());

            } else {
                out.putInt(FLAG_NULL);
            }

            // outline color
            if (getLabelStyle().getOutlineColor() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putDouble(getLabelStyle().getOutlineColor().getAlpha());
                out.putInt(getLabelStyle().getOutlineColor().getRed());
                out.putInt(getLabelStyle().getOutlineColor().getGreen());
                out.putInt(getLabelStyle().getOutlineColor().getBlue());

            } else {
                out.putInt(FLAG_NULL);
            }

            // justification
            if (getLabelStyle().getJustification() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putInt(getLabelStyle().getJustification().toString().length());
                out.put(getLabelStyle().getJustification().toString().getBytes());

            } else {
                out.putInt(FLAG_NULL);
            }

            // Typeface
            if (getLabelStyle().getTypeface() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putInt(getLabelStyle().getTypeface().toString().length());
                out.put(getLabelStyle().getTypeface().toString().getBytes());

            } else {
                out.putInt(FLAG_NULL);
            }

            // FontFamily
            if (getLabelStyle().getFontFamily() != null) {
                out.putInt(FLAG_NOT_NULL);
                out.putInt(getLabelStyle().getFontFamily().length());
                out.put(getLabelStyle().getFontFamily().getBytes());

            } else {
                out.putInt(FLAG_NULL);
            }

            // size
            out.putDouble(getLabelStyle().getSize());

        } else {
            out.putInt(FLAG_NULL);
        }

        // isSelected
        out.putShort((short) (isSelected() ? 1 : 0));
    }

    @Override
    public int length() {

        // modifiers
        int modifiersLength = 0;
        for (Map.Entry entry : getModifiers().entrySet()) {
            modifiersLength += entry.getKey().toString().length();
            modifiersLength += entry.getValue().toString().length();
        }

        return
            /* geoId          */ Integer.SIZE + getGeoId().toString().length() +
            /* name           */ Integer.SIZE + getName().length() +
            /* symbolCode     */ Integer.SIZE + getSymbolCode().length() +
            /* symbolStandard */ Integer.SIZE + getSymbolStandard().toString().length() +
            /* altitudeMode   */ Integer.SIZE + (getAltitudeMode() != null ? Integer.SIZE + getAltitudeMode().toString().length() : 0) +
            /* positions      */ Integer.SIZE + ((Double.SIZE / 8) * getPositions().size() * 3) +
            /* modifiers      */ Integer.SIZE + (Integer.SIZE * getModifiers().size() * 2) + modifiersLength +
            /* fillStyle      */ Integer.SIZE + (getFillStyle() != null ? (Integer.SIZE + (getFillStyle().getFillColor() != null ? (Double.SIZE / 8) + (Integer.SIZE * 3) : 0) +
                                                Integer.SIZE + (getFillStyle().getFillPattern() != null ? Integer.SIZE + getFillStyle().getFillPattern().toString().length() : 0) +
                                                Integer.SIZE + (getFillStyle().getDescription() != null ? Integer.SIZE + getFillStyle().getDescription().length() : 0)) : 0) +
            /* strokeStyle    */ Integer.SIZE + (getStrokeStyle() != null ? (Integer.SIZE + (getStrokeStyle().getStrokeColor() != null ? (Double.SIZE / 8) + (Integer.SIZE * 3) : 0) +
                                                Short.SIZE +
                                                Integer.SIZE +
                                                (Double.SIZE / 8)) : 0) +
            /* labelStyle     */ Integer.SIZE + (getLabelStyle() != null ? (Integer.SIZE + (getLabelStyle().getColor() != null ? (Double.SIZE / 8) + (Integer.SIZE * 3) : 0) +
                                                Integer.SIZE + (getLabelStyle().getOutlineColor() != null ? (Double.SIZE / 8) + (Integer.SIZE * 3) : 0) +                                                (getLabelStyle().getOutlineColor() != null ? (Double.SIZE / 8) + (Integer.SIZE * 3) : 0) +
                                                Integer.SIZE + (getLabelStyle().getJustification() != null ? Integer.SIZE + getLabelStyle().getJustification().toString().length() : 0) +
                                                Integer.SIZE + (getLabelStyle().getTypeface() != null ? Integer.SIZE + getLabelStyle().getTypeface().toString().length() : 0) +
                                                Integer.SIZE + (getLabelStyle().getFontFamily() != null ? getLabelStyle().getFontFamily().length(): 0) +
                                                (Double.SIZE / 8)) : 0) +
            /* isSelected     */ Short.SIZE;
    }

    @Override
    public String getMirrorKey() {
        return mirrorKey;
    }

    @Override
    public void setMirrorKey(String mirrorKey) {
        this.mirrorKey = mirrorKey;
    }

    public boolean isSelected() {
        return isSelected;
    }
    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
