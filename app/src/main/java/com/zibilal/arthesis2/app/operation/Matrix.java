package com.zibilal.arthesis2.app.operation;

/**
 * Created by bmuhamm on 5/22/14.
 */
public class Matrix {

    private float a1=0f, a2=0f, a3=0f;
    private float b1=0f, b2=0f, b3=0f;
    private float c1=0f, c2=0f, c3=0f;

    public Matrix(){}

    public float getA1() {
        return a1;
    }

    public void setA1(float a1) {
        this.a1 = a1;
    }

    public float getA2() {
        return a2;
    }

    public void setA2(float a2) {
        this.a2 = a2;
    }

    public float getA3() {
        return a3;
    }

    public void setA3(float a3) {
        this.a3 = a3;
    }

    public float getB1() {
        return b1;
    }

    public void setB1(float b1) {
        this.b1 = b1;
    }

    public float getB2() {
        return b2;
    }

    public void setB2(float b2) {
        this.b2 = b2;
    }

    public float getB3() {
        return b3;
    }

    public void setB3(float b3) {
        this.b3 = b3;
    }

    public float getC1() {
        return c1;
    }

    public void setC1(float c1) {
        this.c1 = c1;
    }

    public float getC2() {
        return c2;
    }

    public void setC2(float c2) {
        this.c2 = c2;
    }

    public float getC3() {
        return c3;
    }

    public void setC3(float c3) {
        this.c3 = c3;
    }

    public void set(float a1, float a2, float a3, float b1, float b2, float b3, float c1, float c2, float c3) {
        this.a1=a1; this.a2=a2; this.a3=a3; this.b1=b1; this.b2=b2; this.b3=b3;
        this.c1=c1; this.c2=c2; this.c3=c3;
    }

    public void get(float[] array) {
        array[0] = a1;
        array[1] = a2;
        array[2] = a3;
        array[3] = b1;
        array[4] = b2;
        array[5] = b3;
        array[6] = c1;
        array[7] = c2;
        array[8] = c3;
    }

    public void set(Matrix matrix) {
        set(matrix.a1, matrix.a2, matrix.a3, matrix.b1, matrix.b2, matrix.b3, matrix.c1, matrix.c2, matrix.c3);
    }

    public void toIdentity() {
        set(1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f);
    }

    @Override
    public String toString() {
        return "[ (" + this.a1 + "," + this.a2 + "," + this.a3 + ")" + " (" + this.b1 + "," + this.b2 + "," + this.b3 + ")" + " (" + this.c1 + "," + this.c2
                + "," + this.c3 + ") ]";
    }

    public void prod(Matrix matrix) {
        Matrix mthis = new Matrix();
        mthis.set(this);

        this.a1 = (mthis.a1 * matrix.a1) + (mthis.a2 * matrix.b1) + (mthis.a3 * matrix.c1);
        this.a2 = (mthis.a1 * matrix.a2) + (mthis.a2 * matrix.b2) + (mthis.a3 * matrix.c2);
        this.a3 = (mthis.a1 * matrix.a3) + (mthis.a2 * matrix.b3) + (mthis.a3 * matrix.c3);

        this.b1 = (mthis.b1 * matrix.a1) + (mthis.b2 * matrix.b1) + (mthis.b3 * matrix.c1);
        this.b2 = (mthis.b1 * matrix.a2) + (mthis.b2 * matrix.b2) + (mthis.b3 * matrix.c2);
        this.b3 = (mthis.b1 * matrix.a3) + (mthis.b2 * matrix.b3) + (mthis.b3 * matrix.c3);

        this.c1 = (mthis.c1 * matrix.a1) + (mthis.c2 * matrix.b1) + (mthis.c3 * matrix.c1);
        this.c2 = (mthis.c1 * matrix.a2) + (mthis.c2 * matrix.b2) + (mthis.c3 * matrix.c2);
        this.c3 = (mthis.c1 * matrix.a3) + (mthis.c2 * matrix.b3) + (mthis.c3 * matrix.c3);
    }

    public void invert() {
        float det = this.det();
        this.adj();
        this.mult(1 / det);
    }

    public void adj() {
        float a11 = this.a1;
        float a12 = this.a2;
        float a13 = this.a3;

        float a21 = this.b1;
        float a22 = this.b2;
        float a23 = this.b3;

        float a31 = this.c1;
        float a32 = this.c2;
        float a33 = this.c3;

        this.a1 = det2x2(a22, a23, a32, a33);
        this.a2 = det2x2(a13, a12, a33, a32);
        this.a3 = det2x2(a12, a13, a22, a23);

        this.b1 = det2x2(a23, a21, a33, a31);
        this.b2 = det2x2(a11, a13, a31, a33);
        this.b3 = det2x2(a13, a11, a23, a21);

        this.c1 = det2x2(a21, a22, a31, a32);
        this.c2 = det2x2(a12, a11, a32, a31);
        this.c3 = det2x2(a11, a12, a21, a22);
    }

    public void mult(float c) {
        this.a1 = this.a1 * c;
        this.a2 = this.a2 * c;
        this.a3 = this.a3 * c;

        this.b1 = this.b1 * c;
        this.b2 = this.b2 * c;
        this.b3 = this.b3 * c;

        this.c1 = this.c1 * c;
        this.c2 = this.c2 * c;
        this.c3 = this.c3 * c;
    }

    private float det2x2(float a, float b, float c, float d) {
        return (a * d) - (b * c);
    }

    public float det() {
        return (this.a1 * this.b2 * this.c3) - (this.a1 * this.b3 * this.c2) - (this.a2 * this.b1 * this.c3) + (this.a2 * this.b3 * this.c1)
                + (this.a3 * this.b1 * this.c2) - (this.a3 * this.b2 * this.c1);
    }

    public boolean equals(Matrix n) {
        if (n == null) return false;

        if (this.a1 != n.a1)
            return false;
        if (this.a2 != n.a2)
            return false;
        if (this.a3 != n.a3)
            return false;

        if (this.b1 != n.b1)
            return false;
        if (this.b2 != n.b2)
            return false;
        if (this.b3 != n.b3)
            return false;

        if (this.c1 != n.c1)
            return false;
        if (this.c2 != n.c2)
            return false;
        if (this.c3 != n.c3)
            return false;

        return true;
    }


}
