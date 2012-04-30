package com.bitfire.uracer.game.logic.replaying;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.GameDifficulty;
import com.bitfire.uracer.game.Time;
import com.bitfire.uracer.game.actors.Car;
import com.bitfire.uracer.game.actors.Car.Aspect;
import com.bitfire.uracer.game.actors.CarForces;
import com.bitfire.uracer.game.actors.CarModel;
import com.bitfire.uracer.game.logic.notifier.Message;
import com.bitfire.uracer.game.logic.notifier.Message.Position;
import com.bitfire.uracer.game.logic.notifier.Message.Size;
import com.bitfire.uracer.game.logic.notifier.Messager;
import com.bitfire.uracer.utils.UUid;

/** Represents replay data to be feed to a GhostCar, the replay player.
 *
 * @author manuel */

public class Replay {
	public static final int MaxEvents = 5000;
	private int eventsCount;

	// car data
	public Aspect carAspect;
	public CarModel.Type carModelType;
	public Vector2 carPosition = new Vector2();
	public float carOrientation;

	// replay data
	public final long id;
	public String trackName = "no-track";
	public GameDifficulty difficultyLevel = GameDifficulty.Easy;
	public float trackTimeSeconds = 0;
	public CarForces[] forces = null;
	public boolean isValid = false;
	private boolean isLoaded = false;
	private boolean isSaved = false;

	// time track
	private Time time = new Time();

	public Replay() {
		this( UUid.get() );
	}

	public Replay( long id ) {
		eventsCount = 0;
		forces = new CarForces[ MaxEvents ];
		for( int i = 0; i < MaxEvents; i++ ) {
			forces[i] = new CarForces();
		}

		this.id = id;
	}

	public void dispose() {
		reset();
	}

	public void begin( String trackName, GameDifficulty difficulty, Car car ) {
		reset();
		carPosition.set( car.pos() );
		carOrientation = car.orient();
		carAspect = car.getAspect();
		carModelType = car.getCarModel().type;
		this.trackName = trackName;
		difficultyLevel = difficulty;
		time.start();

		// if a previously loaded replay is being used, reset the loaded state
		// since its invalid
		isLoaded = false;

		isSaved = false;
	}

	public void end() {
		time.stop();
		trackTimeSeconds = time.elapsed( Time.Reference.TickSeconds );
		isValid = true;
		isLoaded = false;
		isSaved = false;
	}

	public void reset() {
		eventsCount = 0;
		isValid = false;
		isLoaded = false;
		isSaved = false;
	}

	// recording
	public int getEventsCount() {
		return eventsCount;
	}

	public boolean add( CarForces f ) {
		forces[eventsCount++].set( f );
		if( eventsCount == MaxEvents ) {
			eventsCount = 0;
			return false;
		}

		return true;
	}

	public static Replay loadLocal( String trackname ) {
		String filename = Config.URacerConfigFolder + Config.LocalReplaysStore + trackname;
		FileHandle fh = Gdx.files.external( filename );

		if( fh.exists() ) {
			try {
				// DataInputStream is = new DataInputStream( fh.read() );
				GZIPInputStream gzis = new GZIPInputStream( fh.read() );
				ObjectInputStream is = new ObjectInputStream( gzis );

				// read header
				Replay r = new Replay( is.readLong() );

				// replay info data
				r.trackName = is.readUTF();
				r.difficultyLevel = GameDifficulty.valueOf( is.readUTF() );
				r.trackTimeSeconds = is.readFloat();
				r.eventsCount = is.readInt();

				// car data
				r.carAspect = Aspect.valueOf( is.readUTF() );
				r.carModelType = CarModel.Type.valueOf( is.readUTF() );
				r.carPosition.x = is.readFloat();
				r.carPosition.y = is.readFloat();
				r.carOrientation = is.readFloat();

				for( int i = 0; i < r.eventsCount; i++ ) {
					r.forces[i].velocity_x = is.readFloat();
					r.forces[i].velocity_y = is.readFloat();
					r.forces[i].angularVelocity = is.readFloat();
				}

				is.close();

				r.isValid = true;
				r.isSaved = true;
				r.isLoaded = true;

				Gdx.app.log( "Replay", "Done loading local replay" );
				return r;

			} catch( IOException e ) {
				Gdx.app.log( "Replay", "Couldn't load local replay, reason: " + e.getMessage() );
			}
		} else {
			Gdx.app.log( "Replay", "There are no replays available for this track (" + trackname + ")" );
		}

		return null;
	}

	public void saveLocal( final Messager messager ) {
		if( isValid && !isLoaded && !isSaved ) {

			// this is an asynchronous operation, but it's safe since saving a replay
			// imply this replay won't get overwritten anytime soon
			new Thread( new Runnable() {

				@Override
				public void run() {
					try {
						String filename = Config.URacerConfigFolder + Config.LocalReplaysStore + trackName;
						FileHandle hf = Gdx.files.external( filename );

						// DataOutputStream os = new DataOutputStream( hf.write( false ) );
						GZIPOutputStream gzos = new GZIPOutputStream( hf.write( false ) ) {
							{
								def.setLevel( Deflater.BEST_COMPRESSION );
							}
						};

						ObjectOutputStream os = new ObjectOutputStream( gzos );

						// write header

						// replay info data
						os.writeLong( id );
						os.writeUTF( trackName );
						os.writeUTF( difficultyLevel.toString() );
						os.writeFloat( trackTimeSeconds );
						os.writeInt( eventsCount );

						// car data
						os.writeUTF( carAspect.toString() );
						os.writeUTF( carModelType.toString() );
						os.writeFloat( carPosition.x );
						os.writeFloat( carPosition.y );
						os.writeFloat( carOrientation );

						// write the effective number of captured CarForces events
						for( int i = 0; i < eventsCount; i++ ) {
							CarForces f = forces[i];
							os.writeFloat( f.velocity_x );
							os.writeFloat( f.velocity_y );
							os.writeFloat( f.angularVelocity );
						}

						os.close();

						isSaved = true;

						messager.enqueue( "Replay saved", 1f, Message.Type.Information, Position.Bottom, Size.Normal );
						Gdx.app.log( "Replay", "Done saving local replay (" + trackTimeSeconds + ")" );

					} catch( IOException e ) {
						Gdx.app.log( "Replay", "Couldn't save local replay, reason: " + e.getMessage() );
					}
				}
			} ).start();
		}
	}
}