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
octreenode.h  -  description
-------------------
begin                : Fri Sep 27 2002
copyright            : (C) 2002 by Jon Anderson
email                : janders@users.sf.net

Enhancements 2003 - 2004 (C) The OGRE Team
***************************************************************************/

#ifndef OCTREENODE_H
#define OCTREENODE_H
#include "SE_DynamicLibType.h"
#include <string>
#include "SE_Geometry3D.h"
#include "SE_Octree.h"
#include "SE_OctreeSceneManager.h"
#include "SE_Spatial.h"

typedef std::string String;

/** Specialized SceneNode that is customized for working within an Octree. Each node
* maintains its own bounding box, rather than merging it with all the children.
*
*/
class OctreeNode
{
public:
    enum OCTREENODE_AABB {NO_AABB, HAS_AABB};
    /** Standard constructor */
    OctreeNode(OctreeSceneManager* creator);

    /** Standard constructor */
    OctreeNode(OctreeSceneManager* creator, const String& name);

    /** Standard destructor */
    ~OctreeNode();

    /** Returns the Octree in which this OctreeNode resides
    */
    Octree * getOctant()
    {
        return mOctant;
    };

    /** Sets the Octree in which this OctreeNode resides
    */
    void setOctant(Octree *o)
    {
        mOctant = o;
    };

    /** Determines if the center of this node is within the given box
    */
    bool isIn(SE_AABB &box);

    /** Set the spatial of this OctreeNode.
    */
    void setSpatial(SE_Spatial* spatial) 
    {
        mSpatial =  spatial;
    }

    /** Returns spatial of this OctreeNode.
    */
    SE_Spatial* getSpatial()
    {
        return mSpatial;
    }

    /** Returns the bounding box of this OctreeNode.
    */
    /*SE_AABBBV* getAABBBV()
    {
        return mAABBBV;
    };*/

    /** Set the bounding box of this OctreeNode.
    */
    /*void setAABBBV(SE_AABBBV* aabb)
    {
        mAABBBV = aabb;
    };*/

    /** Internal method for updating the bounds for this OctreeNode.
    @remarks
    This method determines the bounds solely from the attached objects, not
    any children. If the node has changed its bounds, it is removed from its
    current octree, and reinserted into the tree.
    */
    void updateBounds( void );

    /** Returns the name of the node. */
    const String& getName(void) const;

    /** Returns the type of this OctreeNode
    */
    int getType();

    /** Set the type of this OctreeNode.
    */
    void setType(int type); 

protected:

    //local bounding box
    //SE_AABBBV* mAABBBV;

    //OctreeSceneManager which created this node
    OctreeSceneManager* mCreator;

    //Octree this node is attached to.
    Octree *mOctant;

    //Spatial attach to this node
    SE_Spatial *mSpatial;

    // Friendly name of this node, can be automatically generated if you don't care
    String mName;

    // OctreeNode has boundingvolume
    OCTREENODE_AABB mType;
};

#endif
