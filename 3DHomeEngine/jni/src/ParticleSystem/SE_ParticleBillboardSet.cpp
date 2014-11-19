#include "SE_DynamicLibType.h"
#include "ParticleSystem/SE_ParticleBillboardSet.h"
#include "ParticleSystem/SE_ParticleBillboard.h"
#include <iostream>
#include <sstream> 
#include<algorithm>
#include "SE_Vector.h"
#include "SE_GeometryData.h"
#include "SE_TextureCoordData.h"
#include "SE_Application.h"
#include "SE_Camera.h"
#include "SE_MemLeakDetector.h"
#include "SE_Log.h"

BillboardSet::BillboardSet():
    mBoundingRadius(0.0f), 
    mOriginType(BBO_CENTER),
    mRotationType(BBR_TEXCOORD),
    mAllDefaultSize(true),
    mAutoExtendPool(true),
    mSortingEnabled(false),
    mAccurateFacing(false),
    mAllDefaultRotation(true),
    mWorldSpace(false),
    mCullIndividual(false),
    mBillboardType(BBT_POINT),
    mCommonDirection(Vector3::UNIT_Z),
    mCommonUpVector(Vector3::UNIT_Y),
    mPointRendering(false),
    mBuffersCreated(false),
    mPoolSize(0),
    mExternalData(false),
    mAutoUpdate(true),
    mVertexDataIndex(0),
    mBillboardDataChanged(true)
{	
    mBuffer = NULL;
    mIndex = NULL;       
    setDefaultDimensions(100, 100);
    setTextureStacksAndSlices(1, 1);
}

BillboardSet::BillboardSet(const String& name, unsigned int poolSize, bool externalData):
    mBoundingRadius(0.0f), 
    mOriginType(BBO_CENTER),
    mRotationType(BBR_TEXCOORD),
    mAllDefaultSize(true),
    mAutoExtendPool(true),
    mSortingEnabled(false),
    mAccurateFacing(false),
    mAllDefaultRotation(true),
    mWorldSpace(false),
    mCullIndividual(false),
    mBillboardType(BBT_POINT),
    mCommonDirection(Vector3::UNIT_Z),
    mCommonUpVector(Vector3::UNIT_Y),
    mPointRendering(false),
    mBuffersCreated(false),
    mPoolSize(poolSize),
    mExternalData(externalData),
    mAutoUpdate(true),
    mVertexDataIndex(0),
    mBillboardDataChanged(true)
{
    mBuffer = NULL;
    mIndex = NULL;
    setDefaultDimensions(100, 100);
    setPoolSize(poolSize);
    setTextureStacksAndSlices(1, 1);
}

BillboardSet::BillboardSet(const String& name, unsigned int poolSize, bool externalData, SE_Mesh* mesh):
    SE_MeshSimObject(mesh,OWN),
    mBoundingRadius(0.0f), 
    mOriginType(BBO_CENTER),
    mRotationType(BBR_TEXCOORD),
    mAllDefaultSize( true ),
    mAutoExtendPool( true ),
    mSortingEnabled(false),
    mAccurateFacing(false),
    mAllDefaultRotation(true),
    mWorldSpace(false),
    mCullIndividual(false),
    mBillboardType(BBT_POINT),
    mCommonDirection(Vector3::UNIT_Z),
    mCommonUpVector(Vector3::UNIT_Y),
    mPointRendering(false),
    mBuffersCreated(false),
    mPoolSize(poolSize),
    mExternalData(externalData),
    mAutoUpdate(true),
    mVertexDataIndex(0),
    mBillboardDataChanged(true)
{
    mBuffer = NULL;
    mIndex = NULL;
    setDefaultDimensions(100, 100);
    setPoolSize(poolSize);
    setTextureStacksAndSlices(1, 1);
}

BillboardSet::~BillboardSet()
{
    // Free pool items
    BillboardPool::iterator i;
    for (i = mBillboardPool.begin(); i != mBillboardPool.end(); ++i)
    {
        delete[] *i;
    }

    // Delete shared buffers
    _destroyBuffers();
}

Billboard* BillboardSet::createBillboard(const Vector3& position, const ColourValue& colour)
{
    if (mFreeBillboards.empty())
    {
        if (mAutoExtendPool)
        {
            setPoolSize(getPoolSize() * 2);
        }
        else
        {
            return 0;
        }
    }

    // Get a new billboard
    Billboard* newBill = mFreeBillboards.front();
    mActiveBillboards.splice(
	    mActiveBillboards.end(), mFreeBillboards, mFreeBillboards.begin());
    newBill->setPosition(position);
    newBill->setColour(colour);
    newBill->mDirection = Vector3::ZERO;
    newBill->setRotation(Radian(0));
    newBill->setTexcoordIndex(0);
    newBill->resetDimensions();
    newBill->_notifyOwner(this);

    return newBill;
}

