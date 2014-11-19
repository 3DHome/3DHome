package com.borqs.se.engine;

import com.borqs.se.engine.SEObject.IMAGE_TYPE;
import com.borqs.se.engine.SEVector.SERect3D;
import com.borqs.se.engine.SEVector.SEVector3f;

import android.graphics.Bitmap;


public class SEObjectFactory {

    public static void createRectangle(SEObject obj, SERect3D rect, IMAGE_TYPE imageType, String imageName,
            String imagePath, Bitmap bitmap, float[] color, boolean blendingable) {
        obj.setVertexArray(rect.getVertexArray());
        obj.setFaceArray(rect.getFaceArray());
        obj.setTexVertexArray(rect.getTexVertexArray());
        obj.setImage(imageType, imageName, imagePath);
        obj.setBitmap(bitmap);
        obj.setColor(color);
        obj.setBlendingable(blendingable);
        if (blendingable) {
            obj.setNeedForeverBlend(true);
        }
    }

    public static void createRectangleAtScreen(SECamera camera, SEObject obj, IMAGE_TYPE imageType, String imageName,
            String imagePath, Bitmap bitmap, float[] color, float scale, boolean blendingable) {
        SEVector3f xAxis = camera.getAxisX();
        SEVector3f yAxis = camera.getAxisY();
        SERect3D rect = new SERect3D(xAxis, yAxis);
        rect.setSize(camera.getWidth(), camera.getHeight(), scale);
        createRectangle(obj, rect, imageType, imageName, imagePath, bitmap, color, blendingable);
        SETransParas paras = new SETransParas();
        paras.mTranslate = camera.getScreenLocation(scale);
        obj.setLocalTransParas(paras);
    }

//    public static void createRectLine(SEObject line, SERect3D rect) {
//        SEObjectData data = new SEObjectData(line.getName());
//        int lineNumber = 4;
//        float[] vertexArray = rect.getVertexArray();
//        int[] faceArray = new int[lineNumber * 2];
//        for (int i = 0; i < lineNumber; i++) {
//            faceArray[2 * i] = i;
//            faceArray[2 * i + 1] = i + 1;
//        }
//        data.setVertexArray(vertexArray);
//        data.setFaceArray(faceArray);
//        data.setColor(new float[] { 1, 0, 0 });
//        data.setObjectType(SEObjectData.OBJECT_TYPE_LINE);
//        data.setImage(IMAGE_TYPE.COLOR, null, null);
//        data.setLineWidth(5);
//        line.setObjectData(data);
//    }

    public static void createRectLine(SEObject line, SERect3D rect,float[] color) {
        int lineNumber = 4;
        float[] vertexArray = rect.getVertexArray();
        float[] faceVertexArray = new float[24];
        faceVertexArray[0] = vertexArray[0];
        faceVertexArray[1] = vertexArray[1];
        faceVertexArray[2] = vertexArray[2];
        faceVertexArray[3] = vertexArray[3];
        faceVertexArray[4] = vertexArray[4];
        faceVertexArray[5] = vertexArray[5];

        faceVertexArray[6] = vertexArray[3];
        faceVertexArray[7] = vertexArray[4];
        faceVertexArray[8] = vertexArray[5];
        faceVertexArray[9] = vertexArray[6];
        faceVertexArray[10] = vertexArray[7];
        faceVertexArray[11] = vertexArray[8];

        faceVertexArray[12] = vertexArray[6];
        faceVertexArray[13] = vertexArray[7];
        faceVertexArray[14] = vertexArray[8];
        faceVertexArray[15] = vertexArray[9];
        faceVertexArray[16] = vertexArray[10];
        faceVertexArray[17] = vertexArray[11];

        faceVertexArray[18] = vertexArray[9];
        faceVertexArray[19] = vertexArray[10];
        faceVertexArray[20] = vertexArray[11];
        faceVertexArray[21] = vertexArray[0];
        faceVertexArray[22] = vertexArray[1];
        faceVertexArray[23] = vertexArray[2];
        int[] faceArray = new int[lineNumber * 2];
        for (int i = 0; i < lineNumber * 2; i++) {
            faceArray[i] = i;
        }
        line.setVertexArray(faceVertexArray);
        line.setFaceArray(faceArray);
        if (color == null) {
            color = new float[] { 1, 0, 0 };
        }
        line.setColor(color);
        line.setIsLine(true);
        line.setImage(IMAGE_TYPE.COLOR, null, null);
        line.setLineWidth(5);
    }

