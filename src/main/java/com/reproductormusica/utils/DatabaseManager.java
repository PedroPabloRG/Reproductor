package com.reproductormusica.utils;

import com.reproductormusica.model.Playlist;
import com.reproductormusica.model.Song;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Maneja la persistencia de datos en SQLite
 */
public class DatabaseManager {
    
    private static final String DB_NAME = "music_library.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;
    
    private Connection connection;
    
    public void initialize() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            migrateDatabaseIfNeeded();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void createTables() throws SQLException {
        // Songs table
        String createSongsTable = """
            CREATE TABLE IF NOT EXISTS songs (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                artist TEXT,
                album TEXT,
                genre TEXT,
                duration_seconds INTEGER,
                file_path TEXT NOT NULL,
                album_art TEXT,
                track_number INTEGER,
                year INTEGER,
                lyrics TEXT,
                created_date INTEGER,
                modified_date INTEGER
            )
        """;
        
        // Playlists table
        String createPlaylistsTable = """
            CREATE TABLE IF NOT EXISTS playlists (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT,
                created_date INTEGER,
                modified_date INTEGER
            )
        """;
        
        // Playlist songs junction table
        String createPlaylistSongsTable = """
            CREATE TABLE IF NOT EXISTS playlist_songs (
                playlist_id TEXT,
                song_id TEXT,
                position INTEGER,
                FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
                FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE,
                PRIMARY KEY (playlist_id, song_id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSongsTable);
            stmt.execute(createPlaylistsTable);
            stmt.execute(createPlaylistSongsTable);
        }
    }
    
    /**
     * Migra la base de datos si es necesario (agrega columnas faltantes)
     */
    private void migrateDatabaseIfNeeded() throws SQLException {
        // Verificar si la columna lyrics existe
        String checkLyricsColumn = "PRAGMA table_info(songs)";
        boolean lyricsColumnExists = false;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkLyricsColumn)) {
            
            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("lyrics".equals(columnName)) {
                    lyricsColumnExists = true;
                    break;
                }
            }
        }
        
        // Si no existe la columna lyrics, agregarla
        if (!lyricsColumnExists) {
            String addLyricsColumn = "ALTER TABLE songs ADD COLUMN lyrics TEXT";
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(addLyricsColumn);
                System.out.println("Columna 'lyrics' agregada a la tabla songs");
            }
        }
    }
    
    // Song operations
    public void saveSong(Song song) {
        String sql = """
            INSERT OR REPLACE INTO songs 
            (id, title, artist, album, genre, duration_seconds, file_path, album_art, track_number, year, lyrics, created_date, modified_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, song.getId());
            pstmt.setString(2, song.getTitle());
            pstmt.setString(3, song.getArtist());
            pstmt.setString(4, song.getAlbum());
            pstmt.setString(5, song.getGenre());
            pstmt.setLong(6, song.getDuration() != null ? song.getDuration().toSeconds() : 0);
            pstmt.setString(7, song.getFilePath());
            pstmt.setString(8, song.getAlbumArt());
            pstmt.setInt(9, song.getTrackNumber());
            pstmt.setInt(10, song.getYear());
            pstmt.setString(11, song.getLyrics());
            pstmt.setLong(12, System.currentTimeMillis());
            pstmt.setLong(13, System.currentTimeMillis());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs ORDER BY artist, album, track_number";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Song song = new Song();
                song.setId(rs.getString("id"));
                song.setTitle(rs.getString("title"));
                song.setArtist(rs.getString("artist"));
                song.setAlbum(rs.getString("album"));
                song.setGenre(rs.getString("genre"));
                
                long durationSeconds = rs.getLong("duration_seconds");
                if (durationSeconds > 0) {
                    song.setDuration(Duration.ofSeconds(durationSeconds));
                }
                
                song.setFilePath(rs.getString("file_path"));
                song.setAlbumArt(rs.getString("album_art"));
                song.setTrackNumber(rs.getInt("track_number"));
                song.setYear(rs.getInt("year"));
                song.setLyrics(rs.getString("lyrics"));
                
                songs.add(song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return songs;
    }
    
    public void deleteSong(String songId) {
        String sql = "DELETE FROM songs WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, songId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Lyrics operations
    public void updateSongLyrics(String songId, String lyrics) {
        String sql = "UPDATE songs SET lyrics = ?, modified_date = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, lyrics);
            pstmt.setLong(2, System.currentTimeMillis());
            pstmt.setString(3, songId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String getSongLyrics(String songId) {
        String sql = "SELECT lyrics FROM songs WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, songId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("lyrics");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Playlist operations
    public void savePlaylist(Playlist playlist) {
        String sql = """
            INSERT OR REPLACE INTO playlists 
            (id, name, description, created_date, modified_date)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playlist.getId());
            pstmt.setString(2, playlist.getName());
            pstmt.setString(3, playlist.getDescription());
            pstmt.setLong(4, playlist.getCreatedDate());
            pstmt.setLong(5, playlist.getModifiedDate());
            
            pstmt.executeUpdate();
            
            // Save playlist songs
            savePlaylistSongs(playlist);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void savePlaylistSongs(Playlist playlist) {
        // First, delete existing playlist songs
        String deleteSql = "DELETE FROM playlist_songs WHERE playlist_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSql)) {
            pstmt.setString(1, playlist.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        
        // Then, insert current playlist songs
        String insertSql = "INSERT INTO playlist_songs (playlist_id, song_id, position) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
            List<Song> songs = playlist.getSongs();
            for (int i = 0; i < songs.size(); i++) {
                pstmt.setString(1, playlist.getId());
                pstmt.setString(2, songs.get(i).getId());
                pstmt.setInt(3, i);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Playlist> getAllPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        String sql = "SELECT * FROM playlists ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Playlist playlist = new Playlist();
                playlist.setId(rs.getString("id"));
                playlist.setName(rs.getString("name"));
                playlist.setDescription(rs.getString("description"));
                playlist.setCreatedDate(rs.getLong("created_date"));
                playlist.setModifiedDate(rs.getLong("modified_date"));
                
                // Load playlist songs
                loadPlaylistSongs(playlist);
                
                playlists.add(playlist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return playlists;
    }
    
    private void loadPlaylistSongs(Playlist playlist) {
        String sql = """
            SELECT s.* FROM songs s
            JOIN playlist_songs ps ON s.id = ps.song_id
            WHERE ps.playlist_id = ?
            ORDER BY ps.position
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playlist.getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Song> songs = new ArrayList<>();
                while (rs.next()) {
                    Song song = new Song();
                    song.setId(rs.getString("id"));
                    song.setTitle(rs.getString("title"));
                    song.setArtist(rs.getString("artist"));
                    song.setAlbum(rs.getString("album"));
                    song.setGenre(rs.getString("genre"));
                    
                    long durationSeconds = rs.getLong("duration_seconds");
                    if (durationSeconds > 0) {
                        song.setDuration(Duration.ofSeconds(durationSeconds));
                    }
                    
                    song.setFilePath(rs.getString("file_path"));
                    song.setAlbumArt(rs.getString("album_art"));
                    song.setTrackNumber(rs.getInt("track_number"));
                    song.setYear(rs.getInt("year"));
                    
                    songs.add(song);
                }
                playlist.setSongs(songs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void updatePlaylist(Playlist playlist) {
        savePlaylist(playlist); // Same as save for SQLite
    }
    
    public void deletePlaylist(String playlistId) {
        String sql = "DELETE FROM playlists WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playlistId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
