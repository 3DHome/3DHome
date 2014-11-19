#include "SE_DynamicLibType.h"
#include <iostream>
#include <sstream> 
#include "SE_Log.h"
#include "SE_Vector.h"
#include "ParticleSystem/SE_ParticleSystemManager.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_ParticleBillboardRenderer.h"
#include "ParticleSystem/SE_ParticleEmitterFactory.h"
#include "ParticleSystem/SE_ParticleAffectorFactory.h"
#include "ParticleSystem/SE_Particle.h"
#include "ParticleSystem/SE_ParticleEmitter.h"
#include "ParticleSystem/SE_ParticleSystem.h"
#include "ParticleSystem/SE_ParticleVector3.h"
#include "ParticleSystem/SE_ParticlePointEmitter.h"
#include "ParticleSystem/SE_ParticleBoxEmitter.h"
#include "ParticleSystem/SE_ParticleBillboardRenderer.h"
#include "ParticleSystem/SE_ParticleSystemRenderer.h"
#include "ParticleSystem/SE_ParticleAffector.h"
#include "ParticleSystem/SE_ParticleLinearForceAffector.h"
#include "ParticleSystem/SE_ParticleDirectionRandomiserAffector.h"
#include "ParticleSystem/SE_ParticleRotationAffector.h"
#include "ParticleSystem/SE_ParticleScaleAffector.h"
#include "ParticleSystem/SE_ParticleDeflectorPlaneAffector.h"
#include "ParticleSystem/SE_ParticleCylinderEmitter.h"
#include "ParticleSystem/SE_ParticleEllipsoidEmitter.h"
#include "ParticleSystem/SE_ParticleHollowEllipsoidEmitter.h"
#include "ParticleSystem/SE_ParticleRingEmitter.h"
#include "ParticleSystem/SE_ParticleColourFaderAffector.h"
#include "ParticleSystem/SE_ParticleColourInterpolatorAffector.h"
#include "ParticleSystem/SE_ParticleColourFaderAffector2.h"
#include "ParticleSystem/SE_ParticleFollowAttachedPointPositionAffector.h"
#include "SE_Spatial.h"
#include "SE_SceneManager.h"
#include "SE_Application.h"
#include "SE_MemLeakDetector.h"

#define FACTOR  0.017453292f
// Shortcut to set up billboard particle renderer
BillboardParticleRendererFactory* mBillboardRendererFactory = 0;

template<> ParticleSystemManager* Singleton<ParticleSystemManager>::ms_Singleton = 0;

ParticleSystemManager* ParticleSystemManager::getSingletonPtr(void)
{
    return ms_Singleton;
}

ParticleSystemManager& ParticleSystemManager::getSingleton(void)
{  
    assert( ms_Singleton );  return ( *ms_Singleton );  
}

ParticleSystemManager::ParticleSystemManager()
{
    mFactory = new ParticleSystemFactory();		
}

ParticleSystemManager::~ParticleSystemManager()
{
    // Destroy all templates
    ParticleTemplateMap::iterator t;
    for (t = mSystemTemplates.begin(); t != mSystemTemplates.end(); ++t)
    {
        delete t->second;
    }
    mSystemTemplates.clear();

    ParticleSystemMap::iterator pt;
    for (pt = mParticleSystems.begin(); pt != mParticleSystems.end(); ++pt)
    {
        delete pt->second;
    }
    mParticleSystems.clear();

    ParticleEmitterFactoryMap::iterator eFact;
    for (eFact = mEmitterFactories.begin(); eFact != mEmitterFactories.end(); ++eFact)
    {
	delete eFact->second;
    }
    mEmitterFactories.clear();

    ParticleAffectorFactoryMap::iterator aFact;
    for (aFact = mAffectorFactories.begin(); aFact != mAffectorFactories.end(); ++aFact)
    {
	delete aFact->second;
    }
    mAffectorFactories.clear();

    // delete billboard factory
    if (mBillboardRendererFactory)
    {
        delete mBillboardRendererFactory;
	mBillboardRendererFactory = 0;
    }

    if (mFactory)
    {
	// delete particle system factory
	delete mFactory;
	mFactory = 0;
    }
}	

Real ParticleSystemManager::getLoadingOrder(void) const
{
    /// Load late
    return 1000.0f;
}

void ParticleSystemManager::addEmitterFactory(ParticleEmitterFactory* factory)
{

    String name = factory->getName();
    mEmitterFactories[name] = factory;
}

void ParticleSystemManager::addAffectorFactory(ParticleAffectorFactory* factory)
{

    String name = factory->getName();
    mAffectorFactories[name] = factory;

}

void ParticleSystemManager::addRendererFactory(ParticleSystemRendererFactory* factory)
{
    String name = factory->getType();
    mRendererFactories[name] = factory;
}
	
void ParticleSystemManager::addTemplate(const String& name, ParticleSystem* sysTemplate)
{
    // check name

    mSystemTemplates[name] = sysTemplate;
}

void ParticleSystemManager::removeTemplate(const String& name, bool deleteTemplate)
{
    ParticleTemplateMap::iterator itr = mSystemTemplates.find(name);

    if (deleteTemplate)
    {
        delete itr->second;
    }

    mSystemTemplates.erase(itr);
}

void ParticleSystemManager::removeAllTemplates(bool deleteTemplate)
{
    if (deleteTemplate)
    {
        ParticleTemplateMap::iterator itr;
        for (itr = mSystemTemplates.begin(); itr != mSystemTemplates.end(); ++itr)
        {
            delete itr->second;
        }
    }

    mSystemTemplates.clear();
}

void ParticleSystemManager::removeTemplatesByResourceGroup(const String& resourceGroup)
{		
    ParticleTemplateMap::iterator i = mSystemTemplates.begin();
    while (i != mSystemTemplates.end())
    {
	ParticleTemplateMap::iterator icur = i++;

	if (icur->second->getResourceGroupName() == resourceGroup)
	{
	    delete icur->second;
	    mSystemTemplates.erase(icur);
	}
    }    
}

ParticleSystem* ParticleSystemManager::createTemplate(const String& name, const String& resourceGroup)
{
    // check name

    ParticleSystem* tpl = new ParticleSystem(name, resourceGroup);
    addTemplate(name, tpl);
    return tpl;
}

ParticleSystem* ParticleSystemManager::getTemplate(const String& name)
{
    ParticleTemplateMap::iterator i = mSystemTemplates.find(name);
    if (i != mSystemTemplates.end())
    {
        return i->second;
    }
    else
    {
        return 0;
    }
}

ParticleSystem* ParticleSystemManager::createSystemImpl(const String& name,
	size_t quota, const String& resourceGroup)
{
    ParticleSystem* sys = new ParticleSystem(name, resourceGroup);
    sys->setParticleQuota(quota);
    return sys;
}

ParticleSystem* ParticleSystemManager::createSystemImpl(const String& name, const String& templateName)
{
    // Look up template
    ParticleSystem* pTemplate = getTemplate(templateName);

    ParticleSystem* sys = createSystemImpl(name, pTemplate->getParticleQuota(), 
            pTemplate->getResourceGroupName());
    // Copy template settings
    *sys = *pTemplate;
    return sys;       
}

void ParticleSystemManager::destroySystemImpl(ParticleSystem* sys)
{
    delete sys;
}

ParticleEmitter* ParticleSystemManager::_createEmitter(
        const String& emitterType, ParticleSystem* psys)
{
    // Locate emitter type
    ParticleEmitterFactoryMap::iterator pFact = mEmitterFactories.find(emitterType);

    return pFact->second->createEmitter(psys);
}

void ParticleSystemManager::_destroyEmitter(ParticleEmitter* emitter)
{
    // Destroy using the factory which created it
    ParticleEmitterFactoryMap::iterator pFact = mEmitterFactories.find(emitter->getType());

    pFact->second->destroyEmitter(emitter);
}
 
ParticleAffector* ParticleSystemManager::_createAffector(
        const String& affectorType, ParticleSystem* psys)
{
    // Locate affector type
    ParticleAffectorFactoryMap::iterator pFact = mAffectorFactories.find(affectorType);

    return pFact->second->createAffector(psys);
}

void ParticleSystemManager::_destroyAffector(ParticleAffector* affector)
{

    // Destroy using the factory which created it
    ParticleAffectorFactoryMap::iterator pFact = mAffectorFactories.find(affector->getType());

    pFact->second->destroyAffector(affector);
}

ParticleSystemRenderer* ParticleSystemManager::_createRenderer(const String& rendererType)
{
    // Locate affector type
    ParticleSystemRendererFactoryMap::iterator pFact = mRendererFactories.find(rendererType);

    return pFact->second->createInstance(rendererType);
}

void ParticleSystemManager::_destroyRenderer(ParticleSystemRenderer* renderer)
{
    // Destroy using the factory which created it
    ParticleSystemRendererFactoryMap::iterator pFact = mRendererFactories.find(renderer->getType());


    pFact->second->destroyInstance(renderer);
}

void ParticleSystemManager::_initialise(void)
{
    // Create Billboard renderer factory
    mBillboardRendererFactory = new BillboardParticleRendererFactory();
    addRendererFactory(mBillboardRendererFactory);

    this->addEmitterFactory(new PointEmitterFactory()); 
    this->addEmitterFactory(new BoxEmitterFactory()); 
    this->addEmitterFactory(new RingEmitterFactory()); 
    this->addEmitterFactory(new CylinderEmitterFactory()); 
    this->addEmitterFactory(new EllipsoidEmitterFactory()); 
    this->addEmitterFactory(new HollowEllipsoidEmitterFactory()); 
 
    this->addAffectorFactory(new LinearForceAffectorFactory); 
    this->addAffectorFactory(new ColourFaderAffectorFactory); 
    this->addAffectorFactory(new ColourFaderAffectorFactory2); 
    this->addAffectorFactory(new ColourInterpolatorAffectorFactory); 
    this->addAffectorFactory(new ScaleAffectorFactory); 
    this->addAffectorFactory(new RotationAffectorFactory); 
    this->addAffectorFactory(new DirectionRandomiserAffectorFactory); 
    this->addAffectorFactory(new DeflectorPlaneAffectorFactory);

    this->addAffectorFactory(new FollowAffectorFactory);
}

