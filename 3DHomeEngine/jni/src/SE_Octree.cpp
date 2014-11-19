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
octree.cpp  -  description
-------------------
begin                : Mon Sep 30 2002
copyright            : (C) 2002 by Jon Anderson
email                : janders@users.sf.net

Enhancements 2003 - 2004 (C) The OGRE Team

***************************************************************************/

#include "SE_Octree.h"
#include "SE_OctreeNode.h"
#include "SE_Log.h"

/** Returns true is the box will fit in a child.
*/
bool Octree::isTwiceSize(const SE_AABB &box) const
{
    SE_Vector3f halfMBoxSize = (mBox.getMax() - mBox.getMin()) * 0.5f;
    SE_Vector3f boxSize = box.getMax() - box.getMin();

    return ((boxSize.x <= halfMBoxSize.x) && (boxSize.y <= halfMBoxSize.y) && (boxSize.z <= halfMBoxSize.z));
}

/** It's assumed the the given box has already been proven to fit into
* a child.  Since it's a loose octree, only the centers need to be
* compared to find the appropriate node.
*/
void Octree::getChildIndexes(const SE_AABB &box, int *x, int *y, int *z) const
{
    SE_Vector3f center = mBox.getCenter();
    SE_Vector3f ncenter = box.getCenter();

    if (ncenter.x > center.x)
    {
        *x = 1;
    }
    else
    {
        *x = 0;
    }

    if (ncenter.y > center.y)
    {
        *y = 1;
    }
    else
    {
        *y = 0;
    }

    if (ncenter.z > center.z)
    {
        *z = 1;
    }
    else
    {
        *z = 0;
    }
}

Octree::Octree(Octree * parent) 
    :mHalfSize(0, 0, 0)
{
    //initialize all children to null.
    for (int i = 0; i < 2; i++)
    {
        for (int j = 0; j < 2; j++)
        {
            for (int k = 0; k < 2; k++)
            {
                mChildren[ i ][ j ][ k ] = 0;
            }
        }
    }

    mParent = parent;
    mNumNodes = 0;
    id = 0;
}

Octree::~Octree()
{
    //initialize all children to null.
    for (int i = 0; i < 2; i++)
    {
        for (int j = 0; j < 2; j++)
        {
            for (int k = 0; k < 2; k++)
            {
                if (mChildren[ i ][ j ][ k ] != 0)
		{
                    delete mChildren[ i ][ j ][ k ];
		}
            }
        }
    }

    /*NodeList::iterator it;
    for (it = mNodes.begin(); it != mNodes.end(); ++it)
    {
        delete *it;
    }*/

    mParent = 0;
}

void Octree::addNode(OctreeNode * n)
{
    mNodes.push_back(n);
    n->setOctant(this);
   
    //update total counts.
    ref();
}

void Octree::removeNode(OctreeNode * n)
{
    mNodes.erase(std::find(mNodes.begin(), mNodes.end(), n));
    n->setOctant(0);

    //update total counts.
    unref();
}

void Octree::getCullBounds( SE_AABB *b ) const
{
    b->set(mBox.getMin() - mHalfSize, mBox.getMax() + mHalfSize);
}

void Octree::getCullBounds(SE_AABBBV *b) const
{
    //the cullbounds of octree shoud be double of the box of the octree, so it can guarantee the 
    //object be culled in the octree 
    SE_AABBBV* bv = new SE_AABBBV(SE_AABB(mBox.getMin() - mHalfSize , mBox.getMax() + mHalfSize));
    b->merge((SE_BoundingVolume*)(bv));
    delete bv;
}


