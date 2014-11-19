#ifndef SE_PHYSICS_H
#define SE_PHYSICS_H
#include "SE_Matrix.h"
#include "LinearMath/btAlignedObjectArray.h"
class btBroadphaseInterface;
class btCollisionShape;
class btOverlappingPairCache;
class btCollisionDispatcher;
class btConstraintSolver;
struct btCollisionAlgorithmCreateFunc;

class btDefaultCollisionConfiguration;
class	btCollisionShape;
class	btDynamicsWorld;
class	btRigidBody;
class	btTypedConstraint;

class SE_Physics
{
public:
    void initPhysics();
    void exitPhysics();
    void stepSimulation(float second);

    void clientResetScene();
    void setStartPos(const SE_Vector3f& pos)
    {
        mStartPos = pos;
    }
	btDynamicsWorld*		getDynamicsWorld()
	{
		return m_dynamicsWorld;
	}
	SE_Matrix4f getObjMatrix();
private:
    SE_Vector3f mStartPos;
	btCollisionShape* mMoveObj;
	btDynamicsWorld*		m_dynamicsWorld;

	///constraint for mouse picking
	btTypedConstraint*		m_pickConstraint;

	btCollisionShape*	m_shootBoxShape;
    
	//keep the collision shapes, for deletion/cleanup
	btAlignedObjectArray<btCollisionShape*>	m_collisionShapes;

	btBroadphaseInterface*	m_broadphase;

	btCollisionDispatcher*	m_dispatcher;

	btConstraintSolver*	m_solver;

	btDefaultCollisionConfiguration* m_collisionConfiguration;

};
#endif
