#ifndef __Math_H__
#define __Math_H__

#include <limits>
#include <algorithm>
#include <assert.h>
#include <math.h>

typedef float Real;
class Degree;

/** Wrapper class which indicates a given angle value is in Radians.
@remarks
    Radian values are interchangeable with Degree values, and conversions
    will be done automatically between them.
*/
class Radian
{
    Real mRad;

public:
    explicit Radian (Real r = 0): mRad(r) {}
    Radian (const Degree& d);
    Radian& operator = (const Real& f) { mRad = f; return *this; }
    Radian& operator = (const Radian& r) { mRad = r.mRad; return *this; }
    Radian& operator = (const Degree& d);

    Real valueDegrees() const{ return 0.0;}//FIXME:There is no implement; // see bottom of this file
    Real valueRadians() const { return mRad; }
    Real valueAngleUnits() const;

    const Radian& operator + () const { return *this; }
    Radian operator + (const Radian& r) const { return Radian (mRad + r.mRad); }
    Radian operator + (const Degree& d) const;
    Radian& operator += (const Radian& r) { mRad += r.mRad; return *this; }
    Radian& operator += (const Degree& d);
    Radian operator - () const { return Radian(-mRad); }
    Radian operator - (const Radian& r) const { return Radian (mRad - r.mRad); }
    Radian operator - (const Degree& d) const;
    Radian& operator -= (const Radian& r) { mRad -= r.mRad; return *this; }
    Radian& operator -= (const Degree& d);
    Radian operator * (Real f) const { return Radian (mRad * f); }
    Radian operator * (const Radian& f) const { return Radian (mRad * f.mRad); }
    Radian& operator *= (Real f) { mRad *= f; return *this; }
    Radian operator / (Real f) const { return Radian (mRad / f); }
    Radian& operator /=  (Real f) { mRad /= f; return *this; }

    bool operator <  (const Radian& r) const { return mRad <  r.mRad; }
    bool operator <= (const Radian& r) const { return mRad <= r.mRad; }
    bool operator == (const Radian& r) const { return mRad == r.mRad; }
    bool operator != (const Radian& r) const { return mRad != r.mRad; }
    bool operator >= (const Radian& r) const { return mRad >= r.mRad; }
    bool operator >  (const Radian& r) const { return mRad >  r.mRad; }
};

/** Wrapper class which indicates a given angle value is in Degrees.
@remarks
    Degree values are interchangeable with Radian values, and conversions
    will be done automatically between them.
*/
class Degree
{
    Real mDeg; // if you get an error here - make sure to define/typedef 'Real' first

public:
    explicit Degree (Real d = 0): mDeg(d) {}
    Degree (const Radian& r): mDeg(r.valueDegrees()) {}
    Degree& operator = (const Real& f) { mDeg = f; return *this; }
    Degree& operator = (const Degree& d) { mDeg = d.mDeg; return *this; }
    Degree& operator = (const Radian& r) { mDeg = r.valueDegrees(); return *this; }

    Real valueDegrees() const { return mDeg; }
    Real valueRadians() const { return 0.0;}//FIXME:there is no implement// see bottom of this file
    Real valueAngleUnits() const;

    const Degree& operator + () const { return *this; }
    Degree operator + (const Degree& d) const { return Degree (mDeg + d.mDeg); }
    Degree operator + (const Radian& r) const { return Degree (mDeg + r.valueDegrees()); }
    Degree& operator += (const Degree& d) { mDeg += d.mDeg; return *this; }
    Degree& operator += (const Radian& r) { mDeg += r.valueDegrees(); return *this; }
    Degree operator - () const { return Degree(-mDeg); }
    Degree operator - (const Degree& d) const { return Degree (mDeg - d.mDeg); }
    Degree operator - (const Radian& r) const { return Degree (mDeg - r.valueDegrees()); }
    Degree& operator -= (const Degree& d) { mDeg -= d.mDeg; return *this; }
    Degree& operator -= (const Radian& r) { mDeg -= r.valueDegrees(); return *this; }
    Degree operator * (Real f) const { return Degree (mDeg * f); }
    Degree operator * (const Degree& f) const { return Degree (mDeg * f.mDeg); }
    Degree& operator *= (Real f) { mDeg *= f; return *this; }
    Degree operator / (Real f) const { return Degree (mDeg / f); }
    Degree& operator /= (Real f) { mDeg /= f; return *this; }

    bool operator <  (const Degree& d) const { return mDeg <  d.mDeg; }
    bool operator <= (const Degree& d) const { return mDeg <= d.mDeg; }
    bool operator == (const Degree& d) const { return mDeg == d.mDeg; }
    bool operator != (const Degree& d) const { return mDeg != d.mDeg; }
    bool operator >= (const Degree& d) const { return mDeg >= d.mDeg; }
    bool operator >  (const Degree& d) const { return mDeg >  d.mDeg; }
};

/** Wrapper class which identifies a value as the currently default angle 
    type, as defined by Math::setAngleUnit.
@remarks
    Angle values will be automatically converted between radians and degrees,
    as appropriate.
*/
class Angle
{
    Real mAngle;

public:
    explicit Angle (Real angle) : mAngle(angle) {}
    operator Radian() const;
    operator Degree() const;
};

// these functions could not be defined within the class definition of class
// Radian because they required class Degree to be defined
inline Radian::Radian ( const Degree& d ) : mRad(d.valueRadians()) {
}

