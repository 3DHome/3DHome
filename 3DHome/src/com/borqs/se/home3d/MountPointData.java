package com.borqs.se.home3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.borqs.se.engine.SEObject;
import com.borqs.se.engine.SETransParas;
import com.borqs.se.engine.SEVector.SERotate;
import com.borqs.se.engine.SEVector.SEVector3f;

public class MountPointData {
    public enum ContainerType {
        IS_OBJECT, IS_CONTAINER
    };

    private List<SEMountPointGroup> mMountPointGroups = new ArrayList<SEMountPointGroup>();

    public List<SEMountPointGroup> getMountPointGroups() {
        return mMountPointGroups;
    }

    /**
     * @param container
     *            容器对象
     * @return 返回容器中所有可以摆放物体的点
     */
    public SEMountPointGroup getContainerMountPoint(SEObject container) {
        for (SEMountPointGroup mountPointGroup : mMountPointGroups) {
            if (mountPointGroup.mContainerType == ContainerType.IS_CONTAINER
                    && mountPointGroup.mGroupName.equals(container.getName())) {
                return mountPointGroup;
            }
        }
        return null;

    }

    /**
     * 物体摆放至容器前先检查物体是否已经配好了在该容器中应该摆放的位置，假如没有配好使用容器的默认位置
     * 
     * @param containerName
     *            容器名称
     * @return 返回物体在该容器中所有可以摆放的位置
     */
    public SEMountPointGroup getObjectMountPointInContainer(String containerName) {
        for (SEMountPointGroup mountPointGroup : mMountPointGroups) {
            if (mountPointGroup.mContainerType == ContainerType.IS_OBJECT
                    && mountPointGroup.mContainerName.equals(containerName)) {
                return mountPointGroup;
            }
        }
        return null;

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SEMountPointGroup mountPointGroup : mMountPointGroups) {
            stringBuilder.append(mountPointGroup.toString() + "\n");
        }
        return stringBuilder.toString();
    }

    public static MountPointData createfromXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        MountPointData mountPointData = new MountPointData();
        XmlUtils.nextElement(parser);
        while (true) {
            if ("mountPoints".equals(parser.getName())) {
                SEMountPointGroup mountPointGroup = SEMountPointGroup.createfromXML(parser);
                mountPointData.mMountPointGroups.add(mountPointGroup);
                XmlUtils.nextElement(parser);
            } else {
                break;
            }
        }
        return mountPointData;
    }

    public static class SEMountPointGroup {
        private String mContainerName;
        private String mGroupName;
        private ContainerType mContainerType;
        private List<SEMountPointItem> mMountPointItems = new ArrayList<SEMountPointItem>();

        public String getContainerName() {
            return mContainerName;
        }

        public ContainerType getContainerType() {
            return mContainerType;
        }

        public String getGroupName() {
            return mGroupName;
        }

        public List<SEMountPointItem> getMountPointItems() {
            return mMountPointItems;
        }

        public SEMountPointItem getMountPointItem(int slotIndex) {
            for (SEMountPointItem item : mMountPointItems) {
                if (item.getSlotIndex() == slotIndex) {
                    return item;
                }
            }
            return null;
        }

        public SEMountPointItem getMountPointItem(int startX, int startY) {
            for (SEMountPointItem item : mMountPointItems) {
                if (item.getSlotStartX() == startX && item.getSlotStartY() == startY) {
                    return item;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mContainerName :" + mContainerName + "\n");
            stringBuilder.append("mGroupName :" + mGroupName + "\n");
            stringBuilder.append("mContainerType :" + mContainerType + "\n");
            for (SEMountPointItem mountPointItem : mMountPointItems) {
                stringBuilder.append(mountPointItem.toString() + "\n");
            }
            return stringBuilder.toString();
        }

        public static SEMountPointGroup createfromXML(XmlPullParser parser) throws XmlPullParserException, IOException {
            SEMountPointGroup mountPointGroup = new SEMountPointGroup();
            mountPointGroup.mContainerName = parser.getAttributeValue(null, "containerName");
            mountPointGroup.mGroupName = parser.getAttributeValue(null, "groupName");
            String containerTypeStr = parser.getAttributeValue(null, "containerType");
            int containerType = Integer.parseInt(containerTypeStr);
            if (containerType == 0) {
                mountPointGroup.mContainerType = ContainerType.IS_OBJECT;
            } else if (containerType == 1) {
                mountPointGroup.mContainerType = ContainerType.IS_CONTAINER;
            }
            XmlUtils.nextElement(parser);
            while (true) {
                if ("mountPointData".equals(parser.getName())) {
                    SEMountPointItem mountPointItem = SEMountPointItem.createfromXML(parser);
                    mountPointGroup.mMountPointItems.add(mountPointItem);
                    XmlUtils.nextElement(parser);
                } else {
                    break;
                }
            }
            return mountPointGroup;
        }
    }

    public static class SEMountPointItem {
        private SEVector3f mTranslate;
        private SEVector3f mScale;
        private SERotate mRotate;
        private String mName;
        private SEVector3f mMinPoint;
        private SEVector3f mMaxPoint;
        
        private int mSlotIndex;
        private int mStartX;
        private int mStartY;

        public SEMountPointItem(String name, SEVector3f t, SEVector3f s, SERotate r) {
            mTranslate = t;
            mScale = s;
            mRotate = r;
            mName = name;            
            String strSlot = mName.substring(mName.length() - 2, mName.length());
            mSlotIndex = Integer.parseInt(strSlot);
            mStartY = mSlotIndex % 10;
            mStartX = mSlotIndex - mStartY;
        }

        public SEMountPointItem() {

        }

        public String getName() {            
            return mName;
        }

        public void setName(String name) {
            String strSlot = mName.substring(mName.length() - 2, mName.length());
            mSlotIndex = Integer.parseInt(strSlot);
            mStartY = mSlotIndex % 10;
            mStartX = mSlotIndex - mStartY;
            mName = name;
        }

        public void setTranslate(SEVector3f t) {
            mTranslate = t;
        }

        public void setRotate(SERotate r) {
            mRotate = r;
        }

        public void setScale(SEVector3f s) {
            mScale = s;
        }

        public SEVector3f getTranslate() {
            return mTranslate;
        }

        public SEVector3f getScale() {
            return mScale;
        }

        public SERotate getRotate() {
            return mRotate;
        }

        public SETransParas getTransParas() {
            SETransParas transParas = new SETransParas();
            transParas.mTranslate = mTranslate.clone();
            transParas.mScale = mScale.clone();
            transParas.mRotate = mRotate.clone();
            return transParas;

        }

        public int getSlotIndex() {
            return mSlotIndex;
        }

        public int getSlotStartX() {
            return mStartX;
        }

        public int getSlotStartY() {
            return mStartY;
        }

        @Override
        public String toString() {
            return "mName :" + mName + ", mTranslate :" + mTranslate + ", mScale :" + mScale + ", mRotate:" + mRotate;
        }
        public static SEMountPointItem createfromXML(XmlPullParser parser) {
            String name = parser.getAttributeValue(null, "mountPointName");
            String tx = parser.getAttributeValue(null, "translatex");
            String ty = parser.getAttributeValue(null, "translatey");
            String tz = parser.getAttributeValue(null, "translatez");
            String rx = parser.getAttributeValue(null, "rotatex");
            String ry = parser.getAttributeValue(null, "rotatey");
            String rz = parser.getAttributeValue(null, "rotatez");
            String ra = parser.getAttributeValue(null, "rotateAngle");
            String sx = parser.getAttributeValue(null, "scalex");
            String sy = parser.getAttributeValue(null, "scaley");
            String sz = parser.getAttributeValue(null, "scalez");
            SEVector3f translate = new SEVector3f(Float.parseFloat(tx), Float.parseFloat(ty), Float.parseFloat(tz));
            SEVector3f scale = new SEVector3f(Float.parseFloat(sx), Float.parseFloat(sy), Float.parseFloat(sz));
            SERotate rotate = new SERotate(Float.parseFloat(ra), Float.parseFloat(rx), Float.parseFloat(ry),
                    Float.parseFloat(rz));
            SEMountPointItem mpd = new SEMountPointItem(name, translate, scale, rotate);
            return mpd;
        }
    }

}
