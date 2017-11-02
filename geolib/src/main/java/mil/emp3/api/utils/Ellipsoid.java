package mil.emp3.api.utils;

import net.sf.geographiclib.GeoMath;
import net.sf.geographiclib.Pair;

import static java.lang.Math.*;

public class Ellipsoid {

    final private static int maxpow_ = 6;  // GEOGRAPHICLIB_RHUMBAREA_ORDER
    private double[] _alp = new double[maxpow_ + 1];
    private double[] _bet = new double[maxpow_ + 1];
    private double majorRadius,
            flattening,
            secondFlattening,
            thirdFlattening,
            _f12,
            eccentricitySquared,
            _es,
            secondEccentricitySquared,
            minorRadius;
    EllipticFunction _ell;


    // Transverse Mercator coefficients brought over from another source
    final private static double[] alpcoeff = {
            // alp[1]/n^1, polynomial in n of order 5
            31564, -66675, 34440, 47250, -100800, 75600, 151200,
            // alp[2]/n^2, polynomial in n of order 4
            -1983433, 863232, 748608, -1161216, 524160, 1935360,
            // alp[3]/n^3, polynomial in n of order 3
            670412, 406647, -533952, 184464, 725760,
            // alp[4]/n^4, polynomial in n of order 2
            6601661, -7732800, 2230245, 7257600,
            // alp[5]/n^5, polynomial in n of order 1
            -13675556, 3438171, 7983360,
            // alp[6]/n^6, polynomial in n of order 0
            212378941, 319334400,
    };  // count = 27

    final private static double[] betcoeff = {
            // bet[1]/n^1, polynomial in n of order 5
            384796, -382725, -6720, 932400, -1612800, 1209600, 2419200,
            // bet[2]/n^2, polynomial in n of order 4
            -1118711, 1695744, -1174656, 258048, 80640, 3870720,
            // bet[3]/n^3, polynomial in n of order 3
            22276, -16929, -15984, 12852, 362880,
            // bet[4]/n^4, polynomial in n of order 2
            -830251, -158400, 197865, 7257600,
            // bet[5]/n^5, polynomial in n of order 1
            -435388, 453717, 15966720,
            // bet[6]/n^6, polynomial in n of order 0
            20648693, 638668800,
    };  // count = 27

    Ellipsoid(final double a, final double f) {
        majorRadius = a;
        flattening = f;
        secondFlattening = 1 - flattening;
        thirdFlattening = flattening / (2 - flattening);
        _f12 = GeoMath.sq(secondFlattening);
        eccentricitySquared = flattening * (2 - flattening);
        _es = (flattening < 0 ? -1 : 1) * sqrt(abs(eccentricitySquared));
        secondEccentricitySquared = eccentricitySquared / (1 - eccentricitySquared);
        minorRadius = majorRadius * secondFlattening;
        // very doubtful this is converted properly from C++
        _ell = new EllipticFunction(-secondEccentricitySquared, 0);
        int m = maxpow_ / 2;
        int o = 0;
        double d = thirdFlattening;
        for (int l = 1; l <= maxpow_; ++l) {
            m = maxpow_ - l;
            _alp[l] = d * GeoMath.polyval(m, alpcoeff, o, thirdFlattening) / alpcoeff[o + m + 1];
            _bet[l] = d * GeoMath.polyval(m, betcoeff, o, thirdFlattening) / betcoeff[o + m + 1];
            o += m + 2;
            d *= thirdFlattening;
        }
    }

    public double[] ConformalToRectifyingCoeffs() {
        return _alp;
    }

    public double[] RectifyingToConformalCoeffs() {
        return _bet;
    }

    public double Flattening() {
        return flattening;
    }

    public double SecondFlattening() {
        return secondFlattening;
    }

    public double ThirdFlattening() {
        return thirdFlattening;
    }

    public double MajorRadius() {
        return majorRadius;
    }

    public double MinorRadius() {
        return minorRadius;
    }

    public double EccentricitySquared() {
        return eccentricitySquared;
    }

    public double QuarterMeridian() {
        return minorRadius * _ell.E();
    }

    /* what is this number? */
    public double ES() {
        return _es;
    }

