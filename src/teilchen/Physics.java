/*
 * Teilchen
 *
 * Copyright (C) 2015
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * {@link http://www.gnu.org/licenses/lgpl.html}
 *
 */

package teilchen;

import processing.core.PVector;
import teilchen.constraint.IConstraint;
import teilchen.force.IForce;
import teilchen.force.Spring;
import teilchen.force.ViscousDrag;
import teilchen.integration.IIntegrator;
import teilchen.integration.Midpoint;
import teilchen.integration.Verlet;
import teilchen.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Physics {

    public static final float EPSILON = 0.001f;
    public static boolean HINT_UPDATE_OLD_POSITION = true;
    public boolean HINT_OPTIMIZE_STILL = true;
    public boolean HINT_RECOVER_NAN = true;
    public boolean HINT_REMOVE_DEAD = true;
    public int constrain_iterations_per_steps = 1;
    public int integrations_per_steps = 1;
    private final ArrayList<Particle> mParticles;
    private final ArrayList<IForce> mForces;
    private final ArrayList<IConstraint> mConstraints;
    private IIntegrator mIntegrator;

    public Physics() {
        mParticles = new ArrayList<>();
        mForces = new ArrayList<>();
        mConstraints = new ArrayList<>();
        mIntegrator = new Midpoint();
    }

    /* particles */
    public void add(Particle theParticle) {
        mParticles.add(theParticle);
    }

    public void add(Collection<? extends Particle> theParticles) {
        mParticles.addAll(theParticles);
    }

    public void remove(Particle theParticle) {
        mParticles.remove(theParticle);
    }

    public void remove(Collection<? extends Particle> theParticles) {

        mParticles.removeAll(theParticles);
    }

    public ArrayList<Particle> particles() {
        return mParticles;
    }

    public Particle particles(final int theIndex) {
        return mParticles.get(theIndex);
    }

    public BasicParticle makeParticle(final PVector thePosition) {
        BasicParticle myParticle = makeParticle();
        myParticle.setPositionRef(thePosition);
        myParticle.old_position().set(myParticle.position());
        return myParticle;
    }

    public BasicParticle makeParticle() {
        BasicParticle myParticle = new BasicParticle();
        mParticles.add(myParticle);
        return myParticle;
    }

    public BasicParticle makeParticle(final float x, final float y) {
        BasicParticle myParticle = makeParticle();
        myParticle.position().set(x, y);
        myParticle.old_position().set(myParticle.position());
        return myParticle;
    }

    public BasicParticle makeParticle(final float x, final float y, final float z) {
        BasicParticle myParticle = makeParticle();
        myParticle.position().set(x, y, z);
        myParticle.old_position().set(myParticle.position());
        return myParticle;
    }

    public BasicParticle makeParticle(final float x, final float y, final float z, final float pMass) {
        BasicParticle myParticle = makeParticle();
        myParticle.position().set(x, y, z);
        myParticle.mass(pMass);
        myParticle.old_position().set(myParticle.position());
        return myParticle;
    }

    public BasicParticle makeParticle(final PVector thePosition, final float pMass) {
        BasicParticle myParticle = makeParticle();
        myParticle.setPositionRef(thePosition);
        myParticle.old_position().set(myParticle.position());
        myParticle.mass(pMass);
        return myParticle;
    }

    public <T extends Particle> T makeParticle(Class<T> theParticleClass) {
        T myParticle;
        try {
            myParticle = theParticleClass.newInstance();
            mParticles.add(myParticle);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex);
            myParticle = null;
        }
        return myParticle;
    }

    public void removeTags() {
        for (final Particle myParticle : mParticles) {
            myParticle.tag(false);
        }
    }

    /* forces */
    public void add(IForce theForce) {
        if (theForce instanceof ViscousDrag && mIntegrator instanceof Verlet) {
            System.err.println("### WARNING / 'viscous drag' might have no effect with 'verlet' integration. use 'Verlet" + "" +
                               ".damping" + "(float theDamping)' instead.");
        }
        mForces.add(theForce);
    }

    public void addForces(final ArrayList<? extends IForce> theForces) {
        mForces.addAll(theForces);
    }

    public void remove(IForce theForce) {
        mForces.remove(theForce);
    }

    public ArrayList<IForce> forces() {
        return mForces;
    }

    public IForce forces(final int theIndex) {
        return mForces.get(theIndex);
    }

    public void applyForces(final float theDeltaTime) {
        /* accumulate inner forces */
        synchronized (mParticles) {
            for (Particle myParticle : mParticles) {
                if (!myParticle.fixed()) {
                    /* accumulate inner forces */
                    myParticle.accumulateInnerForce(theDeltaTime);
                }
            }
        }

        /* add new forces to each particle */
        synchronized (mForces) {
            for (IForce mForce : mForces) {
                if (mForce.active()) {
                    mForce.apply(theDeltaTime, this);
                }
            }
        }
    }

    public <T extends IForce> T makeForce(Class<T> theForceClass) {
        T myForce;
        try {
            myForce = theForceClass.newInstance();
            mForces.add(myForce);
        } catch (Exception ex) {
            myForce = null;
        }
        return myForce;
    }

    public Spring makeSpring(final Particle theA, final Particle theB) {
        Spring mySpring = new Spring(theA, theB);
        mForces.add(mySpring);
        return mySpring;
    }

    public Spring makeSpring(final Particle theA, final Particle theB, final float theRestLength) {
        Spring mySpring = new Spring(theA, theB, theRestLength);
        mForces.add(mySpring);
        return mySpring;
    }

    public Spring makeSpring(final Particle theA, final Particle theB, final float theSpringConstant, final float theSpringDamping) {
        Spring mySpring = new Spring(theA, theB, theSpringConstant, theSpringDamping);
        mForces.add(mySpring);
        return mySpring;
    }

    public Spring makeSpring(final Particle theA,
                             final Particle theB,
                             final float theSpringConstant,
                             final float theSpringDamping,
                             final float theRestLength) {
        Spring mySpring = new Spring(theA, theB, theSpringConstant, theSpringDamping, theRestLength);
        mForces.add(mySpring);
        return mySpring;
    }

    /* constraints */
    public void add(final IConstraint theConstraint) {
        mConstraints.add(theConstraint);
    }

    public void addConstraints(final ArrayList<? extends IConstraint> theConstraints) {
        mConstraints.addAll(theConstraints);
    }

    public void remove(final IConstraint theConstraint) {
        mConstraints.remove(theConstraint);
    }

    public ArrayList<IConstraint> constraints() {
        return mConstraints;
    }

    public IConstraint constraints(final int theIndex) {
        return mConstraints.get(theIndex);
    }

    /* integration */
    public void setIntegratorRef(IIntegrator theIntegrator) {
        mIntegrator = theIntegrator;
    }

    public IIntegrator getIntegrator() {
        return mIntegrator;
    }

    public void loop(final float theDeltaTime, final int theIterations) {
        for (int i = 0; i < theIterations; i++) {
            step(theDeltaTime / (float) theIterations);
        }
    }

    public void step(final float theDeltaTime) {
        handleForces();
        integrate(theDeltaTime);
        advance(theDeltaTime);
        handleConstraints();
        post(theDeltaTime);
    }

    protected synchronized void integrate(float theDeltaTime) {
        for (int j = 0; j < integrations_per_steps; j++) {
            mIntegrator.step(theDeltaTime / integrations_per_steps, this);
        }
    }

    protected synchronized void handleForces() {
        synchronized (mForces) {
            final Iterator<IForce> i = mForces.iterator();
            while (i.hasNext()) {
                final IForce myForce = i.next();
                if (myForce.dead()) {
                    i.remove();
                }
            }
        }
    }

    protected synchronized void handleConstraints() {
        synchronized (mConstraints) {
            for (int i = 0; i < constrain_iterations_per_steps; i++) {
                for (IConstraint myConstraint : mConstraints) {
                    myConstraint.apply(this);
                }
            }
        }
    }

    protected synchronized void advance(float theDeltaTime) {
        synchronized (mParticles) {
            final Iterator<Particle> i = mParticles.iterator();
            while (i.hasNext()) {
                final Particle mParticle = i.next();
                /* clear force */
                mParticle.force().set(0, 0, 0);
                /* age particle */
                mParticle.age(mParticle.age() + theDeltaTime);
                /* remove dead */
                if (HINT_REMOVE_DEAD) {
                    if (mParticle.dead()) {
                        i.remove();
                    }
                }
                /* recover NAN */
                if (HINT_RECOVER_NAN) {
                    if (Util.isNaN(mParticle.position())) {
                        if (Util.isNaN(mParticle.old_position())) {
                            mParticle.position().set(0, 0, 0);
                        } else {
                            mParticle.position().set(mParticle.old_position());
                        }
                    }
                    if (Util.isNaN(mParticle.velocity())) {
                        mParticle.velocity().set(0, 0, 0);
                    }
                }
                /* still */
                if (HINT_OPTIMIZE_STILL) {
                    final float mySpeed = Util.lengthSquared(mParticle.velocity());
                    mParticle.still(mySpeed > -EPSILON && mySpeed < EPSILON);
                }
            }
        }
    }

    protected synchronized void post(float theDeltaTime) {
        synchronized (mParticles) {
            for (Particle mParticle : mParticles) {
                if (mParticle.fixed()) {
                    mParticle.velocity().set(PVector.sub(mParticle.position(), mParticle.old_position()));
                }
                if (HINT_UPDATE_OLD_POSITION) {
                    mParticle.old_position().set(mParticle.position());
                }
            }
        }
    }
}