Billboard* BillboardSet::createBillboard(
        Real x, Real y, Real z,
        const ColourValue& colour )
{
    return createBillboard( Vector3( x, y, z ), colour );
}

int BillboardSet::getNumBillboards(void) const
{
    return static_cast< int >( mActiveBillboards.size() );
}

void BillboardSet::clear()
{
    // Move actives to free list
    mFreeBillboards.splice(mFreeBillboards.end(), mActiveBillboards);
}

Billboard* BillboardSet::getBillboard(unsigned int index) const
{
    assert(
            index < mActiveBillboards.size() &&
            "Billboard index out of bounds." );

    /* We can't access it directly, so we check wether it's in the first
       or the second half, then we start either from the beginning or the
       end of the list
    */
    ActiveBillboardList::const_iterator it;
    if (index >= ( mActiveBillboards.size() >> 1))
    {
	index = static_cast<unsigned int>(mActiveBillboards.size()) - index;
        for (it = mActiveBillboards.end(); index; --index, --it);
    }
    else
    {
        for (it = mActiveBillboards.begin(); index; --index, ++it);
    }

    return *it;
}

void BillboardSet::removeBillboard(unsigned int index)
{
     assert(
            index < mActiveBillboards.size() &&
            "Billboard index out of bounds." );

    /* We can't access it directly, so we check wether it's in the first
       or the second half, then we start either from the beginning or the
       end of the list.
       We then remove the billboard form the 'used' list and add it to
       the 'free' list.
    */
    ActiveBillboardList::iterator it;
    if (index >= ( mActiveBillboards.size() >> 1))
    {
	index = static_cast<unsigned int>(mActiveBillboards.size()) - index;
        for (it = mActiveBillboards.end(); index; --index, --it);
    }
    else
    {
        for(it = mActiveBillboards.begin(); index; --index, ++it);
    }

    mFreeBillboards.splice(mFreeBillboards.end(), mActiveBillboards, it);
}

void BillboardSet::removeBillboard(Billboard* pBill)
{
    ActiveBillboardList::iterator it =
            std::find(mActiveBillboards.begin(), mActiveBillboards.end(), pBill);
    assert(
            it != mActiveBillboards.end() &&
            "Billboard isn't in the active list." );

    mFreeBillboards.splice(mFreeBillboards.end(), mActiveBillboards, it);
}

void BillboardSet::setBillboardOrigin(BillboardOrigin origin)
{
    mOriginType = origin;
}

BillboardOrigin BillboardSet::getBillboardOrigin(void) const
{
    return mOriginType;
}

void BillboardSet::setBillboardRotationType(BillboardRotationType rotationType)
{
    mRotationType = rotationType;
}

BillboardRotationType BillboardSet::getBillboardRotationType(void) const
{
    return mRotationType;
}

void BillboardSet::setDefaultDimensions( Real width, Real height )
{
    mDefaultWidth = width;
    mDefaultHeight = height;
}

void BillboardSet::setDefaultWidth(Real width)
{
    mDefaultWidth = width;
}

Real BillboardSet::getDefaultWidth(void) const
{
    return mDefaultWidth;
}

void BillboardSet::setDefaultHeight(Real height)
{
    mDefaultHeight = height;
}

Real BillboardSet::getDefaultHeight(void) const
{
    return mDefaultHeight;
}

BillboardSet::SortByDirectionFunctor::SortByDirectionFunctor(const Vector3& dir)
    : sortDir(dir)
{
}

float BillboardSet::SortByDirectionFunctor::operator()(Billboard* bill) const
{
    return sortDir.dotProduct(bill->getPosition());
}

BillboardSet::SortByDistanceFunctor::SortByDistanceFunctor(const Vector3& pos)
    : sortPos(pos)
{
}

float BillboardSet::SortByDistanceFunctor::operator()(Billboard* bill) const
{
    // Sort descending by squared distance
    return - (sortPos - bill->getPosition()).squaredLength();
}