inline Radian& Radian::operator = (const Degree& d) {
    mRad = d.valueRadians(); 
    return *this;
}

inline Radian Radian::operator + (const Degree& d) const {
    return Radian ( mRad + d.valueRadians() );
}

inline Radian& Radian::operator += (const Degree& d) {
    mRad += d.valueRadians();
    return *this;
}

inline Radian Radian::operator - (const Degree& d) const {
    return Radian (mRad - d.valueRadians());
}

inline Radian& Radian::operator -= (const Degree& d) {
    mRad -= d.valueRadians();
    return *this;
}

/** Class to provide access to common mathematical functions.
    @remarks
        Most of the maths functions are aliased versions of the C runtime
        library functions. They are aliased here to provide future
        optimisation opportunities, either from faster RTLs or custom
        math approximations.
    @note
        <br>This is based on MgcMath.h from
        <a href="http://www.geometrictools.com/">Wild Magic</a>.
*/
class Math 
{
protected:

public:
    /** Default constructor.
    @param
        trigTableSize Optional parameter to set the size of the
        tables used to implement Sin, Cos, Tan
    */
    Math(unsigned int trigTableSize = 4096);

    /** Default destructor.
    */
    ~Math();

    static inline int IAbs (int iValue) { return ( iValue >= 0 ? iValue : -iValue ); }		
    static inline Real Abs (Real fValue) { return Real(fabs(fValue)); }
    static inline Degree Abs (const Degree& dValue) { return Degree(fabs(dValue.valueDegrees())); }
    static inline Radian Abs (const Radian& rValue) { return Radian(fabs(rValue.valueRadians())); }
    static Radian ACos (Real fValue);
    static Radian ASin (Real fValue);
    static inline Radian ATan (Real fValue) { return Radian(atan(fValue)); }
    static inline Radian ATan2 (Real fY, Real fX) { return Radian(atan2(fY,fX)); }
    static inline bool isNaN(Real f)
    {
	// std::isnan() is C99, not supported by all compilers
	// However NaN always fails this next test, no other number does.
	return f != f;
    }

    /** Cosine function.
    @param
        fValue Angle in radians
    @param
        useTables If true, uses lookup tables rather than
        calculation - faster but less accurate.
    */
    static inline Real Cos (const Radian& fValue, bool useTables = false) {
	return Real(cos(fValue.valueRadians()));
    }

    /** Cosine function.
    @param
        fValue Angle in radians
    @param
        useTables If true, uses lookup tables rather than
        calculation - faster but less accurate.
    */
    static inline Real Cos (Real fValue, bool useTables = false) {
	return Real(cos(fValue));
    }

    // Stored value of log(2) for frequent use
    static const Real LOG2;

    static inline Real Log2 (Real fValue) { return Real(log(fValue)/LOG2); }

    static inline Real LogN (Real base, Real fValue) { return Real(log(fValue)/log(base)); }


    static inline Real Pow (Real fBase, Real fExponent) { return Real(pow(fBase,fExponent)); }

    /** Sine function.
    @param
        fValue Angle in radians
    @param
        useTables If true, uses lookup tables rather than
        calculation - faster but less accurate.
    */
    static inline Real Sin (const Radian& fValue, bool useTables = false) {			
	return Real(sin(fValue.valueRadians()));
    }

    /** Sine function.
    @param
        fValue Angle in radians
    @param
        useTables If true, uses lookup tables rather than
        calculation - faster but less accurate.
    */
    static inline Real Sin (Real fValue, bool useTables = false) {
	return Real(sin(fValue)) ;
    }

    static inline Real Sqr (Real fValue) { return fValue*fValue; }

    static inline Real Sqrt (Real fValue) { return Real(sqrt(fValue)); }

    static inline Radian Sqrt (const Radian& fValue) { return Radian(sqrt(fValue.valueRadians())); }

    static inline Degree Sqrt (const Degree& fValue) { return Degree(sqrt(fValue.valueDegrees())); }

    /** Inverse square root i.e. 1 / Sqrt(x), good for vector
        normalisation.
    */
    static Real InvSqrt(Real fValue);

    static Real UnitRandom ();  // in [0,1]

    static Real RangeRandom (Real fLow, Real fHigh);  // in [fLow,fHigh]

    static Real SymmetricRandom ();  // in [-1,1]

    /** Compare 2 reals, using tolerance for inaccuracies.
    */
    static bool RealEqual(Real a, Real b,
    Real tolerance = std::numeric_limits<Real>::epsilon());

    /** Clamp a value within an inclusive range. */
    template <typename T> static T Clamp(T val, T minval, T maxval)
    {
	assert (minval < maxval );
	if(val>maxval)
        { 
            val = maxval;
        }
	if(val<minval)
        {
            val = minval;
        }
	return val;
    }

    static const Real POS_INFINITY;
    static const Real NEG_INFINITY;
    static const Real PI;
    static const Real TWO_PI;
    static const Real HALF_PI;
    static const Real fDeg2Rad;
    static const Real fRad2Deg;
};

// these functions must be defined down here, because they rely on the
// angle unit conversion functions in class Math:
inline Radian operator * (Real a, const Radian& b)
{
    return Radian (a * b.valueRadians());
}

inline Radian operator / (Real a, const Radian& b)
{
    return Radian (a / b.valueRadians());
}

#endif