    public static void createCylinder(SEObject obj, int faceNum, float r, float h, float[] color) {
        createCylinder(obj, faceNum, r, h, IMAGE_TYPE.COLOR, null, null, null, color);
    }

    public static void createCylinder(SEObject obj, int faceNum, float r, float h, String imageName, String imagePath) {
        createCylinder(obj, faceNum, r, h, IMAGE_TYPE.IMAGE, imageName, imagePath, null, null);
    }

    public static void createCylinder(SEObject obj, int faceNum, float r, float h, String imageName, String imageKey,
            Bitmap bitmap) {
        createCylinder(obj, faceNum, r, h, IMAGE_TYPE.BITMAP, imageName, imageKey, bitmap, null);
    }

    public static void createCylinderII(SEObject obj, int faceNum, float r, float h, String imageName, String imageKey,
            Bitmap bitmap) {
        createCylinderII(obj, faceNum, r, h, IMAGE_TYPE.BITMAP, imageName, imageKey, bitmap, null);
    }

    private static void createCylinder(SEObject obj, int faceNum, float r, float h, IMAGE_TYPE imageType,
            String imageName, String imageKey, Bitmap bitmap, float[] color) {
        double perAngle = 2 * Math.PI / (faceNum - 1);
        float[] vertexArray = new float[faceNum * 2 * 3];
        for (int i = 0; i < faceNum; i++) {
            double angle = perAngle * i;
            vertexArray[3 * i] = (float) (r * Math.cos(angle));
            vertexArray[3 * i + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * i + 2] = -h / 2;

            vertexArray[3 * (faceNum + i)] = (float) (r * Math.cos(angle));
            vertexArray[3 * (faceNum + i) + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * (faceNum + i) + 2] = h / 2;
        }
        obj.setVertexArray(vertexArray);
        int[] faceArray = new int[(faceNum - 1) * 2 * 3];
        for (int i = 0; i < faceNum - 1; i++) {
            faceArray[6 * i] = i;
            faceArray[6 * i + 1] = i + 1;
            faceArray[6 * i + 2] = i + 1 + faceNum;

            faceArray[6 * i + 3] = i + 1 + faceNum;
            faceArray[6 * i + 4] = i + faceNum;
            faceArray[6 * i + 5] = i;
        }
        obj.setFaceArray(faceArray);
        float[] texVertexArray = new float[faceNum * 2 * 2];
        float perTex = 1.0f / (faceNum - 1);
        for (int i = 0; i < faceNum; i++) {
            float tex = perTex * i;
            texVertexArray[i * 2] = tex;
            texVertexArray[i * 2 + 1] = 0;

            texVertexArray[(faceNum + i) * 2] = tex;
            texVertexArray[(faceNum + i) * 2 + 1] = 1;
        }
        obj.setTexVertexArray(texVertexArray);
        obj.setImage(imageType, imageName, imageKey);
        obj.setBitmap(bitmap);
        obj.setColor(color);
    }

    private static void createCylinderII(SEObject obj, int faceNum, float r, float h, IMAGE_TYPE imageType,
            String imageName, String imageKey, Bitmap bitmap, float[] color) {
        double perAngle = 2 * Math.PI / (faceNum - 1);
        float[] vertexArray = new float[faceNum * 2 * 3];
        for (int i = 0; i < faceNum; i++) {
            double angle = perAngle * i;
            vertexArray[3 * i] = (float) (r * Math.cos(angle));
            vertexArray[3 * i + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * i + 2] = -h / 2;

            vertexArray[3 * (faceNum + i)] = (float) (r * Math.cos(angle));
            vertexArray[3 * (faceNum + i) + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * (faceNum + i) + 2] = h / 2;
        }
        obj.setVertexArray(vertexArray);
        int[] faceArray = new int[(faceNum - 1) * 2 * 3];
        for (int i = 0; i < faceNum - 1; i++) {
            faceArray[6 * i] = i;
            faceArray[6 * i + 1] = i + 1;
            faceArray[6 * i + 2] = i + 1 + faceNum;

            faceArray[6 * i + 3] = i + 1 + faceNum;
            faceArray[6 * i + 4] = i + faceNum;
            faceArray[6 * i + 5] = i;
        }
        obj.setFaceArray(faceArray);
        float[] texVertexArray = new float[faceNum * 2 * 2];
        float perTex = 0.25f / (faceNum - 1);
        for (int i = 0; i < faceNum; i++) {
            float tex = perTex * i;
            texVertexArray[i * 2] = 0;
            texVertexArray[i * 2 + 1] = 0.75f + tex;

            texVertexArray[(faceNum + i) * 2] = 1;
            texVertexArray[(faceNum + i) * 2 + 1] = 0.75f + tex;
        }
        obj.setTexVertexArray(texVertexArray);
        obj.setImage(imageType, imageName, imageKey);
        obj.setBitmap(bitmap);
        obj.setColor(color);
    }

