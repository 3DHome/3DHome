#ifndef __Billboard_H__
#define __Billboard_H__

#include "ParticleSystem/SE_ParticleVector3.h"
#include "ParticleSystem/SE_ParticleColorValue.h"

template< typename T > struct TRect
{
    T left, top, right, bottom;

    TRect() : left(0), top(0), right(0), bottom(0) {}

    TRect( T const & l, T const & t, T const & r, T const & b )
    : left( l ), top( t ), right( r ), bottom( b )
    {
    }

    TRect( TRect const & o )
    : left( o.left ), top( o.top ), right( o.right ), bottom( o.bottom )
    {
    }

    TRect & operator=( TRect const & o )
    {
        left = o.left;
        top = o.top;
        right = o.right;
        bottom = o.bottom;
        return *this;
    }

    T width() const
    {
        return right - left;
    }

    T height() const
    {
        return bottom - top;
    }

    bool isNull() const
    {
	return width() == 0 || height() == 0;
    }

    void setNull()
    {
	left = right = top = bottom = 0;
    }

    TRect & merge(const TRect& rhs)
    {
	if (isNull())
	{
	    *this = rhs;
	}
	else if (!rhs.isNull())
	{
	    left = std::min(left, rhs.left);
	    right = std::max(right, rhs.right);
	    top = std::min(top, rhs.top);
	    bottom = std::max(bottom, rhs.bottom);
	}

	return *this;
    }

    TRect intersect(const TRect& rhs) const
    {
	TRect ret;
	if (isNull() || rhs.isNull())
	{
	    // empty
	    return ret;
	}
	else
	{
	    ret.left = std::max(left, rhs.left);
	    ret.right = std::min(right, rhs.right);
	    ret.top = std::max(top, rhs.top);
	    ret.bottom = std::min(bottom, rhs.bottom);
	}

	if (ret.left > ret.right || ret.top > ret.bottom)
	{
	    // no intersection, return empty
	    ret.left = ret.top = ret.right = ret.bottom = 0;
	}

	return ret;
    }
};

typedef TRect<float> FloatRect;
typedef unsigned short uint16;

/** A billboard is a primitive which always faces the camera in every frame.
    @remarks
        Billboards can be used for special effects or some other trickery which requires the
        triangles to always facing the camera no matter where it is. Ogre groups billboards into
        sets for efficiency, so you should never create a billboard on it's own (it's ok to have a
        set of one if you need it).
    @par
        Billboards have their geometry generated every frame depending on where the camera is. It is most
        beneficial for all billboards in a set to be identically sized since Ogre can take advantage of this and
        save some calculations - useful when you have sets of hundreds of billboards as is possible with special
        effects. You can deviate from this if you wish (example: a smoke effect would probably have smoke puffs
        expanding as they rise, so each billboard will legitimately have it's own size) but be aware the extra
        overhead this brings and try to avoid it if you can.
    @par
        Billboards are just the mechanism for rendering a range of effects such as particles. It is other classes
        which use billboards to create their individual effects, so the methods here are quite generic.
    @see
        BillboardSet
*/
class BillboardSet;

class SE_ENTRY  Billboard 
{
    friend class BillboardSet;
    friend class BillboardParticleRenderer;
protected:
    bool mOwnDimensions;
    bool mUseTexcoordRect;
    unsigned short mTexcoordIndex; // index into the BillboardSet array of texture coordinates
    FloatRect mTexcoordRect; // individual texture coordinates
    Real mWidth;
    Real mHeight;
public:
    // Note the intentional public access to main internal variables used at runtime
    // Forcing access via get/set would be too costly for 000's of billboards
    Vector3 mPosition;
    // Normalised direction vector
    Vector3 mDirection;
    BillboardSet* mParentSet;
    ColourValue mColour;
    Radian mRotation;

    /** Default constructor.
    */
    Billboard();

    /** Default destructor.
    */
    ~Billboard();

    /** Normal constructor as called by BillboardSet.
    */
    Billboard(const Vector3& position, BillboardSet* owner, const ColourValue& colour = ColourValue::White);

    /** Get the rotation of the billboard.
        @remarks
            This rotation is relative to the center of the billboard.
    */
    const Radian& getRotation(void) const { return mRotation; }

    /** Set the rotation of the billboard.
        @remarks
            This rotation is relative to the center of the billboard.
    */
    void setRotation(const Radian& rotation);

