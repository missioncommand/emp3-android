package net.sf.geographiclib;

/**
 * Created by raju on 10/10/17.
 */

public class EllipticFunction {

    final static double
            tolRD = Math.pow(0.2D * (GeoMath.epsilon * 0.01D),
            1 / 8.0D);
    final static int num_ = 13; // max depth required for sncndn
    double _k2, _kp2, _alpha2, _alphap2, _eps;
    double _Kc, _Ec, _Dc, _Pic, _Gc, _Hc;

    EllipticFunction() {
        Reset(0, 0);
    }

    EllipticFunction(double k2, double alpha2) {
        Reset(k2, alpha2);
    }

    EllipticFunction(double k2, double alpha2,
                     double kp2, double alphap2) {
        Reset(k2, alpha2, kp2, alphap2);
    }

    double E() {
        return _Ec;
    }

    double E(double sn, double cn, double dn) {
        double
                cn2 = cn * cn, dn2 = dn * dn, sn2 = sn * sn,
                ei = cn2 != 0 ?
                        Math.abs(sn) * (_k2 <= 0 ?
                                // Carlson, eq. 4.6 and
                                // http://dlmf.nist.gov/19.25.E9
                                RF(cn2, dn2, 1) - _k2 * sn2 * RD(cn2, dn2, 1) / 3 :
                                (_kp2 >= 0 ?
                                        // http://dlmf.nist.gov/19.25.E10
                                        _kp2 * RF(cn2, dn2, 1) +
                                                _k2 * _kp2 * sn2 * RD(cn2, 1, dn2) / 3 +
                                                _k2 * Math.abs(cn) / dn :
                                        // http://dlmf.nist.gov/19.25.E11
                                        -_kp2 * sn2 * RD(dn2, 1, cn2) / 3 +
                                                dn / Math.abs(cn))) :
                        E();
        // Enforce usual trig-like symmetries
        if (cn < 0)
            ei = 2 * E() - ei;
        return GeoMath.copysign(ei, sn);
    }

    double deltaE(double sn, double cn, double dn) {
        // Function is periodic with period pi
        if (cn < 0) { cn = -cn; sn = -sn; }
        return E(sn, cn, dn) * (Math.PI/2) / E() - Math.atan2(sn, cn);
    }

    double E(double phi) {
        double sn = Math.sin(phi), cn = Math.cos(phi), dn = Delta(sn, cn);
        return Math.abs(phi) < Math.PI ? E(sn, cn, dn) :
                (deltaE(sn, cn, dn) + phi) * E() / (Math.PI/2);
    }

    double RF(double x, double y, double z) {
        // Carlson, eqs 2.2 - 2.7
        final double tolRF =
                Math.pow(3 * GeoMath.epsilon * 0.01D, 1 / 8.0D);
        double
                A0 = (x + y + z) / 3,
                An = A0,
                Q = Math.max(Math.max(Math.abs(A0 - x), Math.abs(A0 - y)), Math.abs(A0 - z)) / tolRF,
                x0 = x,
                y0 = y,
                z0 = z,
                mul = 1;
        while (Q >= mul * Math.abs(An)) {
            // Max 6 trips
            double lam = Math.sqrt(x0) * Math.sqrt(y0) + Math.sqrt(y0) * Math.sqrt(z0) + Math.sqrt(z0) * Math.sqrt(x0);
            An = (An + lam) / 4;
            x0 = (x0 + lam) / 4;
            y0 = (y0 + lam) / 4;
            z0 = (z0 + lam) / 4;
            mul *= 4;
        }
        double
                X = (A0 - x) / (mul * An),
                Y = (A0 - y) / (mul * An),
                Z = -(X + Y),
                E2 = X * Y - Z * Z,
                E3 = X * Y * Z;
        // http://dlmf.nist.gov/19.36.E1
        // Polynomial is
        // (1 - E2/10 + E3/14 + E2^2/24 - 3*E2*E3/44
        //    - 5*E2^3/208 + 3*E3^2/104 + E2^2*E3/16)
        // convert to Horner form...
        return (E3 * (6930 * E3 + E2 * (15015 * E2 - 16380) + 17160) +
                E2 * ((10010 - 5775 * E2) * E2 - 24024) + 240240) /
                (240240 * Math.sqrt(An));
    }

    double RF(double x, double y) {
        // Carlson, eqs 2.36 - 2.38
        final double tolRG0 =
                2.7D * Math.sqrt(GeoMath.epsilon * 0.01D);
        double xn = Math.sqrt(x), yn = Math.sqrt(y);
        if (xn < yn) {
            double zn = xn;
            xn = yn;
            yn = zn;
        }
        while (Math.abs(xn - yn) > tolRG0 * xn) {
            // Max 4 trips
            double t = (xn + yn) / 2;
            yn = Math.sqrt(xn * yn);
            xn = t;
        }
        return Math.PI / (xn + yn);
    }

