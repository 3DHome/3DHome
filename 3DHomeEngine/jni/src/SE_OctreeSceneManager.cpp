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
octreescenemanager.cpp  -  description
-------------------
begin                : Fri Sep 27 2002
copyright            : (C) 2002 by Jon Anderson
email                : janders@users.sf.net
 
Enhancements 2003 - 2004 (C) The OGRE Team
 
***************************************************************************/

#include "SE_OctreeSceneManager.h"
#include "SE_OctreeNode.h"
#include "SE_RenderManager.h"

#include "SE_MeshSimObject.h"
#include "SE_Application.h"
#include "SE_BoundingVolume.h"
#include "SE_SceneManager.h"
#include "SE_GeometryData.h"
#include "SE_Mesh.h"
#include "SE_MemLeakDetector.h"
#include "SE_Log.h"

typedef std::string String;
enum Intersection
{
    OUTSIDE = 0,
    INSIDE = 1,
    INTERSECT = 2
};

/** Checks how the box intersects with the ray.
*/
Intersection intersect(const SE_Ray &one, const SE_AABB &two)
{
    // Null box?
    if (two.getExtent().length() == 0) 
    {
        return OUTSIDE;
    }

    bool inside = true;
    const SE_Vector3f& twoMin = two.getMin();
    const SE_Vector3f& twoMax = two.getMax();

    SE_Vector3f origin = one.getOrigin();
    SE_Vector3f dir = one.getDirection();

    SE_Vector3f maxT(-1, -1, -1);

    int i = 0;
    for (i = 0; i < 3; i++)
    {
        if (origin.d[i] < twoMin.d[i])
        {
            inside = false;
            if (dir.d[i] > 0)
            {
                maxT.d[i] = (twoMin.d[i] - origin.d[i]) / dir.d[i];
            }
        }
        else if (origin.d[i] > twoMax.d[i])
        {
            inside = false;
            if (dir.d[i] < 0)
            {
                maxT.d[i] = (twoMax.d[i] - origin.d[i]) / dir.d[i];
            }
        }
    }

    if (inside)
    {
        return INTERSECT;
    }
    int whichPlane = 0;
    if (maxT.d[1] > maxT.d[whichPlane])
    {
        whichPlane = 1;
    }
    if (maxT.d[2] > maxT.d[whichPlane])
    {
        whichPlane = 2;
    }

    if (((int)maxT.d[whichPlane]) & 0x80000000)
    {
        return OUTSIDE;
    }
    for (i = 0; i < 3; i++)
    {
        if(i!= whichPlane)
        {
            float f = origin.d[i] + maxT.d[whichPlane] * dir.d[i];
            if (f < (twoMin.d[i] - 0.00001f) ||
                    f > (twoMax.d[i] +0.00001f))
            {
                return OUTSIDE;
            }
        }
    }
    return INTERSECT;
}

/** Checks how the second box intersects with the first.
*/
Intersection intersect( const SE_AABB &one, const SE_AABB &two )
{
    // Null box?
    if (one.getExtent().length() == 0 || two.getExtent().length() == 0) 
    {
        return OUTSIDE;
    }

    const SE_Vector3f& insideMin = two.getMin();
    const SE_Vector3f& insideMax = two.getMax();
    const SE_Vector3f& outsideMin = one.getMin();
    const SE_Vector3f& outsideMax = one.getMax();

    if (insideMax.x < outsideMin.x ||
        insideMax.y < outsideMin.y ||
        insideMax.z < outsideMin.z ||
        insideMin.x > outsideMax.x ||
        insideMin.y > outsideMax.y ||
        insideMin.z > outsideMax.z)
    {
        return OUTSIDE;
    }

    bool full = (insideMin.x > outsideMin.x &&
                 insideMin.y > outsideMin.y &&
                 insideMin.z > outsideMin.z &&
                 insideMax.x < outsideMax.x &&
                 insideMax.y < outsideMax.y &&
                 insideMax.z < outsideMax.z);

    if (full)
    {
        return INSIDE;
    }
    else
    {
        return INTERSECT;
    }
}