ParticleSystemManager::ParticleAffectorFactoryIterator
ParticleSystemManager::getAffectorFactoryIterator(void)
{
    return mAffectorFactories.begin();
}
	
ParticleSystemManager::ParticleEmitterFactoryIterator
ParticleSystemManager::getEmitterFactoryIterator(void)
{
    return mEmitterFactories.begin();
}

ParticleSystemManager::ParticleRendererFactoryIterator	
ParticleSystemManager::getRendererFactoryIterator(void)
{
    return mRendererFactories.begin();
}

String ParticleSystemFactory::FACTORY_TYPE_NAME = "ParticleSystem";

ParticleSystem* ParticleSystemFactory::createInstanceImpl( const String& name, 
	const NameValuePairList* params)
{
    if (params != 0)
    {
	NameValuePairList::const_iterator ni = params->find("templateName");
	if (ni != params->end())
	{
	    String templateName = ni->second;
	    // create using manager
	    return ParticleSystemManager::getSingleton().createSystemImpl(
		    name, templateName);
	}
    }

    // Not template based, look for quota & resource name
    size_t quota = 500;

    if (params != 0)
    {
	NameValuePairList::const_iterator ni = params->find("quota");
	if (ni != params->end())
	{
	    std::istringstream str(ni->second);
	    unsigned int ret = 500;
	    str >> ret;
	    quota = ret;
	} 			
    }

    // create using manager
    return ParticleSystemManager::getSingleton().createSystemImpl(name, quota, "");			
}
    
const String& ParticleSystemFactory::getType(void) const
{
    return FACTORY_TYPE_NAME;
}

void ParticleSystemFactory::destroyInstance( ParticleSystem* obj) 
{
    // use manager
    ParticleSystemManager::getSingleton().destroySystemImpl(
	    static_cast<ParticleSystem*>(obj));
}

void ParticleSystemManager::setAttribute(const String& particleName, int attribute, 
        const String& type, float x, float y, float z, int index)
{
    ParticleSystem* ps;
    ParticleEmitter* emitter;
    ParticleAffector* affector;

    //status list
    std::vector<ParticleEmitter*> particleEmitterVec;    
    std::vector<ParticleAffector*> particleAffectorVec;    

    ParticleSystemMap::iterator t;
    for (t = mParticleSystems.begin(); t != mParticleSystems.end(); ++t)
    {
        ParticleSystem* particleSystem = t->second;
	if (particleSystem->getName() == particleName)
	{
            ps = particleSystem;
	    break;
	}			
     }	

    for (int i = 0; i < ps->getNumEmitters(); i++)
    {
	if (ps->getEmitter(i)->getType() == type)
	{
	    if (ps->getEmitter(i)->getType() == "Box") 
	    {
                emitter = (BoxEmitter*)(ps->getEmitter(i));
	    } 
	    else if (ps->getEmitter(i)->getType() == "Point") 
	    {
                emitter = (PointEmitter*)(ps->getEmitter(i));
	    } 
	    else if (ps->getEmitter(i)->getType() == "Cylinder") 
	    {
                emitter = (CylinderEmitter*)(ps->getEmitter(i));
	    } 
	    else if (ps->getEmitter(i)->getType() == "Ellipsoid") 
	    {
                emitter = (EllipsoidEmitter*)(ps->getEmitter(i));
	    } 
	    else if (ps->getEmitter(i)->getType() == "HollowEllipsoid") 
	    {
                emitter = (HollowEllipsoidEmitter*)(ps->getEmitter(i));
	    } 
	        else if (ps->getEmitter(i)->getType() == "Ring") 
            {
                emitter = (RingEmitter*)(ps->getEmitter(i));
	    }
            particleEmitterVec.push_back(emitter);
	}
    }
        
    for (int j = 0; j < ps->getNumAffectors(); j++) 
    {
        if (ps->getAffector(j)->getType() == type) 
	{
	    if (type == "LinearForce") 
	    {
                    affector = (LinearForceAffector*)(ps->getAffector(j));
	    } 
	    else if (type == "ColourInterpolator") 
	    {
                    affector = (ColourInterpolatorAffector*)(ps->getAffector(j));
	    } 
	    else if (type == "ColourFader") 
	    {
                    affector = (ColourFaderAffector*)(ps->getAffector(j));
	    } 
	    else if (type == "ColourFader2") 
	    {
                    affector = (ColourFaderAffector2*)(ps->getAffector(j));
	    } 
	    else if (type == "DeflectorPlane") 
	    {
                    affector = (DeflectorPlaneAffector*)(ps->getAffector(j));
	    } 
	    else if (type == "DirectionRandomiser") 
	    {
                    affector = (DirectionRandomiserAffector*)(ps->getAffector(j));
	    } 
	    else if (type == "Rotator") 
	    {
                    affector = (RotationAffector*)(ps->getAffector(j));
	    } 
	    else if (type == "Scaler") 
	    {
                    affector = (ScaleAffector*)(ps->getAffector(j));
	    }
            else if (type == "Follower") 
	        {
                    affector = (FollowAffector*)(ps->getAffector(j));
	        }
            particleAffectorVec.push_back(affector);
	}
    }

    switch (attribute)
    {
	case WIDTH:			
        ps->setDefaultWidth(x);						
	break;

	case HEIGHT:
        ps->setDefaultHeight(y);
	break;

	case DIMENSION:
        ps->setDefaultDimensions(x, y);
	break;

	case PARTICLEQUOTA:
	ps->setParticleQuota(x);
	break;

	case EMITTERQUOTA:
	ps->setEmittedEmitterQuota(x);
	break;
    }

    for(int i = 0; i < particleEmitterVec.size(); ++i)
    {
        switch (attribute)
        {
	case ANGLE:
	emitter->setAngle(Radian(FACTOR * x));
	break;

	case COLOUR:
        emitter->setColour(ColourValue(x, y, z));
	break;

	case COLOURRANGEEND:
        emitter->setColourRangeEnd(ColourValue(x, y, z));
	break;

	case COLOURRANGESTART:
        emitter->setColourRangeStart(ColourValue(x, y, z));
	break;

	case DIRECTION:
	emitter->setDirection(Vector3(x, y, z));
	break;

	case DURATION:
        emitter->setDuration(x);
	break;

	case EMISSIONRATE:
        emitter->setEmissionRate(x);
	break;

	case EMITTED:
        emitter->setEmitted(x);
	break;

	case EMITTEDEMITTER:
        //emitter->setEmittedEmitter();
	break;

        case ENABLED:
        emitter->setEnabled(x);
	break;

	case NAME:
        //emitter->setName();
	break;

	case PARTICLEVELOCITY:
	emitter->setParticleVelocity(x);
	break;

	case POSITION:
        emitter->setPosition(Vector3(x, y, z));
	break;

	case ROTATION:
        emitter->setRotation(Radian(FACTOR * x));
	break;

	case REPEATDELAY:
        emitter->setRepeatDelay(x);
	break;

	case STARTTIME:
        emitter->setStartTime(x);
	break;

	case TIMETOLIVE:
	emitter->setTimeToLive(x);
	break;

	case BOXWIDTH:
        ((BoxEmitter*)emitter)->setWidth(x);
	break;

	case BOXHEIGHT:
	((BoxEmitter*)emitter)->setHeight(y);
	break;

	case BOXDEPTH:
	((BoxEmitter*)emitter)->setDepth(z);
	break;

	case HEINNERSIZE:  
	((HollowEllipsoidEmitter*)emitter)->setInnerSize(x, y, z);
	break;
				
	case HEINNERSIZEX:
        ((HollowEllipsoidEmitter*)emitter)->setInnerSizeX(x);
	break;
			
	case HEINNERSIZEY:
        ((HollowEllipsoidEmitter*)emitter)->setInnerSizeY(y);
	break;
				
	case HEINNERSIZEZ:
        ((HollowEllipsoidEmitter*)emitter)->setInnerSizeZ(z);
	break;

	case RINGEINNERSIZE:
        ((RingEmitter*)emitter)->setInnerSize(x, y);
	break;

	case RINGINNERSIZEX:
        ((RingEmitter*)emitter)->setInnerSizeX(x);
	break;

	case RINGINNERSIZEY:
        ((RingEmitter*)emitter)->setInnerSizeY(y);
	break;
        }
    }

    for(int i = 0; i < particleAffectorVec.size(); ++i)
    {
        switch (attribute)
        {
	case FORCEVECTOR:			
            ((LinearForceAffector*)affector)->setForceVector(Vector3(x, y, z));
	break;

	case COLOURINTERPOLATORADJUST:
	    ((ColourInterpolatorAffector*)affector)->setColourAdjust(index,ColourValue(x, y, z));
	break;

	case TIMEADJUST:
            ((ColourInterpolatorAffector*)affector)->setTimeAdjust(index, x);
	break;

	case COLOURFADERADJUST:
	    ((ColourFaderAffector*)affector)->setAdjust(x, y, z, index);			
	break;

	case REDADJUST:
            ((ColourFaderAffector*)affector)->setRedAdjust(x);
	break;

	case GREENADJUST:
            ((ColourFaderAffector*)affector)->setGreenAdjust(y);
	break;

	case BLUEADJUST:
            ((ColourFaderAffector*)affector)->setBlueAdjust(z);
	break;

	case ALPHAADJUST:
            ((ColourFaderAffector*)affector)->setAlphaAdjust(index);
	break;

	case ADJUST1:
            ((ColourFaderAffector2*)affector)->setAdjust1(x, y, z, index);
	break;

	case ADJUST2:
            ((ColourFaderAffector2*)affector)->setAdjust2(x, y, z, index);
	break;

	case REDADJUST1:
            ((ColourFaderAffector2*)affector)->setRedAdjust1(x);
	break;

	case REDADJUST2:
            ((ColourFaderAffector2*)affector)->setRedAdjust2(x);
	break;

	case GREENADJUST1:
            ((ColourFaderAffector2*)affector)->setGreenAdjust1(y);
	break;

	case GREENADJUST2:
            ((ColourFaderAffector2*)affector)->setGreenAdjust2(y);
	break;

	case BLUEADJUST1:
            ((ColourFaderAffector2*)affector)->setBlueAdjust1(z);
	break;

	case BLUEADJUST2:
            ((ColourFaderAffector2*)affector)->setBlueAdjust2(z);
	break;

	case ALPHAADJUST1:
            ((ColourFaderAffector2*)affector)->setAlphaAdjust1(index);
	break;

	case ALPHAADJUST2:
            ((ColourFaderAffector2*)affector)->setAlphaAdjust2(index);
	break;

	case STATECHANGE:
            ((ColourFaderAffector2*)affector)->setStateChange(x);
	break;

	case PLANEPOINT:
            ((DeflectorPlaneAffector*)affector)->setPlanePoint(Vector3(x, y, z));
	break;

	case PLANENORMAL:
            ((DeflectorPlaneAffector*)affector)->setPlaneNormal(Vector3(x, y, z));
	break;

	case BOUNCE:
            ((DeflectorPlaneAffector*)affector)->setBounce(x);
	break;

	case RANDOMNESS:
            ((DirectionRandomiserAffector*)affector)->setRandomness(x);
	break;

	case SCOPE:
            ((DirectionRandomiserAffector*)affector)->setScope(y);
	break;

	case KEEPVELOCITY:
            ((DirectionRandomiserAffector*)affector)->setKeepVelocity(z);
	break;

	case ROTATIONSPEEDRANGESTART:
            ((RotationAffector*)affector)->setRotationSpeedRangeStart(Radian(FACTOR * x));
	break;

	case ROTATIONSPEEDRANGEEND:
            ((RotationAffector*)affector)->setRotationSpeedRangeEnd(Radian(FACTOR * y));
	break;

	case ROTATIONRANGESTART:
            ((RotationAffector*)affector)->setRotationRangeStart(Radian(FACTOR * x));
	break;

	case ROTATIONRANGEEND:
            ((RotationAffector*)affector)->setRotationRangeEnd(Radian(FACTOR * y));
	break;

	case SCALEADJUST:
            ((ScaleAffector*)affector)->setAdjust(x);
	break;
        case FOLLOWPOSITION:
            ((FollowAffector*)affector)->setCurrentPos(Vector3(x,y,z));

    }
}
}

