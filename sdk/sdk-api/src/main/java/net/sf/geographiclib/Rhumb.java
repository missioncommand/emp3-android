package net.sf.geographiclib;

import static java.lang.Math.*;
import static net.sf.geographiclib.GeodesicMask.*;
import static net.sf.geographiclib.MathGeo.*;

public class Rhumb {
/**
 * \file Rhumb.hpp
 * \brief Header for GeographicLib::Rhumb and GeographicLib::RhumbLine classes
 *
 * Copyright (c) Charles Karney (2014-2017) <charles@karney.com> and licensed
 * under the MIT/X11 License.  For more information, see
 * https://geographiclib.sourceforge.io/
 **********************************************************************/


    /**
     * \brief Solve of the direct and inverse rhumb problems.
     * <p>
     * The path of constant azimuth between two points on a ellipsoid at (\e
     * lat1, \e lon1) and (\e lat2, \e lon2) is called the rhumb line (also
     * called the loxodrome).  Its length is \e s12 and its azimuth is \e azi12.
     * (The azimuth is the heading measured clockwise from north.)
     * <p>
     * Given \e lat1, \e lon1, \e azi12, and \e s12, we can determine \e lat2,
     * and \e lon2.  This is the \e direct rhumb problem and its solution is
     * given by the function Rhumb::Direct.
     * <p>
     * Given \e lat1, \e lon1, \e lat2, and \e lon2, we can determine \e azi12
     * and \e s12.  This is the \e inverse rhumb problem, whose solution is given
     * by Rhumb::Inverse.  This finds the shortest such rhumb line, i.e., the one
     * that wraps no more than half way around the earth.  If the end points are
     * on opposite meridians, there are two shortest rhumb lines and the
     * east-going one is chosen.
     * <p>
     * These routines also optionally calculate the area under the rhumb line, \e
     * S12.  This is the area, measured counter-clockwise, of the rhumb line
     * quadrilateral with corners (<i>lat1</i>,<i>lon1</i>), (0,<i>lon1</i>),
     * (0,<i>lon2</i>), and (<i>lat2</i>,<i>lon2</i>).
     * <p>
     * Note that rhumb lines may be appreciably longer (up to 50%) than the
     * corresponding Geodesic.  For example the distance between London Heathrow
     * and Tokyo Narita via the rhumb line is 11400 km which is 18% longer than
     * the geodesic distance 9600 km.
     * <p>
     * For more information on rhumb lines see \ref rhumb.
     * <p>
     * Example of use:
     * \include example-Rhumb.cpp
     **********************************************************************/

    final private static double[] coeff = {
            // R[0]/n^0, polynomial in n of order 6
            128346268, -107884140, 31126095, 354053700, -908107200, 851350500, 0,
            2554051500L,
            // R[1]/n^1, polynomial in n of order 5
            -114456994, 56868630, 79819740, -240540300, 312161850, -212837625,
            638512875,
            // R[2]/n^2, polynomial in n of order 4
            51304574, 24731070, -78693615, 71621550, -28378350, 212837625,
            // R[3]/n^3, polynomial in n of order 3
            1554472, -6282003, 4684680, -1396395, 14189175,
            // R[4]/n^4, polynomial in n of order 2
            -4913956, 3205800, -791505, 8108100,
            // R[5]/n^5, polynomial in n of order 1
            1092376, -234468, 2027025,
            // R[6]/n^6, polynomial in n of order 0
            -313076, 2027025,
    };  // count = 35

    /* package */ Ellipsoid ellipsoid;
    private boolean _exact;
    double _c2;
    final private static int tm_maxord = 6;// GEOGRAPHICLIB_TRANSVERSEMERCATOR_ORDER
    final private static int maxpow_ = 6;  // GEOGRAPHICLIB_RHUMBAREA_ORDER
    // _R[0] unused
    double[] _R = new double[maxpow_ + 1];

    private static double gd(double x) {
        return atan(sinh(x));
    }

