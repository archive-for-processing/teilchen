import teilchen.*; 
import teilchen.behavior.*; 
import teilchen.constraint.*; 
import teilchen.cubicle.*; 
import teilchen.force.*; 
import teilchen.integration.*; 
import teilchen.util.*; 


/*
 * this sketch demonstrates how to use `TriangleDeflectors` in a 2D context to make particles
 * bounce off a triangle ( that looks like a line ). it also demonstrates how to use
 * `MortalParticle` to remove particles automatically once they leave the screen.
 *
 * press mouse to spawn particles.
 */
Physics mPhysics;
TriangleDeflector mTriangleDeflector;
void settings() {
    size(640, 480, P3D);
}
void setup() {
    /* physics */
    mPhysics = new Physics();
    Gravity myGravity = new Gravity(0, 20, 0);
    mPhysics.add(myGravity);
    /* triangle deflector */
    final float mPadding = 50;
    mTriangleDeflector = teilchen.util.Util.createTriangleDeflector2D(mPadding,
                                                                      height - mPadding - 40,
                                                                      width - mPadding,
                                                                      height - mPadding + 40,
                                                                      1.0f);
    mPhysics.add(mTriangleDeflector);
}
void draw() {
    mPhysics.step(1.0f / frameRate);
    /* draw particles */
    background(255);
    strokeWeight(2);
    for (int i = 0; i < mPhysics.particles().size(); i++) {
        Particle mParticle = mPhysics.particles(i);
        if (mParticle.tagged()) {
            stroke(255, 127, 0);
            fill(0);
        } else {
            noStroke();
            fill(0);
        }
        pushMatrix();
        translate(mParticle.position().x, mParticle.position().y);
        ellipse(0, 0, 5, 5);
        popMatrix();
    }
    /* draw deflectors */
    strokeWeight(1);
    noFill();
    stroke(0);
    line(mTriangleDeflector.a().x, mTriangleDeflector.a().y, mTriangleDeflector.b().x, mTriangleDeflector.b().y);
    /* finally remove the collision tag */
    mPhysics.removeTags();
    if (mousePressed) {
        /* create and add a particle to the system */
        MyMortalParticle mParticle = new MyMortalParticle();
        mPhysics.add(mParticle);
        /* set particle to mouse position with random velocity */
        mParticle.position().set(mouseX, mouseY);
        mParticle.velocity().set(random(-20, 20), 0);
    }
}
class MyMortalParticle extends MortalParticle {
    boolean isDead() {
        return position().y > height || still();
    }
}