void ParticleSystemManager::setEmitterAttribute(ParticleEmitter* emitter, int attribute, float x, float y, float z, float w)
{
	if (!emitter)
	{
		return;
	}
	String type = emitter->getType();
	if (type == "Box")
	{
		emitter = (BoxEmitter*)emitter;
	}
	else if (type == "Point")
	{
		emitter = (PointEmitter*)emitter;
	}
	else if (type == "Cylinder")
	{
		emitter = (CylinderEmitter*)emitter;
	}
	else if (type == "Ellipsoid")
	{
		emitter = (EllipsoidEmitter*)emitter;
	}
	else if (type == "HollowEllipsoid")
	{
		emitter = (HollowEllipsoidEmitter*)emitter;
	}
	else if (type == "Ring")
	{
		emitter = (RingEmitter*)emitter;
	}

	switch (attribute)
	{
	case ANGLE:
		emitter->setAngle(Radian(FACTOR * x));
		break;

	case COLOUR:
		emitter->setColour(ColourValue(x, y, z, w));
		break;

	case COLOURRANGEEND:
		emitter->setColourRangeEnd(ColourValue(x, y, z, w));
		break;

	case COLOURRANGESTART:
		emitter->setColourRangeStart(ColourValue(x, y, z, w));
		break;

	case DIRECTION:
		emitter->setDirection(Vector3(x, y, z));
		break;

	case DURATION:
		emitter->setDuration(x);
		break;

	case EMISSIONRATE:
		emitter->setEmissionRate(x);
		break;

	case EMITTED:
		emitter->setEmitted(x);
		break;

	case EMITTEDEMITTER:
		//emitter->setEmittedEmitter();
		break;

	case ENABLED:
		emitter->setEnabled(x);
		break;

	case NAME:
		//emitter->setName();
		break;

	case PARTICLEVELOCITY:
		emitter->setParticleVelocity(x, y);
		break;

	case POSITION:
		emitter->setPosition(Vector3(x, y, z));
		break;

	case ROTATION:
		emitter->setRotation(Radian(FACTOR * x));
		break;

	case REPEATDELAY:
		emitter->setRepeatDelay(x);
		break;

	case STARTTIME:
		emitter->setStartTime(x);
		break;

	case TIMETOLIVE:
		emitter->setTimeToLive(x, y);
		break;

	case BOXWIDTH:
		((BoxEmitter*)emitter)->setWidth(x);
		break;

	case BOXHEIGHT:
		((BoxEmitter*)emitter)->setHeight(y);
		break;

	case BOXDEPTH:
		((BoxEmitter*)emitter)->setDepth(z);
		break;

	case HEINNERSIZE:
		((HollowEllipsoidEmitter*)emitter)->setInnerSize(x, y, z);
		break;

	case HEINNERSIZEX:
		((HollowEllipsoidEmitter*)emitter)->setInnerSizeX(x);
		break;

	case HEINNERSIZEY:
		((HollowEllipsoidEmitter*)emitter)->setInnerSizeY(y);
		break;

	case HEINNERSIZEZ:
		((HollowEllipsoidEmitter*)emitter)->setInnerSizeZ(z);
		break;

	case RINGEINNERSIZE:
		((RingEmitter*)emitter)->setInnerSize(x, y);
		break;

	case RINGINNERSIZEX:
		((RingEmitter*)emitter)->setInnerSizeX(x);
		break;

	case RINGINNERSIZEY:
		((RingEmitter*)emitter)->setInnerSizeY(y);
		break;
	}
}

void ParticleSystemManager::setAffectorAttribute(ParticleAffector* affector, int attribute,
		float x, float y, float z, float w, int index)
{
	if (!affector) {
		return;
	}
	String type = affector->getType();
	if (type == "LinearForce")
	{
		affector = (LinearForceAffector*)affector;
	}
	else if (type == "ColourInterpolator")
	{
		affector = (ColourInterpolatorAffector*)affector;
	}
	else if (type == "ColourFader")
	{
		affector = (ColourFaderAffector*)affector;
	}
	else if (type == "ColourFader2")
	{
		affector = (ColourFaderAffector2*)affector;
	}
	else if (type == "DeflectorPlane")
	{
		affector = (DeflectorPlaneAffector*)affector;
	}
	else if (type == "DirectionRandomiser")
	{
		affector = (DirectionRandomiserAffector*)affector;
	}
	else if (type == "Rotator")
	{
		affector = (RotationAffector*)affector;
	}
	else if (type == "Scaler")
	{
		affector = (ScaleAffector*)affector;
	}
	else if (type == "Follower")
	{
		affector = (FollowAffector*)affector;
	}

	switch (attribute)
	{
	case FORCEVECTOR:
		((LinearForceAffector*)affector)->setForceVector(Vector3(x, y, z));
		((LinearForceAffector*)affector)->setForceApplication(LinearForceAffector::FA_ADD);
		break;

	case COLOURINTERPOLATORADJUST:
		((ColourInterpolatorAffector*)affector)->setColourAdjust(index,ColourValue(x, y, z));
		break;

	case TIMEADJUST:
		((ColourInterpolatorAffector*)affector)->setTimeAdjust(index, x);
		break;

	case COLOURFADERADJUST:
		((ColourFaderAffector*)affector)->setAdjust(x, y, z, w);
		break;

	case REDADJUST:
		((ColourFaderAffector*)affector)->setRedAdjust(x);
		break;

	case GREENADJUST:
		((ColourFaderAffector*)affector)->setGreenAdjust(y);
		break;

	case BLUEADJUST:
		((ColourFaderAffector*)affector)->setBlueAdjust(z);
		break;

	case ALPHAADJUST:
		((ColourFaderAffector*)affector)->setAlphaAdjust(index);
		break;

	case ADJUST1:
		((ColourFaderAffector2*)affector)->setAdjust1(x, y, z, index);
		break;

	case ADJUST2:
		((ColourFaderAffector2*)affector)->setAdjust2(x, y, z, index);
		break;

	case REDADJUST1:
		((ColourFaderAffector2*)affector)->setRedAdjust1(x);
		break;

	case REDADJUST2:
		((ColourFaderAffector2*)affector)->setRedAdjust2(x);
		break;

	case GREENADJUST1:
		((ColourFaderAffector2*)affector)->setGreenAdjust1(y);
		break;

	case GREENADJUST2:
		((ColourFaderAffector2*)affector)->setGreenAdjust2(y);
		break;

	case BLUEADJUST1:
		((ColourFaderAffector2*)affector)->setBlueAdjust1(z);
		break;

	case BLUEADJUST2:
		((ColourFaderAffector2*)affector)->setBlueAdjust2(z);
		break;

	case ALPHAADJUST1:
		((ColourFaderAffector2*)affector)->setAlphaAdjust1(index);
		break;

	case ALPHAADJUST2:
		((ColourFaderAffector2*)affector)->setAlphaAdjust2(index);
		break;

	case STATECHANGE:
		((ColourFaderAffector2*)affector)->setStateChange(x);
		break;

	case PLANEPOINT:
		((DeflectorPlaneAffector*)affector)->setPlanePoint(Vector3(x, y, z));
		break;

	case PLANENORMAL:
		((DeflectorPlaneAffector*)affector)->setPlaneNormal(Vector3(x, y, z));
		break;

	case BOUNCE:
		((DeflectorPlaneAffector*)affector)->setBounce(x);
		break;

	case RANDOMNESS:
		((DirectionRandomiserAffector*)affector)->setRandomness(x);
		break;

	case SCOPE:
		((DirectionRandomiserAffector*)affector)->setScope(y);
		break;

	case KEEPVELOCITY:
		((DirectionRandomiserAffector*)affector)->setKeepVelocity(z);
		break;

	case ROTATIONSPEEDRANGESTART:
		((RotationAffector*)affector)->setRotationSpeedRangeStart(Radian(FACTOR * x));
		break;

	case ROTATIONSPEEDRANGEEND:
		((RotationAffector*)affector)->setRotationSpeedRangeEnd(Radian(FACTOR * y));
		break;

	case ROTATIONRANGESTART:
		((RotationAffector*)affector)->setRotationRangeStart(Radian(FACTOR * x));
		break;

	case ROTATIONRANGEEND:
		((RotationAffector*)affector)->setRotationRangeEnd(Radian(FACTOR * y));
		break;

	case SCALEADJUST:
		((ScaleAffector*)affector)->setAdjust(x);
		break;
	case FOLLOWPOSITION:
		((FollowAffector*)affector)->setCurrentPos(Vector3(x,y,z));
		break;
	}
}

