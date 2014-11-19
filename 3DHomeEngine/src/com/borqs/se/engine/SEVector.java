package com.borqs.se.engine;

import org.xmlpull.v1.XmlPullParser;

public class SEVector {
    public static enum AXIS {
        X, Y, Z
    };

    public static class SEVector2f {
        public float[] mD = { 0.0f, 0.0f };

        public SEVector2f() {

        }

        public SEVector2f(float x, float y) {
            mD[0] = x;
            mD[1] = y;
        }

        public void set(float x, float y) {
            mD[0] = x;
            mD[1] = y;
        }

        public float getX() {
            return mD[0];
        }

        public float getY() {
            return mD[1];
        }

        public SEVector2f(float[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
        }

        public SEVector2f(SEVector2f vector2f) {
            mD[0] = vector2f.mD[0];
            mD[1] = vector2f.mD[1];
        }

        public boolean equals(SEVector2f vector2f) {
            return vector2f.mD[0] == mD[0] && vector2f.mD[1] == mD[1];
        }

        public SEVector2f subtract(SEVector2f vector2f) {
            SEVector2f result = new SEVector2f();
            result.mD[0] = mD[0] - vector2f.mD[0];
            result.mD[1] = mD[1] - vector2f.mD[1];
            return result;
        }

        public SEVector2f selfSubtract(SEVector2f vector2f) {
            mD[0] = mD[0] - vector2f.mD[0];
            mD[1] = mD[1] - vector2f.mD[1];
            return this;
        }

        public SEVector2f add(SEVector2f vector2f) {
            SEVector2f result = new SEVector2f();
            result.mD[0] = mD[0] + vector2f.mD[0];
            result.mD[1] = mD[1] + vector2f.mD[1];
            return result;
        }

        public SEVector2f selfAdd(SEVector2f vector2f) {
            mD[0] = mD[0] + vector2f.mD[0];
            mD[1] = mD[1] + vector2f.mD[1];
            return this;
        }

        public void normalize() {
            float length = (float) Math.sqrt(mD[0] * mD[0] + mD[1] * mD[1]);
            if (length == 0) {
                mD[0] = 1;
                mD[1] = 0;
            } else {
                mD[0] = mD[0] / length;
                mD[1] = mD[1] / length;
            }
        }

        public SEVector2f mul(float par) {
            SEVector2f result = new SEVector2f();
            result.mD[0] = mD[0] * par;
            result.mD[1] = mD[1] * par;
            return result;
        }

        public SEVector2f selfMul(float par) {
            mD[0] = mD[0] * par;
            mD[1] = mD[1] * par;
            return this;
        }

        public float mul(SEVector2f par) {
            return mD[0] * par.getX() + mD[1] * par.getY();
        }

        public float getLength() {
            float length = (float) Math.sqrt(mD[0] * mD[0] + mD[1] * mD[1]);
            return length;
        }

        public double getAngle() {
            double length = getLength();
            if (length < 0.00001f) {
                return 0;
            }
            double angle = Math.acos(getX() / length);
            if (getY() < 0) {
                angle = 2 * Math.PI - angle;
            }
            return angle;
        }

        public double getAngle_II() {
            double length = getLength();
            double angle = 0;
            if (length < 0.01f) {
                return 0;
            }
            angle = Math.acos(getY() / length);
            if (getX() > 0) {
                angle = (float) (2 * Math.PI - angle);
            }
            return angle;
        }
    }

    public static class SEVector2i {
        public int[] mD = { 0, 0 };

        public SEVector2i() {

        }

        public int getX() {
            return mD[0];
        }

        public int getY() {
            return mD[1];
        }

        public SEVector2i(int x, int y) {
            mD[0] = x;
            mD[1] = y;
        }
        
        public void set(int x, int y) {
            mD[0] = x;
            mD[1] = y;
        }

        public SEVector2i(int[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
        }

        public SEVector2i(SEVector2i vector2i) {
            mD[0] = vector2i.mD[0];
            mD[1] = vector2i.mD[1];
        }

        public boolean equals(SEVector2i vector2i) {
            return vector2i.mD[0] == mD[0] && vector2i.mD[1] == mD[1];
        }

        public SEVector2i subtract(SEVector2i vector2i) {
            SEVector2i result = new SEVector2i();
            result.mD[0] = mD[0] - vector2i.mD[0];
            result.mD[1] = mD[1] - vector2i.mD[1];
            return result;
        }

        public SEVector2i selfSubtract(SEVector2i vector2i) {
            mD[0] = mD[0] - vector2i.mD[0];
            mD[1] = mD[1] - vector2i.mD[1];
            return this;
        }

        public float getLength() {
            float length = (float) Math.sqrt((float) (mD[0] * mD[0] + mD[1] * mD[1]));
            return length;
        }

        public SEVector2i clone() {
            return new SEVector2i(this);
        }
    }