    // Use divided differences to determine (mu2 - mu1) / (psi2 - psi1)
    // accurately
    //
    // Definition: Df(x,y,d) = (f(x) - f(y)) / (x - y)
    // See:
    //   W. M. Kahan and R. J. Fateman,
    //   Symbolic computation of divided differences,
    //   SIGSAM Bull. 33(3), 7-28 (1999)
    //   https://doi.org/10.1145/334714.334716
    //   http://www.cs.berkeley.edu/~fateman/papers/divdiff.pdf

    private static double Dlog(double x, double y) {
        double t = x - y;
        return t != 0 ? 2 * GeoMath.atanh(t / (x + y)) / t : 1 / x;
    }

    // N.B., x and y are in degrees
    private static double Dtan(double x, double y) {
        double d = x - y, tx = tand(x), ty = tand(y), txy = tx * ty;
        return d != 0 ?
                (2 * txy > -1 ? (1 + txy) * tand(d) : tx - ty) /
                        (d * RADIANS_TO_DEGREES) :
                1 + txy;
    }

    private static double Datan(double x, double y) {
        double d = x - y, xy = x * y;
        return d != 0 ?
                (2 * xy > -1 ? atan(d / (1 + xy)) : atan(x) - atan(y)) / d :
                1 / (1 + xy);
    }

    private static double Dsin(double x, double y) {
        double d = (x - y) / 2;
        return cos((x + y) / 2) * (d != 0 ? sin(d) / d : 1);
    }

    private static double Dsinh(double x, double y) {
        double d = (x - y) / 2;
        return cosh((x + y) / 2) * (d != 0 ? sinh(d) / d : 1);
    }

    private static double Dcosh(double x, double y) {
        double d = (x - y) / 2;
        return sinh((x + y) / 2) * (d != 0 ? sinh(d) / d : 1);
    }

    private static double Dasinh(double x, double y) {
        double d = x - y,
                hx = GeoMath.hypot(1.0D, x), hy = GeoMath.hypot(1.0D, y);
        return d != 0 ? asinh(x * y > 0 ? d * (x + y) / (x * hy + y * hx) :
                x * hy - y * hx) / d :
                1 / hx;
    }

    private static double Dgd(double x, double y) {
        return Datan(sinh(x), sinh(y)) * Dsinh(x, y);
    }

    // N.B., x and y are the tangents of the angles
    private static double Dgdinv(double x, double y) {
        return Dasinh(x, y) / Datan(x, y);
    }

    // Copied from LambertConformalConic...
    // Deatanhe(x,y) = eatanhe((x-y)/(1-e^2*x*y))/(x-y)
    double Deatanhe(double x, double y) {
        double t = x - y, d = 1 - ellipsoid.EccentricitySquared() * x * y;
        return t != 0 ? eatanhe(t / d, ellipsoid.ES()) / t : ellipsoid.EccentricitySquared() / d;
    }

    // (E(x) - E(y)) / (x - y) -- E = incomplete elliptic integral of 2nd kind
    double DE(double x, double y) {
        EllipticFunction ei = ellipsoid._ell;
        double d = x - y;
        if (x * y <= 0)
            return d != 0 ? (ei.E(x) - ei.E(y)) / d : 1;
        // See DLMF: Eqs (19.11.2) and (19.11.4) letting
        // theta -> x, phi -> -y, psi -> z
        //
        // (E(x) - E(y)) / d = E(z)/d - k2 * sin(x) * sin(y) * sin(z)/d
        //
        // tan(z/2) = (sin(x)*Delta(y) - sin(y)*Delta(x)) / (cos(x) + cos(y))
        //          = d * Dsin(x,y) * (sin(x) + sin(y))/(cos(x) + cos(y)) /
        //             (sin(x)*Delta(y) + sin(y)*Delta(x))
        //          = t = d * Dt
        // sin(z) = 2*t/(1+t^2); cos(z) = (1-t^2)/(1+t^2)
        // Alt (this only works for |z| <= pi/2 -- however, this conditions holds
        // if x*y > 0):
        // sin(z) = d * Dsin(x,y) * (sin(x) + sin(y))/
        //          (sin(x)*cos(y)*Delta(y) + sin(y)*cos(x)*Delta(x))
        // cos(z) = sqrt((1-sin(z))*(1+sin(z)))
        double sx = sin(x), sy = sin(y), cx = cos(x), cy = cos(y);
        double Dt = Dsin(x, y) * (sx + sy) /
                ((cx + cy) * (sx * ei.Delta(sy, cy) + sy * ei.Delta(sx, cx))),
                t = d * Dt, Dsz = 2 * Dt / (1 + t * t),
                sz = d * Dsz, cz = (1 - t) * (1 + t) / (1 + t * t);
        return ((sz != 0 ? ei.E(sz, cz, ei.Delta(sz, cz)) / sz : 1)
                - ei.k2() * sx * sy) * Dsz;
    }