ParticleSystem* ParticleSystemManager::createParticleSystem(const String& name)
{
    ParticleSystemMap::iterator t;
    for (t = mParticleSystems.begin(); t != mParticleSystems.end(); ++t)
    {
        ParticleSystem* ps = t->second;
	if (ps->getName() == name)
	{
            return ps;
	}
    }
    return new ParticleSystem(name);
}

void ParticleSystemManager::addParticleSystem(const String& name, ParticleSystem* ps, bool needDepthTest, int layerIndex)
{
	mParticleSystems[name] = ps;
	if (needDepthTest)
	{
		SE_Spatial* particle = SE_Application::getInstance()->getSceneManager()->findSpatialByName(((BillboardParticleRenderer*)ps->getRenderer())->getBillboardSet()->getSpatial()->getSpatialName());
		if(particle)
		{
			particle->setSpatialEffectAttribute(SE_SpatialAttribute::BLENDX,false);
			particle->setSpatialEffectAttribute(SE_SpatialAttribute::BLENDY,true);
			particle->setSpatialEffectAttribute(SE_SpatialAttribute::BLENDZ,false);

			particle->getWorldLayer()->setLayer(layerIndex);
			particle->setNeedDepthTest(needDepthTest);
		}
	}
}
			
void ParticleSystemManager::createParticleSystem(int templateName, const String& objectName,SE_Vector3f position)
{
    ParticleSystem*  ps;
    Vector3 pos;
    pos.x = position.x;
    pos.y = position.y;
    pos.z = position.z;

    BoxEmitter* boxEmitter;
    PointEmitter* pointEmitter;
    EllipsoidEmitter* ellipsoidEmitter;
    RingEmitter* ringEmitter;
    HollowEllipsoidEmitter* hollowEllipsoidEmitter;
    CylinderEmitter* cylinderEmitter;

    LinearForceAffector* linearForceAffector;
    ColourInterpolatorAffector* colourInterpolatorAffector;
    ColourFaderAffector2* colourFaderAffector2;
    ColourFaderAffector* colourFaderAffector;
    DeflectorPlaneAffector* deflectorPlaneAffector;
    DirectionRandomiserAffector* directionRandomiserAffector;
    RotationAffector* rotationAffector;
    ScaleAffector* scaleAffector;

    switch (templateName) 
    {
	case  Box:
        ps = new ParticleSystem(objectName);
        //ps->setDefaultNonVisibleUpdateTimeout(5);
	    ps->setParticleQuota(1);
        ps->setDefaultDimensions(800, 800);
	//ps->setCullIndividually(true);
	//ps->setSortingEnabled(true);
	//ps->setKeepParticlesInLocalSpace(false);
        
        boxEmitter = (BoxEmitter*) ps->addEmitter("Box");       
        boxEmitter->setAngle(Radian(FACTOR*30));
	    boxEmitter->setDepth(0);
	    boxEmitter->setHeight(0);
	    boxEmitter->setWidth(0);
	boxEmitter->setDirection(Vector3(0, 1, 0));	
	boxEmitter->setEmissionRate(10);
	boxEmitter->setEnabled(true);		    
	boxEmitter->setName("mainEmitter");
	    boxEmitter->setParticleVelocity(0, 0);
	boxEmitter->setPosition(pos);
	    boxEmitter->setTimeToLive(1, 1);		    


        /*scaleAffector = (ScaleAffector *)ps->addAffector("Scaler");
        scaleAffector->setAdjust(1.2);*/

        /*colourFaderAffector = (ColourFaderAffector*)ps->addAffector("ColourFader");
        colourFaderAffector->setRedAdjust(-0.0);
        colourFaderAffector->setGreenAdjust(-0.0);
        colourFaderAffector->setBlueAdjust(-0.0);
        colourFaderAffector->setAlphaAdjust(-1.0);*/

        /*colourFaderAffector2 = (ColourFaderAffector2*)ps->addAffector("ColourFader2");
        colourFaderAffector2->setStateChange(0.25);
        colourFaderAffector2->setRedAdjust1(-0.0);
        colourFaderAffector2->setGreenAdjust1(-0.0);
        colourFaderAffector2->setBlueAdjust1(-0.0);
        colourFaderAffector2->setAlphaAdjust1(-0.3);

        colourFaderAffector2->setAlphaAdjust2(0.5);*/
        colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
        colourInterpolatorAffector->setTimeAdjust(0, 0);
        colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 1,0.9));
        colourInterpolatorAffector->setTimeAdjust(1, 0.5f);
        colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 1, 1,0.7));
        colourInterpolatorAffector->setTimeAdjust(2, 1.0f);
        colourInterpolatorAffector->setColourAdjust(2,ColourValue(1, 1, 1,0.9));
        break;

	case Fireworks:
	ps = new ParticleSystem(objectName);
	ps->setParticleQuota(1000);
	ps->setEmittedEmitterQuota(10);
        ps->setDefaultDimensions(3,3);

        boxEmitter = (BoxEmitter*)ps->addEmitter("Box");
	//boxEmitter->setName("mainEmitter");
	boxEmitter->setEmittedEmitter("explosion");
	boxEmitter->setAngle(Radian(FACTOR * 30));
	boxEmitter->setEmissionRate(100);
	boxEmitter->setTimeToLive(3);
	boxEmitter->setDirection(Vector3(0, 0, 1));
	boxEmitter->setParticleVelocity(6);
	boxEmitter->setPosition(pos);
        boxEmitter->setDepth(20);
        boxEmitter->setHeight(20);
        boxEmitter->setWidth(20);

        pointEmitter= (PointEmitter*)ps->addEmitter("Point");
	pointEmitter->setName("explosion");
        pointEmitter->setAngle(Radian(FACTOR * 180));
	pointEmitter->setEmissionRate(100);
	pointEmitter->setTimeToLive(4);
	pointEmitter->setDirection(Vector3(0, 0, 1));
	pointEmitter->setParticleVelocity(6);
	pointEmitter->setDuration(0.5f);
	pointEmitter->setRepeatDelay(2, 3);
			
	linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
	linearForceAffector->setForceVector(Vector3(0, 0, -1));
	linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

	colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
	colourInterpolatorAffector->setTimeAdjust(0, 0);
	colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 0));
	colourInterpolatorAffector->setTimeAdjust(1, 0.5f);
	colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 0, 0));
        colourInterpolatorAffector->setTimeAdjust(2, 0.9f);
	colourInterpolatorAffector->setColourAdjust(2,ColourValue(0, 0, 1));
	break;

	case Aureola:
	ps = new ParticleSystem(objectName);
	ps->setParticleQuota(50);
        ps->setDefaultDimensions(100, 100);
	((BillboardParticleRenderer*)ps->getRenderer())->setBillboardType(BBT_PERPENDICULAR_COMMON);
        ((BillboardParticleRenderer*)ps->getRenderer())->setCommonDirection(Vector3(0, 0, 1));
	((BillboardParticleRenderer*)ps->getRenderer())->setCommonUpVector(Vector3(0, 1, 0));

	boxEmitter = (BoxEmitter*)ps->addEmitter("Box");
	boxEmitter->setName("mainEmitter");
        boxEmitter->setAngle(Radian(FACTOR * 30));
        boxEmitter->setEmissionRate(2);
	boxEmitter->setTimeToLive(30);
	boxEmitter->setDirection(Vector3(0, 1, 0));
        boxEmitter->setParticleVelocity(1);
        boxEmitter->setColourRangeStart(ColourValue(0.3f, 0.3f, 0.3f, 0.0f));
        boxEmitter->setColourRangeEnd(ColourValue(0.8f, 0.8f, 0.8f, 0.0f));
	boxEmitter->setDepth(5);
        boxEmitter->setHeight(10);
        boxEmitter->setWidth(5);
	boxEmitter->setPosition(pos);

	linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
	linearForceAffector->setForceVector(Vector3(0, 0, -10));
	linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

	colourFaderAffector2 = (ColourFaderAffector2*)ps->addAffector("ColourFader2");
        colourFaderAffector2->setAdjust1(0.4f, 0.4f, 0.4f, 0.7f);
	colourFaderAffector2->setAdjust2(-0.25f, -0.25f, -0.25f, -0.3333f);
	colourFaderAffector2->setStateChange(3.5f);
        
        rotationAffector = (RotationAffector *)ps->addAffector("Rotator");
        rotationAffector->setRotationRangeStart(Radian(0));
        rotationAffector->setRotationRangeEnd(Radian(FACTOR * 180));
        rotationAffector->setRotationSpeedRangeStart(Radian(0));
	rotationAffector->setRotationSpeedRangeEnd(Radian(FACTOR * 10)); 
	break;

	case Ring:
	ps = new ParticleSystem(objectName);
	ps->setParticleQuota(500);
        ps->setDefaultDimensions(2, 2);   
     
        ringEmitter = (RingEmitter*)ps->addEmitter("Ring"); 
	ringEmitter->setInnerSizeX(0.5f);
        ringEmitter->setInnerSizeY(0.5f);         
        ringEmitter->setAngle(Radian(FACTOR * 180));
        ringEmitter->setEmissionRate(40);
        ringEmitter->setTimeToLive(5);
	ringEmitter->setPosition(pos);
        ringEmitter->setDirection(Vector3(0, 0, 1));
	ringEmitter->setParticleVelocity(1);
        // ringEmitter->setDuration(0);
        ringEmitter->setDepth(1);
        ringEmitter->setHeight(20);
	ringEmitter->setWidth(20);

        colourFaderAffector = (ColourFaderAffector*)ps->addAffector("ColourFader");
        colourFaderAffector->setRedAdjust(-0.8f);
	colourFaderAffector->setGreenAdjust(-0.25f);
	colourFaderAffector->setBlueAdjust(-0.8f);
        break;

	case HollowEllipsoid:
	ps = new ParticleSystem(objectName);
	ps->setParticleQuota(500);
        ps->setDefaultDimensions(2,2);

        hollowEllipsoidEmitter = (HollowEllipsoidEmitter*)ps->addEmitter("HollowEllipsoid");
	hollowEllipsoidEmitter->setAngle(Radian(FACTOR * 180));
        hollowEllipsoidEmitter->setEmissionRate(20);
        hollowEllipsoidEmitter->setTimeToLive(20);
	hollowEllipsoidEmitter->setPosition(Vector3(0, 100, 0));
        hollowEllipsoidEmitter->setDirection(Vector3(0, 1, 0));
	hollowEllipsoidEmitter->setParticleVelocity(20);
        hollowEllipsoidEmitter->setDuration(0);
        hollowEllipsoidEmitter->setDepth(600);
        hollowEllipsoidEmitter->setHeight(100);
	hollowEllipsoidEmitter->setWidth(600);

	deflectorPlaneAffector = (DeflectorPlaneAffector *)ps->addAffector("DeflectorPlane");
        deflectorPlaneAffector->setPlanePoint(Vector3(0, 0, 50));
	deflectorPlaneAffector->setPlaneNormal(Vector3(0, 0, -1));
	deflectorPlaneAffector->setBounce(1);
	break;

	case Ellipsoid:
	ps = new ParticleSystem(objectName);
	ps->setParticleQuota(500);
        ps->setDefaultDimensions(2, 2);
	ellipsoidEmitter = (EllipsoidEmitter*)ps->addEmitter("Ellipsoid");
	ellipsoidEmitter->setAngle(Radian(FACTOR * 180));
        ellipsoidEmitter->setEmissionRate(20);
        ellipsoidEmitter->setTimeToLive(20);
	ellipsoidEmitter->setPosition(Vector3(0, 100, 0));
        ellipsoidEmitter->setDirection(Vector3(0, 1, 0));
	ellipsoidEmitter->setParticleVelocity(20);
        ellipsoidEmitter->setDuration(0);
        ellipsoidEmitter->setDepth(600);
        ellipsoidEmitter->setHeight(100);
	ellipsoidEmitter->setWidth(600);

        directionRandomiserAffector = (DirectionRandomiserAffector *)ps->addAffector("DirectionRandomiser");
        directionRandomiserAffector->setRandomness(10);
        directionRandomiserAffector->setScope(5);
	directionRandomiserAffector->setKeepVelocity(10);
	break;

	case Cylinder:
        ps = new ParticleSystem(objectName);
	ps->setParticleQuota(500);
        ps->setDefaultDimensions(2, 2);
	cylinderEmitter = (CylinderEmitter*)ps->addEmitter("Cylinder");
        //cylinderEmitter->setName("explosion");
	cylinderEmitter->setAngle(Radian(FACTOR * 180));
        cylinderEmitter->setEmissionRate(20);
        cylinderEmitter->setTimeToLive(20);
	cylinderEmitter->setPosition(Vector3(0, 100, 0));
        cylinderEmitter->setDirection(Vector3(0, 1, 0));
	cylinderEmitter->setParticleVelocity(20);
        cylinderEmitter->setDuration(0);
        cylinderEmitter->setDepth(600);
        cylinderEmitter->setHeight(100);
	cylinderEmitter->setWidth(600);
	scaleAffector = (ScaleAffector *)ps->addAffector("Scaler");
        scaleAffector->setAdjust(10);
        break;             
    }
		
    //if (mSystemTemplates.find(objectName) != mSystemTemplates.end())
    //{

    //}

    mParticleSystems[objectName] = ps;       
}