    public static class SEVector3f {
        public static final SEVector3f ZERO = new SEVector3f();
        public static final SEVector3f ONE = new SEVector3f(1f, 1f, 1f);

        public float[] mD = { 0.0f, 0.0f, 0.0f };

        public SEVector3f() {

        }

        public SEVector3f(float x, float y, float z) {
            mD[0] = x;
            mD[1] = y;
            mD[2] = z;
        }

        public float getX() {
            return mD[0];
        }

        public float getY() {
            return mD[1];
        }

        public float getZ() {
            return mD[2];
        }

        public SEVector2f getVectorZ() {
            return new SEVector2f(mD[0], mD[1]);
        }

        public SEVector2f getVectorX() {
            return new SEVector2f(mD[1], mD[2]);
        }

        public SEVector2f getVectorY() {
            return new SEVector2f(mD[2], mD[0]);
        }

        public SEVector3f clone() {
            return new SEVector3f(this);
        }

        public void set(float x, float y, float z) {
            mD[0] = x;
            mD[1] = y;
            mD[2] = z;
        }

        public SEVector3f(float[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
        }

        public void set(float[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
        }

        public SEVector3f(SEVector3f vector3f) {
            mD[0] = vector3f.mD[0];
            mD[1] = vector3f.mD[1];
            mD[2] = vector3f.mD[2];
        }

        public boolean equals(SEVector3f vector3f) {
            return vector3f.mD[0] == mD[0] && vector3f.mD[1] == mD[1] && vector3f.mD[2] == mD[2];
        }

        public SEVector3f subtract(SEVector3f vector3f) {
            SEVector3f result = new SEVector3f();
            result.mD[0] = mD[0] - vector3f.mD[0];
            result.mD[1] = mD[1] - vector3f.mD[1];
            result.mD[2] = mD[2] - vector3f.mD[2];
            return result;
        }

        public SEVector3f selfSubtract(SEVector3f vector3f) {
            mD[0] = mD[0] - vector3f.mD[0];
            mD[1] = mD[1] - vector3f.mD[1];
            mD[2] = mD[2] - vector3f.mD[2];
            return this;
        }

        public SEVector3f add(SEVector3f vector3f) {
            SEVector3f result = new SEVector3f();
            result.mD[0] = mD[0] + vector3f.mD[0];
            result.mD[1] = mD[1] + vector3f.mD[1];
            result.mD[2] = mD[2] + vector3f.mD[2];
            return result;
        }

        public SEVector3f selfAdd(SEVector3f vector3f) {
            mD[0] = mD[0] + vector3f.mD[0];
            mD[1] = mD[1] + vector3f.mD[1];
            mD[2] = mD[2] + vector3f.mD[2];
            return this;
        }

        public SEVector3f cross(SEVector3f rv) {
            return new SEVector3f(getY() * rv.getZ() - getZ() * rv.getY(), getZ() * rv.getX() - getX() * rv.getZ(),
                    getX() * rv.getY() - getY() * rv.getX());
        }

        public SEVector3f div(float par) {
            SEVector3f result = new SEVector3f();
            result.mD[0] = mD[0] / par;
            result.mD[1] = mD[1] / par;
            result.mD[2] = mD[2] / par;
            return result;
        }

        public SEVector3f selfDiv(float par) {
            mD[0] = mD[0] / par;
            mD[1] = mD[1] / par;
            mD[2] = mD[2] / par;
            return this;
        }

        public SEVector3f mul(float par) {
            SEVector3f result = new SEVector3f();
            result.mD[0] = mD[0] * par;
            result.mD[1] = mD[1] * par;
            result.mD[2] = mD[2] * par;
            return result;
        }

        public SEVector3f mul(SEVector3f par) {
            SEVector3f result = new SEVector3f();
            result.mD[0] = mD[0] * par.getX();
            result.mD[1] = mD[1] * par.getY();
            result.mD[2] = mD[2] * par.getZ();
            return result;
        }

        public SEVector3f selfMul(float par) {
            mD[0] = mD[0] * par;
            mD[1] = mD[1] * par;
            mD[2] = mD[2] * par;
            return this;
        }

        public SEVector3f selfMul(SEVector3f par) {
            mD[0] = mD[0] * par.getX();
            mD[1] = mD[1] * par.getY();
            mD[2] = mD[2] * par.getZ();
            return this;
        }

        public SEVector3f normalize() {
            float length = (float) Math.sqrt(mD[0] * mD[0] + mD[1] * mD[1] + mD[2] * mD[2]);
            mD[0] = mD[0] / length;
            mD[1] = mD[1] / length;
            mD[2] = mD[2] / length;
            return this;
        }

        public float getLength() {
            float length = (float) Math.sqrt(mD[0] * mD[0] + mD[1] * mD[1] + mD[2] * mD[2]);
            return length;
        }

        public double getAngle() {
            double length = Math.sqrt(mD[0] * mD[0] + mD[1] * mD[1]);
            double angle = 0;
            if (mD[1] >= 0) {
                angle = Math.acos(mD[0] / length);
            } else {
                angle = 2 * Math.PI - Math.acos(mD[0] / length);
            }
            return angle;
        }

        public void initFromXml(XmlPullParser parser) {
            mD[0] = Float.valueOf(parser.getAttributeValue(null, "x"));
            mD[1] = Float.valueOf(parser.getAttributeValue(null, "y"));
            mD[2] = Float.valueOf(parser.getAttributeValue(null, "z"));

        }

        public void reset() {
            mD[0] = 0;
            mD[1] = 0;
            mD[2] = 0;
        }

        public boolean isZero() {
            return (mD[0] == 0 && mD[1] == 0 && mD[2] == 0);
        }

        @Override
        public String toString() {
            return mD[0] + "," + mD[1] + "," + mD[2];
        }

    }

    public static class SEVector3i {
        public int[] mD = { 0, 0, 0 };

        public SEVector3i() {

        }

        public SEVector3i(int x, int y, int z) {
            mD[0] = x;
            mD[1] = y;
            mD[2] = z;
        }

        public SEVector3i(int[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
        }

        public SEVector3i(SEVector3i vector3i) {
            mD[0] = vector3i.mD[0];
            mD[1] = vector3i.mD[1];
            mD[2] = vector3i.mD[2];
        }

        public boolean equals(SEVector3i vector3i) {
            return vector3i.mD[0] == mD[0] && vector3i.mD[1] == mD[1] && vector3i.mD[2] == mD[2];
        }
    }

    public static class SEQuat {
        public float[] mD = { 0.0f, 0.0f, 0.0f, 0.0f };

        public SEQuat() {

        }

        public SEQuat(float[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
            mD[3] = d[3];
        }

        public void set(float angle, SEVector3f axis) {
            if (axis.isZero()) {
                identity();
                return;
            }
            axis.normalize();
            double radian = angleToRadian(angle) / 2;
            double sinRadian = Math.sin(radian);
            mD[3] = (float) Math.cos(radian);
            mD[0] = (float) (axis.getX() * sinRadian);
            mD[1] = (float) (axis.getY() * sinRadian);
            mD[2] = (float) (axis.getZ() * sinRadian);
        }

        public SEQuat conjugate() {
            return new SEQuat(-mD[0], -mD[1], -mD[2], mD[3]);
        }

        public SEQuat mul(SEQuat rq) {
            SEQuat ret = new SEQuat();
            ret.mD[3] = getW() * rq.getW() - getX() * rq.getX() - getY() * rq.getY() - getZ() * rq.getZ();
            ret.mD[0] = getW() * rq.getX() + getX() * rq.getW() + getY() * rq.getZ() - getZ() * rq.getY();
            ret.mD[1] = getW() * rq.getY() - getX() * rq.getZ() + getY() * rq.getW() + getZ() * rq.getX();
            ret.mD[2] = getW() * rq.getZ() + getX() * rq.getY() - getY() * rq.getX() + getZ() * rq.getW();
            return ret;
        }

        public SERotate toRotate() {
            SEVector3f axis = new SEVector3f(mD[0], mD[1], mD[2]);
            if (axis.isZero()) {
                return new SERotate();
            }
            axis.normalize();
            // float sin = getX() + getY() + getZ();
            float cos = getW();
            float angle = (float) Math.acos(cos);
            angle = (float) (angle * 180.0f / Math.PI);
            // if (sin < 0) {
            // angle = (float) (2 * Math.PI - angle);
            // }
            SERotate rotate = new SERotate(angle * 2.0f, axis.getX(), axis.getY(), axis.getZ());
            return rotate;
        }

        public void identity() {
            mD[0] = 0.0f;
            mD[1] = 0.0f;
            mD[2] = 0.0f;
            mD[3] = 1.0f;
        }

        public SEQuat(float x, float y, float z, float w) {
            mD[0] = x;
            mD[1] = y;
            mD[2] = z;
            mD[3] = w;
        }

        public float getX() {
            return mD[0];
        }

        public float getY() {
            return mD[1];
        }

        public float getZ() {
            return mD[2];
        }

        public float getW() {
            return mD[3];
        }
    }

    public static double angleToRadian(float angle) {
        return angle * Math.PI / 180.0f;
    }

    public static class SERotate {
        public float[] mD = { 0.0f, 0.0f, 0.0f, 0.0f };

        public SERotate() {

        }

        public SERotate(float angle) {
            mD[0] = angle;
            mD[1] = 0;
            mD[2] = 0;
            mD[3] = 1;
        }

        public SERotate(float angle, float x, float y, float z) {
            mD[0] = angle;
            mD[1] = x;
            mD[2] = y;
            mD[3] = z;

        }

        public void set(float angle, float x, float y, float z) {
            mD[0] = angle;
            mD[1] = x;
            mD[2] = y;
            mD[3] = z;

        }

        public float getX() {
            return mD[1];
        }

        public float getY() {
            return mD[2];
        }

        public float getZ() {
            return mD[3];
        }

        public float getAngle() {
            float angle = mD[0] % 360;
            if (angle < 0) {
                angle = angle + 360;
            }
            if (getZ() < 0) {
                angle = 360 - angle;
            }
            return angle;
        }

        public SEVector3f getAxis() {
            return new SEVector3f(mD[1], mD[2], mD[3]);
        }

        public SERotate(float[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
            mD[3] = d[3];
        }

        public SERotate(SERotate vector4f) {
            mD[0] = vector4f.mD[0];
            mD[1] = vector4f.mD[1];
            mD[2] = vector4f.mD[2];
            mD[3] = vector4f.mD[3];
        }

        public SERotate clone() {
            return new SERotate(this);
        }

        public boolean equals(SERotate vector4f) {
            return vector4f.mD[0] == mD[0] && vector4f.mD[1] == mD[1] && vector4f.mD[2] == mD[2]
                    && vector4f.mD[3] == mD[3];
        }

        public SERotate subtract(SERotate vector4f) {
            SERotate result = new SERotate();
            result.mD[0] = mD[0] - vector4f.mD[0];
            result.mD[1] = mD[1] - vector4f.mD[1];
            result.mD[2] = mD[2] - vector4f.mD[2];
            result.mD[3] = mD[3] - vector4f.mD[3];
            return result;
        }

        public SERotate selfSubtract(SERotate vector4f) {
            mD[0] = mD[0] - vector4f.mD[0];
            mD[1] = mD[1] - vector4f.mD[1];
            mD[2] = mD[2] - vector4f.mD[2];
            mD[3] = mD[3] - vector4f.mD[3];
            return this;
        }

        public SERotate add(SERotate vector4f) {
            SERotate result = new SERotate();
            result.mD[0] = mD[0] + vector4f.mD[0];
            result.mD[1] = mD[1] + vector4f.mD[1];
            result.mD[2] = mD[2] + vector4f.mD[2];
            result.mD[3] = mD[3] + vector4f.mD[3];
            return result;
        }

        public SERotate selfadd(SERotate vector4f) {
            mD[0] = mD[0] + vector4f.mD[0];
            mD[1] = mD[1] + vector4f.mD[1];
            mD[2] = mD[2] + vector4f.mD[2];
            mD[3] = mD[3] + vector4f.mD[3];
            return this;
        }

        public SERotate div(float par) {
            SERotate result = new SERotate();
            result.mD[0] = mD[0] / par;
            result.mD[1] = mD[1] / par;
            result.mD[2] = mD[2] / par;
            result.mD[3] = mD[3] / par;
            return result;
        }

        public SERotate selfDiv(float par) {
            mD[0] = mD[0] / par;
            mD[1] = mD[1] / par;
            mD[2] = mD[2] / par;
            mD[3] = mD[3] / par;
            return this;
        }

        public SERotate mul(float par) {
            SERotate result = new SERotate();
            result.mD[0] = mD[0] * par;
            result.mD[1] = mD[1] * par;
            result.mD[2] = mD[2] * par;
            result.mD[3] = mD[3] * par;
            return result;
        }

        public SERotate selfMul(float par) {
            mD[0] = mD[0] * par;
            mD[1] = mD[1] * par;
            mD[2] = mD[2] * par;
            mD[3] = mD[3] * par;
            return this;
        }

        public SEQuat toQuat() {
            SEQuat quat = new SEQuat();
            quat.set(getAngle(), getAxis());
            return quat;
        }

        @Override
        public String toString() {
            return mD[0] + "," + mD[1] + "," + mD[2] + "," + mD[3];
        }
    }

    public static class SEVector4i {
        public int[] mD = { 0, 0, 0, 0 };

        public SEVector4i() {

        }

        public SEVector4i(int x, int y, int z, int w) {
            mD[0] = x;
            mD[1] = y;
            mD[2] = z;
            mD[3] = z;
        }

        public SEVector4i(int[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
            mD[3] = d[3];
        }

        public SEVector4i(SEVector4i vector4i) {
            mD[0] = vector4i.mD[0];
            mD[1] = vector4i.mD[1];
            mD[2] = vector4i.mD[2];
            mD[3] = vector4i.mD[3];
        }

        public boolean equals(SEVector4i vector4i) {
            return vector4i.mD[0] == mD[0] && vector4i.mD[1] == mD[1] && vector4i.mD[2] == mD[2]
                    && vector4i.mD[3] == mD[3];
        }
        

        @Override
        public String toString() {
            return mD[0] + "," + mD[1] + "," + mD[2] + "," + mD[3];
        }
    }
    
    public static class SEVector4f {
        public float[] mD = { 0, 0, 0, 0 };

        public SEVector4f() {

        }

        public SEVector4f(float[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
            mD[3] = d[3];
        }

        public SEVector4f(float x, float y, float z, float w) {
            mD[0] = x;
            mD[1] = y;
            mD[2] = z;
            mD[3] = z;
        }

        public SEVector4f(int[] d) {
            mD[0] = d[0];
            mD[1] = d[1];
            mD[2] = d[2];
            mD[3] = d[3];
        }

        public SEVector4f(SEVector4f vector4f) {
            mD[0] = vector4f.mD[0];
            mD[1] = vector4f.mD[1];
            mD[2] = vector4f.mD[2];
            mD[3] = vector4f.mD[3];
        }

        public boolean equals(SEVector4f vector4f) {
            return vector4f.mD[0] == mD[0] && vector4f.mD[1] == mD[1] && vector4f.mD[2] == mD[2]
                    && vector4f.mD[3] == mD[3];
        }
        
        @Override
        public String toString() {
            return mD[0] + "," + mD[1] + "," + mD[2] + "," + mD[3];
        }
    }


    public static class SERect3D {
        private float mWidth;
        private float mHeight;
        private SEVector3f mXAxis;
        private SEVector3f mYAxis;
        private SEVector3f mCenter;
        private float mMaxTexX;
        private float mMaxTexY;
        private float mMinTexX;
        private float mMinTexY;

        public SERect3D() {
            mWidth = 1;
            mHeight = 1;
            mMaxTexX = 1;
            mMaxTexY = 1;
            mMinTexX = 0;
            mMinTexY = 0;
            mXAxis = new SEVector3f(1, 0, 0);
            mYAxis = new SEVector3f(0, 0, 1);
            mCenter = new SEVector3f(0, 0, 0);
        }

        public SERect3D(float width, float height) {
            mWidth = width;
            mHeight = height;
            mMaxTexX = 1;
            mMaxTexY = 1;
            mXAxis = new SEVector3f(1, 0, 0);
            mYAxis = new SEVector3f(0, 0, 1);
            mCenter = new SEVector3f(0, 0, 0);
        }

        public SERect3D(SEVector3f xAxis, SEVector3f yAxis) {
            mWidth = 1;
            mHeight = 1;
            mMaxTexX = 1;
            mMaxTexY = 1;
            mXAxis = xAxis;
            mYAxis = yAxis;
            mCenter = new SEVector3f(0, 0, 0);
        }

        public SERect3D(SEVector3f xAxis, SEVector3f yAxis, float width, float height) {
            mWidth = width;
            mHeight = height;
            mMaxTexX = 1;
            mMaxTexY = 1;
            mXAxis = xAxis;
            mYAxis = yAxis;
            mCenter = new SEVector3f(0, 0, 0);
        }

        public void setSize(float width, float height, float scale) {
            mWidth = width * scale;
            mHeight = height * scale;
        }

        public void setSize(float x, float y, float width, float height, float scale) {
            mWidth = width * scale;
            mHeight = height * scale;
            mCenter = mXAxis.mul(x).selfAdd(mYAxis.mul(y)).selfMul(scale);
        }

        public float[] getVertexArray() {
            SEVector3f x = mXAxis.mul(mWidth / 2);
            SEVector3f y = mYAxis.mul(mHeight / 2);
            SEVector3f negX = x.mul(-1);
            SEVector3f negY = y.mul(-1);
            SEVector3f a = mCenter.add(negX.add(negY));
            SEVector3f b = mCenter.add(x.add(negY));
            SEVector3f c = mCenter.add(x.add(y));
            SEVector3f d = mCenter.add(negX.add(y));
            float[] vertexArray = new float[12];
            vertexArray[0] = a.mD[0];
            vertexArray[1] = a.mD[1];
            vertexArray[2] = a.mD[2];
            vertexArray[3] = b.mD[0];
            vertexArray[4] = b.mD[1];
            vertexArray[5] = b.mD[2];
            vertexArray[6] = c.mD[0];
            vertexArray[7] = c.mD[1];
            vertexArray[8] = c.mD[2];
            vertexArray[9] = d.mD[0];
            vertexArray[10] = d.mD[1];
            vertexArray[11] = d.mD[2];
            return vertexArray;
        }

        public int[] getFaceArray() {
            int[] face = new int[] { 0, 1, 2, 2, 3, 0 };
            return face;
        }

        public void setMaxTex(float x, float y) {
            mMaxTexX = x;
            mMaxTexY = y;
        }

        public void setMinTex(float x, float y) {
            mMinTexX = x;
            mMinTexY = y;
        }

        public float[] getTexVertexArray() {
            float[] texVertexArray = new float[] { mMinTexX, mMinTexY, mMaxTexX, mMinTexY, mMaxTexX, mMaxTexY,
                    mMinTexX, mMaxTexY };
            return texVertexArray;
        }

    }

    public static class SERay {
        private SEVector3f mLocation;
        private SEVector3f mDirection;

        public SERay(float[] data) {
            mLocation = new SEVector3f(data[0], data[1], data[2]);
            mDirection = new SEVector3f(data[3], data[4], data[5]);
        }

        public SERay(SEVector3f loc, SEVector3f dir) {
            mLocation = loc;
            mDirection = dir;
        }

        public SEVector3f getLocation() {
            return mLocation;
        }

        public SEVector3f getDirection() {
            return mDirection;
        }

        public static SEVector2f rayIntersectRay(SERay rayA, SERay rayB, AXIS face) {
            SEVector2f dirA = new SEVector2f();
            SEVector2f locA = new SEVector2f();
            SEVector2f dirB = new SEVector2f();
            SEVector2f locB = new SEVector2f();
            switch (face) {
            case X:
                dirA = rayA.getDirection().getVectorX();
                locA = rayA.getLocation().getVectorX();
                dirB = rayB.getDirection().getVectorX();
                locB = rayB.getLocation().getVectorX();
                break;
            case Y:
                dirA = rayA.getDirection().getVectorY();
                locA = rayA.getLocation().getVectorY();
                dirB = rayB.getDirection().getVectorY();
                locB = rayB.getLocation().getVectorY();
                break;
            case Z:
                dirA = rayA.getDirection().getVectorZ();
                locA = rayA.getLocation().getVectorZ();
                dirB = rayB.getDirection().getVectorZ();
                locB = rayB.getLocation().getVectorZ();
                break;
            }
            // 直线方程 y = ax + b
            float a = dirA.getY() / dirA.getX();
            float b = locA.getY() - a * locA.getX();
            // 直线方程 y = mx + n
            float m = dirB.getY() / dirB.getX();
            float n = locB.getY() - m * locB.getX();
            float intersectX = (n - b) / (a - m);
            float intersectY = a * intersectX + b;
            SEVector2f intersection = new SEVector2f(intersectX, intersectY);
            return intersection;
        }

    }

    public static class SEMatrix4f {
        public float[] mD;

        public SEMatrix4f() {
            mD = new float[16];
        }

        public void set(int row, int column, float f) {
            mD[row * 4 + column] = f;
        }
    }

}
