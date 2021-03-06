import teilchen.*; 
import teilchen.behavior.*; 
import teilchen.constraint.*; 
import teilchen.cubicle.*; 
import teilchen.force.*; 
import teilchen.integration.*; 
import teilchen.util.*; 


/*
 * this sketch demonstrates how to use `TriangleDeflectors` to make particles bounce off two
 * triangles. it also demonstrates how to use `MortalParticle` to remove particles
 * automatically once they leave the screen.
 *
 * press mouse to create particles. move mouse to rotate view.
 */
Physics mPhysics;
ArrayList<TriangleDeflector> mTriangleDeflectors;
void settings() {
    size(640, 480, P3D);
}
void setup() {
    rectMode(CENTER);
    hint(DISABLE_DEPTH_TEST);
    /* physics */
    mPhysics = new Physics();
    Gravity myGravity = new Gravity(0, 0, -30);
    mPhysics.add(myGravity);
    /* triangle deflectors */
    final PVector[] mVertices = new PVector[]{new PVector(0, 0, 0),
                                              new PVector(width, height, 0),
                                              new PVector(0, height, 0),
                                              new PVector(0, 0, 0),
                                              new PVector(width, 0, 0),
                                              new PVector(width, height, 0),};
    mTriangleDeflectors = teilchen.util.Util.createTriangleDeflectors(mVertices, 1.0f);
    mPhysics.addForces(mTriangleDeflectors);
}
void draw() {
    if (mousePressed) {
        /* create and add a particle to the system */
        MyMortalParticle mParticle = new MyMortalParticle();
        mPhysics.add(mParticle);
        /* set particle to mouse position with random velocity */
        mParticle.position().set(mouseX, random(height), height / 2.0f);
        mParticle.velocity().set(random(-20, 20), 0, random(20));
    }
    final float mDeltaTime = 1.0f / frameRate;
    mPhysics.step(mDeltaTime);
    /* draw particles */
    background(255);
    camera(width / 2.0f, mouseY + height, height * 1.3f - mouseY, width / 2.0f, height / 2.0f, 0, 0, 1, 0);
    noStroke();
    sphereDetail(10);
    for (int i = 0; i < mPhysics.particles().size(); i++) {
        Particle mParticle = mPhysics.particles(i);
        if (mParticle.tagged()) {
            fill(255, 127, 64);
        } else {
            fill(0);
        }
        pushMatrix();
        translate(mParticle.position().x, mParticle.position().y, mParticle.position().z);
        sphere(5);
        popMatrix();
    }
    /* draw deflectors */
    noFill();
    for (TriangleDeflector mTriangleDeflector : mTriangleDeflectors) {
        DrawLib.draw(g, mTriangleDeflector, color(0), color(255, 127, 0), color(0, 127, 255));
    }
    /* finally remove the collision tag */
    mPhysics.removeTags();
}
class MyMortalParticle extends MortalParticle {
    boolean isDead() {
        return position().z < -height;
    }
}
