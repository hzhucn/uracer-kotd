package com.bitfire.uracer.entities.vehicles;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.uracer.entities.EntityManager;
import com.bitfire.uracer.factories.CarFactory.CarType;
import com.bitfire.uracer.simulations.car.CarForces;
import com.bitfire.uracer.simulations.car.CarInputMode;
import com.bitfire.uracer.simulations.car.CarModel;
import com.bitfire.uracer.simulations.car.Replay;

/**
 * Implements an automated Car, playing previously recorded events. It will
 * ignore car-to-car collisions, but will respect in-track collisions and
 * responses.
 *
 * @author manuel
 *
 */

public class GhostCar extends Car
{
	private Replay replay;
	private int indexPlay;
	private boolean hasReplay;

	private GhostCar( CarGraphics graphics, CarType type, CarModel model )
	{
		super( graphics, model, type, CarInputMode.InputFromReplay, new Vector2( 0, 0 ), 0 );
		indexPlay = 0;
		hasReplay = false;
		replay = null;
		setActive( false, true );
	}

	// factory methods

	public static GhostCar createForFactory( CarGraphics graphics, CarType type, CarModel model )
	{
		GhostCar ghost = new GhostCar( graphics, type, model );
		EntityManager.add( ghost );
		return ghost;
	}

	public void setReplay( Replay replay )
	{
		this.replay = replay;
		hasReplay = (replay != null && replay.getEventsCount() > 0 );
		setActive( hasReplay, true );
		if( hasReplay )
		{
			restart(replay);
		}
		else
		{
			System.out.println("Replay has no recorded events, disabling replaying.");
		}
	}

	private void restart( Replay replay )
	{
		pos( replay.carPosition );
		orient( replay.carOrientation );
		getCarDescriptor().set( replay.carDescriptor );
		indexPlay = 0;
	}

	@Override
	public void onRender( SpriteBatch batch )
	{
		if( isActive() )
		{
			graphics.render( batch, stateRender, 0.5f );
		}
	}

	@Override
	public void onDebug()
	{
	}

	@Override
	protected void onComputeCarForces( CarForces forces )
	{
		forces.reset();

		if( hasReplay )
		{
			if( indexPlay == replay.getEventsCount() )
			{
				System.out.println( "restarting replay" );
				restart(replay);
			}

			forces.set( replay.forces.get( indexPlay++ ) );
		}
	}
}