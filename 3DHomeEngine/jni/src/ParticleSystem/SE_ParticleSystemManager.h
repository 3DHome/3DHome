#ifndef __ParticleSystemManager_H__
#define __ParticleSystemManager_H__

#include "SE_Singleton.h"
#include <map>
#include <string>
#include "SE_Time.h"
#include "SE_Vector.h"
#include "ParticleSystem/SE_ParticleSystem.h"

typedef std::string String;
typedef std::map<String, String> NameValuePairList;

class ParticleSystem;
class ParticleAffectorFactory;
class ParticleEmitter;
class ParticleEmitterFactory;
class ParticleAffector;
class ParticleSystemRendererFactory;
class ParticleSystemRenderer;

enum SE_Effect
{
    SNOW,
    RAIN,
    STAR,
    ATTACHED
};

struct EffectData
{
    SE_Vector3f pos;
    SE_Vector3f posBoxSize;
    SE_Vector3f helpPos;
    SE_Vector3f helpPosBoxSize;
};
enum ParticleSystemName
{   
    Box,
    Fireworks,
    Aureola,
    Ring,
    HollowEllipsoid,
    Ellipsoid,
    Cylinder,
};

enum ParticleAttritute
{   
    WIDTH,
    HEIGHT,
    DIMENSION,
    PARTICLEQUOTA,
    EMITTERQUOTA,
    ANGLE,
    COLOUR,
    COLOURRANGEEND,
    COLOURRANGESTART,
    DIRECTION,
    DURATION,
    EMISSIONRATE,
    EMITTED,
    EMITTEDEMITTER,
    ENABLED,
    NAME,
    PARTICLEVELOCITY,
    POSITION,
    ROTATION,
    STARTTIME,
    REPEATDELAY,
    TIMETOLIVE,
    BOXWIDTH,
    BOXHEIGHT,
    BOXDEPTH,
    HEINNERSIZE,
    HEINNERSIZEX,
    HEINNERSIZEY,
    HEINNERSIZEZ,
    RINGEINNERSIZE,
    RINGINNERSIZEX,
    RINGINNERSIZEY,
    FORCEVECTOR,
    COLOURINTERPOLATORADJUST,
    TIMEADJUST,
    COLOURFADERADJUST,
    REDADJUST,
    GREENADJUST,
    BLUEADJUST,
    ALPHAADJUST,
    ADJUST1,
    ADJUST2,
    REDADJUST1,
    REDADJUST2,
    GREENADJUST1,
    GREENADJUST2,
    BLUEADJUST1,
    BLUEADJUST2,
    ALPHAADJUST1,
    ALPHAADJUST2,
    STATECHANGE,
    PLANEPOINT,
    PLANENORMAL,
    BOUNCE,
    RANDOMNESS,
    SCOPE,
    KEEPVELOCITY,
    ROTATIONSPEEDRANGESTART,
    ROTATIONSPEEDRANGEEND,
    ROTATIONRANGESTART,
    ROTATIONRANGEEND,
    SCALEADJUST,
    FOLLOWPOSITION
};

/** Factory object for creating ParticleSystem instances */
class SE_ENTRY  ParticleSystemFactory
{
protected:
    ParticleSystem* createInstanceImpl(const String& name, const NameValuePairList* params);
public:
    ParticleSystemFactory() {}
    ~ParticleSystemFactory() {}
		
    static String FACTORY_TYPE_NAME;

    const String& getType(void) const;
    void destroyInstance( ParticleSystem* obj);  
};

