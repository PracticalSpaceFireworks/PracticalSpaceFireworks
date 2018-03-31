package net.gegy1000.psf.server.util;

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

    public void transform(Point3d point) {
        this.matrix.transform(point);
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
