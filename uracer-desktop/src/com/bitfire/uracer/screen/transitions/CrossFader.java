
package com.bitfire.uracer.screen.transitions;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.bitfire.postprocessing.utils.FullscreenQuad;
import com.bitfire.uracer.URacer;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.screen.Screen;
import com.bitfire.uracer.screen.ScreenFactory;
import com.bitfire.uracer.screen.ScreenFactory.ScreenId;
import com.bitfire.uracer.screen.ScreenTransition;
import com.bitfire.uracer.screen.ScreenUtils;
import com.bitfire.uracer.utils.AMath;
import com.bitfire.utils.ShaderLoader;

/** Implements a cross fader, transitioning between the current and the next screen. */
public final class CrossFader extends ScreenTransition {
	FrameBuffer from, to;
	long duration, elapsed;
	float factor;
	FullscreenQuad quad;
	ShaderProgram fade;
	Screen next;

	public CrossFader (ScreenFactory factory) {
		super(factory);
		quad = new FullscreenQuad();
		fade = ShaderLoader.fromFile("fade", "fade");
		duration = 1000;
		reset();
	}

	private void rebind () {
		fade.begin();
		fade.setUniformi("u_texture0", 0);
		fade.setUniformi("u_texture1", 1);
		fade.setUniformf("Ratio", 0);
		fade.end();
	}

	@Override
	public void reset () {
		rebind();
		next = null;
		factor = 0;
		elapsed = 0;
		setDuration(duration);
	}

	@Override
	public void dispose () {
		quad.dispose();
		fade.dispose();
	}

	@Override
	public void frameBuffersReady (Screen current, FrameBuffer from, ScreenId nextScreen, FrameBuffer to) {
		this.from = from;
		this.to = to;

		ScreenUtils.copyScreen(current, from);

		next = createScreen(nextScreen);
		ScreenUtils.copyScreen(next, to);
	}

	@Override
	public Screen nextScreen () {
		return next;
	}

	/** Sets the duration of the effect, in milliseconds. */
	@Override
	public void setDuration (long durationMs) {
		duration = durationMs;
		if (durationMs == 0) {
			throw new GdxRuntimeException("Invalid transition duration specified.");
		}
	}

	@Override
	public void resume () {
		rebind();
	}

	@Override
	public void update () {
		long delta = (long)URacer.Game.getLastDeltaMs();
		delta = AMath.clamp(delta, 0, (long)(Config.Physics.PhysicsDt * 1000));

		elapsed += delta;

		if (elapsed > duration) {
			elapsed = duration;
		}

		factor = (float)elapsed / (float)duration;
	}

	@Override
	public void render () {
		from.getColorBufferTexture().bind(0);
		to.getColorBufferTexture().bind(1);

		fade.begin();
		fade.setUniformf("Ratio", factor);
		quad.render(fade);
		fade.end();
	}

	@Override
	public boolean isComplete () {
		return elapsed >= duration;
	}
}