void BillboardSet::beginBillboards(size_t numBillboards)
{
    /* Generate the vertices for all the billboards relative to the camera
        Also take the opportunity to update the vertex colours
        May as well do it here to save on loops elsewhere
    */

    /* NOTE: most engines generate world coordinates for the billboards
        directly, taking the world axes of the camera as offsets to the
        center points. I take a different approach, reverse-transforming
        the camera world axes into local billboard space.
        Why?
        Well, it's actually more efficient this way, because I only have to
        reverse-transform using the billboardset world matrix (inverse)
        once, from then on it's simple additions (assuming identically
        sized billboards). If I transformed every billboard center by it's
        world transform, that's a matrix multiplication per billboard
        instead.
        I leave the final transform to the render pipeline since that can
        use hardware TnL if it is available.
    */

    // create vertex and index buffers if they haven't already been
    if (!mBuffersCreated) 
    {
        _createBuffers();
    }

    // Only calculate vertex offets et al if we're not point rendering
    if (!mPointRendering)
    {
	// Get offsets for origin type
	getParametricOffsets(mLeftOff, mRightOff, mTopOff, mBottomOff);

	// Generate axes etc up-front if not oriented per-billboard
	    if (mBillboardType != BBT_ORIENTED_SELF && mBillboardType != BBT_PERPENDICULAR_SELF && 
		!(mAccurateFacing && mBillboardType != BBT_PERPENDICULAR_COMMON))
	{
	    genBillboardAxes(&mCamX, &mCamY);

	    /* If all billboards are the same size we can precalculate the
		offsets and just use '+' instead of '*' for each billboard,
		and it should be faster.
            */
	        genVertOffsets(mLeftOff, mRightOff, mTopOff, mBottomOff,mDefaultWidth, mDefaultHeight, mCamX, mCamY, mVOffset);

    }
    }

    // Init num visible
    mNumVisibleBillboards = 0;
        
    // Lock the buffer
    if (numBillboards) // optimal lock
    {
	// clamp to max
	if (mPoolSize < numBillboards) 
	{
            numBillboards = mPoolSize;
	}
	mVertexDataIndex = 0;
    }
}

void BillboardSet::injectBillboard(const Billboard& bb)
{
    // Don't accept injections beyond pool size
    if (mNumVisibleBillboards == mPoolSize)
    { 
        return;
    }

    // Skip if not visible (NB always true if not bounds checking individual billboards)
    // if (!billboardVisible(mCurrentCamera, bb)) return;

    if (!mPointRendering &&
	    (mBillboardType == BBT_ORIENTED_SELF ||
            mBillboardType == BBT_PERPENDICULAR_SELF ||
            (mAccurateFacing && mBillboardType != BBT_PERPENDICULAR_COMMON)))
    {
        // Have to generate axes & offsets per billboard
        genBillboardAxes(&mCamX, &mCamY, &bb);
    }

    // If they're all the same size or we're point rendering
    if (mAllDefaultSize || mPointRendering)
    {
        /* No per-billboard checking, just blast through.
        Saves us an if clause every billboard which may
        make a difference.
        */

        if (!mPointRendering &&
		(mBillboardType == BBT_ORIENTED_SELF ||
           	mBillboardType == BBT_PERPENDICULAR_SELF ||
           	(mAccurateFacing && mBillboardType != BBT_PERPENDICULAR_COMMON)))
        {
            genVertOffsets(mLeftOff, mRightOff, mTopOff, mBottomOff,
                    mDefaultWidth, mDefaultHeight, mCamX, mCamY, mVOffset);
        }
            genVertices(mVOffset, bb);
    }
    else // not all default size and not point rendering
    {
        Vector3 vOwnOffset[4];
        // If it has own dimensions, or self-oriented, gen offsets
        if (mBillboardType == BBT_ORIENTED_SELF ||
                mBillboardType == BBT_PERPENDICULAR_SELF ||
                bb.mOwnDimensions ||
                (mAccurateFacing && mBillboardType != BBT_PERPENDICULAR_COMMON))
        {
            // Generate using own dimensions
	    genVertOffsets(mLeftOff, mRightOff, mTopOff, mBottomOff,
                    bb.mWidth, bb.mHeight, mCamX, mCamY, vOwnOffset);
            // Create vertex data
            genVertices(vOwnOffset, bb);
        }
        else // Use default dimension, already computed before the loop, for faster creation
        {
            genVertices(mVOffset, bb);
        }
    }
    // Increment visibles
    mNumVisibleBillboards++;
}