class FindSpatialTravel : public SE_SpatialTravel
{
public:
    FindSpatialTravel(OctreeSceneManager* octreeSceneManager)
    {
        mOctreeSceneManager = octreeSceneManager;
    }
	
    int visit(SE_Spatial* spatial)
    {
        SE_Scene* rootScene = SE_Application::getInstance()->getSceneManager()->getMainScene();
	SE_BoundingVolume* bv = rootScene->getRoot()->getWorldBoundingVolume();

	if(spatial->getSpatialType() == GEOMETRY)
	{
	    SE_BoundingVolume* bbv = spatial->getWorldBoundingVolume();
            SE_AABB aabb = ((SE_AABBBV*) bbv)->getGeometry();

	    //OctreeSceneManager* octreeSceneManager = SE_Application::getInstance()->getOctreeSceneManager();
	    //OctreeNode* onode =  octreeSceneManager->createSceneNode(spatial->getSpatialName());
	    OctreeNode* onode =  mOctreeSceneManager->createSceneNode(spatial->getSpatialName());
            //onode->setAABBBV(((SE_AABBBV*) bbv));
	    onode->setSpatial(spatial);
            //octreeSceneManager->updateOctreeNode(onode);
	    mOctreeSceneManager->updateOctreeNode(onode);
	}
	return 0;
    }

    int visit(SE_SimObject* simObject)
    {
	return 0;
    }
    OctreeSceneManager* mOctreeSceneManager;
};

OctreeSceneManager::OctreeSceneManager(const String& name)
{
    SE_AABB b(SE_Vector3f(-10000, -10000, -10000), SE_Vector3f(10000, 10000, 10000));
    int depth = 8; 
    mOctree = 0;
    init(b, depth);
}

OctreeSceneManager::OctreeSceneManager(const String& name, SE_AABB &box, int max_depth) 
{
    mOctree = 0;
    init(box, max_depth);
}

void OctreeSceneManager::init(SE_AABB &box, int depth)
{
    if (mOctree != 0)
    {
        delete mOctree;
    }

    mOctree = new Octree(0);

    mMaxDepth = depth;
    mBox = box;
    mOctree->mBox = box;

    SE_Vector3f min = box.getMin();
    SE_Vector3f max = box.getMax();
    mOctree->mHalfSize = (max - min) * 0.5f;
    mOctree->id = 1;
    mNumObjects = 0;
}

OctreeSceneManager::~OctreeSceneManager()
{    
    if (mOctree)
    {
        delete mOctree;
	mOctree = 0;
    }

    OctreeNodeList::iterator it;
    for (it = mOctreeNodes.begin(); it != mOctreeNodes.end(); ++it)
    {
        delete it->second;
    }
    mOctreeNodes.clear();
}

void OctreeSceneManager::destroySceneNode(const String &name)
{
    OctreeNode * on = getSceneNode(name);

    if (on != 0)
    {
        removeOctreeNode(on);
    }

    OctreeNodeList::iterator i = mOctreeNodes.find(name);

    if (i == mOctreeNodes.end())
    {
        return;
    }
   
    delete i->second;
    mOctreeNodes.erase(i);
}

OctreeNode* OctreeSceneManager::getSceneNode(const String& name) const
{
    OctreeNodeList::const_iterator i = mOctreeNodes.find(name);

    if (i == mOctreeNodes.end())
    {
        return NULL;
    }
    return i->second;
}

void OctreeSceneManager::setupOctree()
{		
    SE_Scene* rootScene = SE_Application::getInstance()->getSceneManager()->getMainScene();
    SE_BoundingVolume* bo = rootScene->getRoot()->getWorldBoundingVolume();
    SE_AABBBV* bv = (SE_AABBBV*)bo;
     const SE_AABB& box = bv->getGeometry();
     const SE_Vector3f min = box.getMin();
    const SE_Vector3f max = box.getMax();
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("##########****************** %f %f  %f  %f %f %f\n", min.x, min.y, min.z, max.x, max.y, max.z);		
   // SE_Scene* rootScene = SE_Application::getInstance()->getSceneManager()->getMainScene();   
    FindSpatialTravel* ft = new FindSpatialTravel(this);
    rootScene->getRoot()->travel(ft, false);
    delete ft;
}