    public static void createRibbon(SEObject obj, int length, float r, float h, float step, float[] color) {
        createRibbon(obj, r, h, length, step, IMAGE_TYPE.COLOR, null, null, null, color);
    }

    public static void createRibbon(SEObject obj, int length, float r, float h, float step, String imageKey,
            String imageName, String imagePath) {
        createRibbon(obj, r, h, length, step, IMAGE_TYPE.IMAGE, imageName, imagePath, null, null);
    }

    public static void createRibbon(SEObject obj, int length, float r, float h, float step, String imageName,
            String imageKey, Bitmap bitmap) {
        createRibbon(obj, r, h, length, step, IMAGE_TYPE.BITMAP, imageName, imageKey, bitmap, null);
    }

    private static void createRibbon(SEObject obj, float r, float h, int length, float step, IMAGE_TYPE imageType,
            String imageName, String imageKey, Bitmap bitmap, float[] color) {
        double perAngle = Math.PI / 180;
        float[] vertexArray = new float[length * 2 * 3];
        for (int i = 0; i < length; i++) {
            double angle = perAngle * i;
            vertexArray[3 * i] = (float) (r * Math.cos(angle));
            vertexArray[3 * i + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * i + 2] = -h / 2 - i * step;

            vertexArray[3 * (length + i)] = (float) (r * Math.cos(angle));
            vertexArray[3 * (length + i) + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * (length + i) + 2] = h / 2 - i * step;
        }
        obj.setVertexArray(vertexArray);
        int[] faceArray = new int[(length - 1) * 2 * 3];
        for (int i = 0; i < length - 1; i++) {
            faceArray[6 * i] = i;
            faceArray[6 * i + 1] = i + 1;
            faceArray[6 * i + 2] = i + 1 + length;

            faceArray[6 * i + 3] = i + 1 + length;
            faceArray[6 * i + 4] = i + length;
            faceArray[6 * i + 5] = i;
        }
        obj.setFaceArray(faceArray);
        float[] texVertexArray = new float[length * 2 * 2];
        float perTex = 1.0f / (length - 1);
        for (int i = 0; i < length; i++) {
            float tex = perTex * i;
            texVertexArray[i * 2] = tex;
            texVertexArray[i * 2 + 1] = 0;

            texVertexArray[(length + i) * 2] = tex;
            texVertexArray[(length + i) * 2 + 1] = 1;
        }
        obj.setTexVertexArray(texVertexArray);
        obj.setImage(imageType, imageName, imageKey);
        obj.setBitmap(bitmap);
        obj.setColor(color);
    }

    public static void createTest(SEObject obj, float r, float[] color) {
        float[] vertexArray = new float[13 * 3];
        vertexArray[0] = 0;
        vertexArray[1] = 0;
        vertexArray[2] = 20;
        for (int i = 1; i < 13; i++) {
            double angle = (i - 1) * Math.PI / 6 + Math.PI / 12;
            vertexArray[3 * i] = (float) (r * Math.cos(angle));
            vertexArray[3 * i + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * i + 2] = 20;
        }
        obj.setVertexArray(vertexArray);
        int[] faceArray = new int[6 * 3];
        for (int i = 0; i < 6; i++) {
            faceArray[3 * i] = 0;
            faceArray[3 * i + 1] = 2 * i + 1;
            faceArray[3 * i + 2] = 2 * i + 2;
        }
        obj.setFaceArray(faceArray);
        obj.setColor(color);
    }

    public static void createCircle(SEObject obj, int faceNum, float r, String imageName, String imagePath) {
        createCircle(obj, faceNum, r, IMAGE_TYPE.IMAGE, imageName, imagePath, null);
    }

    public static void createCircle(SEObject obj, int faceNum, float r, String imageName, String imageKey,
            Bitmap bitmap) {
        createCircle(obj, faceNum, r, IMAGE_TYPE.BITMAP, imageName, imageKey, bitmap);
    }

