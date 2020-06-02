import teilchen.*; 
import teilchen.behavior.*; 
import teilchen.constraint.*; 
import teilchen.cubicle.*; 
import teilchen.force.*; 
import teilchen.integration.*; 
import teilchen.util.*; 


/*
 * this sketch demonstratwa some advanced use of particles, springs ( e.g `MuscleSpring` )
 * and attractors to create a group of `StickMan`.
 */
Physics mPhysics;
Attractor mAttractor;
Gravity mGravity;
StickMan[] mMyStickMan;
void settings() {
    size(640, 480, P3D);
}
void setup() {
    noFill();
    mPhysics = new Physics();
    mPhysics.setIntegratorRef(new RungeKutta());
    mGravity = new Gravity();
    mGravity.force().y = 20;
    mPhysics.add(mGravity);
    ViscousDrag mViscousDrag = new ViscousDrag();
    mViscousDrag.coefficient = 0.85f;
    mPhysics.add(mViscousDrag);
    mAttractor = new Attractor();
    mAttractor.radius(500);
    mAttractor.strength(0);
    mAttractor.position().set(width / 2.0f, height / 2.0f);
    mPhysics.add(mAttractor);
    mMyStickMan = new StickMan[20];
    for (int i = 0; i < mMyStickMan.length; i++) {
        mMyStickMan[i] = new StickMan(mPhysics, random(0, width), random(0.3f, 0.6f));
    }
}
void draw() {
    mPhysics.step(1f / 60f);
    Overlap.resolveOverlap(mPhysics.particles());
    /* constraint particles */
    for (int i = 0; i < mPhysics.particles().size(); i++) {
        if (mPhysics.particles(i).position().y > height - 10) {
            mPhysics.particles(i).position().y = height - 10;
        }
        if (mPhysics.particles(i).position().x > width) {
            mPhysics.particles(i).position().x = width;
        }
        if (mPhysics.particles(i).position().x < 0) {
            mPhysics.particles(i).position().x = 0;
        }
    }
    /* handle particles */
    if (mousePressed) {
        mAttractor.position().set(mouseX, mouseY);
        if (mouseButton == RIGHT) {
            mAttractor.strength(-500);
            mAttractor.radius(500);
        } else {
            mAttractor.strength(500);
            mAttractor.radius(100);
        }
    } else {
        mAttractor.strength(0);
    }
    if (keyPressed) {
        mGravity.force().y = -10;
    } else {
        mGravity.force().y = 20;
    }
    /* draw */
    background(255);
    /* draw springs */
    stroke(0, 20);
    for (int i = 0; i < mPhysics.forces().size(); i++) {
        if (mPhysics.forces(i) instanceof Spring) {
            Spring mySpring = (Spring) mPhysics.forces(i);
            line(mySpring.a().position().x,
                 mySpring.a().position().y,
                 mySpring.b().position().x,
                 mySpring.b().position().y);
        }
    }
    /* draw particles */
    for (int i = 0; i < mPhysics.particles().size(); i++) {
        ellipse(mPhysics.particles(i).position().x, mPhysics.particles(i).position().y, 5, 5);
    }
    /* draw man */
    for (StickMan mMyStickMan1 : mMyStickMan) {
        mMyStickMan1.draw(g);
    }
}