void BillboardSet::endBillboards(void)
{	
    if (mPointRendering)
    {
        this->getMesh()->getSurface(0)->updateParticleData(mVertexDataIndex * 6);
        this->getMesh()->getSurface(0)->setParticleData(mBuffer, mIndex, mVertexDataIndex * 6, true, false, true);
        this->getMesh()->getSurface(0)->setDrawMode((SE_Surface::DrawMode)0);
        this->getSpatial()->updateBoundingVolume();
        this->getSpatial()->updateRenderState();
        //this->getSpatial()->updateWorldLayer();
        this->getSpatial()->updateWorldTransform();
    }
    else
    {
        this->getMesh()->getSurface(0)->updateParticleData(mVertexDataIndex * 6);
        this->getMesh()->getSurface(0)->setParticleData(mBuffer, mIndex, mVertexDataIndex * 6, true, true, true);
        this->getMesh()->getSurface(0)->setDrawMode((SE_Surface::DrawMode)2);
        this->getSpatial()->updateBoundingVolume();
        this->getSpatial()->updateRenderState();
        //this->getSpatial()->updateWorldLayer();
        this->getSpatial()->updateWorldTransform();
    }
}

void BillboardSet::_updateBounds(void)
{
}

void BillboardSet::setAutoextend(bool autoextend)
{
    mAutoExtendPool = autoextend;
}

bool BillboardSet::getAutoextend(void) const
{
    return mAutoExtendPool;
}

void BillboardSet::setSortingEnabled(bool sortenable)
{
    mSortingEnabled = sortenable;
}

bool BillboardSet::getSortingEnabled(void) const
{
    return mSortingEnabled;
}

void BillboardSet::setPoolSize(size_t size)
{
    // If we're driving this from our own data, allocate billboards
    if (!mExternalData)
    {
        // Never shrink below size()
        size_t currSize = mBillboardPool.size();
        if (currSize >= size)
        {
            return;
        }

        this->increasePool(size);

        for (size_t i = currSize; i < size; ++i)
        {
            // Add new items to the queue
            mFreeBillboards.push_back( mBillboardPool[i] );
        }
    }

    mPoolSize = size;

    _destroyBuffers();
}

void BillboardSet::_createBuffers(void)
{
    /* Allocate / reallocate vertex data
        Note that we allocate enough space for ALL the billboards in the pool, but only issue
        rendering operations for the sections relating to the active billboards
    */

    /* Alloc positions   ( 1 or 4 verts per billboard, 3 components )
        colours     ( 1 x RGBA per vertex )
        indices     ( 6 per billboard ( 2 tris ) if not point rendering )
        tex. coords ( 2D coords, 1 or 4 per billboard )
    */

    // Warn if user requested an invalid setup
    // Do it here so it only appears once
    if (mPointRendering && mBillboardType != BBT_POINT)
    {
        if(SE_Application::getInstance()->SEHomeDebug)
	LOGI("Warning: BillboardSet has point rendering enabled but is using a type "
	"other than BBT_POINT, this may not give you the results you expect.");
    }

    if (mPointRendering)
    {
	mBuffer = new float[mPoolSize * 7];
    }
    else
    {
	mBuffer = new float[mPoolSize * 36];
    }

    if (!mPointRendering)
    {
        mIndex = new unsigned short[mPoolSize * 6];

	for (int idx, idxOff, bboard = 0; bboard < mPoolSize; ++bboard)
	{
	    // Do indexes
	    idx = bboard;
	    idxOff = bboard * 4;

	    mIndex[idx * 6]   = (idxOff);
            mIndex[idx * 6 + 1] = (idxOff + 2);
	    mIndex[idx * 6 + 2] = (idxOff + 1);
	    mIndex[idx * 6 + 3] = (idxOff + 1);
	    mIndex[idx * 6 + 4] = (idxOff + 2);
	    mIndex[idx * 6 + 5] = (idxOff + 3);
	}
    }         

    /* Create indexes (will be the same every frame)
	Using indexes because it means 1/3 less vertex transforms (4 instead of 6)

	Billboard layout relative to camera:

	0-----1
	|    /|
	|  /  |
	|/    |
	2-----3
    */
    mBuffersCreated = true;
}

void BillboardSet::_destroyBuffers(void)
{
    mBuffersCreated = false;
}

unsigned int BillboardSet::getPoolSize(void) const
{
    return static_cast< unsigned int >(mBillboardPool.size());
}

void BillboardSet::_notifyBillboardResized(void)
{
    mAllDefaultSize = false;
}

void BillboardSet::_notifyBillboardRotated(void)
{
    mAllDefaultRotation = false;
}

