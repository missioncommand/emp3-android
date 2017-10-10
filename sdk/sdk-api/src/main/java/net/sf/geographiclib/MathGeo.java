package net.sf.geographiclib;

import static net.sf.geographiclib.GeoMath.atanh;

public class MathGeo {

    final static double DEGREES_TO_RADIANS = 180.0 / Math.PI;
    final static double RADIANS_TO_DEGREES = Math.PI / 180.0;

    static double tand(double x) {
        return Math.tan(x * Math.PI / 180.0);
    }

    static double sind(double x) { return Math.sin(x * Math.PI / 180.0); }

    static double atand(double x) {
        return Math.atan(x) * 180.0 / Math.PI;
    }

    static double asinh(double x)
    {
        return Math.log(x + Math.sqrt(x*x + 1.0));
    }

    static double acosh(double x)
    {
        return Math.log(x + Math.sqrt(x*x - 1.0));
    }
    static double eatanhe(double x, double es)  {
        return es > 0 ? es * atanh(es * x) : -es * Math.atan(es * x);
    }

    static double taupf(double tau, double es) {
        double tau1 = GeoMath.hypot(1.0D, tau),
                sig = Math.sinh( eatanhe(tau / tau1, es ) );
        return GeoMath.hypot(1.0D, sig) * tau - sig * tau1;
    }
    final static int numit = 5;
    final static double tol = Math.sqrt(GeoMath.epsilon) / 10.0D;
    static double tauf(double taup, double es) {

        double e2m = 1.0D - GeoMath.sq(es),
                // To lowest order in e^2, taup = (1 - e^2) * tau = _e2m * tau; so use
                // tau = taup/_e2m as a starting guess.  (This starting guess is the
                // geocentric latitude which, to first order in the flattening, is equal
                // to the conformal latitude.)  Only 1 iteration is needed for |lat| <
                // 3.35 deg, otherwise 2 iterations are needed.  If, instead, tau = taup
                // is used the mean number of iterations increases to 1.99 (2 iterations
                // are needed except near tau = 0).
                tau = taup/e2m,
                stol = tol * Math.max(1.0D, Math.abs(taup));
        // min iterations = 1, max iterations = 2; mean = 1.94
        for (int i = 0; i <= numit; ++i) {
            if (i == numit) {
                throw new GeographicErr("Convergence failure");
            }
            double taupa = taupf(tau, es),
                    dtau = (taup - taupa) * (1 + e2m * GeoMath.sq(tau)) /
                            ( e2m * GeoMath.hypot(1.0D, tau) * GeoMath.hypot(1.0D, taupa) );
            tau += dtau;
            if (!(Math.abs(dtau) >= stol))
                break;
        }
        return tau;
    }
}