void OctreeSceneManager::deleteObject(const String& name)
{
    destroySceneNode(name);
}

/** Only removes the node from the octree.  It leaves the octree, even if it's empty.
*/
void OctreeSceneManager::removeOctreeNode(OctreeNode * n)
{
	// Skip if octree has been destroyed (shutdown conditions)
    if (!mOctree)
    {
	return;
    }

    Octree * oct = n->getOctant();

    if (oct)
    {
        oct->removeNode(n);
    }

    n->setOctant(0);
}

void OctreeSceneManager::updateOctreeNode(OctreeNode * onode)
{
    //const SE_AABB& box = onode->getSpatial()->getWorldBoundingVolume();

    /*if ( box.isNull() )
    {
        return ;
    }*/

    // Skip if octree has been destroyed (shutdown conditions)
    if (!mOctree)
    {
	return;
    }
  
    if (onode->getSpatial()->getWorldBoundingVolume() == NULL)
    {
      //LOGI("# *********************2222*************************octree  bv = NULL#  %s \n",onode->getSpatial()->getSpatialName());
      mOctree->addNode(onode);
      onode->setType(OctreeNode::NO_AABB);
      return;
    } 

    if (onode->getOctant() == 0)
    {
        //if outside the octree, force into the root node.
        if (!onode->isIn(mOctree->mBox))
	{
            mOctree->addNode(onode);
	}
        else
	{
            addOctreeNode(onode, mOctree);
	}
        return ;
    }

   // if (!onode->isIn(onode->getOctant()->mBox))
    if ((!onode->isIn(onode->getOctant()->mBox)) || (onode->getOctant() == mOctree && onode->getType() == OctreeNode::NO_AABB))
    {
        if(onode->getType() == OctreeNode::NO_AABB)
        {
            onode->setType(OctreeNode::HAS_AABB);
        }
        removeOctreeNode(onode);

        //if outside the octree, force into the root node.
        if (! onode->isIn(mOctree->mBox))
	{
            mOctree->addNode(onode);
	}
        else
	{
            addOctreeNode(onode, mOctree);
	}
    }
}

void OctreeSceneManager::addOctreeNode(OctreeNode * n, Octree *octant, int depth)
{
    // Skip if octree has been destroyed (shutdown conditions)
    if (!mOctree)
    {
	return;
    }

    SE_BoundingVolume* bbv = n->getSpatial()->getWorldBoundingVolume();
    const SE_AABB& bx = ((SE_AABBBV*) bbv)->getGeometry();

    //if the octree is twice as big as the scene node,
    //we will add it to a child.
    if ((depth < mMaxDepth) && octant->isTwiceSize(bx))
    {
        int x, y, z;
        octant->getChildIndexes(bx, &x, &y, &z);

        if (octant->mChildren[ x ][ y ][ z ] == 0)
        {
            octant->mChildren[ x ][ y ][ z ] = new Octree(octant);
			
            octant->mChildren[ x ][ y ][ z ]->id = octant->id * 10 + (x * 1 + y * 2 + z * 4);
			
            //LOGI("# octree # %d \n", octant->mChildren[ x ][ y ][ z ]->id);			

            const SE_Vector3f& octantMin = octant->mBox.getMin();
            const SE_Vector3f& octantMax = octant->mBox.getMax();
            SE_Vector3f min, max;

            if (x == 0)
            {
                min.x = octantMin.x;
                max.x = (octantMin.x + octantMax.x) / 2;
            }

            else
            {
                min.x = (octantMin.x + octantMax.x) / 2;
                max.x = octantMax.x;
            }

            if (y == 0)
            {
                min.y = octantMin.y;
                max.y = (octantMin.y + octantMax.y) / 2;
            }

            else
            {
                min.y = (octantMin.y + octantMax.y) / 2;
                max.y = octantMax.y;
            }

            if (z == 0)
            {
                min.z = octantMin.z;
                max.z = (octantMin.z + octantMax.z) / 2;
            }

            else
            {
                min.z = (octantMin.z + octantMax.z) / 2;
                max.z = octantMax.z;
            }

            octant->mChildren[ x ][ y ][ z ]->mBox.set(min, max);         
            octant->mChildren[ x ][ y ][ z ]->mHalfSize = (max - min) * 0.5f;
        }
        addOctreeNode(n, octant->mChildren[ x ][ y ][ z ], ++depth);
    }
    else
    {	
        //LOGI("# octree add # %d    %s\n", octant->id ,n->getName().c_str());
        octant->addNode(n);
    }
}