    double RC(double x, double y) {
        // Defined only for y != 0 and x >= 0.
        return (!(x >= y) ?        // x < y  and catch nans
                // http://dlmf.nist.gov/19.2.E18
                Math.atan(Math.sqrt((y - x) / x)) / Math.sqrt(y - x) :
                (x == y ? 1 / Math.sqrt(y) :
                        MathGeo.asinh(y > 0 ?
                                // http://dlmf.nist.gov/19.2.E19
                                // atanh(Math.sqrt((x - y) / x))
                                Math.sqrt((x - y) / y) :
                                // http://dlmf.nist.gov/19.2.E20
                                // atanh(Math.sqrt(x / (x - y)))
                                Math.sqrt(-x / y)) / Math.sqrt(x - y)));
    }

    double RG(double x, double y, double z) {
        if (z == 0) {
            z = y;
            y = 0;
        }
        // Carlson, eq 1.7
        return (z * RF(x, y, z) - (x - z) * (y - z) * RD(x, y, z) / 3
                + Math.sqrt(x * y / z)) / 2;
    }

    double RG(double x, double y) {
        // Carlson, eqs 2.36 - 2.39
        final double tolRG0 =
                2.7D * Math.sqrt(GeoMath.epsilon * 0.01D);
        double
                x0 = Math.sqrt(Math.max(x, y)),
                y0 = Math.sqrt(Math.min(x, y)),
                xn = x0,
                yn = y0,
                s = 0,
                mul = 0.25D;
        while (Math.abs(xn - yn) > tolRG0 * xn) {
            // Max 4 trips
            double t = (xn + yn) / 2;
            yn = Math.sqrt(xn * yn);
            xn = t;
            mul *= 2;
            t = xn - yn;
            s += mul * t * t;
        }
        return (GeoMath.sq((x0 + y0) / 2) - s) * Math.PI / (2 * (xn + yn));
    }

    double RJ(double x, double y, double z, double p) {
        // Carlson, eqs 2.17 - 2.25
        double
                A0 = (x + y + z + 2 * p) / 5,
                An = A0,
                delta = (p - x) * (p - y) * (p - z),
                Q = Math.max(Math.max(Math.abs(A0 - x), Math.abs(A0 - y)), Math.max(Math.abs(A0 - z), Math.abs(A0 - p))) / tolRD,
                x0 = x,
                y0 = y,
                z0 = z,
                p0 = p,
                mul = 1,
                mul3 = 1,
                s = 0;
        while (Q >= mul * Math.abs(An)) {
            // Max 7 trips
            double
                    lam = Math.sqrt(x0) * Math.sqrt(y0) + Math.sqrt(y0) * Math.sqrt(z0) + Math.sqrt(z0) * Math.sqrt(x0),
                    d0 = (Math.sqrt(p0) + Math.sqrt(x0)) * (Math.sqrt(p0) + Math.sqrt(y0)) * (Math.sqrt(p0) + Math.sqrt(z0)),
                    e0 = delta / (mul3 * GeoMath.sq(d0));
            s += RC(1, 1 + e0) / (mul * d0);
            An = (An + lam) / 4;
            x0 = (x0 + lam) / 4;
            y0 = (y0 + lam) / 4;
            z0 = (z0 + lam) / 4;
            p0 = (p0 + lam) / 4;
            mul *= 4;
            mul3 *= 64;
        }
        double
                X = (A0 - x) / (mul * An),
                Y = (A0 - y) / (mul * An),
                Z = (A0 - z) / (mul * An),
                P = -(X + Y + Z) / 2,
                E2 = X * Y + X * Z + Y * Z - 3 * P * P,
                E3 = X * Y * Z + 2 * P * (E2 + 2 * P * P),
                E4 = (2 * X * Y * Z + P * (E2 + 3 * P * P)) * P,
                E5 = X * Y * Z * P * P;
        // http://dlmf.nist.gov/19.36.E2
        // Polynomial is
        // (1 - 3*E2/14 + E3/6 + 9*E2^2/88 - 3*E4/22 - 9*E2*E3/52 + 3*E5/26
        //    - E2^3/16 + 3*E3^2/40 + 3*E2*E4/20 + 45*E2^2*E3/272
        //    - 9*(E3*E4+E2*E5)/68)
        return ((471240 - 540540 * E2) * E5 +
                (612612 * E2 - 540540 * E3 - 556920) * E4 +
                E3 * (306306 * E3 + E2 * (675675 * E2 - 706860) + 680680) +
                E2 * ((417690 - 255255 * E2) * E2 - 875160) + 4084080) /
                (4084080 * mul * An * Math.sqrt(An)) + 6 * s;
    }

