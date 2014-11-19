/*
-----------------------------------------------------------------------------
This source file is part of OGRE
(Object-oriented Graphics Rendering Engine)
For the latest info, see http://www.ogre3d.org/

Copyright (c) 2000-2009 Torus Knot Software Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-----------------------------------------------------------------------------
*/
/***************************************************************************
octreenode.cpp  -  description
-------------------
begin                : Fri Sep 27 2002
copyright            : (C) 2002 by Jon Anderson
email                : janders@users.sf.net

Enhancements 2003 - 2004 (C) The OGRE Team

***************************************************************************/

#include "SE_OctreeNode.h"
#include "SE_SceneManager.h"
#include "SE_Log.h"

OctreeNode::OctreeNode(OctreeSceneManager* creator)
{
    mOctant = 0;
    mCreator = creator;
    mType = HAS_AABB;
}

OctreeNode::OctreeNode(OctreeSceneManager* creator, const String& name)
{
    mName = name;
    mOctant = 0;
    mCreator = creator;
    mType = HAS_AABB;
}

const String& OctreeNode::getName(void) const
{
    return mName;
}

OctreeNode::~OctreeNode()
{
}

//same as SceneNode, only it doesn't care about children...
void OctreeNode::updateBounds(void)
{
    //update the OctreeSceneManager that things might have moved.
    // if it hasn't been added to the octree, add it, and if has moved
    // enough to leave it's current node, we'll update it.	
    mCreator->updateOctreeNode(this);
}

/** Since we are loose, only check the center.
*/
bool OctreeNode::isIn(SE_AABB &box)
{
    // Always fail if not in the scene graph or box is null
    /*if (box.isNull()) 
    {
	return false;
    }*/

    //SE_Vector3f center = mAABBBV->getGeometry().getCenter();
    SE_Vector3f center = ((SE_AABBBV*)mSpatial->getWorldBoundingVolume())->getGeometry().getCenter();
    SE_Vector3f bmin = box.getMin();
    SE_Vector3f bmax = box.getMax();

    bool centre = (bmin.x < center.x && bmin.y < center.y && bmin.z < center.z && 
            bmax.x > center.x && bmax.y > center.y && bmax.z > center.z);
    if (!centre)
    {
	return false;
    }

    // Even if covering the centre line, need to make sure this BB is not large
    // enough to require being moved up into parent. When added, bboxes would
    // end up in parent due to cascade but when updating need to deal with
    // bbox growing too large for this child
    SE_Vector3f octreeSize = bmax - bmin;
    //SE_Vector3f nodeSize = mAABBBV->getGeometry().getMax() - mAABBBV->getGeometry().getMin();
    SE_Vector3f nodeSize = ((SE_AABBBV*)mSpatial->getWorldBoundingVolume())->getGeometry().getMax() 
            - ((SE_AABBBV*)mSpatial->getWorldBoundingVolume())->getGeometry().getMin();
    return (nodeSize.x < octreeSize.x && nodeSize.y < octreeSize.y && nodeSize.z < octreeSize.z);
}

int OctreeNode::getType()
{
    return mType;
}

void OctreeNode::setType(int type)
{
    mType = (OCTREENODE_AABB)type;
}