    // (mux - muy) / (phix - phiy) using elliptic integrals
    double DRectifying(double latx, double laty) {
        double
                tbetx = ellipsoid.SecondFlattening() * tand(latx),
                tbety = ellipsoid.SecondFlattening() * tand(laty);
        return (PI / 2) * ellipsoid.MinorRadius() * ellipsoid.SecondFlattening() * DE(atan(tbetx), atan(tbety))
                * Dtan(latx, laty) * Datan(tbetx, tbety) / ellipsoid.QuarterMeridian();
    }

    // (psix - psiy) / (phix - phiy)
    double DIsometric(double latx, double laty) {
        double
                phix = latx * RADIANS_TO_DEGREES, tx = tand(latx),
                phiy = laty * RADIANS_TO_DEGREES, ty = tand(laty);
        return Dasinh(tx, ty) * Dtan(latx, laty)
                - Deatanhe(sin(phix), sin(phiy)) * Dsin(phix, phiy);
    }

    // (sum(c[j]*sin(2*j*x),j=1..n) - sum(c[j]*sin(2*j*x),j=1..n)) / (x - y)
    private static double SinCosSeries(boolean sinp,
                                       double x, double y, double c[], int n) {
        // N.B. n >= 0 and c[] has n+1 elements 0..n, of which c[0] is ignored.
        //
        // Use Clenshaw summation to evaluate
        //   m = (g(x) + g(y)) / 2         -- mean value
        //   s = (g(x) - g(y)) / (x - y)   -- average slope
        // where
        //   g(x) = sum(c[j]*SC(2*j*x), j = 1..n)
        //   SC = sinp ? sin : cos
        //   CS = sinp ? cos : sin
        //
        // This function returns only s; m is discarded.
        //
        // Write
        //   t = [m; s]
        //   t = sum(c[j] * f[j](x,y), j = 1..n)
        // where
        //   f[j](x,y) = [ (SC(2*j*x)+SC(2*j*y))/2 ]
        //               [ (SC(2*j*x)-SC(2*j*y))/d ]
        //
        //             = [       cos(j*d)*SC(j*p)    ]
        //               [ +/-(2/d)*sin(j*d)*CS(j*p) ]
        // (+/- = sinp ? + : -) and
        //    p = x+y, d = x-y
        //
        //   f[j+1](x,y) = A * f[j](x,y) - f[j-1](x,y)
        //
        //   A = [  2*cos(p)*cos(d)      -sin(p)*sin(d)*d]
        //       [ -4*sin(p)*sin(d)/d   2*cos(p)*cos(d)  ]
        //
        // Let b[n+1] = b[n+2] = [0 0; 0 0]
        //     b[j] = A * b[j+1] - b[j+2] + c[j] * I for j = n..1
        //    t =  (c[0] * I  - b[2]) * f[0](x,y) + b[1] * f[1](x,y)
        // c[0] is not accessed for s = t[2]
        double p = x + y, d = x - y,
                cp = cos(p), cd = cos(d),
                sp = sin(p), sd = d != 0 ? sin(d) / d : 1,
                m = 2 * cp * cd, s = sp * sd;
        // 2x2 matrices stored in row-major order
        final double[] a = {m, -s * d * d, -4 * s, m};
        double[] ba = {0, 0, 0, 0};
        double[] bb = {0, 0, 0, 0};
        double[] b1 = ba;
        double[] b2 = bb;
        if (n > 0) b1[0] = b1[3] = c[n];
        for (int j = n - 1; j > 0; --j) { // j = n-1 .. 1
            // implemented C++ swap, somewhat doubtful with pointers std::swap (b1, b2);
            double[] tmp = b1;
            b1 = b2;
            b2 = tmp;
            // b1 = A * b2 - b1 + c[j] * I
            b1[0] = a[0] * b2[0] + a[1] * b2[2] - b1[0] + c[j];
            b1[1] = a[0] * b2[1] + a[1] * b2[3] - b1[1];
            b1[2] = a[2] * b2[0] + a[3] * b2[2] - b1[2];
            b1[3] = a[2] * b2[1] + a[3] * b2[3] - b1[3] + c[j];
        }
        // Here are the full expressions for m and s
        // m =   (c[0] - b2[0]) * f01 - b2[1] * f02 + b1[0] * f11 + b1[1] * f12;
        // s = - b2[2] * f01 + (c[0] - b2[3]) * f02 + b1[2] * f11 + b1[3] * f12;
        if (sinp) {
            // double f01 = 0, f02 = 0;
            double f11 = cd * sp, f12 = 2 * sd * cp;
            // m = b1[0] * f11 + b1[1] * f12;
            s = b1[2] * f11 + b1[3] * f12;
        } else {
            // double f01 = 1, f02 = 0;
            double f11 = cd * cp, f12 = -2 * sd * sp;
            // m = c[0] - b2[0] + b1[0] * f11 + b1[1] * f12;
            s = -b2[2] + b1[2] * f11 + b1[3] * f12;
        }
        return s;
    }