    private static void createCircle(SEObject obj, int faceNum, float r, IMAGE_TYPE imageType, String imageName,
            String imageKey, Bitmap bitmap) {
        double perAngle = 2 * Math.PI / (faceNum - 1);
        float[] vertexArray = new float[faceNum * 3];
        for (int i = 0; i < faceNum; i++) {
            double angle = perAngle * i;
            if (i == faceNum - 1) {
                vertexArray[3 * i] = 0;
                vertexArray[3 * i + 1] = 0;
                vertexArray[3 * i + 2] = 0;
                break;
            }
            vertexArray[3 * i] = (float) (r * Math.cos(angle));
            vertexArray[3 * i + 1] = (float) (r * Math.sin(angle));
            vertexArray[3 * i + 2] = 0;
        }
        obj.setVertexArray(vertexArray);
        int[] faceArray = new int[(faceNum - 1) * 3];
        for (int i = 0; i < faceNum - 1; i++) {
            faceArray[3 * i] = i;
            faceArray[3 * i + 1] = i + 1;
            faceArray[3 * i + 2] = faceNum - 1;
        }
        obj.setFaceArray(faceArray);
        float[] texVertexArray = new float[faceNum * 2];
        for (int i = 0; i < faceNum; i++) {
            double angle = perAngle * i;
            if (i == faceNum - 1) {
                texVertexArray[2 * i] = 0.5f;
                texVertexArray[2 * i + 1] = 0.5f;
                break;
            }

            texVertexArray[2 * i] = (float) (0.5f * Math.cos(angle)) + 0.5f;
            texVertexArray[2 * i + 1] = (float) (0.5f * Math.sin(angle)) + 0.5f;
        }
        obj.setTexVertexArray(texVertexArray);
        obj.setImage(imageType, imageName, imageKey);
        obj.setBitmap(bitmap);
    }

    public static void createUnitSphere(SEObject obj, int spanXY, int spanZ, float r, String imageName, String imagePath) {
        createUnitSphere(obj, spanXY, spanZ, r, IMAGE_TYPE.IMAGE, imageName, imagePath, null);
    }

    public static void createUnitSphere(SEObject obj, int spanXY, int spanZ, float r, String imageName,
            String imageKey, Bitmap bitmap) {
        createUnitSphere(obj, spanXY, spanZ, r, IMAGE_TYPE.BITMAP, imageName, imageKey, bitmap);
    }

