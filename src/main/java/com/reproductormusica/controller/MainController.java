package com.reproductormusica.controller;

import com.reproductormusica.audio.AudioPlayer;
import com.reproductormusica.model.*;
import com.reproductormusica.utils.DatabaseManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Controlador principal del reproductor de música
 */
public class MainController {
    
    private AudioPlayer audioPlayer;
    private DatabaseManager databaseManager;
    
    // Observable properties
    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>();
    private final ObjectProperty<PlaybackState> playbackState = new SimpleObjectProperty<>(PlaybackState.STOPPED);
    private final DoubleProperty volume = new SimpleDoubleProperty(0.5);
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final BooleanProperty shuffle = new SimpleBooleanProperty(false);
    private final ObjectProperty<RepeatMode> repeatMode = new SimpleObjectProperty<>(RepeatMode.OFF);
    private final ObjectProperty<Playlist> currentPlaylist = new SimpleObjectProperty<>();
    
    // Collections
    private final ObservableList<Song> library = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    private final ObservableList<Song> currentQueue = FXCollections.observableArrayList();
    
    public MainController() {
        try {
            System.out.println("Inicializando AudioPlayer...");
            this.audioPlayer = new AudioPlayer();
            
            System.out.println("Inicializando DatabaseManager...");
            this.databaseManager = new DatabaseManager();
            
            // Initialize database
            System.out.println("Configurando base de datos...");
            databaseManager.initialize();
            
            // Load data
            System.out.println("Cargando biblioteca musical...");
            loadLibrary();
            
            System.out.println("Cargando playlists...");
            loadPlaylists();
            
            // Setup audio player listeners
            System.out.println("Configurando listeners de audio...");
            setupAudioPlayerListeners();
            
            System.out.println("MainController inicializado correctamente");
            
        } catch (Exception e) {
            System.err.println("Error inicializando MainController:");
            e.printStackTrace();
            // Inicializar con valores por defecto en caso de error
            if (this.audioPlayer == null) {
                this.audioPlayer = new AudioPlayer();
            }
            if (this.databaseManager == null) {
                this.databaseManager = new DatabaseManager();
            }
        }
    }
    
    private void setupAudioPlayerListeners() {
        if (audioPlayer != null) {
            audioPlayer.stateProperty().addListener((obs, oldState, newState) -> {
                playbackState.set(newState);
            });
            
            audioPlayer.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                progress.set(newProgress.doubleValue());
            });
            
            audioPlayer.volumeProperty().addListener((obs, oldVolume, newVolume) -> {
                volume.set(newVolume.doubleValue());
            });
            
            // Bind controller volume to audio player volume (bidirectional)
            volume.addListener((obs, oldVolume, newVolume) -> {
                if (audioPlayer != null) {
                    audioPlayer.setVolume(newVolume.doubleValue());
                }
            });
            
            // Set initial volume
            audioPlayer.setVolume(volume.get());
            