    // (mux - muy) / (chix - chiy) using Krueger's series
    double DConformalToRectifying(double chix, double chiy) {
        return 1 + SinCosSeries(true, chix, chiy,
                ellipsoid.ConformalToRectifyingCoeffs(), tm_maxord);
    }

    // (chix - chiy) / (mux - muy) using Krueger's series
    double DRectifyingToConformal(double mux, double muy) {
        return 1 - SinCosSeries(true, mux, muy,
                ellipsoid.RectifyingToConformalCoeffs(), tm_maxord);
    }

    // (mux - muy) / (psix - psiy)
    // N.B., psix and psiy are in degrees
    double DIsometricToRectifying(double psix, double psiy) {
        if (_exact) {
            double
                    latx = ellipsoid.InverseIsometricLatitude(psix),
                    laty = ellipsoid.InverseIsometricLatitude(psiy);
            return DRectifying(latx, laty) / DIsometric(latx, laty);
        } else {
            psix *= RADIANS_TO_DEGREES;
            psiy *= RADIANS_TO_DEGREES;
            return DConformalToRectifying(gd(psix), gd(psiy)) * Dgd(psix, psiy);
        }
    }

    // (psix - psiy) / (mux - muy)
    double DRectifyingToIsometric(double mux, double muy) {
        double
                latx = ellipsoid.InverseRectifyingLatitude(mux / RADIANS_TO_DEGREES),
                laty = ellipsoid.InverseRectifyingLatitude(muy / RADIANS_TO_DEGREES);
        return _exact ?
                DIsometric(latx, laty) / DRectifying(latx, laty) :
                Dgdinv(taupf(tand(latx), ellipsoid.ES()),
                        taupf(tand(laty), ellipsoid.ES())) *
                        DRectifyingToConformal(mux, muy);
    }

    double MeanSinXi(double psix, double psiy) {
        return Dlog(cosh(psix), cosh(psiy)) * Dcosh(psix, psiy)
                + SinCosSeries(false, gd(psix), gd(psiy), _R, maxpow_) * Dgd(psix, psiy);
    }

    /**
     * Constructor for a ellipsoid with
     *
     * @throws GeographicErr if \e a or (1 &minus; \e f) \e a is not
     *                       positive.
     *                       <p>
     *                       See \ref rhumb, for a detailed description of the \e exact parameter.
     * @param[in] a equatorial radius (meters).
     * @param[in] f flattening of ellipsoid.  Setting \e f = 0 gives a sphere.
     * Negative \e f gives a prolate ellipsoid.
     * @param[in] exact if true (the default) use an addition theorem for
     * elliptic integrals to compute divided differences; otherwise use
     * series expansion (accurate for |<i>f</i>| < 0.01).
     **********************************************************************/
    public Rhumb(double a, double f, boolean exact) {
        ellipsoid = new Ellipsoid(a, f);
        _c2 = ellipsoid.Area() / 720.0D;
        _exact = exact;

        double d = 1;
        int o = 0;
        for (int l = 0; l <= maxpow_; ++l) {
            int m = maxpow_ - l;
            // R[0] is just an integration constant so it cancels when evaluating a
            // definite integral.  So don't bother computing it.  It won't be used
            // when invoking SinCosSeries.
            if (l != 0)
                _R[l] = d * GeoMath.polyval(m, coeff, o, ellipsoid.ThirdFlattening()) / coeff[o + m + 1];
            o += m + 2;
            d *= ellipsoid.ThirdFlattening();
        }
        // Post condition: o == sizeof(alpcoeff) / sizeof(real)
    }

