
package com.bitfire.uracer.game.logic.post;

import com.bitfire.uracer.game.player.PlayerCar;

public interface PostProcessingAnimator {
	void update (float timeModFactor);

	void ErrorScreenShow (int milliseconds);

	void ErrorScreenHide (int milliseconds);

	void reset ();

	void setPlayer (PlayerCar player);
}