OctreeNode* OctreeSceneManager::createSceneNodeImpl(void)
{
    return new OctreeNode(this);
}

OctreeNode* OctreeSceneManager::createSceneNodeImpl(const String &name)
{
    return new OctreeNode(this, name);
}

OctreeNode* OctreeSceneManager::createSceneNode(const String &name)
{
    
    OctreeNode* sn = createSceneNodeImpl(name);
    mOctreeNodes[sn->getName()] = sn;
    return sn;
}

void OctreeSceneManager::updateBounds(const String& name)
{
    //LOGI("BORQS## updateBounds### name = %s\n", name.c_str());
    OctreeNode* on = getSceneNode(name);
    if (on == NULL)
    {
        return;
    }
    updateOctreeNode(on);
}

void OctreeSceneManager::update(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
#if 0
    SE_SimObjectManager* particlesimObjectManager = SE_Application::getInstance()->getSimObjectManager();
    SE_SimObject*  particlesimobj = particlesimObjectManager->findByName("fireworks");
    if (particlesimobj)
    {
	particlesimobj->getSpatial()->updateBoundingVolume();
    }
#endif
}

void OctreeSceneManager::renderScene(SE_Camera * cam, SE_RenderManager& renderManager)
{
    mNumObjects = 0;
    //walk the octree, adding all visible Octreenodes nodes to the render queue.
    walkOctree(cam, mOctree, false, renderManager);
}

