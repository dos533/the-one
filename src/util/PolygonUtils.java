package util;

import core.Coord;

import java.util.List;
import java.util.Random;

public class PolygonUtils {

    private static Random _random = new Random();

    public static boolean IsInside(
            final List<Coord> polygon,
            final Coord point) {
        final int count = CountIntersectedEdges(polygon, point,
                new Coord(-10, 0));
        return ((count % 2) != 0);
    }

    public static boolean PathIntersects(
            final List<Coord> polygon,
            final Coord start,
            final Coord end) {
        final int count = CountIntersectedEdges(polygon, start, end);
        return (count > 0);
    }


    public static Coord RandomPointInside(final List<Coord> polygon) {
        Coord[] bounds = GetPolygonBounds(polygon);

        Coord c = null;
        while(c == null) {
            Coord randomC = new Coord(_random.nextDouble(bounds[0].getX(), bounds[1].getX()), _random.nextDouble(bounds[0].getY(), bounds[1].getY()));
            if(IsInside(polygon, randomC)) {
                c = randomC;
            }
        }

        return c;
    }


    private static Coord[] GetPolygonBounds(final List<Coord> polygon) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = 0;
        double maxY = 0;

        for(Coord c : polygon) {
            double x = c.getX();
            double y = c.getY();
            if( x > maxX) {
                maxX = x;
            }
            if(x < minX) {
                minX = x;
            }
            if( y > maxY) {
                maxY = y;
            }
            if( y < minY) {
                minY = y;
            }
        }

        return new Coord[] {new Coord(minX, minY), new Coord(maxX, maxY)};
    }

    private static int CountIntersectedEdges(
            final List<Coord> polygon,
            final Coord start,
            final Coord end) {
        int count = 0;
        for (int i = 0; i < polygon.size() - 1; i++) {
            final Coord polyP1 = polygon.get(i);
            final Coord polyP2 = polygon.get(i + 1);

            final Coord intersection = Intersection(start, end, polyP1, polyP2);
            if (intersection == null) continue;

            if (IsOnSegment(polyP1, polyP2, intersection)
                    && IsOnSegment(start, end, intersection)) {
                count++;
            }
        }
        return count;
    }

    private static boolean IsOnSegment(
            final Coord L0,
            final Coord L1,
            final Coord point) {
        final double crossProduct
                = (point.getY() - L0.getY()) * (L1.getX() - L0.getX())
                - (point.getX() - L0.getX()) * (L1.getY() - L0.getY());
        if (Math.abs(crossProduct) > 0.0000001) return false;

        final double dotProduct
                = (point.getX() - L0.getX()) * (L1.getX() - L0.getX())
                + (point.getY() - L0.getY()) * (L1.getY() - L0.getY());
        if (dotProduct < 0) return false;

        final double squaredLength
                = (L1.getX() - L0.getX()) * (L1.getX() - L0.getX())
                + (L1.getY() - L0.getY()) * (L1.getY() - L0.getY());
        if (dotProduct > squaredLength) return false;

        return true;
    }

    private static Coord Intersection(
            final Coord L0_p0,
            final Coord L0_p1,
            final Coord L1_p0,
            final Coord L1_p1) {
        final double[] p0 = GetParams(L0_p0, L0_p1);
        final double[] p1 = GetParams(L1_p0, L1_p1);
        final double D = p0[1] * p1[0] - p0[0] * p1[1];
        if (D == 0.0) return null;

        final double x = (p0[2] * p1[1] - p0[1] * p1[2]) / D;
        final double y = (p0[2] * p1[0] - p0[0] * p1[2]) / D;

        return new Coord(x, y);
    }

    private static double[] GetParams(
            final Coord c0,
            final Coord c1) {
        final double A = c0.getY() - c1.getY();
        final double B = c0.getX() - c1.getX();
        final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
        return new double[]{A, B, C};
    }
}