void ParticleSystemManager::createParticleSystem(int templateName, const String& objectName,const String& attachedSpatialName)
{
    ParticleSystem*  ps;
    SE_Spatial* attachedSp = SE_Application::getInstance()->getSceneManager()->findSpatialByName(attachedSpatialName.c_str());
    SE_Vector3f sepos;
        
    if(attachedSp)
    {
        sepos = attachedSp->getCenter();
    }
    else
    {
        if(SE_Application::getInstance()->SEHomeDebug)
        LOGI("\n Particle attached spatial[%s] not found,particle create fail!!!\n",attachedSpatialName.c_str());
        return;
    }

    Vector3 pos;
    pos.x = sepos.x;
    pos.y = sepos.y;
    pos.z = sepos.z;
    
    BoxEmitter* boxEmitter;
    BoxEmitter* boxEmitter2;
    PointEmitter* pointEmitter;
    EllipsoidEmitter* ellipsoidEmitter;
    RingEmitter* ringEmitter;
    HollowEllipsoidEmitter* hollowEllipsoidEmitter;
    CylinderEmitter* cylinderEmitter;

    LinearForceAffector* linearForceAffector;
    ColourInterpolatorAffector* colourInterpolatorAffector;
    ColourFaderAffector2* colourFaderAffector2;
    ColourFaderAffector* colourFaderAffector;
    DeflectorPlaneAffector* deflectorPlaneAffector;
    DirectionRandomiserAffector* directionRandomiserAffector;
    RotationAffector* rotationAffector;
    ScaleAffector* scaleAffector;

    switch (templateName) 
    {
	case  Box:
        ps = new ParticleSystem(objectName);
        //ps->setDefaultNonVisibleUpdateTimeout(5);
	    ps->setParticleQuota(500);
        ps->setDefaultDimensions(40, 40);
        //ps->setCullIndividually(true);
        //ps->setSortingEnabled(true);
        //ps->setKeepParticlesInLocalSpace(false);

        boxEmitter = (BoxEmitter*) ps->addEmitter("Box");       
        boxEmitter->setAngle(Radian(FACTOR*5));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter->setDepth(200);
	    boxEmitter->setHeight(1000);
	    boxEmitter->setWidth(2000);
	    boxEmitter->setDirection(Vector3(0, 0.2, -1));	
	    boxEmitter->setEmissionRate(20);
	    boxEmitter->setEnabled(true);		    
	    boxEmitter->setName("mainEmitter");
	    boxEmitter->setParticleVelocity(100, 500);
	    boxEmitter->setPosition(pos);
	    boxEmitter->setTimeToLive(5, 10);


        boxEmitter2  = (BoxEmitter*) ps->addEmitter("Box");
        boxEmitter2->setAngle(Radian(FACTOR*5));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter2->setDepth(200);
	    boxEmitter2->setHeight(1000);
	    boxEmitter2->setWidth(2000);
	    boxEmitter2->setDirection(Vector3(0, 0, 1));	
	    boxEmitter2->setEmissionRate(20);
	    boxEmitter2->setEnabled(true);		    
	    boxEmitter2->setName("helpEmitter");
	    boxEmitter2->setParticleVelocity(100, 500);
	    boxEmitter2->setPosition(pos);
	    boxEmitter2->setTimeToLive(5, 10);


        /*scaleAffector = (ScaleAffector *)ps->addAffector("Scaler");
        scaleAffector->setAdjust(1.2);*/

        /*colourFaderAffector = (ColourFaderAffector*)ps->addAffector("ColourFader");
        colourFaderAffector->setRedAdjust(-0.0);
        colourFaderAffector->setGreenAdjust(-0.0);
        colourFaderAffector->setBlueAdjust(-0.0);
        colourFaderAffector->setAlphaAdjust(-1.0);*/

        /*colourFaderAffector2 = (ColourFaderAffector2*)ps->addAffector("ColourFader2");
        colourFaderAffector2->setStateChange(0.25);
        colourFaderAffector2->setRedAdjust1(-0.0);
        colourFaderAffector2->setGreenAdjust1(-0.0);
        colourFaderAffector2->setBlueAdjust1(-0.0);
        colourFaderAffector2->setAlphaAdjust1(-0.3);

        colourFaderAffector2->setAlphaAdjust2(0.5);*/
        colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
        colourInterpolatorAffector->setTimeAdjust(0, 0);
        colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 1,1.0));
        colourInterpolatorAffector->setTimeAdjust(1, 0.5f);
        colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 1, 1,0.2));  
        colourInterpolatorAffector->setTimeAdjust(2, 1.0f);
        colourInterpolatorAffector->setColourAdjust(2,ColourValue(1, 1, 1,1.0));

        linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);
    break;

	case Fireworks:
        ps = new ParticleSystem(objectName);
	    ps->setParticleQuota(1000);
	    ps->setEmittedEmitterQuota(10);
        ps->setDefaultDimensions(3,3);

        boxEmitter = (BoxEmitter*)ps->addEmitter("Box");
        //boxEmitter->setName("mainEmitter");
        boxEmitter->setEmittedEmitter("explosion");
        boxEmitter->setAngle(Radian(FACTOR * 30));
        boxEmitter->setEmissionRate(100);
        boxEmitter->setTimeToLive(3);
        boxEmitter->setDirection(Vector3(0, 0, 1));
        boxEmitter->setParticleVelocity(6);
        boxEmitter->setPosition(pos);
        boxEmitter->setDepth(20);
        boxEmitter->setHeight(20);
        boxEmitter->setWidth(20);

        pointEmitter= (PointEmitter*)ps->addEmitter("Point");
        pointEmitter->setName("explosion");
        pointEmitter->setAngle(Radian(FACTOR * 180));
        pointEmitter->setEmissionRate(100);
        pointEmitter->setTimeToLive(4);
        pointEmitter->setDirection(Vector3(0, 0, 1));
        pointEmitter->setParticleVelocity(6);
        pointEmitter->setDuration(0.5f);
        pointEmitter->setRepeatDelay(2, 3);

        linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
        colourInterpolatorAffector->setTimeAdjust(0, 0);
        colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 0));
        colourInterpolatorAffector->setTimeAdjust(1, 0.5f);
        colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 0, 0));
        colourInterpolatorAffector->setTimeAdjust(2, 0.9f);
        colourInterpolatorAffector->setColourAdjust(2,ColourValue(0, 0, 1));
	break;

	case Aureola:
        ps = new ParticleSystem(objectName);
        ps->setParticleQuota(50);
        ps->setDefaultDimensions(100, 100);
        ((BillboardParticleRenderer*)ps->getRenderer())->setBillboardType(BBT_PERPENDICULAR_COMMON);
        ((BillboardParticleRenderer*)ps->getRenderer())->setCommonDirection(Vector3(0, 0, 1));
        ((BillboardParticleRenderer*)ps->getRenderer())->setCommonUpVector(Vector3(0, 1, 0));

        boxEmitter = (BoxEmitter*)ps->addEmitter("Box");
        boxEmitter->setName("mainEmitter");
        boxEmitter->setAngle(Radian(FACTOR * 30));
        boxEmitter->setEmissionRate(2);
        boxEmitter->setTimeToLive(30);
        boxEmitter->setDirection(Vector3(0, 1, 0));
        boxEmitter->setParticleVelocity(1);
        boxEmitter->setColourRangeStart(ColourValue(0.3f, 0.3f, 0.3f, 0.0f));
        boxEmitter->setColourRangeEnd(ColourValue(0.8f, 0.8f, 0.8f, 0.0f));
        boxEmitter->setDepth(5);
        boxEmitter->setHeight(10);
        boxEmitter->setWidth(5);
        boxEmitter->setPosition(pos);

        linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -10));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        colourFaderAffector2 = (ColourFaderAffector2*)ps->addAffector("ColourFader2");
        colourFaderAffector2->setAdjust1(0.4f, 0.4f, 0.4f, 0.7f);
        colourFaderAffector2->setAdjust2(-0.25f, -0.25f, -0.25f, -0.3333f);
        colourFaderAffector2->setStateChange(3.5f);

        rotationAffector = (RotationAffector *)ps->addAffector("Rotator");
        rotationAffector->setRotationRangeStart(Radian(0));
        rotationAffector->setRotationRangeEnd(Radian(FACTOR * 180));
        rotationAffector->setRotationSpeedRangeStart(Radian(0));
        rotationAffector->setRotationSpeedRangeEnd(Radian(FACTOR * 10)); 
	break;

	case Ring:
        ps = new ParticleSystem(objectName);
        ps->setParticleQuota(500);
        ps->setDefaultDimensions(2, 2);   

        ringEmitter = (RingEmitter*)ps->addEmitter("Ring"); 
        ringEmitter->setInnerSizeX(0.5f);
        ringEmitter->setInnerSizeY(0.5f);         
        ringEmitter->setAngle(Radian(FACTOR * 180));
        ringEmitter->setEmissionRate(40);
        ringEmitter->setTimeToLive(5);
        ringEmitter->setPosition(pos);
        ringEmitter->setDirection(Vector3(0, 0, 1));
        ringEmitter->setParticleVelocity(1);
        // ringEmitter->setDuration(0);
        ringEmitter->setDepth(1);
        ringEmitter->setHeight(20);
        ringEmitter->setWidth(20);

        colourFaderAffector = (ColourFaderAffector*)ps->addAffector("ColourFader");
        colourFaderAffector->setRedAdjust(-0.8f);
        colourFaderAffector->setGreenAdjust(-0.25f);
        colourFaderAffector->setBlueAdjust(-0.8f);
        break;

	case HollowEllipsoid:
        ps = new ParticleSystem(objectName);
        ps->setParticleQuota(500);
        ps->setDefaultDimensions(2,2);

        hollowEllipsoidEmitter = (HollowEllipsoidEmitter*)ps->addEmitter("HollowEllipsoid");
        hollowEllipsoidEmitter->setAngle(Radian(FACTOR * 180));
        hollowEllipsoidEmitter->setEmissionRate(20);
        hollowEllipsoidEmitter->setTimeToLive(20);
        hollowEllipsoidEmitter->setPosition(Vector3(0, 100, 0));
        hollowEllipsoidEmitter->setDirection(Vector3(0, 1, 0));
        hollowEllipsoidEmitter->setParticleVelocity(20);
        hollowEllipsoidEmitter->setDuration(0);
        hollowEllipsoidEmitter->setDepth(600);
        hollowEllipsoidEmitter->setHeight(100);
        hollowEllipsoidEmitter->setWidth(600);

        deflectorPlaneAffector = (DeflectorPlaneAffector *)ps->addAffector("DeflectorPlane");
        deflectorPlaneAffector->setPlanePoint(Vector3(0, 0, 50));
        deflectorPlaneAffector->setPlaneNormal(Vector3(0, 0, -1));
        deflectorPlaneAffector->setBounce(1);
	break;

	case Ellipsoid:
        ps = new ParticleSystem(objectName);
        ps->setParticleQuota(500);
        ps->setDefaultDimensions(2, 2);
        ellipsoidEmitter = (EllipsoidEmitter*)ps->addEmitter("Ellipsoid");
        ellipsoidEmitter->setAngle(Radian(FACTOR * 180));
        ellipsoidEmitter->setEmissionRate(20);
        ellipsoidEmitter->setTimeToLive(20);
        ellipsoidEmitter->setPosition(Vector3(0, 100, 0));
        ellipsoidEmitter->setDirection(Vector3(0, 1, 0));
        ellipsoidEmitter->setParticleVelocity(20);
        ellipsoidEmitter->setDuration(0);
        ellipsoidEmitter->setDepth(600);
        ellipsoidEmitter->setHeight(100);
        ellipsoidEmitter->setWidth(600);

        directionRandomiserAffector = (DirectionRandomiserAffector *)ps->addAffector("DirectionRandomiser");
        directionRandomiserAffector->setRandomness(10);
        directionRandomiserAffector->setScope(5);
        directionRandomiserAffector->setKeepVelocity(10);
	break;

	case Cylinder:
        ps = new ParticleSystem(objectName);
        ps->setParticleQuota(500);
        ps->setDefaultDimensions(2, 2);
        cylinderEmitter = (CylinderEmitter*)ps->addEmitter("Cylinder");
        //cylinderEmitter->setName("explosion");
        cylinderEmitter->setAngle(Radian(FACTOR * 180));
        cylinderEmitter->setEmissionRate(20);
        cylinderEmitter->setTimeToLive(20);
        cylinderEmitter->setPosition(Vector3(0, 100, 0));
        cylinderEmitter->setDirection(Vector3(0, 1, 0));
        cylinderEmitter->setParticleVelocity(20);
        cylinderEmitter->setDuration(0);
        cylinderEmitter->setDepth(600);
        cylinderEmitter->setHeight(100);
        cylinderEmitter->setWidth(600);
        scaleAffector = (ScaleAffector *)ps->addAffector("Scaler");
        scaleAffector->setAdjust(10);
        break;             
    }
		
    //if (mSystemTemplates.find(objectName) != mSystemTemplates.end())
    //{

    //}

    if(ps)
    {
        ps->setIsAttached(true);
        ps->setAttachedSpatial(attachedSp);
    }
    mParticleSystems[objectName] = ps;       
}

