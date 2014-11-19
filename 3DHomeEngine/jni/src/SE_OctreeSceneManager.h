/***************************************************************************
octreescenemanager.h  -  description
-------------------
begin                : Fri Sep 27 2002
copyright            : (C) 2002 by Jon Anderson
email                : janders@users.sf.net
***************************************************************************/

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

#ifndef OCTREESCENEMANAGER_H
#define OCTREESCENEMANAGER_H
#include "SE_DynamicLibType.h"
#include <list>
#include <algorithm>
#include "SE_Octree.h"
#include "SE_Geometry3D.h"
#include "SE_Spatial.h"
#include "SE_Camera.h"
#include "SE_Time.h"

class OctreeNode;
typedef std::string String;

/** Specialized SceneManager that divides the geometry into an octree in order to facilitate spatial queries.
@remarks
*/
class OctreeSceneManager
{
public:
    static int intersect_call;
    /** Standard Constructor.  Initializes the octree to -10000,-10000,-10000 to 10000,10000,10000 with a depth of 8. */
    OctreeSceneManager(const String& name);

    /** Standard Constructor */
    OctreeSceneManager(const String& name, SE_AABB &box, int max_depth);

    /** Standard destructor */
    ~OctreeSceneManager();

    // @copydoc SceneManager::getTypeName
    const String& getTypeName(void) const;

    /** Initializes the manager to the given box and depth.
    */
    void init(SE_AABB &box, int d);

    /** Creates a specialized OctreeNode */
    OctreeNode * createSceneNodeImpl (void);

    /** Creates a specialized OctreeNode */
    OctreeNode * createSceneNodeImpl (const String &name);

    /** Creates a specialized OctreeNode */
    OctreeNode * createSceneNode(const String &name);

    /** Deletes a scene node */
    void destroySceneNode( const String &name );

    /** Recurses through the octree determining which nodes are visible. */
    void findVisibleObjects (SE_Camera * cam, SE_RenderManager& renderManager);

    /** Render the visible object */
    void renderScene(SE_Camera * cam, SE_RenderManager& renderManager);

    /** Walks through the octree, adding any visible objects to the render queue.
    @remarks
    If any octant in the octree if completely within the view frustum,
    all subchildren are automatically added with no visibility tests.
    */
    void walkOctree(SE_Camera*, Octree *,  bool foundvisible, SE_RenderManager& renderManager);

    /** Checks the given OctreeNode, and determines if it needs to be moved
    * to a different octant.
    */
    void updateOctreeNode(OctreeNode *);

    /** Removes the given octree node */
    void removeOctreeNode(OctreeNode *);

    /** Adds the Octree Node, starting at the given octree, and recursing at max to the specified depth.
    */
    void addOctreeNode(OctreeNode *, Octree *octree, int depth = 0);

    /** Recurses the octree, adding any nodes intersecting with the ray into the given list.
      It ignores the exclude scene node.
    */
    void findNodesIn( const SE_Ray &ray, std::list< OctreeNode * > &list, OctreeNode *exclude=0 );

    void setLooseOctree( bool b )
    {
        mLoose = b;
    };

    /** Resizes the octree to the given size */
    void resize( const SE_AABB &box );

    int getNum()
    {
        return mNumObjects;
    };

    void update(SE_TimeMS realDelta, SE_TimeMS simulateDelta);

    /** update the bound of the OctreeNode  */
    void updateBounds(const String& name);

    /** Retrieves a named OctreeNode from the scene graph.
    @remarks
    If you chose to name a OctreeNode as you created it, or if you
    happened to make a note of the generated name, you can look it
    up wherever it is in the scene graph using this method.
    @note Throws an exception if the named instance does not exist
    */
    OctreeNode* getSceneNode(const String& name) const;

    /** ray test and find the choose object*/
    SE_Spatial* travel(const SE_Ray &ray);

    /** setup the octree*/
    void setupOctree();

    /** Adds the Octree Node, starting at the given octree, and recursing at max to the specified depth. */
    //void addObject(const String& name);
    void addObject(SE_Spatial* spatial);

    /** delete an object */
    void deleteObject(const String& name);

    /** the list of the OctreeNode */
    typedef std::map<String, OctreeNode*> OctreeNodeList;
    OctreeNodeList mOctreeNodes;

protected:
    // The root octree
    Octree *mOctree;

    bool mLoose;

    // Number of rendered objs
    int mNumObjects;

    // Max depth for the tree
    int mMaxDepth;

    // Size of the octree
    SE_AABB mBox;

    struct CollisionResult
    {
	SE_Spatial* spatial;
	SE_SimObject* simObject;
	float distance;
	CollisionResult()
	{
	    spatial = NULL;
	    simObject = NULL;
	    distance = SE_FLT_MAX;
	}
     };
     CollisionResult mResult;
     CollisionResult mMinResult;
};

#endif

