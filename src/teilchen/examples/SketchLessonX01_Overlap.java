package teilchen.examples;

import processing.core.PApplet;
import processing.core.PVector;
import teilchen.Particle;
import teilchen.Physics;
import teilchen.force.Gravity;
import teilchen.force.Spring;
import teilchen.force.ViscousDrag;
import teilchen.util.Overlap;

public class SketchLessonX01_Overlap extends PApplet {

    /*
     * this sketch is exactly like Lesson06_Springs, except that it also shows how
     * to resolveOverlap overlaps.
     */

    private static final float PARTICLE_RADIUS = 13;
    private Physics mPhysics;
    private Particle mRoot;

    public void settings() {
        size(640, 480, P3D);
    }

    public void setup() {
        mPhysics = new Physics();

        /* create drag */
        mPhysics.add(new ViscousDrag());
        mPhysics.add(new Gravity(new PVector(0, 100f, 0)));

        mRoot = mPhysics.makeParticle(width / 2, height / 2, 0.0f);
        mRoot.mass(30);
        mRoot.fixed(true);
        mRoot.radius(PARTICLE_RADIUS);
    }

    public void draw() {
        if (mousePressed) {
            Particle mParticle = mPhysics.makeParticle(mouseX, mouseY, 0);
            mPhysics.makeSpring(mRoot, mParticle);

            /*
             * we define a radius for the particle so the particle has
             * dimensions
             */
            mParticle.radius(random(PARTICLE_RADIUS / 2) + PARTICLE_RADIUS);
        }


        /* move overlapping particles away from each other */
        for (int i = 0; i < 10; i++) {
            mRoot.position().set(width / 2, height / 2, 0.0f); // a bit of a 'hack'
            Overlap.resolveOverlap(mPhysics.particles());
        }

        /* update the particle system */
        final float mDeltaTime = 1.0f / frameRate;
        mPhysics.step(mDeltaTime);

        /* draw particles and connecting line */
        background(255);

        /* draw springs */
        noFill();
        stroke(255, 0, 127, 64);
        for (int i = 0; i < mPhysics.forces().size(); i++) {
            if (mPhysics.forces().get(i) instanceof Spring) {
                Spring mSSpring = (Spring) mPhysics.forces().get(i);
                line(mSSpring.a().position().x,
                     mSSpring.a().position().y,
                     mSSpring.b().position().x,
                     mSSpring.b().position().y);
            }
        }
        /* draw particles */
        fill(255, 127);
        stroke(164);
        for (int i = 0; i < mPhysics.particles().size(); i++) {
            ellipse(mPhysics.particles().get(i).position().x,
                    mPhysics.particles().get(i).position().y,
                    mPhysics.particles().get(i).radius() * 2,
                    mPhysics.particles().get(i).radius() * 2);
        }
    }

    public static void main(String[] args) {
        PApplet.main(SketchLessonX01_Overlap.class.getName());
    }
}