void ParticleSystemManager::update(SE_TimeMS realDelta, SE_TimeMS simulateDelta)
{
    ParticleSystemMap::iterator t;
    for (t = mParticleSystems.begin(); t != mParticleSystems.end(); ++t)
    {
        ParticleSystem* ps = t->second;
        if(ps->isAttachedSpatial())
        {
            if(!ps->getAttachedSpatial())
            {
                this->destroyParticleSystem(ps->getName());
                continue;
            }
            
           // SE_Vector3f sepos = ps->getAttachedSpatial()->getCenter();
            SE_Vector4f in = SE_Vector4f(0, 0, 0, 1);
            SE_Vector4f out = ps->getAttachedSpatial()->getWorldTransform().map(in);
            SE_Vector3f translate = SE_Vector3f(out.x, out.y, out.z);
            ps->setTranslate(translate);
            
            //String emitername = "Box";
            //String affectorname = "Follower";
            //this->setAttribute(ps->getName(),FOLLOWPOSITION,affectorname,out.x,out.y,out.z,0);
            //this->setAttribute(ps->getName(),POSITION,emitername,out.x,out.y,out.z,0);
          
            }
        ps->_update(realDelta * 0.001);
        ps->_updateRender();	

        /*if (ps->getName()=="particle")
	{
            ps->_update(1.0189);
            ps->_updateRender();	
	}*/
    }
}

void ParticleSystemManager::destroyParticleSystem(const String& objectName)
{
    ParticleSystemMap::iterator pt;
    for (pt = mParticleSystems.begin(); pt != mParticleSystems.end(); ++pt)
    {
        ParticleSystem*  ps = pt->second;
	    if (ps->getName() == objectName) 
        {
	    delete ps;
            mParticleSystems.erase(pt);					
	    break;
	}
    }
}

void ParticleSystemManager::setImagePath(const char* imagePath, const String& objectName)
{
    if (imagePath == NULL) 
    {
        return;
    }

    ParticleSystemMap::iterator t;
    for (t = mParticleSystems.begin(); t != mParticleSystems.end(); ++t)
    {
        ParticleSystem* ps = t->second;
	if (ps->getName() == objectName)
	{
            ps->setImagePath(imagePath);
	}
    }		
}

