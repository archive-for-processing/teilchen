import teilchen.*; 
import teilchen.behavior.*; 
import teilchen.constraint.*; 
import teilchen.cubicle.*; 
import teilchen.force.*; 
import teilchen.integration.*; 
import teilchen.util.*; 


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
                                              new PVector(0,
                                                          height,
                                                          0),
                                              new PVector(0, 0, 0),
                                              new PVector(width, 0, 0),
                                              new PVector(width, height, 0),};
    mTriangleDeflectors = teilchen.util.Util.createTriangleDeflectors(mVertices, 1.0f);
    mPhysics.addForces(mTriangleDeflectors);
}
void draw() {
    if (mousePressed) {
        /* create and add a particle to the system */
        Particle mParticle = mPhysics.makeParticle();
        /* set particle to mouse position with random velocity */
        mParticle.position().set(mouseX, random(height), height / 2);
        mParticle.velocity().set(random(-20, 20), 0, random(20));
    }
    final float mDeltaTime = 1.0f / frameRate;
    mPhysics.step(mDeltaTime);
    /* remove particles  */
    for (int i = 0; i < mPhysics.particles().size(); i++) {
        Particle mParticle = mPhysics.particles(i);
        if (mParticle.position().z < -height) {
            mPhysics.particles().remove(i);
        }
    }
    /* draw particles */
    background(255);
    camera(width / 2, mouseY + height, height * 1.3f - mouseY, width / 2, height / 2, 0, 0, 1, 0);
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
        DrawLib.draw(g, mTriangleDeflector, color(0), color(255, 0, 0), color(0, 255, 0));
    }
    /* finally remove the collision tag */
    mPhysics.removeTags();
}
