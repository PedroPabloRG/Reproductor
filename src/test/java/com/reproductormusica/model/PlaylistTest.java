package com.reproductormusica.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Playlist
 */
class PlaylistTest {
    
    private Playlist playlist;
    private Song song1;
    private Song song2;
    
    @BeforeEach
    void setUp() {
        playlist = new Playlist("Test Playlist");
        song1 = new Song("1", "Song 1", "Artist 1", "/path/1.mp3");
        song2 = new Song("2", "Song 2", "Artist 2", "/path/2.mp3");
    }
    
    @Test
    void testPlaylistCreation() {
        assertNotNull(playlist);
        assertEquals("Test Playlist", playlist.getName());
        assertTrue(playlist.isEmpty());
        assertEquals(0, playlist.size());
    }
    
    @Test
    void testAddSong() {
        playlist.addSong(song1);
        
        assertEquals(1, playlist.size());
        assertFalse(playlist.isEmpty());
        assertTrue(playlist.getSongs().contains(song1));
    }
    
    @Test
    void testAddDuplicateSong() {
        playlist.addSong(song1);
        playlist.addSong(song1); // Add same song again
        
        assertEquals(1, playlist.size(), "Duplicate songs should not be added");
    }
    
    @Test
    void testRemoveSong() {
        playlist.addSong(song1);
        playlist.addSong(song2);
        
        playlist.removeSong(song1);
        
        assertEquals(1, playlist.size());
        assertFalse(playlist.getSongs().contains(song1));
        assertTrue(playlist.getSongs().contains(song2));
    }
    
    @Test
    void testRemoveSongByIndex() {
        playlist.addSong(song1);
        playlist.addSong(song2);
        
        playlist.removeSong(0); // Remove first song
        
        assertEquals(1, playlist.size());
        assertEquals(song2, playlist.getSongs().get(0));
    }
    
    @Test
    void testMoveSong() {
        playlist.addSong(song1);
        playlist.addSong(song2);
        
        playlist.moveSong(0, 1); // Move first song to second position
        
        assertEquals(song2, playlist.getSongs().get(0));
        assertEquals(song1, playlist.getSongs().get(1));
    }
    
    @Test
    void testToString() {
        String expected = "Test Playlist (0 songs)";
        assertEquals(expected, playlist.toString());
        
        playlist.addSong(song1);
        expected = "Test Playlist (1 songs)";
        assertEquals(expected, playlist.toString());
    }
}
