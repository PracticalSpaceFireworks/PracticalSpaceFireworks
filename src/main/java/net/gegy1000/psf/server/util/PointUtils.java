package net.gegy1000.psf.server.util;

import javax.vecmath.Point3d;

public class PointUtils {
    public static Point3d min(Point3d... points) {
        double minX = points[0].x;
        double minY = points[0].y;
        double minZ = points[0].z;

        for (int i = 1; i < points.length; i++) {
            Point3d point = points[i];
            if (point.x < minX) {
                minX = point.x;
            }
            if (point.y < minY) {
                minY = point.y;
            }
            if (point.z < minZ) {
                minZ = point.z;
            }
        }

        return new Point3d(minX, minY, minZ);
    }

    public static Point3d max(Point3d... points) {
        double maxX = points[0].x;
        double maxY = points[0].y;
        double maxZ = points[0].z;

        for (int i = 1; i < points.length; i++) {
            Point3d point = points[i];
            if (point.x > maxX) {
                maxX = point.x;
            }
            if (point.y > maxY) {
                maxY = point.y;
            }
            if (point.z > maxZ) {
                maxZ = point.z;
            }
        }

        return new Point3d(maxX, maxY, maxZ);
    }
}