class SE_ENTRY ParticleSystemManager: 
    public Singleton<ParticleSystemManager>
{
    friend class ParticleSystemFactory;
public:
    typedef std::map<String, ParticleSystem*> ParticleTemplateMap;
    typedef std::map<String, ParticleSystem*> ParticleSystemMap;
    typedef std::map<String, ParticleAffectorFactory*> ParticleAffectorFactoryMap;
    typedef std::map<String, ParticleEmitterFactory*> ParticleEmitterFactoryMap;
    typedef std::map<String, ParticleSystemRendererFactory*> ParticleSystemRendererFactoryMap;
protected:		
    // Templates based on scripts
    ParticleTemplateMap mSystemTemplates;

    ParticleSystemMap mParticleSystems;
        
    // Factories for named emitter types (can be extended using plugins)
    ParticleEmitterFactoryMap mEmitterFactories;

    // Factories for named affector types (can be extended using plugins)
    ParticleAffectorFactoryMap mAffectorFactories;

    // Map of renderer types to factories
    ParticleSystemRendererFactoryMap mRendererFactories;

    // Factory instance
    ParticleSystemFactory* mFactory;

    // Internal implementation of createSystem
    ParticleSystem* createSystemImpl(const String& name, size_t quota, const String& resourceGroup);
    // Internal implementation of createSystem
    ParticleSystem* createSystemImpl(const String& name, const String& templateName);
    // Internal implementation of destroySystem
    void destroySystemImpl(ParticleSystem* sys);
		
public:
    ParticleSystemManager();
    virtual ~ParticleSystemManager();

    /** Adds a new 'factory' object for emitters to the list of available emitter types.
    @remarks
        This method allows plugins etc to add new particle emitter types to Ogre. Particle emitters
        are sources of particles, and generate new particles with their start positions, colours and
        momentums appropriately. Plugins would create new subclasses of ParticleEmitter which 
        emit particles a certain way, and register a subclass of ParticleEmitterFactory to create them 
        (since multiple emitters can be created for different particle systems).
    @par
        All particle emitter factories have an assigned name which is used to identify the emitter
        type. This must be unique.
    @par
        Note that the object passed to this function will not be destroyed by the ParticleSystemManager,
        since it may have been allocated on a different heap in the case of plugins. The caller must
        destroy the object later on, probably on plugin shutdown.
    @param
        factory Pointer to a ParticleEmitterFactory subclass created by the plugin or application code.
    */
    void addEmitterFactory(ParticleEmitterFactory* factory);

    /** Adds a new 'factory' object for affectors to the list of available affector types.
    @remarks
        This method allows plugins etc to add new particle affector types to Ogre. Particle
        affectors modify the particles in a system a certain way such as affecting their direction
        or changing their colour, lifespan etc. Plugins would
        create new subclasses of ParticleAffector which affect particles a certain way, and register
        a subclass of ParticleAffectorFactory to create them.
    @par
        All particle affector factories have an assigned name which is used to identify the affector
        type. This must be unique.
    @par
        Note that the object passed to this function will not be destroyed by the ParticleSystemManager,
        since it may have been allocated on a different heap in the case of plugins. The caller must
        destroy the object later on, probably on plugin shutdown.
    @param
        factory Pointer to a ParticleAffectorFactory subclass created by the plugin or application code.
    */
    void addAffectorFactory(ParticleAffectorFactory* factory);

    /** Registers a factory class for creating ParticleSystemRenderer instances. 
    @par
        Note that the object passed to this function will not be destroyed by the ParticleSystemManager,
        since it may have been allocated on a different heap in the case of plugins. The caller must
        destroy the object later on, probably on plugin shutdown.
    @param
        factory Pointer to a ParticleSystemRendererFactory subclass created by the plugin or application code.
    */
    void addRendererFactory(ParticleSystemRendererFactory* factory);

    /** Adds a new particle system template to the list of available templates. 
    @remarks
        Instances of particle systems in a scene are not normally unique - often you want to place the
        same effect in many places. This method allows you to register a ParticleSystem as a named template,
        which can subsequently be used to create instances using the createSystem method.
    @par
        Note that particle system templates can either be created programmatically by an application 
        and registered using this method, or they can be defined in a script file (*.particle) which is
        loaded by the engine at startup, very much like Material scripts.
    @param
        name The name of the template. Must be unique across all templates.
    @param
        sysTemplate A pointer to a particle system to be used as a template. The manager
        will take over ownership of this pointer.           
    */
    void addTemplate(const String& name, ParticleSystem* sysTemplate);

    /** Removes a specified template from the ParticleSystemManager.
    @remarks
        This method removes a given template from the particle system manager, optionally deleting
        the template if the deleteTemplate method is called.  Throws an exception if the template
        could not be found.
    @param
        name The name of the template to remove.
    @param
        deleteTemplate Whether or not to delete the template before removing it.
    */
    void removeTemplate(const String& name, bool deleteTemplate = true);

    /** Removes a specified template from the ParticleSystemManager.
    @remarks
        This method removes all templates from the ParticleSystemManager.
    @param
        deleteTemplate Whether or not to delete the templates before removing them.
    */
    void removeAllTemplates(bool deleteTemplate = true);
      
    /** Removes all templates that belong to a secific Resource Group from the ParticleSystemManager.
    @remarks
        This method removes all templates that belong in a particular resource group from the ParticleSystemManager.
    @param
        resourceGroup to delete templates for
    */
    void removeTemplatesByResourceGroup(const String& resourceGroup);

    /** Create a new particle system template. 
    @remarks
        This method is similar to the addTemplate method, except this just creates a new template
        and returns a pointer to it to be populated. Use this when you don't already have a system
        to add as a template and just want to create a new template which you will build up in-place.
    @param
        name The name of the template. Must be unique across all templates.
    @param
        resourceGroup The name of the resource group which will be used to load any dependent resources.            
    */
    ParticleSystem* createTemplate(const String& name, const String& resourceGroup);

    /** Retrieves a particle system template for possible modification. 
    @remarks
        Modifying a template does not affect the settings on any ParticleSystems already created
        from this template.
    */
    ParticleSystem* getTemplate(const String& name);

    /**
     * Create a particle system
     */
    ParticleSystem* createParticleSystem(const String& name);

    /**
     * Add a particle system to particle system manager
     */
    void addParticleSystem(const String& name, ParticleSystem* ps, bool needDepthTest, int layerIndex);

    /**
     * Set attributes to a known emitter
     */
    void setEmitterAttribute(ParticleEmitter* emitter, int attribute, float x, float y, float z, float w);

    /**
     * Set attributes to a known affector
     */
    void setAffectorAttribute(ParticleAffector* affector, int attribute, float x, float y, float z, float w, int index);

    /** Internal method for creating a new emitter from a factory.
    @remarks
        Used internally by the engine to create new ParticleEmitter instances from named
        factories. Applications should use the ParticleSystem::addEmitter method instead, 
        which calls this method to create an instance.
    @param
        emitterType String name of the emitter type to be created. A factory of this type must have been registered.
    @param 
        psys The particle system this is being created for
    */
    ParticleEmitter* _createEmitter(const String& emitterType, ParticleSystem* psys);

    /** Internal method for destroying an emitter.
    @remarks
        Because emitters are created by factories which may allocate memory from separate heaps,
        the memory allocated must be freed from the same place. This method is used to ask the factory
        to destroy the instance passed in as a pointer.
    @param
        emitter Pointer to emitter to be destroyed. On return this pointer will point to invalid (freed) memory.
    */
    void _destroyEmitter(ParticleEmitter* emitter);

    /** Internal method for creating a new affector from a factory.
    @remarks
        Used internally by the engine to create new ParticleAffector instances from named
        factories. Applications should use the ParticleSystem::addAffector method instead, 
        which calls this method to create an instance.
    @param
        effectorType String name of the affector type to be created. A factory of this type must have been registered.
    @param
        psys The particle system it is being created for
    */
    ParticleAffector* _createAffector(const String& affectorType, ParticleSystem* psys);

    /** Internal method for destroying an affector.
    @remarks
        Because affectors are created by factories which may allocate memory from separate heaps,
        the memory allocated must be freed from the same place. This method is used to ask the factory
        to destroy the instance passed in as a pointer.
    @param
        affector Pointer to affector to be destroyed. On return this pointer will point to invalid (freed) memory.
    */
    void _destroyAffector(ParticleAffector* affector);

    /** Internal method for creating a new renderer from a factory.
    @remarks
        Used internally by the engine to create new ParticleSystemRenderer instances from named
        factories. Applications should use the ParticleSystem::setRenderer method instead, 
        which calls this method to create an instance.
    @param
        rendererType String name of the renderer type to be created. A factory of this type must have been registered.
    */
    ParticleSystemRenderer* _createRenderer(const String& rendererType);

    /** Internal method for destroying a renderer.
    @remarks
        Because renderer are created by factories which may allocate memory from separate heaps,
        the memory allocated must be freed from the same place. This method is used to ask the factory
        to destroy the instance passed in as a pointer.
    @param
        renderer Pointer to renderer to be destroyed. On return this pointer will point to invalid (freed) memory.
    */
    void _destroyRenderer(ParticleSystemRenderer* renderer);

    /** Init method to be called by OGRE system.
    @remarks
        Due to dependencies between various objects certain initialisation tasks cannot be done
        on construction. OGRE will call this method when the rendering subsystem is initialised.
    */
    void _initialise(void);

    float getLoadingOrder(void) const;

    typedef ParticleAffectorFactoryMap::iterator  ParticleAffectorFactoryIterator;
    typedef ParticleEmitterFactoryMap::iterator ParticleEmitterFactoryIterator;
    typedef ParticleSystemRendererFactoryMap::iterator ParticleRendererFactoryIterator;
    /** Return an iterator over the affector factories currently registered */
    ParticleAffectorFactoryIterator getAffectorFactoryIterator(void);
    /** Return an iterator over the emitter factories currently registered */
    ParticleEmitterFactoryIterator getEmitterFactoryIterator(void);
    /** Return an iterator over the renderer factories currently registered */
    ParticleRendererFactoryIterator getRendererFactoryIterator(void);

    typedef ParticleTemplateMap::iterator ParticleSystemTemplateIterator;
    //** Gets an iterator over the list of particle system templates. */
    //ParticleSystemTemplateIterator getTemplateIterator(void)
    //{
    //    return ParticleSystemTemplateIterator(
    //        mSystemTemplates.begin(), mSystemTemplates.end());
    //} 

    /** Get an instance of ParticleSystemFactory (internal use). */
    ParticleSystemFactory* _getFactory(void) { return mFactory; }
		
    /** Override standard Singleton retrieval.
    @remarks
        Why do we do this? Well, it's because the Singleton
        implementation is in a .h file, which means it gets compiled
        into anybody who includes it. This is needed for the
        Singleton template to work, but we actually only want it
        compiled into the implementation of the class based on the
        Singleton, not all of them. If we don't change this, we get
        link errors when trying to use the Singleton-based class from
        an outside dll.
    @par
        This method just delegates to the template version anyway,
        but the implementation stays in this single compilation unit,
        preventing link errors.
    */
    static ParticleSystemManager& getSingleton(void);

    /** Override standard Singleton retrieval.
    @remarks
        Why do we do this? Well, it's because the Singleton
        implementation is in a .h file, which means it gets compiled
        into anybody who includes it. This is needed for the
        Singleton template to work, but we actually only want it
        compiled into the implementation of the class based on the
        Singleton, not all of them. If we don't change this, we get
        link errors when trying to use the Singleton-based class from
        an outside dll.
    @par
        This method just delegates to the template version anyway,
        but the implementation stays in this single compilation unit,
        preventing link errors.
    */
    static ParticleSystemManager* getSingletonPtr(void);
    
    /**
    *create a particle object
    *
    *@templateName template of particle, realize a particle effect
    *@objectName the name of particle object, we can find the object through objectName
    *@position the position of the particle object 
    */
    void createParticleSystem(int templateName, const String& objectName, SE_Vector3f position);
    void createParticleSystem(int templateName, const String& objectName, const String& attachedSpatialName);

    /**
    *delete a particle object
    *
    *@particleName the name of particle object to delete
    */
    void destroyParticleSystem(const String& objectName);

    /**
    * set attribute of the particle system or emitter or affector
    *
    *@particleName  name of the particle object 
    *@attribute  which attribute of the particle system or emitter or affector
    *@type  the type of the emitter or the affector
    *@x  the value of the attribute
    *@y  the value of the attribute
    *@z  the value of the attribute
    *@index  the index of the color or time adjust
    */
    void setAttribute(const String& particleName, int attribute, const String& type, float x, float y, float z, int i);

    //update each frame
    void update(SE_TimeMS realDelta, SE_TimeMS simulateDelta);
    /**
    *set the path of the particle object
    *
    *@objectName the name of particle object, we can find the object through objectName
    *@path the path of the particle texture image
    */
    void setImagePath(const char* imagePath, const String& objectName);

    virtual void createEffect(SE_Effect effect, SE_Vector3f position,const char* mainImagePathOrId,const char* helpImagePathOrId,SE_Vector3f boxsize);
    virtual void createEffect( SE_Effect effect, const char* attachedSpatialName,const char* mainImagePathOrId,const char* helpImagePathOrId,SE_Vector3f boxsize,SE_Vector2f particleDimension);
    void removeEffect(SE_Effect effect);
};

#endif