    double RD(double x, double y, double z) {
        // Carlson, eqs 2.28 - 2.34

        double
                A0 = (x + y + 3 * z) / 5,
                An = A0,
                Q = Math.max(Math.max(Math.abs(A0 - x), Math.abs(A0 - y)), Math.abs(A0 - z)) / tolRD,
                x0 = x,
                y0 = y,
                z0 = z,
                mul = 1,
                s = 0;
        while (Q >= mul * Math.abs(An)) {
            // Max 7 trips
            double lam = Math.sqrt(x0) * Math.sqrt(y0) + Math.sqrt(y0) * Math.sqrt(z0) + Math.sqrt(z0) * Math.sqrt(x0);
            s += 1 / (mul * Math.sqrt(z0) * (z0 + lam));
            An = (An + lam) / 4;
            x0 = (x0 + lam) / 4;
            y0 = (y0 + lam) / 4;
            z0 = (z0 + lam) / 4;
            mul *= 4;
        }
        double
                X = (A0 - x) / (mul * An),
                Y = (A0 - y) / (mul * An),
                Z = -(X + Y) / 3,
                E2 = X * Y - 6 * Z * Z,
                E3 = (3 * X * Y - 8 * Z * Z) * Z,
                E4 = 3 * (X * Y - Z * Z) * Z * Z,
                E5 = X * Y * Z * Z * Z;
        // http://dlmf.nist.gov/19.36.E2
        // Polynomial is
        // (1 - 3*E2/14 + E3/6 + 9*E2^2/88 - 3*E4/22 - 9*E2*E3/52 + 3*E5/26
        //    - E2^3/16 + 3*E3^2/40 + 3*E2*E4/20 + 45*E2^2*E3/272
        //    - 9*(E3*E4+E2*E5)/68)
        return ((471240 - 540540 * E2) * E5 +
                (612612 * E2 - 540540 * E3 - 556920) * E4 +
                E3 * (306306 * E3 + E2 * (675675 * E2 - 706860) + 680680) +
                E2 * ((417690 - 255255 * E2) * E2 - 875160) + 4084080) /
                (4084080 * mul * An * Math.sqrt(An)) + 3 * s;
    }

    void Reset(double k2, double alpha2) {
        Reset(k2, alpha2, 1 - k2, 1 - alpha2);
    }