void BillboardSet::getParametricOffsets(
        Real& left, Real& right, Real& top, Real& bottom)
{
    switch(mOriginType)
    {
    case BBO_TOP_LEFT:
        left = 0.0f;
        right = 1.0f;
        top = 0.0f;
        bottom = -1.0f;
        break;

    case BBO_TOP_CENTER:
        left = -0.5f;
        right = 0.5f;
        top = 0.0f;
        bottom = -1.0f;
        break;

    case BBO_TOP_RIGHT:
        left = -1.0f;
        right = 0.0f;
        top = 0.0f;
        bottom = -1.0f;
        break;

    case BBO_CENTER_LEFT:
        left = 0.0f;
        right = 1.0f;
        top = 0.5f;
        bottom = -0.5f;
        break;

    case BBO_CENTER:
        left = -0.5f;
        right = 0.5f;
        top = 0.5f;
        bottom = -0.5f;
        break;

    case BBO_CENTER_RIGHT:
        left = -1.0f;
        right = 0.0f;
        top = 0.5f;
        bottom = -0.5f;
        break;

    case BBO_BOTTOM_LEFT:
        left = 0.0f;
        right = 1.0f;
        top = 1.0f;
        bottom = 0.0f;
        break;

    case BBO_BOTTOM_CENTER:
        left = -0.5f;
        right = 0.5f;
        top = 1.0f;
        bottom = 0.0f;
        break;

    case BBO_BOTTOM_RIGHT:
        left = -1.0f;
        right = 0.0f;
        top = 1.0f;
        bottom = 0.0f;
        break;
    }
}

bool BillboardSet::getCullIndividually(void) const
{
    return mCullIndividual;
}

void BillboardSet::setCullIndividually(bool cullIndividual)
{
    mCullIndividual = cullIndividual;
}

void BillboardSet::increasePool(size_t size)
{
    size_t oldSize = mBillboardPool.size();

    // Increase size
    mBillboardPool.reserve(size);
    mBillboardPool.resize(size);

    // Create new billboards
    for (size_t i = oldSize; i < size; ++i)
    {
        mBillboardPool[i] = new Billboard();
    }
}

void BillboardSet::genBillboardAxes(Vector3* pX, Vector3 *pY, const Billboard* bb)
{
    SE_Camera* camera; 
    // If we're using accurate facing, recalculate camera direction per BB
    if (mAccurateFacing && 
            (mBillboardType == BBT_POINT || 
            mBillboardType == BBT_ORIENTED_COMMON ||
            mBillboardType == BBT_ORIENTED_SELF))
    {
        // cam -> bb direction
        camera = SE_Application::getInstance()->getCurrentCamera();
	SE_Vector3f camPos = camera->getLocation();
        mCamPos.x = camPos.x;
	mCamPos.y = camPos.y;
	mCamPos.z = camPos.z;
        mCamDir = bb->mPosition - mCamPos;
        mCamDir.normalise();
    }

    switch (mBillboardType)
    {
    case BBT_POINT: 
        camera = SE_Application::getInstance()->getCurrentCamera();
        if (mAccurateFacing)
        {
            // Point billboards will have 'up' based on but not equal to cameras
            // Use pY temporarily to avoid allocation
            SE_Vector3f py = camera->getAxisY();
	    pY->x = py.x;
            pY->y = py.y;
	    pY->z = py.z;
            *pX = mCamDir.crossProduct(*pY);
            pX->normalise();
            SE_Vector3f pz = camera->getAxisZ();
            mCamDir.x = pz.x;
            mCamDir.y = pz.y;
	    mCamDir.z = pz.z;
            *pY = pX->crossProduct(mCamDir); // both normalised already
        }
        else
        {
            // Get camera axes for X and Y (depth is irrelevant)
	    SE_Vector3f px = camera->getAxisX();
	    pX->x = px.x;
	    pX->y = px.y;
	    pX->z = px.z;
            SE_Vector3f py = camera->getAxisY();
	    pY->x = py.x;
            pY->y = py.y;
	    pY->z = py.z;
        }
        break;

    case BBT_ORIENTED_COMMON:
        // Y-axis is common direction
        // X-axis is cross with camera direction
        *pY = mCommonDirection;
        *pX = mCamDir.crossProduct(*pY);
        pX->normalise();
        break;

    case BBT_ORIENTED_SELF:
        // Y-axis is direction
        // X-axis is cross with camera direction
        // Scale direction first
        *pY = bb->mDirection;
        *pX = mCamDir.crossProduct(*pY);
        pX->normalise();
        break;

    case BBT_PERPENDICULAR_COMMON:
        // X-axis is up-vector cross common direction
        // Y-axis is common direction cross X-axis
        *pX = mCommonUpVector.crossProduct(mCommonDirection);
        *pY = mCommonDirection.crossProduct(*pX);
        break;

    case BBT_PERPENDICULAR_SELF:
        // X-axis is up-vector cross own direction
        // Y-axis is own direction cross X-axis
        *pX = mCommonUpVector.crossProduct(bb->mDirection);
        pX->normalise();
        *pY = bb->mDirection.crossProduct(*pX); // both should be normalised
        break;
    }
}

