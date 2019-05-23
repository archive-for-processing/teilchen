package teilchen.examples;

import processing.core.PApplet;
import teilchen.Particle;
import teilchen.Physics;
import teilchen.ShortLivedParticle;
import teilchen.force.Gravity;
import teilchen.force.LineDeflector2D;
import teilchen.force.ViscousDrag;

/**
 this sketch shows 1 how to create and use line deflectors 2 how to use 'ShortLivedParticle'
 */
public class SketchLesson04_LineDeflector extends PApplet {

    private Physics mPhysics;
    private LineDeflector2D mDeflector;

    public void settings() {
        size(640, 480, P3D);
    }

    public void setup() {
        smooth();
        frameRate(30);

        /* create a particle system */
        mPhysics = new Physics();

        mDeflector = new LineDeflector2D();
        mDeflector.a().set(50, height / 2);
        mDeflector.b().set(width - 50, height / 2 - 100);
        mPhysics.add(mDeflector);

        /* create gravity */
        Gravity myGravity = new Gravity();
        myGravity.force().y = 50;
        mPhysics.add(myGravity);

        /* create drag */
        ViscousDrag myViscousDrag = new ViscousDrag();
        myViscousDrag.coefficient = 0.1f;
        mPhysics.add(myViscousDrag);
    }

    public void draw() {
        /* rotate deflector plane */
        if (mousePressed) {
            mDeflector.a().set(mouseX, mouseY);
        }

        /* create a special particle */
        ShortLivedParticle myNewParticle = new ShortLivedParticle();
        myNewParticle.position().set(mouseX, mouseY);
        myNewParticle.velocity().set(0, random(100) + 50);
        /* this particle is removed after a specific interval */
        myNewParticle.setMaxAge(4);
        myNewParticle.radius(5);
        /* add particle manually to the particle system */
        mPhysics.add(myNewParticle);

        /* update physics */
        final float mDeltaTime = 1.0f / frameRate;
        mPhysics.step(mDeltaTime);

        /* draw all the particles in the particle system */
        background(255);
        for (int i = 0; i < mPhysics.particles().size(); i++) {
            Particle mParticle = mPhysics.particles(i);
            /* this special particle can tell you how much time it has to live.
             * we map this information to its transparency.
             */
            float myRatio = 1 - ((ShortLivedParticle) mParticle).ageRatio();
            stroke(0, 127);
            stroke(0, 64 * myRatio);
            if (mParticle.tagged()) {
                fill(255, 127, 0, 127 * myRatio);
            } else {
                fill(0, 32 * myRatio);
            }
            ellipse(mParticle.position().x, mParticle.position().y, mParticle.radius() * 2, mParticle.radius() * 2);
        }

        /* draw deflector */
        mDeflector.draw(g);

        /* finally remove the collision tag */
        mPhysics.removeTags();
    }

    public static void main(String[] args) {
        PApplet.main(SketchLesson04_LineDeflector.class.getName());
    }
}