    /** Set the position of the billboard.
        @remarks
            This position is relative to a point on the quad which is the billboard. Depending on the BillboardSet,
            this may be the center of the quad, the top-left etc. See BillboardSet::setBillboardOrigin for more info.
    */
    void setPosition(const Vector3& position);

    /** Set the position of the billboard.
        @remarks
            This position is relative to a point on the quad which is the billboard. Depending on the BillboardSet,
            this may be the center of the quad, the top-left etc. See BillboardSet::setBillboardOrigin for more info.
    */
    void setPosition(Real x, Real y, Real z);

    /** Get the position of the billboard.
        @remarks
            This position is relative to a point on the quad which is the billboard. Depending on the BillboardSet,
            this may be the center of the quad, the top-left etc. See BillboardSet::setBillboardOrigin for more info.
    */
    const Vector3& getPosition(void) const;

    /** Sets the width and height for this billboard.
        @remarks
            Note that it is most efficient for every billboard in a BillboardSet to have the same dimensions. If you
            choose to alter the dimensions of an individual billboard the set will be less efficient. Do not call
            this method unless you really need to have different billboard dimensions within the same set. Otherwise
            just call the BillboardSet::setDefaultDimensions method instead.
    */
    void setDimensions(Real width, Real height);

    /** Resets this Billboard to use the parent BillboardSet's dimensions instead of it's own. */
    void resetDimensions(void) { mOwnDimensions = false; }
    /** Sets the colour of this billboard.
        @remarks
            Billboards can be tinted based on a base colour. This allows variations in colour irrespective of the
            base colour of the material allowing more varied billboards. The default colour is white.
            The tinting is effected using vertex colours.
    */
    void setColour(const ColourValue& colour);

    /** Gets the colour of this billboard.
    */
    const ColourValue& getColour(void) const;

    /** Returns true if this billboard deviates from the BillboardSet's default dimensions (i.e. if the
        Billboard::setDimensions method has been called for this instance).
        @see
            Billboard::setDimensions
    */
    bool hasOwnDimensions(void) const;

    /** Retrieves the billboard's personal width, if hasOwnDimensions is true. */
    Real getOwnWidth(void) const;

    /** Retrieves the billboard's personal width, if hasOwnDimensions is true. */
    Real getOwnHeight(void) const;

    /** Internal method for notifying the billboard of it's owner.
    */
    void _notifyOwner(BillboardSet* owner);

    /** Returns true if this billboard use individual texture coordinate rect (i.e. if the 
        Billboard::setTexcoordRect method has been called for this instance), or returns
        false if use texture coordinates defined in the parent BillboardSet's texture
        coordinates array (i.e. if the Billboard::setTexcoordIndex method has been called
        for this instance).
        @see
            Billboard::setTexcoordIndex()
            Billboard::setTexcoordRect()
    */
    bool isUseTexcoordRect(void) const { return mUseTexcoordRect; }

    /** setTexcoordIndex() sets which texture coordinate rect this billboard will use 
        when rendering. The parent billboard set may contain more than one, in which 
        case a billboard can be textured with different pieces of a larger texture 
        sheet very efficiently.
        @see
            BillboardSet::setTextureCoords()
    */
    void setTexcoordIndex(unsigned short texcoordIndex);

    /** getTexcoordIndex() returns the previous value set by setTexcoordIndex(). 
        The default value is 0, which is always a valid texture coordinate set.
        @remarks
            This value is useful only when isUseTexcoordRect return false.
    */
    unsigned short getTexcoordIndex(void) const { return mTexcoordIndex; }

    /** setTexcoordRect() sets the individual texture coordinate rect of this billboard
        will use when rendering. The parent billboard set may contain more than one, in
        which case a billboard can be textured with different pieces of a larger texture
        sheet very efficiently.
    */
    void setTexcoordRect(const FloatRect& texcoordRect);

    /** setTexcoordRect() sets the individual texture coordinate rect of this billboard
        will use when rendering. The parent billboard set may contain more than one, in
        which case a billboard can be textured with different pieces of a larger texture
        sheet very efficiently.
    */
    void setTexcoordRect(Real u0, Real v0, Real u1, Real v1);

    /** getTexcoordRect() returns the previous value set by setTexcoordRect(). 
        @remarks
            This value is useful only when isUseTexcoordRect return true.
    */
    const FloatRect& getTexcoordRect(void) const { return mTexcoordRect; }
};

#endif
