package com.zibilal.arthesis2.app.operation;

/**
 * Created by bmuhamm on 5/22/14.
 */
public class Vector {
    private float x=0f;
    private float y=0f;
    private float z=0f;

    public Vector(){this(0f, 0f, 0f);}

    public Vector(float x, float y, float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void set(float[] arry){
        setX(arry[0]);
        setY(arry[1]);
        setZ(arry[2]);
    }

    public void set(float x, float y, float z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public void set(Vector v) {
        set(v.getX(), v.getY(), v.getZ());
    }

    public void get(float[] arry) {
        arry[0] = this.x;
        arry[1] = this.y;
        arry[2] = this.z;
    }

    public void add(Vector v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public void sub(Vector v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public void mult(float s) {
        this.x *= s;
        this.y *= s;
        this.z *= s;
    }

    public void prod(Matrix matrix) {
        float[] farray = new float[9];
        matrix.get(farray);

        float xTemp = farray[0] * x + farray[1] * y + farray[2] * z;
        float yTemp = farray[3] * x + farray[4] * y + farray[5] * z;
        float zTemp = farray[6] * x + farray[7] * y + farray[8] * z;

        this.x = xTemp;
        this.y = yTemp;
        this.z = zTemp;
    }

    public float length() {
        return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    @Override
    public boolean equals(Object o) {
        Vector v = (Vector) o;
        return (v.x == this.x && v.y == this.y && v.z == this.z);
    }

    @Override
    public String toString() {
        return String.format("[ %.4f %.4f %.4f]", x, y,z);
    }
}
