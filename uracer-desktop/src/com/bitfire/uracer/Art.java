package com.bitfire.uracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Art
{
	public static TextureRegion[][] base6;
	public static TextureRegion titleScreen;
	public static TextureRegion quad;
	public static TextureRegion rope;
	public static TextureRegion disc;
	public static TextureAtlas cars;

	public static void load()
	{
		titleScreen = load( "data/base/titlescreen.png", 480, 320 );
		base6 = split( "data/base/base6.png", 6, 6 );
		quad = new TextureRegion( base6[0][10], 0, 0, 18, 18 );
		rope = new TextureRegion( new Texture( Gdx.files.internal( "data/base/rope.png" ) ), 0, 0, 64, 18 );
		disc = new TextureRegion( new Texture( Gdx.files.internal( "data/base/disc.png" ) ), 0, 0, 256, 256 );
		cars = new TextureAtlas(Gdx.files.internal("data/base/cars.pack"));
	}

	private static TextureRegion[][] split( String name, int width, int height )
	{
		return split( name, width, height, false, true );
	}

	private static TextureRegion[][] split( String name, int width, int height, boolean flipX, boolean flipY )
	{
		Texture texture = newTexture( name );
		int xSlices = texture.getWidth() / width;
		int ySlices = texture.getHeight() / height;
		TextureRegion[][] res = new TextureRegion[ xSlices ][ ySlices ];
		for( int x = 0; x < xSlices; x++ )
		{
			for( int y = 0; y < ySlices; y++ )
			{
				res[x][y] = new TextureRegion( texture, x * width, y * height, width, height );
				res[x][y].flip( flipX, flipY );
			}
		}
		return res;
	}

	public static TextureRegion load( String name, int width, int height )
	{
		Texture texture = newTexture( name );
		TextureRegion region = new TextureRegion( texture, 0, 0, width, height );
		region.flip( false, true );
		return region;
	}

	private static Texture newTexture( String name )
	{
		return new Texture( Gdx.files.internal( name ), Format.RGB565, false );
	}
}