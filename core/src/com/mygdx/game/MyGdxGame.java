package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import net.dermetfan.gdx.physics.box2d.Box2DUtils;
import net.dermetfan.gdx.physics.box2d.Chain;
import net.dermetfan.gdx.physics.box2d.Chain.DefBuilder;

public class MyGdxGame extends ApplicationAdapter {

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Chain corda;
    private Texture texturaPedacoCorda;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();

        EdgeShape groundShape = new EdgeShape();
        groundShape.set(-50, -1, 50, -1);

        Body ground = world.createBody(new BodyDef());
        ground.createFixture(groundShape, 10);

        groundShape.dispose();
        
        this.corda = createRope();
        texturaPedacoCorda = new Texture("corda.png");
        Gdx.gl.glClearColor(1, 1, 1, 1);
    }

    private Chain createRope() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(.1f, .25f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 2;

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.localAnchorA.y = -Box2DUtils.height(shape) / 2;
        jointDef.localAnchorB.y = Box2DUtils.height(shape) / 2;

        DefBuilder builder = new DefBuilder(world, bodyDef, fixtureDef, jointDef);

        Chain chain = new Chain(3, builder);

        shape.dispose();
        
        return chain;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width / 50;
        camera.viewportHeight = height / 50;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        world.step(1/240f, 8, 3);
        debugRenderer.render(world, camera.combined);
        batch.begin();
        for (int i = 0; i < this.corda.length(); i++) {
            Body pedacoBody = this.corda.getSegment(i);
            Vector2 posicaoCentroPedaco = pedacoBody.getPosition();
            Vector2 cantoEsquerdoInferiorPedaco = posicaoCentroPedaco.sub(0.1f, 0.25f);
            batch.setTransformMatrix(new Matrix4(new Quaternion(Vector3.Z, (float) (pedacoBody.getAngle() * 180 / Math.PI))));
            batch.draw(texturaPedacoCorda, cantoEsquerdoInferiorPedaco.x, cantoEsquerdoInferiorPedaco.y, 0.25f * 2, 0.01f * 2);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        world.dispose();
        debugRenderer.dispose();
    }
}