void BillboardSet::setBillboardType(BillboardType bbt)
{
    mBillboardType = bbt;
}

BillboardType BillboardSet::getBillboardType(void) const
{
    return mBillboardType;
}

void BillboardSet::setCommonDirection(const Vector3& vec)
{
    mCommonDirection = vec;
}

const Vector3& BillboardSet::getCommonDirection(void) const
{
    return mCommonDirection;
}

void BillboardSet::setCommonUpVector(const Vector3& vec)
{
    mCommonUpVector = vec;
}

const Vector3& BillboardSet::getCommonUpVector(void) const
{
    return mCommonUpVector;
}

uint32 BillboardSet::getTypeFlags(void) const
{
    return 0;
}

void BillboardSet::genVertices(const Vector3* const offsets, const Billboard& bb)
{
    // Texcoords
    assert( bb.mUseTexcoordRect || bb.mTexcoordIndex < mTextureCoords.size());
    const FloatRect & r = bb.mUseTexcoordRect ? bb.mTexcoordRect : mTextureCoords[bb.mTexcoordIndex];

    if (mPointRendering)
    {
	// Single vertex per billboard, ignore offsets
	// position, RGBA colour;
        // No texture coords in point rendering
	mBuffer[mVertexDataIndex * 7]     = bb.mPosition.x;
        mBuffer[mVertexDataIndex * 7 +1 ] = bb.mPosition.y;
	mBuffer[mVertexDataIndex * 7 + 2] = bb.mPosition.z;
	mBuffer[mVertexDataIndex * 7 + 3] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 7 + 4] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 7 + 5] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 7 + 6] = bb.mColour.a;
    }
    else if (mAllDefaultRotation || bb.mRotation == Radian(0))
    {
        // Left-top
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36]     = offsets[0].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 1] = offsets[0].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 2] = offsets[0].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 3] = r.left;
	mBuffer[mVertexDataIndex * 36 + 4] = r.top;
	mBuffer[mVertexDataIndex * 36 + 5] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 6] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 7] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 8] = bb.mColour.a;

        // Right-top
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36 + 9]  = offsets[1].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 10] = offsets[1].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 11] = offsets[1].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 12] = r.right;
	mBuffer[mVertexDataIndex * 36 + 13] = r.top;
	mBuffer[mVertexDataIndex * 36 + 14] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 15] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 16] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 17] = bb.mColour.a;

        // Left-bottom
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36 + 18] = offsets[2].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 19] = offsets[2].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 20] = offsets[2].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 21] = r.left;
	mBuffer[mVertexDataIndex * 36 + 22] = r.bottom;
	mBuffer[mVertexDataIndex * 36 + 23] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 24] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 25] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 26] = bb.mColour.a;

        // Right-bottom
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36 + 27] = offsets[3].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 28] = offsets[3].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 29] = offsets[3].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 30] = r.right;
	mBuffer[mVertexDataIndex * 36 + 31] = r.bottom;
	mBuffer[mVertexDataIndex * 36 + 32] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 33] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 34] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 35] = bb.mColour.a;
    }
    else if (mRotationType == BBR_VERTEX)
    {
        // TODO: Cache axis when billboard type is BBT_POINT or BBT_PERPENDICULAR_COMMON
        Vector3 axis = (offsets[3] - offsets[0]).crossProduct(offsets[2] - offsets[1]).normalisedCopy();
        Matrix3 rotation;
	rotation.FromAxisAngle(axis, bb.mRotation);
        Vector3 pt;

        // Left-top
        // Positions，Texture coords，Colour
        pt = rotation * offsets[0];
	mBuffer[mVertexDataIndex * 36]     = pt.x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 1] = pt.y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 2] = pt.z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 3] = r.left;
	mBuffer[mVertexDataIndex * 36 + 4] = r.top;
	mBuffer[mVertexDataIndex * 36 + 5] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 6] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 7] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 8] = bb.mColour.a;

        // Right-top
        // Positions，Texture coords，Colour
        pt = rotation * offsets[1];
	mBuffer[mVertexDataIndex * 36 + 9]  = pt.x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 10] = pt.y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 11] = pt.z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 12] = r.right;
	mBuffer[mVertexDataIndex * 36 + 13] = r.top;
	mBuffer[mVertexDataIndex * 36 + 14] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 15] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 16] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 17] = bb.mColour.a;

        // Left-bottom
        // Positions，Texture coords，Colour
        pt = rotation * offsets[2];
	mBuffer[mVertexDataIndex * 36 + 18] = pt.x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 19] = pt.y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 20] = pt.z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 21] = r.left;
	mBuffer[mVertexDataIndex * 36 + 22] = r.bottom;
	mBuffer[mVertexDataIndex * 36 + 23] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 24] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 25] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 26] = bb.mColour.a;

        // Right-bottom
        // Positions，Texture coords，Colour
        pt = rotation * offsets[3];
	mBuffer[mVertexDataIndex * 36 + 27] = pt.x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 28] = pt.y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 29] = pt.z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 30] = r.right;
	mBuffer[mVertexDataIndex * 36 + 31] = r.bottom;
	mBuffer[mVertexDataIndex * 36 + 32] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 33] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 34] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 35] = bb.mColour.a;
    }
    else
    {
        const Real cos_rot(Math::Cos(bb.mRotation));
        const Real sin_rot(Math::Sin(bb.mRotation));

        float width = (r.right-r.left) / 2;
        float height = (r.bottom-r.top) / 2;
        float mid_u = r.left+width;
        float mid_v = r.top+height;

        float cos_rot_w = cos_rot * width;
        float cos_rot_h = cos_rot * height;
        float sin_rot_w = sin_rot * width;
        float sin_rot_h = sin_rot * height;

        // Left-top
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36]     = offsets[0].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 1] = offsets[0].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 2] = offsets[0].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 3] = mid_u - cos_rot_w + sin_rot_h;
	mBuffer[mVertexDataIndex * 36 + 4] = mid_v - sin_rot_w - cos_rot_h;
	mBuffer[mVertexDataIndex * 36 + 5] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 6] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 7] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 8] = bb.mColour.a;

        // Right-top
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36 + 9]  = offsets[1].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 10] = offsets[1].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 11] = offsets[1].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 12] = mid_u + cos_rot_w + sin_rot_h;
	mBuffer[mVertexDataIndex * 36 + 13] = mid_v + sin_rot_w - cos_rot_h;
	mBuffer[mVertexDataIndex * 36 + 14] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 15] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 16] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 17] = bb.mColour.a;

        // Left-bottom
        // Positions，Texture coords，Colour
	mBuffer[mVertexDataIndex * 36 + 18] = offsets[2].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 19] = offsets[2].y + bb.mPosition.y;
	mBuffer[mVertexDataIndex * 36 + 20] = offsets[2].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 21] = mid_u - cos_rot_w - sin_rot_h;
	mBuffer[mVertexDataIndex * 36 + 22] = mid_v - sin_rot_w + cos_rot_h;
	mBuffer[mVertexDataIndex * 36 + 23] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 24] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 25] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 26] = bb.mColour.a;

        // Right-bottom
        // Positions，Texture coords，Colour			
	mBuffer[mVertexDataIndex * 36 + 27] = offsets[3].x + bb.mPosition.x;
        mBuffer[mVertexDataIndex * 36 + 28] = offsets[3].y + bb.mPosition.y;
        mBuffer[mVertexDataIndex * 36 + 29] = offsets[3].z + bb.mPosition.z;
	mBuffer[mVertexDataIndex * 36 + 30] = mid_u + cos_rot_w - sin_rot_h;
	mBuffer[mVertexDataIndex * 36 + 31] = mid_v + sin_rot_w + cos_rot_h;
	mBuffer[mVertexDataIndex * 36 + 32] = bb.mColour.r;
	mBuffer[mVertexDataIndex * 36 + 33] = bb.mColour.g;
	mBuffer[mVertexDataIndex * 36 + 34] = bb.mColour.b;
        mBuffer[mVertexDataIndex * 36 + 35] = bb.mColour.a;
    }
    ++mVertexDataIndex;
}

