#ifndef __ParticleIterator_H__
#define __ParticleIterator_H__

#include <list>

class Particle;

/** Convenience class to make it easy to step through all particles in a ParticleSystem.
*/
class SE_ENTRY ParticleIterator
{
    friend class ParticleSystem;
protected:
    std::list<Particle*>::iterator mPos;
    std::list<Particle*>::iterator mStart;
    std::list<Particle*>::iterator mEnd;

    // Protected constructor, only available from ParticleSystem::getIterator
    ParticleIterator(std::list<Particle*>::iterator start, std::list<Particle*>::iterator end)
    {
	mStart = mPos = start;
	mEnd = end;
    }

public:
    // Returns true when at the end of the particle list
    bool end(void)
    {
	return (mPos == mEnd);
    }

    /** Returns a pointer to the next particle, and moves the iterator on by 1 element. */
    Particle* getNext(void)
    {
	return static_cast<Particle*>(*mPos++);
    }
};

#endif

