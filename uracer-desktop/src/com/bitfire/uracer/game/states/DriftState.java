package com.bitfire.uracer.game.states;

import com.bitfire.uracer.game.Time;
import com.bitfire.uracer.game.actors.Car;
import com.bitfire.uracer.game.events.DriftStateEvent.Type;
import com.bitfire.uracer.game.events.GameEvents;
import com.bitfire.uracer.game.events.GameLogicEvent;
import com.bitfire.uracer.utils.AMath;

public final class DriftState {
	public Car car;
	public boolean isDrifting = false;
	public boolean hasCollided = false;
	public float lateralForcesFront = 0, lateralForcesRear = 0;
	public float driftStrength;

	private float lastRear = 0, lastFront = 0;
	private Time time, collisionTime;

	private final GameLogicEvent.Listener gameLogicEvent = new GameLogicEvent.Listener() {
		@Override
		public void gameLogicEvent( GameLogicEvent.Type type ) {
			switch( type ) {
			case onReset:
			case onRestart:
				reset();
				break;
			}
		}
	};

	public DriftState( Car car ) {
		GameEvents.gameLogic.addListener( gameLogicEvent, GameLogicEvent.Type.onReset );
		GameEvents.gameLogic.addListener( gameLogicEvent, GameLogicEvent.Type.onRestart );
		this.car = car;
		reset();
	}

	// TODO, a State interface with a reset() method! this way it could be assumed the state can be bound to some other
	// car
	public void reset() {
		time = new Time();
		collisionTime = new Time();

		lastFront = 0;
		lastRear = 0;
		hasCollided = false;
		isDrifting = false;
		lateralForcesFront = 0;
		lateralForcesRear = 0;
		driftStrength = 0;
	}

	// onCollision?
	public void invalidateByCollision() {
		if( !isDrifting ) {
			return;
		}

		isDrifting = false;
		hasCollided = true;
		collisionTime.start();
		time.stop();
		GameEvents.driftState.trigger( car, Type.onEndDrift );
	}

	public void update() {
		float oneOnMaxGrip = 1f / car.getCarModel().max_grip;

		// lateral forces are in the range [-max_grip, max_grip]
		lateralForcesFront = AMath.lowpass( lastFront, car.getLateralForceFront().y, 0.2f );
		lastFront = lateralForcesFront;
		lateralForcesFront = AMath.clamp( Math.abs( lateralForcesFront ) * oneOnMaxGrip, 0f, 1f );	// normalize

		lateralForcesRear = AMath.lowpass( lastRear, car.getLateralForceRear().y, 0.2f );
		lastRear = lateralForcesRear;
		lateralForcesRear = AMath.clamp( Math.abs( lateralForcesRear ) * oneOnMaxGrip, 0f, 1f );	// normalize

		// compute strength
		driftStrength = AMath.fixup( (lateralForcesFront + lateralForcesRear) * 0.5f );

		if( hasCollided ) {
			// ignore drifts for a couple of seconds
			// TODO use this in a penalty system
			if( collisionTime.elapsed( Time.Reference.TickSeconds ) >= 2 ) {
				collisionTime.stop();
				hasCollided = false;
			}
		} else {
			// FIXME should be expressed as a percent value ref. maxvel, to scale to different max velocities
			float vel = car.getVelocity().len();

			if( !isDrifting ) {
				// search for onBeginDrift
				if( driftStrength > 0.4f && vel > 20 ) {
					isDrifting = true;
					hasCollided = false;
					// driftStartTime = System.currentTimeMillis();
					time.start();
					GameEvents.driftState.trigger( car, Type.onBeginDrift );
					// Gdx.app.log( "DriftState", car.getClass().getSimpleName() + " onBeginDrift()" );
				}
			} else {
				// search for onEndDrift
				if( isDrifting && (driftStrength < 0.2f || vel < 15f) ) {
					time.stop();
					isDrifting = false;
					hasCollided = false;
					GameEvents.driftState.trigger( car, Type.onEndDrift );
					// Gdx.app.log( "DriftState", car.getClass().getSimpleName() + " onEndDrift(), " + time.elapsed(
					// Time.Reference.TickSeconds ) + "s" );
				}
			}
		}
	}

	public float driftSeconds() {
		return time.elapsed( Time.Reference.TickSeconds );
	}
}