void ParticleSystemManager::createEffect(SE_Effect effect,SE_Vector3f position,const char* mainImagePathOrId,const char* helpImagePathOrId,SE_Vector3f boxsize)
{
    
    switch(effect)
    {
    case SNOW:
        {
        String psname = "SNOW";
        String helppsname = "HELPSNOW";
        ParticleSystem*  ps;
        ParticleSystem*  helpps;
        Vector3 pos,size;

        position.x -= 1000;
        position.z += 1500;
        position.y += 200;

        pos.x = position.x;
        pos.y = position.y;
        pos.z = position.z;

        if(boxsize.isZero())
        {
            boxsize.set(4000,4000,2000);
        }

        size.x = boxsize.x;
        size.y = boxsize.y;
        size.z = boxsize.z; 
        

        BoxEmitter* boxEmitter;
        BoxEmitter* boxEmitter2;
        PointEmitter* pointEmitter;
        EllipsoidEmitter* ellipsoidEmitter;
        RingEmitter* ringEmitter;
        HollowEllipsoidEmitter* hollowEllipsoidEmitter;
        CylinderEmitter* cylinderEmitter;

        LinearForceAffector* linearForceAffector;
        ColourInterpolatorAffector* colourInterpolatorAffector;
        ColourFaderAffector2* colourFaderAffector2;
        ColourFaderAffector* colourFaderAffector;
        DeflectorPlaneAffector* deflectorPlaneAffector;
        DirectionRandomiserAffector* directionRandomiserAffector;
        RotationAffector* rotationAffector;
        ScaleAffector* scaleAffector;

        ParticleSystemMap::iterator psit;
        psit = mParticleSystems.find(psname);
        if(psit != mParticleSystems.end())
        {
            //ps has been in system
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("particle system [%s] has been in system,return!!!!\n",psname.c_str());
            }
            return;
        }
        ps = new ParticleSystem(psname);
        helpps = new ParticleSystem(helppsname);
        //ps->setDefaultNonVisibleUpdateTimeout(5);
	    ps->setParticleQuota(200);
        ps->setDefaultDimensions(40, 40);
        //ps->setCullIndividually(true);
        //ps->setSortingEnabled(true);
        //ps->setKeepParticlesInLocalSpace(false);
        helpps->setParticleQuota(200);
        helpps->setDefaultDimensions(20, 20);

        boxEmitter = (BoxEmitter*) ps->addEmitter("Box");       
        boxEmitter->setAngle(Radian(FACTOR*10));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter->setDepth(size.z);
	    boxEmitter->setHeight(size.y);
	    boxEmitter->setWidth(size.x);
	    boxEmitter->setDirection(Vector3(0, 0, -1));	
	    boxEmitter->setEmissionRate(20);
	    boxEmitter->setEnabled(true);		    
	    boxEmitter->setName("mainSnowEmitter");
	    boxEmitter->setParticleVelocity(50, 200);
	    boxEmitter->setPosition(pos);
	    boxEmitter->setTimeToLive(5, 10);

        boxEmitter2  = (BoxEmitter*) helpps->addEmitter("Box");
        boxEmitter2->setAngle(Radian(FACTOR*0));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter2->setDepth(size.z);
	    boxEmitter2->setHeight(size.y);
	    boxEmitter2->setWidth(size.x);
	    boxEmitter2->setDirection(Vector3(0.5, 0, -1));	
	    boxEmitter2->setEmissionRate(20);
	    boxEmitter2->setEnabled(true);		    
	    boxEmitter2->setName("helpSnowEmitter");
	    boxEmitter2->setParticleVelocity(300, 500);
        pos.y += 800;
	    boxEmitter2->setPosition(pos);
	    boxEmitter2->setTimeToLive(5, 10);

       
        colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
        colourInterpolatorAffector->setTimeAdjust(0, 0);
        colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 1,1.0));
        colourInterpolatorAffector->setTimeAdjust(1, 2.5f);
        colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 1, 1,0.5));  
        colourInterpolatorAffector->setTimeAdjust(2, 5.0f);
        colourInterpolatorAffector->setColourAdjust(2,ColourValue(1, 1, 1,0.0));

        linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        linearForceAffector = (LinearForceAffector*)helpps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        ps->setImagePath(mainImagePathOrId);
        helpps->setImagePath(helpImagePathOrId);
        mParticleSystems[psname] = ps;
        mParticleSystems[helppsname] = helpps;
        }

        break;
    case RAIN:
        {
        String psname = "RAIN";
        String helppsname = "HELPRAIN";
        ParticleSystem*  ps;
        ParticleSystem*  helpps;
        Vector3 pos,size;

        position.x -= 1000;
        position.z += 1500;
        position.y += 200;

        pos.x = position.x;
        pos.y = position.y;
        pos.z = position.z;

        if(boxsize.isZero())
        {
            boxsize.set(4000,4000,2000);
        }

        size.x = boxsize.x;
        size.y = boxsize.y;
        size.z = boxsize.z; 


        BoxEmitter* boxEmitter;
        BoxEmitter* boxEmitter2;
        PointEmitter* pointEmitter;
        EllipsoidEmitter* ellipsoidEmitter;
        RingEmitter* ringEmitter;
        HollowEllipsoidEmitter* hollowEllipsoidEmitter;
        CylinderEmitter* cylinderEmitter;

        LinearForceAffector* linearForceAffector;
        ColourInterpolatorAffector* colourInterpolatorAffector;
        ColourFaderAffector2* colourFaderAffector2;
        ColourFaderAffector* colourFaderAffector;
        DeflectorPlaneAffector* deflectorPlaneAffector;
        DirectionRandomiserAffector* directionRandomiserAffector;
        RotationAffector* rotationAffector;
        ScaleAffector* scaleAffector;

        ParticleSystemMap::iterator psit;
        psit = mParticleSystems.find(psname);
        if(psit != mParticleSystems.end())
        {
            //ps has been in system
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("particle system [%s] has been in system,return!!!!\n",psname.c_str());
            }
            return;
        }
        ps = new ParticleSystem(psname);
        helpps = new ParticleSystem(helppsname);
        //ps->setDefaultNonVisibleUpdateTimeout(5);
	    ps->setParticleQuota(1000);
        ps->setDefaultDimensions(10, 40);
        //ps->setCullIndividually(true);
        //ps->setSortingEnabled(true);
        //ps->setKeepParticlesInLocalSpace(false);
        helpps->setParticleQuota(1000);
        helpps->setDefaultDimensions(10, 20);

        boxEmitter = (BoxEmitter*) ps->addEmitter("Box");       
        boxEmitter->setAngle(Radian(FACTOR*10));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter->setDepth(size.z);
	    boxEmitter->setHeight(size.y);
	    boxEmitter->setWidth(size.x);
	    boxEmitter->setDirection(Vector3(0, 0, -1));	
	    boxEmitter->setEmissionRate(120);
	    boxEmitter->setEnabled(true);		    
	    boxEmitter->setName("mainRainEmitter");
	    boxEmitter->setParticleVelocity(600, 800);
	    boxEmitter->setPosition(pos);
	    boxEmitter->setTimeToLive(2, 4);

        boxEmitter2  = (BoxEmitter*) helpps->addEmitter("Box");
        boxEmitter2->setAngle(Radian(FACTOR*0));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter2->setDepth(size.z);
	    boxEmitter2->setHeight(size.y);
	    boxEmitter2->setWidth(size.x);
	    boxEmitter2->setDirection(Vector3(0.2, 0, -1));	
	    boxEmitter2->setEmissionRate(90);
	    boxEmitter2->setEnabled(true);		    
	    boxEmitter2->setName("helpRainEmitter");
	    boxEmitter2->setParticleVelocity(800, 900);
        pos.y += 800;
	    boxEmitter2->setPosition(pos);
	    boxEmitter2->setTimeToLive(2, 4);

       
        colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
        colourInterpolatorAffector->setTimeAdjust(0, 0);
        colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 1,1.0));
        colourInterpolatorAffector->setTimeAdjust(1, 2.5f);
        colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 1, 1,0.5));  
        colourInterpolatorAffector->setTimeAdjust(2, 5.0f);
        colourInterpolatorAffector->setColourAdjust(2,ColourValue(1, 1, 1,0.0));

        linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        linearForceAffector = (LinearForceAffector*)helpps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        ps->setImagePath(mainImagePathOrId);
        helpps->setImagePath(helpImagePathOrId);

        mParticleSystems[psname] = ps;
        mParticleSystems[helppsname] = helpps;
        }
        break;

    case STAR:
        {
        String psname = "STAR";
        String psflashname = "STAR_FLASH";
        String helppsname = "HELPSTAR";
        ParticleSystem*  ps;
        ParticleSystem*  flashps;
        ParticleSystem*  helpps;
        Vector3 pos,size;        

        position.x = 0;
        position.z += 900;
        position.y += 200;

        pos.x = position.x;
        pos.y = position.y;
        pos.z = position.z;

        if(boxsize.isZero())
        {
            boxsize.set(2000,300,350);
        }

        size.x = boxsize.x;
        size.y = boxsize.y;
        size.z = boxsize.z; 


        BoxEmitter* boxEmitter;
        BoxEmitter* boxEmitter2;
        BoxEmitter* boxEmitterflash;

        PointEmitter* pointEmitter;
        EllipsoidEmitter* ellipsoidEmitter;
        RingEmitter* ringEmitter;
        HollowEllipsoidEmitter* hollowEllipsoidEmitter;
        CylinderEmitter* cylinderEmitter;

        LinearForceAffector* linearForceAffector;
        ColourInterpolatorAffector* colourInterpolatorAffector;
        ColourFaderAffector2* colourFaderAffector2;
        ColourFaderAffector* colourFaderAffector;
        DeflectorPlaneAffector* deflectorPlaneAffector;
        DirectionRandomiserAffector* directionRandomiserAffector;
        RotationAffector* rotationAffector;
        ScaleAffector* scaleAffector;

        ParticleSystemMap::iterator psit;
        psit = mParticleSystems.find(psname);
        if(psit != mParticleSystems.end())
        {
            //ps has been in system
            if(SE_Application::getInstance()->SEHomeDebug)
            {
                LOGI("particle system [%s] has been in system,return!!!!\n",psname.c_str());
            }
            return;
        }
        ps = new ParticleSystem(psname);
        flashps = new ParticleSystem(psflashname);
        helpps = new ParticleSystem(helppsname);
        //ps->setDefaultNonVisibleUpdateTimeout(5);
	    ps->setParticleQuota(60);
        ps->setDefaultDimensions(20, 20);
        flashps->setParticleQuota(20);
        flashps->setDefaultDimensions(20, 20);
        //ps->setCullIndividually(true);
        //ps->setSortingEnabled(true);
        //ps->setKeepParticlesInLocalSpace(false);
        helpps->setParticleQuota(1);
        helpps->setDefaultDimensions(80, 80);

        boxEmitter = (BoxEmitter*) ps->addEmitter("Box");       
        boxEmitter->setAngle(Radian(FACTOR*5));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter->setDepth(size.z);
	    boxEmitter->setHeight(size.y);
	    boxEmitter->setWidth(size.x);
	    boxEmitter->setDirection(Vector3(0, 0, -1));	
	    boxEmitter->setEmissionRate(2000);
	    boxEmitter->setEnabled(true);		    
	    boxEmitter->setName("mainStarEmitter");
	    boxEmitter->setParticleVelocity(0, 0);
	    boxEmitter->setPosition(pos);
	    boxEmitter->setTimeToLive(10000, 10000);
        ColourValue star(1.0,1.0,1.0,1.0);
        boxEmitter->setColour(star);

        boxEmitterflash = (BoxEmitter*) flashps->addEmitter("Box");       
        boxEmitterflash->setAngle(Radian(FACTOR*5));
	    boxEmitterflash->setDepth(size.z);
	    boxEmitterflash->setHeight(size.y);
	    boxEmitterflash->setWidth(size.x);
	    boxEmitterflash->setDirection(Vector3(0, 0, -1));	
	    boxEmitterflash->setEmissionRate(2000);
	    boxEmitterflash->setEnabled(true);		    
	    boxEmitterflash->setName("mainStarEmitter1");
	    boxEmitterflash->setParticleVelocity(0, 0);
	    boxEmitterflash->setPosition(pos);
	    boxEmitterflash->setTimeToLive(5, 10);
        ColourValue starflash(1.0,1.0,1.0,0.0);
        boxEmitterflash->setColour(starflash);



        boxEmitter2  = (BoxEmitter*) helpps->addEmitter("Box");
        boxEmitter2->setAngle(Radian(FACTOR*10));
        //boxEmitter->setAngle(Radian(0));
	    boxEmitter2->setDepth(500);
	    boxEmitter2->setHeight(200);
	    boxEmitter2->setWidth(500);
	    boxEmitter2->setDirection(Vector3(-1, 1, 0.1));	
	    boxEmitter2->setEmissionRate(2);
	    boxEmitter2->setEnabled(true);		    
	    boxEmitter2->setName("helpStarEmitter");
	    boxEmitter2->setParticleVelocity(300, 500);
        
        pos.x = 500;  
        pos.y = 300;
        pos.z = 1200;      

	    boxEmitter2->setPosition(pos);
	    boxEmitter2->setTimeToLive(2, 2);

        colourInterpolatorAffector = (ColourInterpolatorAffector*)flashps->addAffector("ColourInterpolator");
        colourInterpolatorAffector->setTimeAdjust(0, 0);
        colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 1, 0));
        colourInterpolatorAffector->setTimeAdjust(1, 0.2);
        colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 1, 1, 1));  
        colourInterpolatorAffector->setTimeAdjust(2, 0.4);
        colourInterpolatorAffector->setColourAdjust(2,ColourValue(1, 1, 1, 0));
        colourInterpolatorAffector->setTimeAdjust(3, 0.6);
        colourInterpolatorAffector->setColourAdjust(3,ColourValue(1, 1, 1, 1));
        colourInterpolatorAffector->setTimeAdjust(4, 0.8);
        colourInterpolatorAffector->setColourAdjust(4,ColourValue(1, 1, 1, 0));
        colourInterpolatorAffector->setTimeAdjust(5, 1);
        colourInterpolatorAffector->setColourAdjust(5,ColourValue(1, 1, 1, 1));
       
        /*colourFaderAffector = (ColourFaderAffector*)ps->addAffector("ColourFader");
        colourFaderAffector->setRedAdjust(-0.0);
        colourFaderAffector->setGreenAdjust(-0.0);
        colourFaderAffector->setBlueAdjust(-0.0);
        colourFaderAffector->setAlphaAdjust(-0.1);*/

        colourFaderAffector = (ColourFaderAffector*)helpps->addAffector("ColourFader");
        colourFaderAffector->setRedAdjust(-0.0);
        colourFaderAffector->setGreenAdjust(-0.0);
        colourFaderAffector->setBlueAdjust(-0.0);
        colourFaderAffector->setAlphaAdjust(-0.5);

        /*linearForceAffector = (LinearForceAffector*)ps->addAffector("LinearForce");
        linearForceAffector->setForceVector(Vector3(0, 0, -1));
        linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);*/

        //linearForceAffector = (LinearForceAffector*)helpps->addAffector("LinearForce");
        //linearForceAffector->setForceVector(Vector3(-1, 1, 0));
        //linearForceAffector->setForceApplication(LinearForceAffector::FA_ADD);

        ps->setImagePath(mainImagePathOrId);
        flashps->setImagePath(mainImagePathOrId);
        helpps->setImagePath(helpImagePathOrId);

        mParticleSystems[psname] = ps;
        mParticleSystems[psflashname] = flashps;
        mParticleSystems[helppsname] = helpps;


            //SE_SpatialManager* spManager = SE_Application::getInstance()->getSpatialManager();
            SE_Spatial* particle = SE_Application::getInstance()->getSceneManager()->findSpatialByName(((BillboardParticleRenderer*)flashps->getRenderer())->getBillboardSet()->getSpatial()->getSpatialName());
            if(particle) 
            {        
                particle->setNeedDepthTest(true);
            }
        }
        break;

    }

   
}