    /**
     * The general direct rhumb problem.  Rhumb::Direct is defined in terms
     * of this function.
     *
     * @param[in] lat1 latitude of point 1 (degrees).
     * @param[in] lon1 longitude of point 1 (degrees).
     * @param[in] azi12 azimuth of the rhumb line (degrees).
     * @param[in] s12 distance between point 1 and point 2 (meters); it can be
     * negative.
     * @param[in] outmask a bitor'ed combination of Rhumb::mask values
     * specifying which of the following parameters should be set.
     * @param[out] lat2 latitude of point 2 (degrees).
     * @param[out] lon2 longitude of point 2 (degrees).
     * @param[out] S12 area under the rhumb line (meters<sup>2</sup>).
     * <p>
     * The Rhumb::mask values possible for \e outmask are
     * - \e outmask |= Rhumb::LATITUDE for the latitude \e lat2;
     * - \e outmask |= Rhumb::LONGITUDE for the latitude \e lon2;
     * - \e outmask |= Rhumb::AREA for the area \e S12;
     * - \e outmask |= Rhumb::ALL for all of the above;
     * - \e outmask |= Rhumb::LONG_UNROLL to unroll \e lon2 instead of wrapping
     * it into the range [&minus;180&deg;, 180&deg;].
     * .
     * With the Rhumb::LONG_UNROLL bit set, the quantity \e lon2 &minus;
     * \e lon1 indicates how many times and in what sense the rhumb line
     * encircles the ellipsoid.
     **********************************************************************/
    public GeodesicData Direct(double lat1, double lon1, double azi12, double s12,
                   int outmask) {
        return Line(lat1, lon1, azi12).Position(s12, outmask);
    }

    /**
     * The general inverse rhumb problem.  Rhumb::Inverse is defined in terms
     * of this function.
     *
     * @param[in] lat1 latitude of point 1 (degrees).
     * @param[in] lon1 longitude of point 1 (degrees).
     * @param[in] lat2 latitude of point 2 (degrees).
     * @param[in] lon2 longitude of point 2 (degrees).
     * @param[in] outmask a bitor'ed combination of Rhumb::mask values
     * specifying which of the following parameters should be set.
     * @param[out] s12 rhumb distance between point 1 and point 2 (meters).
     * @param[out] azi12 azimuth of the rhumb line (degrees).
     * @param[out] S12 area under the rhumb line (meters<sup>2</sup>).
     * <p>
     * The Rhumb::mask values possible for \e outmask are
     * - \e outmask |= Rhumb::DISTANCE for the latitude \e s12;
     * - \e outmask |= Rhumb::AZIMUTH for the latitude \e azi12;
     * - \e outmask |= Rhumb::AREA for the area \e S12;
     * - \e outmask |= Rhumb::ALL for all of the above;
     **********************************************************************/
    public GeodesicData Inverse(double lat1, double lon1, double lat2, double lon2,
                    int outmask) {

        GeodesicData g = new GeodesicData();
        g.lat1 = lat1;
        g.lon1 = lon1;
        g.lat2 = lat2;
        g.lon2 = lon2;
        double
                lon12 = GeoMath.AngDiff(lon1, lon2).first,
                psi1 = ellipsoid.IsometricLatitude(lat1),
                psi2 = ellipsoid.IsometricLatitude(lat2),
                psi12 = psi2 - psi1,
                h = GeoMath.hypot(lon12, psi12);
        if ((outmask & AZIMUTH) != 0)
            g.azi2 = GeoMath.atan2d(lon12, psi12);  // original C++ had azi12, repurposed here
        if ((outmask & DISTANCE) != 0) {
            double dmudpsi = DIsometricToRectifying(psi2, psi1);
            g.s12 = h * dmudpsi * ellipsoid.QuarterMeridian() / 90;
        }
        if ((outmask & AREA) != 0)
            g.S12 = _c2 * lon12 *
                    MeanSinXi(psi2 * RADIANS_TO_DEGREES, psi1 * RADIANS_TO_DEGREES);
        return g;
    }