    void Reset(double k2, double alpha2,
               double kp2, double alphap2) {
        // Accept nans here (needed for GeodesicExact)
        if (k2 > 1)
            throw new GeographicErr("Parameter k2 is not in (-inf, 1]");
        if (alpha2 > 1)
            throw new GeographicErr("Parameter alpha2 is not in (-inf, 1]");
        if (kp2 < 0)
            throw new GeographicErr("Parameter kp2 is not in [0, inf)");
        if (alphap2 < 0)
            throw new GeographicErr("Parameter alphap2 is not in [0, inf)");
        _k2 = k2;
        _kp2 = kp2;
        _alpha2 = alpha2;
        _alphap2 = alphap2;
        _eps = _k2 / GeoMath.sq(Math.sqrt(_kp2) + 1);
        // Values of complete elliptic integrals for k = 0,1 and alpha = 0,1
        //         K     E     D
        // k = 0:  pi/2  pi/2  pi/4
        // k = 1:  inf   1     inf
        //                    Pi    G     H
        // k = 0, alpha = 0:  pi/2  pi/2  pi/4
        // k = 1, alpha = 0:  inf   1     1
        // k = 0, alpha = 1:  inf   inf   pi/2
        // k = 1, alpha = 1:  inf   inf   inf
        //
        // Pi(0, k) = K(k)
        // G(0, k) = E(k)
        // H(0, k) = K(k) - D(k)
        // Pi(0, k) = K(k)
        // G(0, k) = E(k)
        // H(0, k) = K(k) - D(k)
        // Pi(alpha2, 0) = pi/(2*Math.sqrt(1-alpha2))
        // G(alpha2, 0) = pi/(2*Math.sqrt(1-alpha2))
        // H(alpha2, 0) = pi/(2*(1 + Math.sqrt(1-alpha2)))
        // Pi(alpha2, 1) = inf
        // H(1, k) = K(k)
        // G(alpha2, 1) = H(alpha2, 1) = RC(1, alphap2)
        if (_k2 != 0) {
            // Complete elliptic integral K(k), Carlson eq. 4.1
            // http://dlmf.nist.gov/19.25.E1
            _Kc = _kp2 != 0 ? RF(_kp2, 1) : Double.POSITIVE_INFINITY;
            // Complete elliptic integral E(k), Carlson eq. 4.2
            // http://dlmf.nist.gov/19.25.E1
            _Ec = _kp2 != 0 ? 2 * RG(_kp2, 1) : 1;
            // D(k) = (K(k) - E(k))/k^2, Carlson eq.4.3
            // http://dlmf.nist.gov/19.25.E1
            _Dc = _kp2 != 0 ? RD(0, _kp2, 1) / 3 : Double.POSITIVE_INFINITY;
        } else {
            _Kc = _Ec = Math.PI / 2;
            _Dc = _Kc / 2;
        }
        if (_alpha2 != 0) {
            // http://dlmf.nist.gov/19.25.E2
            double rj = (_kp2 != 0 && _alphap2 != 0) ? RJ(0, _kp2, 1, _alphap2) :
                    Double.POSITIVE_INFINITY,
                    // Only use rc if _kp2 = 0.
                    rc = _kp2 != 0 ? 0 :
                            (_alphap2 != 0 ? RC(1, _alphap2) : Double.POSITIVE_INFINITY);
            // Pi(alpha^2, k)
            _Pic = _kp2 != 0 ? _Kc + _alpha2 * rj / 3 : Double.POSITIVE_INFINITY;
            // G(alpha^2, k)
            _Gc = _kp2 != 0 ? _Kc + (_alpha2 - _k2) * rj / 3 : rc;
            // H(alpha^2, k)
            _Hc = _kp2 != 0 ? _Kc - (_alphap2 != 0 ? _alphap2 * rj : 0) / 3 : rc;
        } else {
            _Pic = _Kc;
            _Gc = _Ec;
            // Hc = Kc - Dc but this involves large cancellations if k2 is close to
            // 1.  So write (for alpha2 = 0)
            //   Hc = int(cos(phi)^2/Math.sqrt(1-k2*sin(phi)^2),phi,0,pi/2)
            //      = 1/Math.sqrt(1-k2) * int(sin(phi)^2/Math.sqrt(1-k2/kp2*sin(phi)^2,...)
            //      = 1/kp * D(i*k/kp)
            // and use D(k) = RD(0, kp2, 1) / 3
            // so Hc = 1/kp * RD(0, 1/kp2, 1) / 3
            //       = kp2 * RD(0, 1, kp2) / 3
            // using http://dlmf.nist.gov/19.20.E18
            // Equivalently
            //   RF(x, 1) - RD(0, x, 1)/3 = x * RD(0, 1, x)/3 for x > 0
            // For k2 = 1 and alpha2 = 0, we have
            //   Hc = int(cos(phi),...) = 1
            _Hc = _kp2 != 0 ? _kp2 * RD(0, 1, _kp2) / 3 : 1;
        }
    }


    /**
     * The &Delta; amplitude function.
     *
     * @param[in] sn sin&phi;.
     * @param[in] cn cos&phi;.
     * @return &Delta; = sqrt(1 &minus; <i>k</i><sup>2</sup>
     *   sin<sup>2</sup>&phi;).
     **********************************************************************/
    double Delta(double sn, double cn) {
        return Math.sqrt(_k2 < 0 ? 1 - _k2 * sn*sn : _kp2 + _k2 * cn*cn);
    }

    double Ed(double ang) {
        double n = Math.ceil(ang/360 - 0.5D);
        ang -= 360 * n;
        double sn, cn;
        Pair sc = GeoMath.sincosd(ang);
        sn = sc.first;
        cn = sc.second;
        return E(sn, cn, Delta(sn, cn)) + 4 * E() * n;
    }

    double Einv(double x) {
        final double tolJAC =
                Math.sqrt(GeoMath.epsilon * 0.01D);
        double n = Math.floor(x / (2 * _Ec) + 0.5D);
        x -= 2 * _Ec * n;           // x now in [-ec, ec)
        // Linear approximation
        double phi = Math.PI * x / (2 * _Ec); // phi in [-pi/2, pi/2)
        // First order correction
        phi -= _eps * Math.sin(2 * phi) / 2;
        // For kp2 close to zero use asin(x/_Ec) or
        // J. P. Boyd, Applied Math. and Computation 218, 7005-7013 (2012)
        // https://doi.org/10.1016/j.amc.2011.12.021
        for (int i = 0; i <= num_; ++i) {
            if (i == num_) {
                throw new GeographicErr("Convergence failure");
            }
            double
                    sn = Math.sin(phi),
                    cn = Math.cos(phi),
                    dn = Delta(sn, cn),
                    err = (E(sn, cn, dn) - x)/dn;
            phi -= err;
            if (Math.abs(err) < tolJAC)
                break;
        }
        return n * Math.PI + phi;
    }
}
