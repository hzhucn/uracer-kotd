package com.bitfire.uracer.game.logic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.bitfire.uracer.Art;
import com.bitfire.uracer.Config;
import com.bitfire.uracer.ScalingStrategy;
import com.bitfire.uracer.game.events.GameEvents;
import com.bitfire.uracer.game.events.GameRendererEvent;
import com.bitfire.uracer.game.input.Replay;
import com.bitfire.uracer.game.logic.GameTask;
import com.bitfire.uracer.game.logic.LapState;
import com.bitfire.uracer.utils.Manager;
import com.bitfire.uracer.utils.NumberString;

// FIXME should extrapolate the lap times thing.. this is just a fucking manager
public final class Hud extends GameTask {

	private final Manager<HudElement> manager = new Manager<HudElement>();

	private HudLabel best, curr, last;
	private LapState lapState;
	// private HudDebugMeter meterLatForce, meterSkidMarks, meterSmoke;

	private GameRendererEvent.Listener gameRendererEvent = new GameRendererEvent.Listener() {
		@Override
		public void gameRendererEvent( GameRendererEvent.Type type ) {
			SpriteBatch batch = GameEvents.gameRenderer.batch;

			switch( type ) {
			case BatchAfterMeshes:
				renderAfterMeshes( batch );
				break;
			case BatchDebug:
				if( Config.Graphics.RenderHudDebugInfo ) {
					onDebug( batch );
				}
				break;
			}
		}
	};

	private void renderAfterMeshes( SpriteBatch batch ) {
		Array<HudElement> items = manager.items;
		for( int i = 0; i < items.size; i++ ) {
			items.get( i ).onRender( batch );
		}

		curr.render( batch );
		best.render( batch );
		last.render( batch );
	}

	// effects
	public Hud( ScalingStrategy scalingStrategy, LapState lapState ) {
		GameEvents.gameRenderer.addListener( gameRendererEvent, GameRendererEvent.Type.BatchAfterMeshes, GameRendererEvent.Order.DEFAULT );
		GameEvents.gameRenderer.addListener( gameRendererEvent, GameRendererEvent.Type.BatchDebug, GameRendererEvent.Order.DEFAULT );

		this.lapState = lapState;

		// grid-based position
		int gridX = (int)((float)Gdx.graphics.getWidth() / 5f);

		// laptimes component
		best = new HudLabel( scalingStrategy, Art.fontCurseYRbig, "BEST  TIME\n-.----" );
		curr = new HudLabel( scalingStrategy, Art.fontCurseYRbig, "YOUR  TIME\n-.----" );
		last = new HudLabel( scalingStrategy, Art.fontCurseYRbig, "LAST  TIME\n-.----" );

		curr.setPosition( gridX, 50 * scalingStrategy.invTileMapZoomFactor );
		last.setPosition( gridX * 3, 50 * scalingStrategy.invTileMapZoomFactor );
		best.setPosition( gridX * 4, 50 * scalingStrategy.invTileMapZoomFactor );

		// // meter lateral forces
		// meterLatForce = new HudDebugMeter( car, 0, 100, 5 );
		// meterLatForce.setLimits( 0, 1 );
		// meterLatForce.setName( "lat-force-FRONT" );
		//
		// // meter skid marks count
		// meterSkidMarks = new HudDebugMeter( car, 1, 100, 5 );
		// meterSkidMarks.setLimits( 0, CarSkidMarks.MaxSkidMarks );
		// meterSkidMarks.setName( "skid marks count" );
		//
		// meterSmoke = new HudDebugMeter( car, 2, 100, 5 );
		// meterSmoke.setLimits( 0, SmokeTrails.MaxParticles );
		// meterSmoke.setName( "smokepar count" );
	}

	public void add( HudElement element ) {
		manager.add( element );
	}

	public void remove( HudElement element ) {
		manager.remove( element );
	}

	@Override
	public void dispose() {
		super.dispose();
		manager.dispose();
	}

	@Override
	public void onReset() {
		Array<HudElement> items = manager.items;
		for( int i = 0; i < items.size; i++ ) {
			items.get( i ).onReset();
		}
	}

	@Override
	protected void onTick() {
		for( int i = 0; i < manager.items.size; i++ ) {
			manager.items.get( i ).onTick();
		}

		update();
	}

	private void update() {
		// current time
		curr.setString( "YOUR  TIME\n" + NumberString.format( lapState.getElapsedSeconds() ) + "s" );

		// render best lap time
		Replay rbest = lapState.getBestReplay();

		// best time
		if( rbest != null && rbest.isValid ) {
			// has best
			best.setString( "BEST  TIME\n" + NumberString.format( rbest.trackTimeSeconds ) + "s" );
		} else {
			// temporarily use last track time
			if( lapState.hasLastTrackTimeSeconds() ) {
				best.setString( "BEST  TIME\n" + NumberString.format( lapState.getLastTrackTimeSeconds() ) + "s" );
			} else {
				best.setString( "BEST TIME\n-:----" );
			}
		}

		// last time
		if( lapState.hasLastTrackTimeSeconds() ) {
			// has only last
			last.setString( "LAST  TIME\n" + NumberString.format( lapState.getLastTrackTimeSeconds() ) + "s" );
		} else {
			last.setString( "LAST  TIME\n-:----" );
		}
	}

	// FIXME, create a DebugHud as a debug aid instead..
	public void onDebug( SpriteBatch batch ) {
		// DriftState drift = GameData.States.driftState;

		// // lateral forces
		// meterLatForce.setValue( drift.driftStrength );
		//
		// if( drift.isDrifting ) {
		// meterLatForce.color.set( .3f, 1f, .3f, 1f );
		// } else {
		// meterLatForce.color.set( 1f, 1f, 1f, 1f );
		// }
		//
		// meterLatForce.render( batch );

		// meterSkidMarks.setValue( GameData.Systems.trackEffects.getParticleCount( Type.CarSkidMarks ) );
		// meterSkidMarks.render( batch );
		//
		// meterSmoke.setValue( GameData.Systems.trackEffects.getParticleCount( Type.SmokeTrails ) );
		// meterSmoke.render( batch );
	}
}