    /**
     * Set up to compute several points on a single rhumb line.
     *
     * @return a RhumbLine object.
     * <p>
     * \e lat1 should be in the range [&minus;90&deg;, 90&deg;].
     * <p>
     * If point 1 is a pole, the cosine of its latitude is taken to be
     * 1/&epsilon;<sup>2</sup> (where &epsilon; is 2<sup>-52</sup>).  This
     * position, which is extremely close to the actual pole, allows the
     * calculation to be carried out in finite terms.
     * @param[in] lat1 latitude of point 1 (degrees).
     * @param[in] lon1 longitude of point 1 (degrees).
     * @param[in] azi12 azimuth of the rhumb line (degrees).
     **********************************************************************/
    RhumbLine Line(double lat1, double lon1, double azi12) {
        return new RhumbLine(this,lat1, lon1, azi12, _exact);
    }

    /**
     * @return \e a the equatorial radius of the ellipsoid (meters).  This is
     * the value used in the constructor.
     **********************************************************************/
    double MajorRadius() {
        return ellipsoid.MajorRadius();
    }

    /**
     * @return \e f the  flattening of the ellipsoid.  This is the
     * value used in the constructor.
     **********************************************************************/
    double Flattening() {
        return ellipsoid.Flattening();
    }

    double EllipsoidArea() {
        return ellipsoid.Area();
    }

    /**
     * A global instantiation of Rhumb with the parameters for the WGS84
     * ellipsoid.
     **********************************************************************/
    final public static Rhumb WGS84 = new Rhumb(Constants.WGS84_a, Constants.WGS84_f, true);

}

/**
 * \brief Find a sequence of points on a single rhumb line.
 * <p>
 * RhumbLine facilitates the determination of a series of points on a single
 * rhumb line.  The starting point (\e lat1, \e lon1) and the azimuth \e
 * azi12 are specified in the call to Rhumb::Line which returns a RhumbLine
 * object.  RhumbLine.Position returns the location of point 2 (and,
 * optionally, the corresponding area, \e S12) a distance \e s12 along the
 * rhumb line.
 * <p>
 * There is no public constructor for this class.  (Use Rhumb::Line to create
 * an instance.)  The Rhumb object used to create a RhumbLine must stay in
 * scope as long as the RhumbLine.
 * <p>
 * Example of use:
 * \include example-RhumbLine.cpp
 **********************************************************************/

class RhumbLine {

    private final Rhumb rhumb;
    private double _lat1, _lon1, _azi12, _salp, _calp, _mu1, _psi1, _r1;

    RhumbLine(Rhumb rh, double lat1, double lon1, double azi12,
              boolean exact)


    {
        rhumb = rh;
        _lat1 = GeoMath.LatFix(lat1);
        _lon1 = lon1;
        _azi12 = GeoMath.AngNormalize(azi12);
        double alp12 = _azi12 * RADIANS_TO_DEGREES;
        _salp = _azi12 == -180 ? 0 : sin(alp12);
        _calp = abs(_azi12) == 90 ? 0 : cos(alp12);
        _mu1 = rhumb.ellipsoid.RectifyingLatitude(lat1);
        _psi1 = rhumb.ellipsoid.IsometricLatitude(lat1);
        _r1 = rhumb.ellipsoid.CircleRadius(lat1);
    }

