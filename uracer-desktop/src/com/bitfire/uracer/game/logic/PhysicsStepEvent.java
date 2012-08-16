
package com.bitfire.uracer.game.logic;

import com.bitfire.uracer.utils.Event;
import com.bitfire.uracer.utils.EventListener;
import com.bitfire.uracer.utils.EventNotifier;

public class PhysicsStepEvent extends Event<PhysicsStep> {
	public enum Type {
		onBeforeTimestep, onAfterTimestep, onSubstepCompleted
	}

	public interface Listener extends EventListener {
		void physicsEvent (Type type);
	}

	/* This constructor will permits late-binding of the "source" member via the "trigger" method */
	public PhysicsStepEvent () {
		super(null);
		for (Type t : Type.values()) {
			notifiers[t.ordinal()] = new Notifier();
		}
	}

	public void addListener (Listener listener, Type type) {
		notifiers[type.ordinal()].addListener(listener);
	}

	public void removeListener (Listener listener, Type type) {
		notifiers[type.ordinal()].removeListener(listener);
	}

	public void removeAllListeners () {
		for (Type t : Type.values()) {
			notifiers[t.ordinal()].removeAllListeners();
		}
	}

	public void trigger (PhysicsStep source, Type type) {
		this.source = source;
		notifiers[type.ordinal()].physicsEvent(type);
	}

	public float temporalAliasingFactor = 0;

	private Notifier[] notifiers = new Notifier[Type.values().length];

	private class Notifier extends EventNotifier<Listener> implements Listener {
		@Override
		public void physicsEvent (Type type) {
			for (Listener listener : listeners) {
				listener.physicsEvent(type);
			}
		}
	};
}
