package com.bitfire.uracer.game.logic.sounds.effects;

import com.badlogic.gdx.audio.Sound;
import com.bitfire.uracer.Config;
import com.bitfire.uracer.Sounds;
import com.bitfire.uracer.game.actors.player.PlayerCar;
import com.bitfire.uracer.game.logic.sounds.SoundEffect;
import com.bitfire.uracer.utils.AMath;

public final class PlayerEngineSoundEffect extends SoundEffect {
	private Sound carEngine = null;
	private long carEngineId = -1;
	private static float carEnginePitchStart = 0;
	private float carEnginePitchLast = 0;
	private static final float carEnginePitchMin = 1f;
	private PlayerCar player;

	public PlayerEngineSoundEffect( PlayerCar player ) {
		this.player = player;
		carEngine = Sounds.carEngine;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void tick() {
		if( carEngineId > -1 ) {
			float speedFactor = player.carState.currSpeedFactor;

			float pitch = carEnginePitchMin + speedFactor * 0.65f;
			if( !AMath.equals( pitch, carEnginePitchLast ) ) {
				carEngine.setPitch( carEngineId, pitch );
				carEnginePitchLast = pitch;
			}
		}
	}

	@Override
	public void start() {
		if( Config.isDesktop ) {
			carEngineId = carEngine.loop( 1f );
		} else {
			// UGLY HACK FOR ANDROID
			carEngineId = checkedLoop( carEngine, 1f );
		}

		reset();
	}

	@Override
	public void stop() {
		carEngine.stop();
	}

	@Override
	public void reset() {
		stop();
		carEnginePitchStart = carEnginePitchMin;
		carEnginePitchLast = carEnginePitchMin;
		carEngine.setPitch( carEngineId, carEnginePitchStart );
	}
}