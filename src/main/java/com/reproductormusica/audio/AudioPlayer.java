package com.reproductormusica.audio;

import com.reproductormusica.model.PlaybackState;
import com.reproductormusica.model.Song;
import javafx.beans.property.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

/**
 * Clase para manejar la reproducción de audio
 */
public class AudioPlayer {
    
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private Runnable onSongFinishedCallback;
    
    // Properties
    private final ObjectProperty<PlaybackState> state = new SimpleObjectProperty<>(PlaybackState.STOPPED);
    private final DoubleProperty volume = new SimpleDoubleProperty(0.5);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<Duration> currentTime = new SimpleObjectProperty<>(Duration.ZERO);
    private final ObjectProperty<Duration> totalDuration = new SimpleObjectProperty<>(Duration.ZERO);
    
    public AudioPlayer() {
        // Initialize properties
    }
    
    public void loadSong(Song song) {
        if (song == null || song.getFilePath() == null) {
            return;
        }
        
        // Stop current playback
        stop();
        
        try {
            File file = new File(song.getFilePath());
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + song.getFilePath());
            }
            
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            currentSong = song;
            
            setupMediaPlayerListeners();
            
            state.set(PlaybackState.LOADING);
            
        } catch (Exception e) {
            e.printStackTrace();
            state.set(PlaybackState.STOPPED);
        }
    }
    
    private void setupMediaPlayerListeners() {
        if (mediaPlayer == null) return;
        
        mediaPlayer.setOnReady(() -> {
            totalDuration.set(mediaPlayer.getTotalDuration());
            state.set(PlaybackState.STOPPED);
        });
        
        mediaPlayer.setOnPlaying(() -> {
            state.set(PlaybackState.PLAYING);
        });
        
        mediaPlayer.setOnPaused(() -> {
            state.set(PlaybackState.PAUSED);
        });
        
        mediaPlayer.setOnStopped(() -> {
            state.set(PlaybackState.STOPPED);
            currentTime.set(Duration.ZERO);
            progress.set(0.0);
        });
        
        mediaPlayer.setOnEndOfMedia(() -> {
            state.set(PlaybackState.STOPPED);
            // Trigger next song callback
            if (onSongFinishedCallback != null) {
                onSongFinishedCallback.run();
            }
        });
        
        mediaPlayer.setOnError(() -> {
            System.err.println("Media player error: " + mediaPlayer.getError());
            state.set(PlaybackState.STOPPED);
        });
        
        // Update progress
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            currentTime.set(newTime);
            Duration total = totalDuration.get();
            if (total != null && total.toMillis() > 0) {
                progress.set(newTime.toMillis() / total.toMillis());
            }
        });
        
        // Set initial volume
        mediaPlayer.setVolume(volume.get());
        volume.addListener((obs, oldVol, newVol) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVol.doubleValue());
            }
        });
    }
    
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
    
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
    
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    public void seek(double position) {
        if (mediaPlayer != null && totalDuration.get() != null) {
            Duration seekTime = totalDuration.get().multiply(position);
            mediaPlayer.seek(seekTime);
        }
    }
    
    public void setVolume(double volume) {
        this.volume.set(Math.max(0.0, Math.min(1.0, volume)));
    }
    
    public boolean isPlaying() {
        return state.get() == PlaybackState.PLAYING;
    }
    
    public boolean isPaused() {
        return state.get() == PlaybackState.PAUSED;
    }
    
    public boolean isStopped() {
        return state.get() == PlaybackState.STOPPED;
    }
    
    public Song getCurrentSong() {
        return currentSong;
    }
    
    /**
     * Establece el callback a ejecutar cuando termina una canción
     */
    public void setOnSongFinished(Runnable callback) {
        this.onSongFinishedCallback = callback;
    }
    
    // Property getters
    public ObjectProperty<PlaybackState> stateProperty() { return state; }
    public DoubleProperty volumeProperty() { return volume; }
    public DoubleProperty progressProperty() { return progress; }
    public ObjectProperty<Duration> currentTimeProperty() { return currentTime; }
    public ObjectProperty<Duration> totalDurationProperty() { return totalDuration; }
    
    public void shutdown() {
        stop();
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}