    /**
     * The general position routine.  RhumbLine::Position is defined in term so
     * this function.
     *
     * @param[in] s12 distance between point 1 and point 2 (meters); it can be
     * negative.
     * @param[in] outmask a bitor'ed combination of RhumbLine::mask values
     * specifying which of the following parameters should be set.
     * @param[out] lat2 latitude of point 2 (degrees).
     * @param[out] lon2 longitude of point 2 (degrees).
     * @param[out] S12 area under the rhumb line (meters<sup>2</sup>).
     * <p>
     * The RhumbLine::mask values possible for \e outmask are
     * - \e outmask |= RhumbLine::LATITUDE for the latitude \e lat2;
     * - \e outmask |= RhumbLine::LONGITUDE for the latitude \e lon2;
     * - \e outmask |= RhumbLine::AREA for the area \e S12;
     * - \e outmask |= RhumbLine::ALL for all of the above;
     * - \e outmask |= RhumbLine::LONG_UNROLL to unroll \e lon2 instead of
     * wrapping it into the range [&minus;180&deg;, 180&deg;].
     * .
     * With the RhumbLine::LONG_UNROLL bit set, the quantity \e lon2 &minus; \e
     * lon1 indicates how many times and in what sense the rhumb line encircles
     * the ellipsoid.
     * <p>
     * If \e s12 is large enough that the rhumb line crosses a pole, the
     * longitude of point 2 is indeterminate (a NaN is returned for \e lon2 and
     * \e S12).
     **********************************************************************/
    public GeodesicData Position(double s12, int outmask) {
        GeodesicData g = new GeodesicData();
        double
                mu12 = s12 * _calp * 90 / rhumb.ellipsoid.QuarterMeridian(),
                mu2 = _mu1 + mu12;
        double psi2, lat2x, lon2x;
        if (abs(mu2) <= 90) {
            if (_calp != 0) {
                lat2x = rhumb.ellipsoid.InverseRectifyingLatitude(mu2);
                double psi12 = rhumb.DRectifyingToIsometric(mu2 * RADIANS_TO_DEGREES,
                        _mu1 * RADIANS_TO_DEGREES) * mu12;
                lon2x = _salp * psi12 / _calp;
                psi2 = _psi1 + psi12;
            } else {
                lat2x = _lat1;
                lon2x = _salp * s12 / (_r1 * RADIANS_TO_DEGREES);
                psi2 = _psi1;
            }
            if ((outmask & AREA) != 0)
                g.S12 = rhumb._c2 * lon2x *
                        rhumb.MeanSinXi(_psi1 * RADIANS_TO_DEGREES, psi2 * RADIANS_TO_DEGREES);
            lon2x = (outmask & LONG_UNROLL) != 0 ? _lon1 + lon2x :
                    GeoMath.AngNormalize(GeoMath.AngNormalize(_lon1) + lon2x);
        } else {
            // Reduce to the interval [-180, 180)
            mu2 = GeoMath.AngNormalize(mu2);
            // Deal with points on the anti-meridian
            if (abs(mu2) > 90) mu2 = GeoMath.AngNormalize(180 - mu2);
            lat2x = rhumb.ellipsoid.InverseRectifyingLatitude(mu2);
            lon2x = Double.NaN;
            if ((outmask & AREA) != 0)
                g.S12 = Double.NaN;
        }
        if ((outmask & LATITUDE) != 0) g.lat2 = lat2x;
        if ((outmask & LONGITUDE) != 0) g.lon2 = lon2x;
        return g;
    }

    /** \name Inspector functions
     **********************************************************************/
    ///@{

    /**
     * @return \e lat1 the latitude of point 1 (degrees).
     **********************************************************************/
    public double latitude() {
        return _lat1;
    }

    /**
     * @return \e lon1 the longitude of point 1 (degrees).
     **********************************************************************/
    public double longitude() {
        return _lon1;
    }

    /**
     * @return \e azi12 the azimuth of the rhumb line (degrees).
     **********************************************************************/
    public double Azimuth() {
        return _azi12;
    }

    /**
     * @return \e a the equatorial radius of the ellipsoid (meters).  This is
     * the value inherited from the Rhumb object used in the constructor.
     **********************************************************************/
    public double MajorRadius() {
        return rhumb.MajorRadius();
    }

    /**
     * @return \e f the flattening of the ellipsoid.  This is the value
     * inherited from the Rhumb object used in the constructor.
     **********************************************************************/
    public double Flattening() {
        return rhumb.Flattening();
    }


}