    public double Area() {
        return 4 * PI *
                ((GeoMath.sq(majorRadius) + GeoMath.sq(minorRadius) *
                        (eccentricitySquared == 0 ? 1 :
                                (eccentricitySquared > 0 ? GeoMath.atanh(sqrt(eccentricitySquared)) : atan(sqrt(-eccentricitySquared))) /
                                        sqrt(abs(eccentricitySquared)))) / 2);
    }

    public double ParametricLatitude(final double phi) {
        return MathGeo.atand(secondFlattening * MathGeo.tand(GeoMath.LatFix(phi)));
    }

    public double InverseParametricLatitude(final double beta) {
        return MathGeo.atand(MathGeo.tand(GeoMath.LatFix(beta)) / secondFlattening);
    }

    public double GeocentricLatitude(final double phi) {
        return MathGeo.atand(_f12 * MathGeo.tand(GeoMath.LatFix(phi)));
    }

    public double InverseGeocentricLatitude(final double theta) {
        return MathGeo.atand(MathGeo.tand(GeoMath.LatFix(theta)) / _f12);
    }

    double RectifyingLatitude(final double phi) {
        return abs(phi) == 90 ? phi :
                90 * MeridianDistance(phi) / QuarterMeridian();
    }

    public double InverseRectifyingLatitude(final double mu) {
        if (abs(mu) == 90)
            return mu;
        return InverseParametricLatitude(_ell.Einv(mu * _ell.E() / 90) /
                MathGeo.RADIANS_TO_DEGREES);
    }

/*    public double AuthalicLatitude(final double phi)
    { return MathGeo.atand(_au.txif(MathGeo.tand(GeoMath.LatFix(phi)))); }

    public double InverseAuthalicLatitude(final double xi)
    { return MathGeo.atand(_au.tphif(MathGeo.tand(GeoMath.LatFix(xi)))); }
    */

    public double ConformalLatitude(final double phi) {
        return MathGeo.atand(MathGeo.taupf(MathGeo.tand(GeoMath.LatFix(phi)), _es));
    }

    public double InverseConformalLatitude(final double chi) {
        return MathGeo.atand(MathGeo.tauf(MathGeo.tand(GeoMath.LatFix(chi)), _es));
    }

    public double IsometricLatitude(final double phi) {
        return MathGeo.asinh(MathGeo.taupf(MathGeo.tand(GeoMath.LatFix(phi)), _es)) /
                MathGeo.RADIANS_TO_DEGREES;
    }

    public double InverseIsometricLatitude(final double psi) {
        return MathGeo.atand(MathGeo.tauf(sinh(psi * MathGeo.RADIANS_TO_DEGREES), _es));
    }

    double CircleRadius(final double phi) {
        return abs(phi) == 90 ? 0 :
                // a * cos(beta)
                majorRadius / GeoMath.hypot(1.0D, secondFlattening * MathGeo.tand(GeoMath.LatFix(phi)));
    }

    public double CircleHeight(final double phi) {
        double tbeta = secondFlattening * MathGeo.tand(phi);
        // b * sin(beta)
        return minorRadius * tbeta / GeoMath.hypot(1.0D,
                secondFlattening * MathGeo.tand(GeoMath.LatFix(phi)));
    }

    public double MeridianDistance(final double phi) {
        return minorRadius * _ell.Ed(ParametricLatitude(phi));
    }

    double MeridionalCurvatureRadius(final double phi) {
        double v = 1 - eccentricitySquared * GeoMath.sq(MathGeo.sind(GeoMath.LatFix(phi)));
        return majorRadius * (1 - eccentricitySquared) / (v * sqrt(v));
    }

    public double TransverseCurvatureRadius(final double phi) {
        double v = 1 - eccentricitySquared * GeoMath.sq(MathGeo.sind(GeoMath.LatFix(phi)));
        return majorRadius / sqrt(v);
    }

    public double NormalCurvatureRadius(final double phi, final double azi) {
        double calp, salp,
                v = 1 - eccentricitySquared * GeoMath.sq(MathGeo.sind(GeoMath.LatFix(phi)));
        Pair scalp = GeoMath.sincosd(azi);
        salp = scalp.first;
        calp = scalp.second;
        return majorRadius / (sqrt(v) * (GeoMath.sq(calp) * v / (1 - eccentricitySquared) + GeoMath.sq(salp)));
    }

}
