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
octree.h  -  description
-------------------
begin                : Mon Sep 30 2002
copyright            : (C) 2002 by Jon Anderson
email                : janders@users.sf.net

Enhancements 2003 - 2004 (C) The OGRE Team
***************************************************************************/

#ifndef OCTREE_H
#define OCTREE_H
#include "SE_DynamicLibType.h"
#include <list>
#include "SE_BoundingVolume.h"
#include "SE_Geometry3D.h"

class OctreeNode;

/** Octree datastructure for managing scene nodes.
@remarks
This is a loose octree implementation, meaning that each
octant child of the octree actually overlaps it's siblings by a factor
of .5.  This guarantees that any thing that is half the size of the parent will
fit completely into a child, with no splitting necessary.
*/
class Octree 
{
public:
    Octree(Octree * p);
    ~Octree();

    /** Adds an Octree scene node to this octree level.
    @remarks
    This is called by the OctreeSceneManager after
    it has determined the correct Octree to insert the node into.
    */
    void addNode(OctreeNode *);

    /** Removes an Octree scene node to this octree level.
    */
    void removeNode(OctreeNode *);

    /** Returns the number of scene nodes attached to this octree
    */
    int numNodes()
    {
        return mNumNodes;
    };

    /** The bounding box of the octree
    @remarks
    This is used for octant index determination and rendering, but not culling
    */
    SE_AABB mBox;

    /** Vector containing the dimensions of this octree / 2
    */
    SE_Vector3f mHalfSize;

    /** 3D array of children of this octree.
    @remarks
    Children are dynamically created as needed when nodes are inserted in the Octree.
    If, later, all the nodes are removed from the child, it is still kept around.
    */
    Octree * mChildren[ 2 ][ 2 ][ 2 ];

    /** Determines if this octree is twice as big as the given box.
    @remarks
    This method is used by the OctreeSceneManager to determine if the given
    box will fit into a child of this octree.
    */
    bool isTwiceSize(const SE_AABB &box) const;

    /**  Returns the appropriate indexes for the child of this octree into which the box will fit.
    @remarks
    This is used by the OctreeSceneManager to determine which child to traverse next when
    finding the appropriate octree to insert the box.  Since it is a loose octree, only the
    center of the box is checked to determine the octant.
    */
    void getChildIndexes(const SE_AABB &, int *x, int *y, int *z) const;

    /** Creates the AxisAlignedBox used for culling this octree.
    @remarks
    Since it's a loose octree, the culling bounds can be different than the actual bounds of the octree.
    */
    void getCullBounds(SE_AABB *) const;

    /** Creates the AxisAlignedBox used for culling this octree.
    @remarks
    Since it's a loose octree, the culling bounds can be different than the actual bounds of the octree.
    */
    void getCullBounds(SE_AABBBV *) const;
   

    typedef std::list<OctreeNode *> NodeList;

    /** Public list of SceneNodes attached to this particular octree
    */
    NodeList mNodes;

    int id;

protected:
    /** Increments the overall node count of this octree and all its parents
    */
    inline void ref()
    {
        mNumNodes++;

	if (mParent != 0) 
	{
	    mParent->ref();
	}
    };

    /** Decrements the overall node count of this octree and all its parents
    */
    inline void unref()
    {
        mNumNodes--;

	if (mParent != 0) 
	{
	    mParent->unref();
	}
    };

    //number of SceneNodes in this octree and all its children.
    int mNumNodes;

    //parent octree
    Octree * mParent;
};

#endif