void BillboardSet::genVertOffsets(Real inleft, Real inright, Real intop, Real inbottom,
        Real width, Real height, const Vector3& x, const Vector3& y, Vector3* pDestVec)
{
    Vector3 vLeftOff, vRightOff, vTopOff, vBottomOff;
    /* Calculate default offsets. Scale the axes by
       parametric offset and dimensions, ready to be added to
       positions.
    */

    vLeftOff   = x * ( inleft   * width );
    vRightOff  = x * ( inright  * width );
    vTopOff    = y * ( intop   * height );
    vBottomOff = y * ( inbottom * height );

    // Make final offsets to vertex positions
    pDestVec[0] = vLeftOff  + vTopOff;
    pDestVec[1] = vRightOff + vTopOff;
    pDestVec[2] = vLeftOff  + vBottomOff;
    pDestVec[3] = vRightOff + vBottomOff;
}

const String& BillboardSet::getMovableType(void) const
{
    return BillboardSetFactory::FACTORY_TYPE_NAME;
}

Real BillboardSet::getBoundingRadius(void) const
{
    return mBoundingRadius;
}

void BillboardSet::setTextureCoords(FloatRect const * coords, uint16 numCoords)
{
    if (!numCoords || !coords) {
        setTextureStacksAndSlices(1, 1);
	return;
    }
    //  clear out any previous allocation (as vectors may not shrink)
    TextureCoordSets().swap(mTextureCoords);
    //  make room
    mTextureCoords.resize(numCoords);
    //  copy in data
    std::copy(coords, coords+numCoords, &mTextureCoords.front());
}