void OctreeSceneManager::walkOctree(SE_Camera *camera, Octree *octant, bool foundvisible, SE_RenderManager& renderManager)
{
    //return immediately if nothing is in the node.
    if (octant->numNodes() == 0)
    {
        return;
    }

    int culled = SE_FULL_CULL;

    if (foundvisible)
    {
	culled = SE_NOT_CULL;
    }
    else if (octant == mOctree)
    {
        culled = SE_PART_CULL;
    }
    else
    {
        SE_AABBBV box;
        octant->getCullBounds(&box);       
	culled = camera->cullBV(box);	       
    }
    

    // if the octant is visible, or if it's the root node...
    if (culled != SE_FULL_CULL)
    {     
        //Add stuff to be rendered;
        Octree::NodeList::iterator it = octant->mNodes.begin();
	int vis = SE_NOT_CULL;
        while (it != octant->mNodes.end())
        {
            OctreeNode * sn = *it;
            
            if(sn->getSpatial()->getParent()->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE) == false && sn->getSpatial()->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE) == true)
            {     
                ++it;
                continue;
            }
            
            // if this octree is partially visible, manually cull all
            // octreescene nodes attached directly to this level.
            if (culled == SE_PART_CULL)
	    {
                if(sn->getSpatial()->getWorldBoundingVolume() == NULL)
                {
                    vis = SE_FULL_CULL;
                } 
                else 
                { 
                    vis = camera->cullBV(*(sn->getSpatial()->getWorldBoundingVolume()));        
	        } 
            }
                           
            if (vis == SE_PART_CULL || vis == SE_NOT_CULL)
            {           
                mNumObjects++;          
                sn->getSpatial()->renderScene(camera, &renderManager,(SE_CULL_TYPE)vis);              
            }
            ++it;            
        }
        
        Octree* child;
	bool childfoundvisible = (culled == SE_NOT_CULL);
        if ((child = octant->mChildren[ 0 ][ 0 ][ 0 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}

        if ((child = octant->mChildren[ 1 ][ 0 ][ 0 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}

        if ((child = octant->mChildren[ 0 ][ 1 ][ 0 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}

        if ((child = octant->mChildren[ 1 ][ 1 ][ 0 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}

        if ((child = octant->mChildren[ 0 ][ 0 ][ 1 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}

        if ((child = octant->mChildren[ 1 ][ 0 ][ 1 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}

        if ((child = octant->mChildren[ 0 ][ 1 ][ 1 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible,renderManager);
	}

        if ((child = octant->mChildren[ 1 ][ 1 ][ 1 ]) != 0)
	{
            walkOctree(camera, child, childfoundvisible, renderManager);
	}
    }   
}

// --- non template versions
void findNodes(const SE_AABB &t, std::list<OctreeNode *> &list, OctreeNode *exclude, bool full, Octree *octant)
{
    if (!full)
    {
	SE_AABB obox;
	octant->getCullBounds(&obox);

	Intersection isect = intersect(t, obox);

	if (isect == OUTSIDE)
	{
	    return;
	}

	full = (isect == INSIDE);
    }

    Octree::NodeList::iterator it = octant->mNodes.begin();	

    while (it != octant->mNodes.end())
    {
	OctreeNode * on = (*it);

	if (on != exclude)
	{
	    if (full)
	    {
		list.push_back(on);
	    }
	    else
	    {
                 Intersection nsect = intersect(t, ((SE_AABBBV*)on->getSpatial()->getWorldBoundingVolume())->getGeometry());

		if (nsect != OUTSIDE)
		{
		    list.push_back(on);
		}
	    }
	}
	++it;
    }

    Octree* child;

    if ((child = octant->mChildren[ 0 ][ 0 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 0 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 0 ][ 1 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 1 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 0 ][ 0 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 0 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 0 ][ 1 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 1 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }
}

void findNodes(const SE_Ray &t, std::list<OctreeNode *> &list, OctreeNode *exclude, bool full, Octree *octant)
{
    if (!full)
    {
        //SE_AABBBV obox;
        SE_AABB obox;        
	octant->getCullBounds(&obox);      
	//Intersection isect = intersect(t, obox.getGeometry());
        Intersection isect = intersect(t, obox);
         
	if (isect == OUTSIDE)
	{  
	    return;
	}

	full = (isect == INSIDE);
    }

    Octree::NodeList::iterator it = octant->mNodes.begin();
  
    while (it != octant->mNodes.end())
    {
	OctreeNode * on = (*it);

	if (on != exclude)
	{
	    if (full)
	    {
                  
		list.push_back(on);
	    }
	    else
	    {
                Intersection nsect = intersect(t, ((SE_AABBBV*)on->getSpatial()->getWorldBoundingVolume())->getGeometry());
		if (nsect != OUTSIDE)
		{
		    list.push_back(on);
		}
	    }
	}
	++it;
    }

    Octree* child;
	
    if ((child = octant->mChildren[ 0 ][ 0 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 0 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 0 ][ 1 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 1 ][ 0 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 0 ][ 0 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 0 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 0 ][ 1 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }

    if ((child = octant->mChildren[ 1 ][ 1 ][ 1 ]) != 0)
    {
	findNodes(t, list, exclude, full, child);
    }
}

void OctreeSceneManager::findNodesIn(const SE_Ray &r, std::list<OctreeNode *> &list, OctreeNode *exclude)
{
    findNodes(r, list, exclude, false, mOctree);
}

SE_Spatial* OctreeSceneManager::travel(const SE_Ray &ray)
{   
    mMinResult.simObject = NULL;
    mMinResult.distance = 0;
    mMinResult.spatial = NULL;
    std::list<OctreeNode *> nodeList;
    //find the nodes that intersect the AAB
    findNodesIn(ray, nodeList, 0);
    //grab all objects from the node that intersect...
    std::list<OctreeNode *>::iterator it = nodeList.begin();
    while (it != nodeList.end())
    { 
	SE_SimObject* so = (*it)->getSpatial()->getCurrentAttachedSimObj();
   
	if (!so->getSpatial()->isSpatialStateHasAttribute(SE_SpatialAttribute::VISIBLE))
	{   
            ++it;
	    continue;
	}

    //generate world geometry data
    SE_GeometryData data;
    SE_Matrix4f m2w = so->getSpatial()->getWorldTransform();
    SE_GeometryData::transform(so->getMesh()->getGeometryData(),m2w,&data);


    int faceNum = data.getFaceNum();
	SE_Vector3i* faces = data.getFaceArray();
	SE_Vector3f* vertex = data.getVertexArray();
	float distance = SE_FLT_MAX;
	bool intersected = 0;
	for (int i = 0; i < faceNum; i++)
	{
            SE_Triangle triangle(vertex[faces[i].x], vertex[faces[i].y], vertex[faces[i].z]);
	    SE_IntersectResult ret = triangle.intersect(ray);
	    if (ret.intersected && ret.distance[0] < distance)
	    {
		distance = ret.distance[0];
		intersected = 1;
	    }
	}

	if (intersected)
	{
	    mResult.simObject = so;
	    mResult.distance = distance;
	    mResult.spatial = so->getSpatial();
	    if(mResult.spatial != NULL && mMinResult.spatial == NULL)
	    {
		mMinResult = mResult;
	    }
	    else if(mResult.spatial->getWorldLayer() > mMinResult.spatial->getWorldLayer())
	    {
		mMinResult = mResult;
	    }
	    else if(mResult.distance < mMinResult.distance)
	    {
		mMinResult = mResult;
	    }
	}       
         ++it;       
    }    
    return mMinResult.spatial; 
}

/*void OctreeSceneManager::addObject(const String& name)
{
    SE_SimObjectManager* simObjectManager = SE_Application::getInstance()->getSimObjectManager();
    SE_SimObject* simobj = simObjectManager->findByName(name.c_str());			
    SE_Spatial* spatial = simobj->getSpatial();
    SE_BoundingVolume* bv = spatial->getWorldBoundingVolume();
    SE_AABB aabb = ((SE_AABBBV*)bv)->getGeometry();
    OctreeNode* onode = createSceneNode(name);
    //onode->setAABBBV(((SE_AABBBV*)bv));
    onode->setSpatial(spatial);
    updateOctreeNode(onode);
}*/

void OctreeSceneManager::addObject(SE_Spatial* spatial)
{
    OctreeNode* onode = createSceneNode(spatial->getSpatialName());
    onode->setSpatial(spatial);
    updateOctreeNode(onode);
}

void OctreeSceneManager::resize(const SE_AABB &box)
{   
    if(SE_Application::getInstance()->SEHomeDebug)
    LOGI("##########*************************************************************resize*****************************************");
    std::list<OctreeNode *> nodes;
    std::list<OctreeNode *> ::iterator it;

    findNodes(mOctree->mBox, nodes, 0, true, mOctree);

    delete mOctree;

    mOctree = new Octree(0);
    mOctree->mBox = box;
    //mBox = box;

    const SE_Vector3f min = box.getMin();
    const SE_Vector3f max = box.getMax();
    mOctree->mHalfSize = (max - min) * 0.5f;

    it = nodes.begin();

    while (it != nodes.end())
    {
        OctreeNode * on = static_cast <OctreeNode *> (*it);
        on->setOctant(0);
        updateOctreeNode(on);
        ++it;
    }
}


