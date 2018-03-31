package net.gegy1000.psf.server.util;

import net.minecraft.util.math.AxisAlignedBB;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import java.util.Stack;

public class Matrix {
    private final Stack<Matrix4d> pool = new Stack<>();

    private final Stack<Matrix4d> matrices = new Stack<>();
    private final Matrix4d matrix = new Matrix4d();

    public Matrix(int poolSize) {
        for (int i = 0; i < poolSize; i++) {
            this.pool.push(new Matrix4d());
        }
    }

    public void identity() {
        while (!this.matrices.empty()) {
            this.pool.push(this.matrices.pop());
        }
        this.matrix.setIdentity();
    }

    public void translate(double x, double y, double z) {
        Matrix4d matrix = this.matrices.push(this.takePool());
        matrix.set(new Vector3d(x, y, z));

        this.updateMatrix();
    }

    public void rotate(double angle, double x, double y, double z) {
        Matrix4d matrix = this.matrices.push(this.takePool());

        Quat4d rotation = new Quat4d();
        rotation.set(new AxisAngle4d(x, y, z, Math.toRadians(angle)));
        matrix.setRotation(rotation);

        this.updateMatrix();
    }

    public Point3d transform(Point3d point) {
        this.matrix.transform(point);
        return point;
    }

    public Point3d transform(double x, double y, double z) {
        return this.transform(new Point3d(x, y, z));
    }

    public AxisAlignedBB transform(AxisAlignedBB bounds) {
        Point3d[] transformedPoints = new Point3d[] {
                this.transform(bounds.minX, bounds.minY, bounds.minZ),
                this.transform(bounds.minX, bounds.minY, bounds.maxZ),
                this.transform(bounds.minX, bounds.maxY, bounds.minZ),
                this.transform(bounds.minX, bounds.maxY, bounds.maxZ),
                this.transform(bounds.maxX, bounds.minY, bounds.minZ),
                this.transform(bounds.maxX, bounds.minY, bounds.maxZ),
                this.transform(bounds.maxX, bounds.maxY, bounds.minZ),
                this.transform(bounds.maxX, bounds.maxY, bounds.maxZ)
        };

        Point3d min = PointUtils.min(transformedPoints);
        Point3d max = PointUtils.max(transformedPoints);

        return new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    private Matrix4d takePool() {
        Matrix4d pop = this.pool.pop();
        pop.setIdentity();
        return pop;
    }

    private void updateMatrix() {
        this.matrix.setIdentity();
        for (Matrix4d matrix : this.matrices) {
            this.matrix.mul(matrix);
        }
    }
}
