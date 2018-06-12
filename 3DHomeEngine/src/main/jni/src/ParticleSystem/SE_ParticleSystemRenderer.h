#ifndef __ParticleSystemRenderer_H__
#define __ParticleSystemRenderer_H__

#include "ParticleSystem/SE_Particle.h"
#include <list>
#include <string>

typedef std::string String;
typedef unsigned char uint8;

class ParticleVisualData;

/** Abstract class defining the interface required to be implemented
    by classes which provide rendering capability to ParticleSystem instances.
*/
class SE_ENTRY ParticleSystemRenderer 
{
public:
    // Constructor
    ParticleSystemRenderer() {}
    // Destructor
    virtual ~ParticleSystemRenderer() {}

    /** Gets the type of this renderer - must be implemented by subclasses */
    virtual const String& getType(void) const = 0;

    /** Delegated to by ParticleSystem::_updateRenderQueue
    @remarks
        The subclass must update the render queue using whichever Renderable
        instance(s) it wishes.
    */
    virtual void _updateRenderQueue(std::list<Particle*>& currentParticles, bool cullIndividually) = 0;

    /** Optional callback notified when particles are rotated */
    virtual void _notifyParticleRotated(void) {}
    /** Optional callback notified when particles are resized individually */
    virtual void _notifyParticleResized(void) {}
    /** Tells the renderer that the particle quota has changed */
    virtual void _notifyParticleQuota(size_t quota) = 0;
    /** Tells the renderer that the particle default size has changed */
    virtual void _notifyDefaultDimensions(Real width, Real height) = 0;
    /** Optional callback notified when particle emitted */
    virtual void _notifyParticleEmitted(Particle* particle) {}
    /** Optional callback notified when particle expired */
    virtual void _notifyParticleExpired(Particle* particle) {}
    /** Optional callback notified when particles moved */
    virtual void _notifyParticleMoved(std::list<Particle*>& currentParticles) {}
    /** Optional callback notified when particles cleared */
    virtual void _notifyParticleCleared(std::list<Particle*>& currentParticles) {}
    /** Create a new ParticleVisualData instance for attachment to a particle.
    @remarks
	If this renderer needs additional data in each particle, then this should
	be held in an instance of a subclass of ParticleVisualData, and this method
	should be overridden to return a new instance of it. The default
	behaviour is to return null.
    */
    virtual ParticleVisualData* _createVisualData(void) { return 0; }
    /** Destroy a ParticleVisualData instance.
    @remarks
	If this renderer needs additional data in each particle, then this should
	be held in an instance of a subclass of ParticleVisualData, and this method
	should be overridden to destroy an instance of it. The default
	behaviour is to do nothing.
    */
    virtual void _destroyVisualData(ParticleVisualData* vis) { assert (vis == 0); }

    // set the path of texture image
    virtual void setTextureImage(const char* imagePath) = 0;
    // set the name of the billboardset, create textureCoordDataID and geometryDataID
    virtual void setBillboardSetName(const String& billboardSetName) = 0;

    virtual void setTranslate(const SE_Vector3f& translate){}
    virtual void setRotate(const SE_Quat& rotate){}
    virtual void setScale(const SE_Vector3f& scale){}
};

/** Abstract class definition of a factory object for ParticleSystemRenderer. */
class SE_ENTRY  ParticleSystemRendererFactory 
{
public:
    // No methods, must just override all methods inherited from FactoryObj

    virtual const String& getType() const = 0;

    /** Creates a new object.
    @param name Name of the object to create
    @return
        An object created by the factory. The type of the object depends on
        the factory.
    */
    virtual ParticleSystemRenderer* createInstance( const String& name ) = 0;    
    /** Destroys an object which was created by this factory.
    @param ptr Pointer to the object to destroy
    */
    virtual void destroyInstance( ParticleSystemRenderer* ) = 0;  
    virtual ~ParticleSystemRendererFactory() {}   
};

#endif