void ParticleSystemManager::removeEffect(SE_Effect effect)
{

    switch(effect)
    {
    case SNOW:
        {
            String main = "SNOW";
            String help = "HELPSNOW";
            destroyParticleSystem(main);
            destroyParticleSystem(help);
        }
        break;
    case RAIN:
        {
            String main = "RAIN";
            String help = "HELPRAIN";
            destroyParticleSystem(main);
            destroyParticleSystem(help);
        }
        break;
    case STAR:
        {
            String main = "STAR";
            String help = "HELPSTAR";
            String flash = "STAR_FLASH";
            destroyParticleSystem(main);
            destroyParticleSystem(help);
            destroyParticleSystem(flash);
        }
        break;
        case ATTACHED:
        {
            String main = "attached";            
            destroyParticleSystem(main);      
        }
        break;


    }
}
void ParticleSystemManager::createEffect(SE_Effect effect,const char* attachedSpatialName,const char* mainImagePathOrId,const char* helpImagePathOrId,SE_Vector3f boxsize,SE_Vector2f particleDimension)
{
    switch(effect)
    {
    case ATTACHED:
        {
            String psname = "attached";
            
            ParticleSystem*  ps;

            SE_Spatial* attachedSp = SE_Application::getInstance()->getSceneManager()->findSpatialByName(attachedSpatialName);
            SE_Vector3f sepos;
                
            if(attachedSp)
            {
                sepos = attachedSp->getCenter();
            }
            else
            {
                if(SE_Application::getInstance()->SEHomeDebug)
                LOGI("\n Particle attached spatial[%s] not found,particle create fail!!!\n",attachedSpatialName);
                return;
            }

            Vector3 pos;
            pos.x = sepos.x;
            pos.y = sepos.y;
            pos.z = sepos.z;

            BoxEmitter* boxEmitter;
            BoxEmitter* boxEmitter2;
            PointEmitter* pointEmitter;
            EllipsoidEmitter* ellipsoidEmitter;
            RingEmitter* ringEmitter;
            HollowEllipsoidEmitter* hollowEllipsoidEmitter;
            CylinderEmitter* cylinderEmitter;

            LinearForceAffector* linearForceAffector;
            ColourInterpolatorAffector* colourInterpolatorAffector;
            ColourFaderAffector2* colourFaderAffector2;
            ColourFaderAffector* colourFaderAffector;
            DeflectorPlaneAffector* deflectorPlaneAffector;
            DirectionRandomiserAffector* directionRandomiserAffector;
            RotationAffector* rotationAffector;
            ScaleAffector* scaleAffector;
            FollowAffector* followAffector;

            ParticleSystemMap::iterator psit;
            psit = mParticleSystems.find(psname);
            if(psit != mParticleSystems.end())
            {
                //ps has been in system
                if(SE_Application::getInstance()->SEHomeDebug)
                {
                    LOGI("particle system [%s] has been in system,return!!!!\n",psname.c_str());
                }
                return;
            }

            ps = new ParticleSystem(psname);
            
            //ps->setDefaultNonVisibleUpdateTimeout(5);
	        ps->setParticleQuota(1);
            ps->setDefaultDimensions(particleDimension.x, particleDimension.y);
            //ps->setCullIndividually(true);
            //ps->setSortingEnabled(true);
            //ps->setKeepParticlesInLocalSpace(false);
            ps->setCurrentPosition(sepos.x,sepos.y,sepos.z);
            

            boxEmitter = (BoxEmitter*) ps->addEmitter("Box");       
            boxEmitter->setAngle(Radian(FACTOR*0));
            //boxEmitter->setAngle(Radian(0));
	        boxEmitter->setDepth(0);
	        boxEmitter->setHeight(0);
	        boxEmitter->setWidth(0);
	        boxEmitter->setDirection(Vector3(0, 0, -1));	
	        boxEmitter->setEmissionRate(1);
	        boxEmitter->setEnabled(true);		    
	        boxEmitter->setName("mainAttachedEmitter");
	        boxEmitter->setParticleVelocity(0, 0);
	        boxEmitter->setPosition(pos);
	        boxEmitter->setTimeToLive(1,1);  
            ColourValue attached(1.0,1.0,1.0,0.0);
            boxEmitter->setColour(attached);

           
            followAffector = (FollowAffector*)ps->addAffector("Follower");
            followAffector->setCurrentPos(pos);
            
           
            colourInterpolatorAffector = (ColourInterpolatorAffector*)ps->addAffector("ColourInterpolator");
            colourInterpolatorAffector->setTimeAdjust(0, 0);
            colourInterpolatorAffector->setColourAdjust(0,ColourValue(1, 1, 1,0.0));
            colourInterpolatorAffector->setTimeAdjust(1, 0.5f);
            colourInterpolatorAffector->setColourAdjust(1,ColourValue(1, 1, 1,0.9));  
            colourInterpolatorAffector->setTimeAdjust(2, 1.0f);
            colourInterpolatorAffector->setColourAdjust(2,ColourValue(1, 1, 1,0.0));
            

            ps->setImagePath(mainImagePathOrId);  
            if(ps)
            {
                ps->setIsAttached(true);
                ps->setAttachedSpatial(attachedSp);
            }          
            mParticleSystems[psname] = ps;
            
        }

    }

}
