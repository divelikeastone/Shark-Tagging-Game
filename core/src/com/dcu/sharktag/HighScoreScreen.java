package com.dcu.sharktag;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

public class HighScoreScreen extends AbstractScreen{
	private Array<String> leaderboard = new Array<String>();
	
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private BitmapFont bitmapFont;
	private GlyphLayout textLayout;
	
	public HighScoreScreen(SharkTag game){
		super(game);
	}
	
	@Override
	public void show(){
		super.show();
		
		batch = game.getBatch();
		shapeRenderer = game.getShapeRenderer();
		bitmapFont = new BitmapFont();
		textLayout = new GlyphLayout();
		
		buildHighscore();
		buildGUI();
	}
	
	@Override
	public void render(float delta){
		clearScreen();
		game.drawBackground(stage);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0, 0, 0, 0.3f);
		shapeRenderer.rect(50, 0,
				game.WORLD_WIDTH - 100, game.WORLD_HEIGHT);
		shapeRenderer.end();
		
		batch.begin();
		for(int i = 0; i < leaderboard.size; i += 3){
			
			float x = 60;
			float y = game.WORLD_HEIGHT - 50 - i * 10;
			
			if(i > 10 * 3){
				x = game.WORLD_WIDTH / 2 + 10;
				y = game.WORLD_HEIGHT - 50 - (i - 10 * 3) * 10;
			}
			
			textLayout.setText(bitmapFont, leaderboard.get(i));
			bitmapFont.draw(batch, leaderboard.get(i), x, y);
			textLayout.setText(bitmapFont, leaderboard.get(i + 1));
			bitmapFont.draw(batch, leaderboard.get(i + 1), x + 60 - textLayout.width / 2, y);
			textLayout.setText(bitmapFont, leaderboard.get(i + 2));
			bitmapFont.draw(batch, leaderboard.get(i + 2), x + 190, y);
		}
		batch.end();
		super.render(delta);
	}
	
	private void buildGUI(){
		TextButton backButton = new TextButton("Back", game.getUISkin());
		backButton.setSize(game.WORLD_WIDTH / 2.2f, 40);
		backButton.setPosition(uiOriginX, 50, Align.center);
		stage.addActor(backButton);
		
		backButton.addListener(new ActorGestureListener(){
			@Override
			public void tap(InputEvent event, float x, float y, int count, int button){
				super.tap(event, x, y, count, button);
				game.setScreen(new MainMenu(game));
				dispose();
			}
		});
	}
	
	private void buildHighscore(){
		JsonValue table = game.getComm().requestHighscore().get("leaderboard");
		
		int place = 1;
		
		if(table != null){
			leaderboard.add("Place");
			leaderboard.add("Score");
			leaderboard.add("Player");
			
			for(int i = 0; i < table.size; i++){
				leaderboard.add(Integer.toString(place) + ".");
				leaderboard.add(table.get(i).get("score").asString());
				leaderboard.add(table.get(i).get("username").asString());
				place++;
			}
		}
	}
}
