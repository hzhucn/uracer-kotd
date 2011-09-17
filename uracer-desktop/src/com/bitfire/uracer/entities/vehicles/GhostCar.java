package com.bitfire.uracer.entities.vehicles;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.uracer.entities.EntityManager;
import com.bitfire.uracer.simulations.car.CarInput;

public class GhostCar extends Car
{
	private CarInput input = new CarInput();

	private GhostCar( Car car )
	{
		super( car.graphics, car.carDesc.carModel, car.carType, new Vector2(0,0), 0, false );
		inputMode = CarInputMode.InputFromReplay;
	}

	// factory method
	public static GhostCar createForFactory( Car car )
	{
		GhostCar ghost = new GhostCar( car );
		EntityManager.add( ghost );
		return ghost;
	}

	@Override
	public void onRender( SpriteBatch batch )
	{
		if( recorder.hasReplay() )
		{
			graphics.render( batch, stateRender, 0.5f );
		}
	}

	@Override
	public void onDebug()
	{
		// Debug.drawString( "[R] input count = " + input.size(), 0, 124 );
		// Debug.drawString( "[R] play index = " + playIndex, 0, 132 );
	}

	@Override
	protected CarInput acquireInput()
	{
		input.reset();

		if( recorder.hasReplay() )
		{
			if( !recorder.get( input ) )
			{
				if( recorder.hasFinishedPlaying() )
				{
					// restart playing
					System.out.println( "restarting replay" );
					recorder.beginPlay( this );
				}
			}
		}

		return input;
	}
}
