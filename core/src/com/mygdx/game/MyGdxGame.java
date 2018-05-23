package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.utils.Array;
import javafx.util.Pair;
import net.dermetfan.gdx.physics.box2d.Box2DUtils;
import net.dermetfan.gdx.physics.box2d.Chain;
import net.dermetfan.gdx.physics.box2d.Chain.DefBuilder;

public class MyGdxGame extends ApplicationAdapter {

    private World world;
    private final float alturaChao = -1;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Chain corda;
    private Texture texturaPedacoCorda;
    private Array<Pair<Body, Sprite>> cordaSprites;
    private ShapeRenderer shapes;
    private final float mundoDaFisicaParaTela = 50f;
    private final float telaParaMundoDaFisica = 1 / mundoDaFisicaParaTela;
    private final Vector2 tamanhoSegmento = new Vector2(0.1f, 0.05f);
    private final int numeroDeSegmentos = 50;
    private Body segmentoPuxadoDaEsquerda;
    private Body segmentoPuxadoDaDireita;
    
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        world = new World(new Vector2(0, -9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();

        EdgeShape groundShape = new EdgeShape();
        groundShape.set(-mundoDaFisicaParaTela, alturaChao, mundoDaFisicaParaTela, alturaChao);

        Body ground = world.createBody(new BodyDef());
        ground.createFixture(groundShape, 10);

        groundShape.dispose();
        
        texturaPedacoCorda = new Texture("corda.png");
        texturaPedacoCorda.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        corda = createRope();
        
        segmentoPuxadoDaEsquerda = corda.getSegment(corda.length()/8);
        segmentoPuxadoDaDireita = corda.getSegment((int)(corda.length() * (7f/8)));
        Body segmentoMeio = corda.getSegment(corda.length()/2);
        
        segmentoMeio.applyLinearImpulse(new Vector2(0, 4), Vector2.Zero, true);
        
        Gdx.gl.glClearColor(1, 1, 1, 1);
    }

    private Chain createRope() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(tamanhoSegmento.x, tamanhoSegmento.y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 2;

        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.localAnchorA.y = -Box2DUtils.height(shape) / 2;
        jointDef.localAnchorB.y = Box2DUtils.height(shape) / 2;

        DefBuilder builder = new DefBuilder(world, bodyDef, fixtureDef, jointDef);

        Chain chain = new Chain(numeroDeSegmentos, builder);

        shape.dispose();
        cordaSprites = new Array<Pair<Body,Sprite>>(chain.getSegments().size);
        for (Body b : chain.getSegments()) {
            cordaSprites.add(new Pair<Body, Sprite>(b, new Sprite(texturaPedacoCorda)));
        }
        
        return chain;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width / mundoDaFisicaParaTela;
        camera.viewportHeight = height / mundoDaFisicaParaTela;
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        segmentoPuxadoDaEsquerda.applyForceToCenter(new Vector2(-20, 0), true);
        segmentoPuxadoDaDireita.applyForceToCenter(new Vector2(20, 0), true);

        
        world.step(1/240f, 8, 3);
//        debugRenderer.render(world, camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.line(-mundoDaFisicaParaTela, alturaChao, mundoDaFisicaParaTela, alturaChao, Color.GRAY, Color.GRAY);
        shapes.end();
        batch.begin();
        for (Pair<Body, Sprite> segmento : this.cordaSprites) {
            Sprite sprite = segmento.getValue();
            Body body = segmento.getKey();
            
            sprite.setScale(telaParaMundoDaFisica * tamanhoSegmento.x, telaParaMundoDaFisica * tamanhoSegmento.y);
            sprite.setCenter(body.getWorldCenter().x, body.getWorldCenter().y);
            sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
            sprite.draw(batch);
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
