package net.sf.geographiclib;

public class Ellipsoid {

    private double stol_;
    double _a, _f, _f1, _f12, _e2, _es, _e12, _n, _b;
    EllipticFunction _ell;

    Ellipsoid(double a, double f) {
        stol_ = 0.01 * Math.sqrt(GeoMath.epsilon);
        _a = a;
        _f = f;
        _f1 = 1 - _f;
        _f12 = GeoMath.sq(_f1);
        _e2 = _f * (2 - _f);
        _es = (_f < 0 ? -1 : 1) * Math.sqrt(Math.abs(_e2));
        _e12 = _e2 / (1 - _e2);
        _n = _f / (2 - _f);
        _b = _a * _f1;
//        _tm = _a, _f, real(1);
        // very doubtful this is converted properly from C++
        _ell = new EllipticFunction(-_e12, 0);
    }

    double QuarterMeridian()
    { return _b * _ell.E(); }

    double Area() {
        return 4 * Math.PI *
                ((GeoMath.sq(_a) + GeoMath.sq(_b) *
                (_e2 == 0 ? 1 :
                        (_e2 > 0 ? GeoMath.atanh(Math.sqrt(_e2)) : Math.atan(Math.sqrt(-_e2))) /
        Math.sqrt(Math.abs(_e2))))/2);
    }

    double ParametricLatitude(double phi)
    { return MathGeo.atand(_f1 * MathGeo.tand(GeoMath.LatFix(phi))); }

    double InverseParametricLatitude(double beta)
    { return MathGeo.atand(MathGeo.tand(GeoMath.LatFix(beta)) / _f1); }

    double GeocentricLatitude(double phi)
    { return MathGeo.atand(_f12 * MathGeo.tand(GeoMath.LatFix(phi))); }

    double InverseGeocentricLatitude(double theta)
    { return MathGeo.atand(MathGeo.tand(GeoMath.LatFix(theta)) / _f12); }

    double RectifyingLatitude(double phi) {
        return Math.abs(phi) == 90 ? phi:
                90 * MeridianDistance(phi) / QuarterMeridian();
    }

    double InverseRectifyingLatitude(double mu) {
        if (Math.abs(mu) == 90)
            return mu;
        return InverseParametricLatitude(_ell.Einv(mu * _ell.E() / 90) /
                MathGeo.RADIANS_TO_DEGREES);
    }

/*    double AuthalicLatitude(double phi)
    { return MathGeo.atand(_au.txif(MathGeo.tand(GeoMath.LatFix(phi)))); }

    double InverseAuthalicLatitude(double xi)
    { return MathGeo.atand(_au.tphif(MathGeo.tand(GeoMath.LatFix(xi)))); }
    */

    double ConformalLatitude(double phi)
    { return MathGeo.atand(MathGeo.taupf(MathGeo.tand(GeoMath.LatFix(phi)), _es)); }

    double InverseConformalLatitude(double chi)
    { return MathGeo.atand(MathGeo.tauf(MathGeo.tand(GeoMath.LatFix(chi)), _es)); }

    double IsometricLatitude(double phi)
    { return MathGeo.asinh(MathGeo.taupf(MathGeo.tand(GeoMath.LatFix(phi)), _es)) /
        MathGeo.RADIANS_TO_DEGREES; }

    double InverseIsometricLatitude(double psi)
    { return MathGeo.atand(MathGeo.tauf(Math.sinh(psi * MathGeo.RADIANS_TO_DEGREES), _es)); }

    double CircleRadius(double phi) {
        return Math.abs(phi) == 90 ? 0 :
                // a * cos(beta)
                _a / GeoMath.hypot(1.0D, _f1 * MathGeo.tand(GeoMath.LatFix(phi)));
    }

    double CircleHeight(double phi) {
        double tbeta = _f1 * MathGeo.tand(phi);
        // b * sin(beta)
        return _b * tbeta / GeoMath.hypot(1.0D,
                _f1 * MathGeo.tand(GeoMath.LatFix(phi)));
    }

    double MeridianDistance(double phi)
    { return _b * _ell.Ed( ParametricLatitude(phi) ); }

    double MeridionalCurvatureRadius(double phi) {
        double v = 1 - _e2 * GeoMath.sq(MathGeo.sind(GeoMath.LatFix(phi)));
        return _a * (1 - _e2) / (v * Math.sqrt(v));
    }

    double TransverseCurvatureRadius(double phi) {
        double v = 1 - _e2 * GeoMath.sq(MathGeo.sind(GeoMath.LatFix(phi)));
        return _a / Math.sqrt(v);
    }

    double NormalCurvatureRadius(double phi, double azi) {
        double calp, salp,
                v = 1 - _e2 * GeoMath.sq(MathGeo.sind(GeoMath.LatFix(phi)));
        Pair scalp = GeoMath.sincosd(azi);
        salp = scalp.first;
        calp = scalp.second;
        return _a / (Math.sqrt(v) * (GeoMath.sq(calp) * v / (1 - _e2) + GeoMath.sq(salp)));
    }

}
