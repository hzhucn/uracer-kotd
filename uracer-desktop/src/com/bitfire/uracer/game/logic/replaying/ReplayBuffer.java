package com.bitfire.uracer.game.logic.replaying;

/** Encapsulates a Replay buffering scheme, where a double-buffer approach is being used as the main implementation.
 *
 * @author bmanuel */
public final class ReplayBuffer {
	private Replay[] replays;
	private Replay best, worst;

	public ReplayBuffer() {
		// construct replay buffers
		replays = new Replay[ 2 ];
		replays[0] = new Replay();
		replays[1] = new Replay();

		best = null;
		worst = null;
		updateReplays();
	}

	public void reset() {
		best = null;
		worst = null;
		replays[0].reset();
		replays[1].reset();
	}

	public void restart() {
		if( !replays[0].isValid ) {
			replays[0].reset();
		}

		if( !replays[1].isValid ) {
			replays[1].reset();
		}

		updateReplays();
	}

	public void setBestReplay( Replay replay ) {
		replays[0] = replay;
		best = replays[0];
	}

	public Replay getAnyReplay() {
		if( replays[0].isValid ) {
			return replays[0];
		}

		if( replays[1].isValid ) {
			return replays[1];
		}

		return null;
	}

	public boolean hasAllReplayData() {
		return (replays[0].isValid && replays[1].isValid);
	}

	public boolean hasAnyReplayData() {
		return (replays[0].isValid || replays[1].isValid);
	}

	private Replay getFirstValid() {
		if( replays[0].isValid ) {
			return replays[0];
		} else {
			return replays[1];
		}
	}

	public void updateReplays() {
		if( hasAllReplayData() ) {
			best = replays[1];
			worst = replays[0];

			if( replays[0].trackTimeSeconds < replays[1].trackTimeSeconds ) {
				best = replays[0];
				worst = replays[1];
			}
		} else {
			Replay r = getFirstValid();
			best = r;
		}
	}

	public Replay getNextBuffer() {
		updateReplays();
		if( !replays[0].isValid ) {
			return replays[0];
		}
		if( !replays[1].isValid ) {
			return replays[1];
		}

		// if both are valid
		return getWorstReplay();
	}

	public Replay getBestReplay() {
		return best;
	}

	public Replay getWorstReplay() {
		return worst;
	}

}