            // Setup callback for automatic playback when song finishes
            audioPlayer.setOnSongFinished(() -> {
                // Automatically play next song when current song finishes
                handleSongFinished();
            });
        }
    }
    
    /**
     * Maneja la lógica cuando termina una canción para reproducción automática
     */
    private void handleSongFinished() {
        // Check repeat mode first
        RepeatMode currentRepeatMode = repeatMode.get();
        
        if (currentRepeatMode == RepeatMode.ONE) {
            // Repeat the current song
            Song current = currentSong.get();
            if (current != null) {
                audioPlayer.loadSong(current);
                audioPlayer.play();
                return;
            }
        }
        
        // For RepeatMode.OFF and RepeatMode.ALL, play next song
        next();
    }
    
    // Playback control methods
    public void play() {
        if (currentSong.get() != null) {
            audioPlayer.play();
        }
    }
    
    public void pause() {
        audioPlayer.pause();
    }
    
    public void stop() {
        audioPlayer.stop();
    }
    
    public void next() {
        // Implementation for next song logic
        // Consider shuffle and repeat modes
        if (currentQueue.isEmpty()) {
            return;
        }
        
        Song currentSong = this.currentSong.get();
        if (currentSong == null) {
            // No current song, play first in queue
            playNow(currentQueue.get(0));
            return;
        }
        
        int currentIndex = currentQueue.indexOf(currentSong);
        if (currentIndex == -1) {
            // Current song not in queue, play first
            playNow(currentQueue.get(0));
            return;
        }
        
        Song nextSong = null;
        
        if (shuffle.get()) {
            // Shuffle mode: play random song
            int randomIndex;
            do {
                randomIndex = (int) (Math.random() * currentQueue.size());
            } while (randomIndex == currentIndex && currentQueue.size() > 1);
            nextSong = currentQueue.get(randomIndex);
        } else {
            // Normal mode: play next song in sequence
            if (currentIndex + 1 < currentQueue.size()) {
                nextSong = currentQueue.get(currentIndex + 1);
            } else {
                // End of queue, handle repeat mode
                RepeatMode mode = repeatMode.get();
                if (mode == RepeatMode.ALL) {
                    nextSong = currentQueue.get(0); // Start from beginning
                } else if (mode == RepeatMode.ONE) {
                    nextSong = currentSong; // Repeat current song
                }
                // RepeatMode.OFF: do nothing (stop at end)
            }
        }
        
        if (nextSong != null) {
            playNow(nextSong);
        } else {
            stop();
        }
    }
    
    public void previous() {
        // Implementation for previous song logic
        if (currentQueue.isEmpty()) {
            return;
        }
        
        Song currentSong = this.currentSong.get();
        if (currentSong == null) {
            // No current song, play last in queue
            playNow(currentQueue.get(currentQueue.size() - 1));
            return;
        }
        
        int currentIndex = currentQueue.indexOf(currentSong);
        if (currentIndex == -1) {
            // Current song not in queue, play last
            playNow(currentQueue.get(currentQueue.size() - 1));
            return;
        }
        
        Song previousSong = null;
        
        if (shuffle.get()) {
            // Shuffle mode: play random song
            int randomIndex;
            do {
                randomIndex = (int) (Math.random() * currentQueue.size());
            } while (randomIndex == currentIndex && currentQueue.size() > 1);
            previousSong = currentQueue.get(randomIndex);
        } else {
            // Normal mode: play previous song in sequence
            if (currentIndex > 0) {
                previousSong = currentQueue.get(currentIndex - 1);
            } else {
                // Beginning of queue, handle repeat mode
                RepeatMode mode = repeatMode.get();
                if (mode == RepeatMode.ALL) {
                    previousSong = currentQueue.get(currentQueue.size() - 1); // Go to last song
                } else if (mode == RepeatMode.ONE) {
                    previousSong = currentSong; // Repeat current song
                }
                // RepeatMode.OFF: do nothing (stay at beginning)
            }
        }
        
        if (previousSong != null) {
            playNow(previousSong);
        }
    }
    
    public void seek(double position) {
        audioPlayer.seek(position);
    }
    
    public void setVolume(double volume) {
        audioPlayer.setVolume(volume);
        this.volume.set(volume);
    }
    
    public void toggleShuffle() {
        shuffle.set(!shuffle.get());
        System.out.println("Shuffle " + (shuffle.get() ? "enabled" : "disabled"));
    }
    
    public void toggleRepeatMode() {
        RepeatMode current = repeatMode.get();
        RepeatMode next;
        
        switch (current) {
            case OFF:
                next = RepeatMode.ALL;
                break;
            case ALL:
                next = RepeatMode.ONE;
                break;
            case ONE:
                next = RepeatMode.OFF;
                break;
            default:
                next = RepeatMode.OFF;
        }
        
        repeatMode.set(next);
        System.out.println("Repeat mode: " + next);
    }
    
    public void shuffleQueue() {
        if (currentQueue.size() <= 1) {
            return;
        }
        
        // Create a shuffled copy
        java.util.List<Song> shuffledList = new java.util.ArrayList<>(currentQueue);
        java.util.Collections.shuffle(shuffledList);
        
        // Update the queue
        currentQueue.clear();
        currentQueue.addAll(shuffledList);
        
        System.out.println("Queue shuffled");
    }
    
    // Library management
    public void importMusicFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        
        for (File file : files) {
            try {
                if (file.exists() && file.isFile() && 
                    com.reproductormusica.utils.MetadataExtractor.isSupportedAudioFile(file)) {
                    
                    // Extract metadata using MetadataExtractor
                    Song song = com.reproductormusica.utils.MetadataExtractor.extractMetadata(file);
                    
                    if (song != null) {
                        addSongToLibrary(song);
                        System.out.println("Imported: " + song.getTitle() + " by " + song.getArtist());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error importing file " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void addSongToLibrary(Song song) {
        if (!library.contains(song)) {
            library.add(song);
            databaseManager.saveSong(song);
        }
    }
    
    public void removeSongFromLibrary(Song song) {
        library.remove(song);
        
        // Also remove from current queue if present
        currentQueue.remove(song);
        
        // If this is the currently playing song, stop playback
        if (currentSong.get() != null && currentSong.get().equals(song)) {
            stop();
            currentSong.set(null);
        }
        
        // Delete from database
        databaseManager.deleteSong(song.getId());
        System.out.println("Removed song: " + song.getTitle() + " by " + song.getArtist());
    }
    
    /**
     * Elimina múltiples canciones de la biblioteca
     */
    public void removeSongsFromLibrary(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return;
        }
        
        for (Song song : songs) {
            removeSongFromLibrary(song);
        }
        
        System.out.println("Removed " + songs.size() + " songs from library");
    }
    
    /**
     * Agrega múltiples canciones a la cola de reproducción
     */
    public void addSongsToQueue(List<Song> songs) {
        if (songs != null && !songs.isEmpty()) {
            currentQueue.addAll(songs);
            System.out.println("Added " + songs.size() + " songs to queue");
        }
    }
    
    /**
     * Reproduce una lista de canciones seleccionadas
     * Reemplaza la cola actual con las canciones seleccionadas y comienza la reproducción
     */
    public void playSelectedSongs(List<Song> songs) {
        playSelectedSongs(songs, null);
    }
    
    /**
     * Reproduce una lista de canciones seleccionadas desde una playlist específica
     * Reemplaza la cola actual con las canciones seleccionadas y comienza la reproducción
     */
    public void playSelectedSongs(List<Song> songs, Playlist fromPlaylist) {
        if (songs != null && !songs.isEmpty()) {
            // Clear current queue and add selected songs
            currentQueue.clear();
            currentQueue.addAll(songs);
            
            // Set the current playlist
            currentPlaylist.set(fromPlaylist);
            
            // Start playing the first song
            Song firstSong = songs.get(0);
            setCurrentSong(firstSong);
            play();
            
            System.out.println("Playing " + songs.size() + " selected songs" + 
                             (fromPlaylist != null ? " from playlist: " + fromPlaylist.getName() : "") +
                             ", starting with: " + firstSong.getTitle());
        }
    }
    
    /**
     * Reproduce una playlist desde una canción específica hacia adelante
     * Reemplaza la cola actual con las canciones desde la seleccionada hasta el final
     */
    public void playPlaylistFromSong(Song startingSong, Playlist playlist) {
        if (startingSong != null && playlist != null) {
            List<Song> playlistSongs = playlist.getSongs();
            int startIndex = playlistSongs.indexOf(startingSong);
            
            if (startIndex >= 0) {
                // Get songs from the starting song to the end of the playlist
                List<Song> songsToPlay = playlistSongs.subList(startIndex, playlistSongs.size());
                
                // Clear current queue and add songs from starting point
                currentQueue.clear();
                currentQueue.addAll(songsToPlay);
                
                // Set the current playlist
                currentPlaylist.set(playlist);
                
                // Start playing the selected song
                setCurrentSong(startingSong);
                play();
                
                System.out.println("Playing playlist \"" + playlist.getName() + 
                                 "\" from song \"" + startingSong.getTitle() + 
                                 "\" (" + songsToPlay.size() + " songs remaining)");
            }
        }
    }
    
    /**
     * Establece la canción actual
     */
    private void setCurrentSong(Song song) {
        currentSong.set(song);
        if (audioPlayer != null) {
            audioPlayer.loadSong(song);
        }
    }
    
    // Playlist management
    public Playlist createPlaylist(String name) {
        return createPlaylist(name, null);
    }
    
    public Playlist createPlaylist(String name, String description) {
        Playlist playlist = new Playlist(name);
        if (description != null && !description.trim().isEmpty()) {
            playlist.setDescription(description.trim());
        }
        playlists.add(playlist);
        databaseManager.savePlaylist(playlist);
        return playlist;
    }
    
    public void deletePlaylist(Playlist playlist) {
        playlists.remove(playlist);
        databaseManager.deletePlaylist(playlist.getId());
    }
    
    public void updatePlaylist(Playlist playlist) {
        if (playlist != null) {
            databaseManager.updatePlaylist(playlist);
            System.out.println("Updated playlist: " + playlist.getName());
        }
    }
    
    public void addSongToPlaylist(Song song, Playlist playlist) {
        playlist.addSong(song);
        databaseManager.updatePlaylist(playlist);
    }
    
    public void addSongsToPlaylist(List<Song> songs, Playlist playlist) {
        if (songs != null && !songs.isEmpty()) {
            for (Song song : songs) {
                playlist.addSong(song);
            }
            databaseManager.updatePlaylist(playlist);
            System.out.println("Added " + songs.size() + " songs to playlist: " + playlist.getName());
        }
    }
    
    public void removeSongsFromPlaylist(List<Song> songs, Playlist playlist) {
        if (songs != null && !songs.isEmpty() && playlist != null) {
            for (Song song : songs) {
                playlist.removeSong(song);
            }
            databaseManager.updatePlaylist(playlist);
            System.out.println("Removed " + songs.size() + " songs from playlist: " + playlist.getName());
        }
    }
    
    public void clearPlaylist(Playlist playlist) {
        if (playlist != null) {
            int songsCount = playlist.getSongs().size();
            playlist.clearSongs();
            databaseManager.updatePlaylist(playlist);
            System.out.println("Cleared playlist: " + playlist.getName() + " (" + songsCount + " songs removed)");
        }
    }
    
    public void moveSongUp(Song song, Playlist playlist) {
        if (song != null && playlist != null) {
            List<Song> songs = playlist.getSongs();
            int index = songs.indexOf(song);
            if (index > 0) {
                playlist.moveSong(index, index - 1);
                databaseManager.updatePlaylist(playlist);
                System.out.println("Moved song up in playlist: " + song.getTitle());
            }
        }
    }
    
    public void moveSongDown(Song song, Playlist playlist) {
        if (song != null && playlist != null) {
            List<Song> songs = playlist.getSongs();
            int index = songs.indexOf(song);
            if (index >= 0 && index < songs.size() - 1) {
                playlist.moveSong(index, index + 1);
                databaseManager.updatePlaylist(playlist);
                System.out.println("Moved song down in playlist: " + song.getTitle());
            }
        }
    }
    
    public void moveSongsUp(List<Song> selectedSongs, Playlist playlist) {
        if (selectedSongs != null && !selectedSongs.isEmpty() && playlist != null) {
            // Sort by index to move from top to bottom to avoid index conflicts
            List<Song> songs = playlist.getSongs();
            selectedSongs.sort((s1, s2) -> Integer.compare(songs.indexOf(s1), songs.indexOf(s2)));
            
            boolean anyMoved = false;
            for (Song song : selectedSongs) {
                int index = songs.indexOf(song);
                if (index > 0) {
                    playlist.moveSong(index, index - 1);
                    anyMoved = true;
                }
            }
            
            if (anyMoved) {
                databaseManager.updatePlaylist(playlist);
                System.out.println("Moved " + selectedSongs.size() + " songs up in playlist");
            }
        }
    }
    
    public void moveSongsDown(List<Song> selectedSongs, Playlist playlist) {
        if (selectedSongs != null && !selectedSongs.isEmpty() && playlist != null) {
            // Sort by index in reverse order to move from bottom to top to avoid index conflicts
            List<Song> songs = playlist.getSongs();
            selectedSongs.sort((s1, s2) -> Integer.compare(songs.indexOf(s2), songs.indexOf(s1)));
            
            boolean anyMoved = false;
            for (Song song : selectedSongs) {
                int index = songs.indexOf(song);
                if (index >= 0 && index < songs.size() - 1) {
                    playlist.moveSong(index, index + 1);
                    anyMoved = true;
                }
            }
            
            if (anyMoved) {
                databaseManager.updatePlaylist(playlist);
                System.out.println("Moved " + selectedSongs.size() + " songs down in playlist");
            }
        }
    }
    
    // Queue management
    public void playNow(Song song) {
        currentSong.set(song);
        audioPlayer.loadSong(song);
        // Clear current playlist since we're playing individual song
        currentPlaylist.set(null);
        play();
    }
    
    public void addToQueue(Song song) {
        currentQueue.add(song);
    }
    
    public void clearQueue() {
        currentQueue.clear();
    }
    
    /**
     * Elimina una canción de la cola de reproducción
     */
    public void removeSongFromQueue(Song song) {
        currentQueue.remove(song);
        System.out.println("Removed from queue: " + song.getTitle());
    }
    
    /**
     * Elimina múltiples canciones de la cola de reproducción
     */
    public void removeSongsFromQueue(List<Song> songs) {
        if (songs != null && !songs.isEmpty()) {
            currentQueue.removeAll(songs);
            System.out.println("Removed " + songs.size() + " songs from queue");
        }
    }
    
    // Search and filter
    public ObservableList<Song> searchSongs(String query) {
        if (query == null || query.trim().isEmpty()) {
            return library;
        }
        
        String lowercaseQuery = query.toLowerCase().trim();
        ObservableList<Song> filteredSongs = FXCollections.observableArrayList();
        
        for (Song song : library) {
            if (matchesQuery(song, lowercaseQuery)) {
                filteredSongs.add(song);
            }
        }
        
        return filteredSongs;
    }
    
    private boolean matchesQuery(Song song, String query) {
        return (song.getTitle() != null && song.getTitle().toLowerCase().contains(query)) ||
               (song.getArtist() != null && song.getArtist().toLowerCase().contains(query)) ||
               (song.getAlbum() != null && song.getAlbum().toLowerCase().contains(query)) ||
               (song.getGenre() != null && song.getGenre().toLowerCase().contains(query));
    }
    
    // Data loading methods
    private void loadLibrary() {
        try {
            List<Song> songs = databaseManager.getAllSongs();
            library.setAll(songs);
            System.out.println("Biblioteca cargada: " + songs.size() + " canciones");
        } catch (Exception e) {
            System.err.println("Error cargando biblioteca:");
            e.printStackTrace();
            library.clear();
        }
    }
    
    private void loadPlaylists() {
        try {
            List<Playlist> loadedPlaylists = databaseManager.getAllPlaylists();
            playlists.setAll(loadedPlaylists);
            System.out.println("Playlists cargadas: " + loadedPlaylists.size());
        } catch (Exception e) {
            System.err.println("Error cargando playlists:");
            e.printStackTrace();
            playlists.clear();
        }
    }
    
    // Getters for properties
    public ObjectProperty<Song> currentSongProperty() { return currentSong; }
    public ObjectProperty<PlaybackState> playbackStateProperty() { return playbackState; }
    public DoubleProperty volumeProperty() { return volume; }
    public DoubleProperty progressProperty() { return progress; }
    public BooleanProperty shuffleProperty() { return shuffle; }
    public ObjectProperty<RepeatMode> repeatModeProperty() { return repeatMode; }
    public ObjectProperty<Playlist> currentPlaylistProperty() { return currentPlaylist; }
    
    public ObservableList<Song> getLibrary() { return library; }
    public ObservableList<Playlist> getPlaylists() { return playlists; }
    public ObservableList<Song> getCurrentQueue() { return currentQueue; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    
    // Cleanup
    public void shutdown() {
        audioPlayer.shutdown();
        databaseManager.close();
    }
}