void BillboardSet::setTextureStacksAndSlices( unsigned char stacks, unsigned char slices )
{
    if (stacks == 0)
    { 
        stacks = 1;
    }
    if (slices == 0) 
    {
        slices = 1;
    }
    //  clear out any previous allocation (as vectors may not shrink)
    TextureCoordSets().swap( mTextureCoords );
    //  make room
    mTextureCoords.resize( (size_t)stacks * slices );
    unsigned int coordIndex = 0;
    //  spread the U and V coordinates across the rects
    for (unsigned int v = 0; v < stacks; ++v) 
    {
        //  (float)X / X is guaranteed to be == 1.0f for X up to 8 million, so
        //  our range of 1..256 is quite enough to guarantee perfect coverage.
        float top = (float)v / (float)stacks;
        float bottom = ((float)v + 1) / (float)stacks;
        for (unsigned int u = 0; u < slices; ++u) 
        {
            FloatRect & r = mTextureCoords[coordIndex];
#if 0
            r.left = (float)u / (float)slices;
            r.bottom = bottom;
            r.right = ((float)u + 1) / (float)slices;
            r.top = top;
#endif
            r.left = 0;
            r.bottom = 0;
            r.right = 1;
            r.top = 1;

            ++coordIndex;
        }
    }
    assert( coordIndex == (size_t)stacks * slices );
}

FloatRect const * BillboardSet::getTextureCoords( uint16 * oNumCoords )
{
    *oNumCoords = (uint16)mTextureCoords.size();
    //  std::vector<> is guaranteed to be contiguous
    return &mTextureCoords.front();
}

void BillboardSet::setPointRenderingEnabled(bool enabled)
{
    // Override point rendering if not supported

    if (enabled != mPointRendering)
    {
	mPointRendering = enabled;
	// Different buffer structure (1 or 4 verts per billboard)
	_destroyBuffers();
    }
}

void BillboardSet::setAutoUpdate(bool autoUpdate)
{
    // Case auto update buffers changed we have to destroy the current buffers
    // since their usage will be different.
    if (autoUpdate != mAutoUpdate)
    {
	mAutoUpdate = autoUpdate;
	_destroyBuffers();
    }
}

String BillboardSetFactory::FACTORY_TYPE_NAME = "BillboardSet";
	
const String& BillboardSetFactory::getType(void) const
{
    return FACTORY_TYPE_NAME;
}

BillboardSet* BillboardSetFactory::createInstanceImpl(const String& name,
	const NameValuePairList* params)
{
    // may have parameters
    bool externalData = false;
    unsigned int poolSize = 0;

    if (params != 0)
    {
	NameValuePairList::const_iterator ni = params->find("poolSize");
	if (ni != params->end())
	{
	    std::istringstream str(ni->second);
	    unsigned int ret = 10;
	    str >> ret;
	    poolSize = ret;
	}
	ni = params->find("externalData");
	if (ni != params->end())
	{
	    //externalData = StringConverter::parseBool(ni->second);
	}

    }

    if (poolSize > 0)
    {
	return new BillboardSet(name, poolSize, externalData);
    }
    else
    {
	return new BillboardSet(name);
    }
}

void BillboardSetFactory::destroyInstance( BillboardSet* obj)
{
    delete obj;
}

   