    private static void createUnitSphere(SEObject obj, int spanXY, int spanZ, float r, IMAGE_TYPE imageType,
            String imageName, String imageKey, Bitmap bitmap) {
        double DTOR = Math.PI / 180;
        int faceSizeXY = 360 / spanXY;
        int faceSizeZ = 180 / spanZ;
        int faceSize = faceSizeXY * faceSizeZ;
        float[] vertexArray = new float[faceSize * 12];
        float[] texVertexArray = new float[faceSize * 8];
        int[] faceArray = new int[faceSize * 6];
        for (int z = 0; z < faceSizeZ; z++) {
            for (int xy = 0; xy < faceSizeXY; xy++) {
                int n = 12 * (z * faceSizeXY + xy);
                double theta = -90 + z * spanZ;
                double phi = xy * spanXY;
                vertexArray[n] = (float) (r * Math.cos(theta * DTOR) * Math.cos(phi * DTOR));
                vertexArray[n + 1] = (float) (r * Math.cos(theta * DTOR) * Math.sin(phi * DTOR));
                vertexArray[n + 2] = (float) (r * Math.sin(theta * DTOR));

                vertexArray[n + 3] = (float) (r * Math.cos(theta * DTOR) * Math.cos((phi + spanXY) * DTOR));
                vertexArray[n + 4] = (float) (r * Math.cos(theta * DTOR) * Math.sin((phi + spanXY) * DTOR));
                vertexArray[n + 5] = (float) (r * Math.sin(theta * DTOR));

                vertexArray[n + 6] = (float) (r * Math.cos((theta + spanZ) * DTOR) * Math.cos((phi + spanXY) * DTOR));
                vertexArray[n + 7] = (float) (r * Math.cos((theta + spanZ) * DTOR) * Math.sin((phi + spanXY) * DTOR));
                vertexArray[n + 8] = (float) (r * Math.sin((theta + spanZ) * DTOR));

                vertexArray[n + 9] = (float) (r * Math.cos((theta + spanZ) * DTOR) * Math.cos(phi * DTOR));
                vertexArray[n + 10] = (float) (r * Math.cos((theta + spanZ) * DTOR) * Math.sin(phi * DTOR));
                vertexArray[n + 11] = (float) (r * Math.sin((theta + spanZ) * DTOR));

                n = 8 * (z * faceSizeXY + xy);
                // texVertexArray[n] = (float) xy / faceSizeXY;
                // texVertexArray[n + 1] = (float) z / faceSizeZ;
                //
                // texVertexArray[n + 2] = (float) (xy + 1) / faceSizeXY;
                // texVertexArray[n + 3] = (float) z / faceSizeZ;
                //
                // texVertexArray[n + 4] = (float) (xy + 1) / faceSizeXY;
                // texVertexArray[n + 5] = (float) (z + 1) / faceSizeZ;
                //
                // texVertexArray[n + 6] = (float) xy / faceSizeXY;
                // texVertexArray[n + 7] = (float) (z + 1) / faceSizeZ;
                if (z >= faceSizeZ / 2) {
                    texVertexArray[n] = (float) (0.5 + Math.cos(theta * DTOR) * Math.cos(phi * DTOR) * 0.28);
                    texVertexArray[n + 1] = (float) (0.5 + Math.cos(theta * DTOR) * Math.sin(phi * DTOR) * 0.28);

                    texVertexArray[n + 2] = (float) (0.5 + Math.cos(theta * DTOR) * Math.cos((phi + spanXY) * DTOR)
                            * 0.28);
                    texVertexArray[n + 3] = (float) (0.5 + Math.cos(theta * DTOR) * Math.sin((phi + spanXY) * DTOR)
                            * 0.28);

                    texVertexArray[n + 4] = (float) (0.5 + Math.cos((theta + spanZ) * DTOR)
                            * Math.cos((phi + spanXY) * DTOR) * 0.28);
                    texVertexArray[n + 5] = (float) (0.5 + Math.cos((theta + spanZ) * DTOR)
                            * Math.sin((phi + spanXY) * DTOR) * 0.28);

                    texVertexArray[n + 6] = (float) (0.5 + Math.cos((theta + spanZ) * DTOR) * Math.cos(phi * DTOR)
                            * 0.28);
                    texVertexArray[n + 7] = (float) (0.5 + Math.cos((theta + spanZ) * DTOR) * Math.sin(phi * DTOR)
                            * 0.28);
                } else {
                    texVertexArray[n] = (float) (0.5 - Math.cos(theta * DTOR) * Math.cos(phi * DTOR) * 0.28);
                    texVertexArray[n + 1] = (float) (0.5 + Math.cos(theta * DTOR) * Math.sin(phi * DTOR) * 0.28);

                    texVertexArray[n + 2] = (float) (0.5 - Math.cos(theta * DTOR) * Math.cos((phi + spanXY) * DTOR)
                            * 0.28);
                    texVertexArray[n + 3] = (float) (0.5 + Math.cos(theta * DTOR) * Math.sin((phi + spanXY) * DTOR)
                            * 0.28);

                    texVertexArray[n + 4] = (float) (0.5 - Math.cos((theta + spanZ) * DTOR)
                            * Math.cos((phi + spanXY) * DTOR) * 0.28);
                    texVertexArray[n + 5] = (float) (0.5 + Math.cos((theta + spanZ) * DTOR)
                            * Math.sin((phi + spanXY) * DTOR) * 0.28);

                    texVertexArray[n + 6] = (float) (0.5 - Math.cos((theta + spanZ) * DTOR) * Math.cos(phi * DTOR)
                            * 0.28);
                    texVertexArray[n + 7] = (float) (0.5 + Math.cos((theta + spanZ) * DTOR) * Math.sin(phi * DTOR)
                            * 0.28);
                }

                n = 6 * (z * faceSizeXY + xy);
                int index = 4 * (z * faceSizeXY + xy);
                faceArray[n] = index;
                faceArray[n + 1] = index + 1;
                faceArray[n + 2] = index + 2;
                faceArray[n + 3] = index + 2;
                faceArray[n + 4] = index + 3;
                faceArray[n + 5] = index;
            }
        }
        obj.setVertexArray(vertexArray);
        obj.setFaceArray(faceArray);
        obj.setTexVertexArray(texVertexArray);
        obj.setImage(imageType, imageName, imageKey);
        obj.setBitmap(bitmap);
    }
}
