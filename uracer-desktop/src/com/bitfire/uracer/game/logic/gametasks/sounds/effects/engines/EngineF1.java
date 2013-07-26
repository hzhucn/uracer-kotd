
package com.bitfire.uracer.game.logic.gametasks.sounds.effects.engines;

import net.sourceforge.jFuzzyLogic.FIS;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.bitfire.uracer.resources.Sounds;

public class EngineF1 extends EngineSoundSet {
	private float[] gears = {3, 1, 0.7f, 0.5f, 0.3f, 0.2f, 0.1f};

	public EngineF1 () {
		super();
		engine = Sounds.carEngine_f1;
		rpm = 1000;
		gear = 0;

		//@off
		feIdle = FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolIdle.fcl", FileType.Internal).read(), true);
		feOnLow = FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolOnLow.fcl", FileType.Internal).read(), true);
		feOnMid= FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolOnMid.fcl", FileType.Internal).read(), true);
		feOnHigh = FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolOnHigh.fcl", FileType.Internal).read(), true);
		feOffLow = FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolOffLow.fcl", FileType.Internal).read(), true);
		feOffMid= FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolOffMid.fcl", FileType.Internal).read(), true);
		feOffHigh = FIS.load(Gdx.files.getFileHandle("data/audio/car-engine/fuzzy/engineVolOffHigh.fcl", FileType.Internal).read(), true);
		//@on
	}

	@Override
	public float getGearRatio () {
		return gears[gear];
	}

	@Override
	public void updatePitches () {
		// sample specific
		if (rpm < 3000) {
			setXnaPitch(0, rpm / 3000);
		}

		setXnaPitch(1, rpm / 10000 - 0.4f);
		setXnaPitch(2, rpm / 10000 - 0.4f);
		setXnaPitch(3, rpm / 10000 - 1f);
		setXnaPitch(4, rpm / 10000 - 1f);
		setXnaPitch(5, rpm / 10000 - 1f);
		setXnaPitch(6, rpm / 10000 - 1f);
	}
}
