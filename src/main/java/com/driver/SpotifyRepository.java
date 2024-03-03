package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;
    public HashMap<Artist, Integer> likedArtist;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();
        likedArtist = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        for (User u : users){
            if (u.getMobile().equals(mobile)){
                return u;
            }
        }
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        for (Artist a : artists){
            if (a.getName().equals(name)){
                return a;
            }
        }
        Artist a = new Artist(name);
        artists.add(a);
        return a;
    }

    public Album createAlbum(String title, String artistName) {
        boolean isArtistPresent = false;
        Artist artist = null;
        for (Artist a : artists){
            if (a.getName().equals(artistName)) {
                isArtistPresent = true;
                artist = a;
                break;
            }
        }

        if (!isArtistPresent){
            artist = createArtist(artistName);
        }

        Album album = new Album(title);
        albums.add(album);

        if (artistAlbumMap.containsKey(artist)){
            List<Album> albumList = artistAlbumMap.get(artist);
            albumList.add(album);
            artistAlbumMap.put(artist, albumList);
            return album;
        }

        List<Album> albumList = new ArrayList<>();
        albumList.add(album);
        artistAlbumMap.put(artist, albumList);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean isAlbumPresent = false;
        Album album = null;
        for (Album a : albums){
            if (a.getTitle().equals(albumName)){
                isAlbumPresent = true;
                album = a;
                break;
            }
        }

        if (!isAlbumPresent){
            throw new Exception("Album not present");
        }

        Song s = new Song(title, length);
        songs.add(s);

        if (albumSongMap.containsKey(album)){
            if (!albumSongMap.get(album).contains(s)){
                List<Song> songList = albumSongMap.get(album);
                songList.add(s);
                albumSongMap.put(album, songList);
            }
        }

        List<Song> songList = new ArrayList<>();
        songList.add(s);
        albumSongMap.put(album, songList);
        return s;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Playlist p = new Playlist(title);
        List<Song> listSong = new ArrayList<>();
        for (Song s : songs){
            if (s.getLength() == length){
                listSong.add(s);
                break;
            }
        }

        playlistSongMap.put(p, listSong);

        User u = findUser(mobile);

        creatorPlaylistMap.put(u, p);

        List<User> userList = new ArrayList<>();
        userList.add(u);
        playlistListenerMap.put(p, userList);

        return p;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Playlist p = new Playlist(title);
        List<Song> listSong = new ArrayList<>();
        for (String s : songTitles){
            for (Song song : songs){
                if (song.getTitle().equals(s)){
                    listSong.add(song);
                }
            }
        }

        playlistSongMap.put(p, listSong);

        User u = findUser(mobile);

        List<User> userList = new ArrayList<>();
        userList.add(u);
        playlistListenerMap.put(p, userList);

        creatorPlaylistMap.put(u, p);
        return p;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User u = findUser(mobile);

        if (u == null){
            throw new Exception("No user found");
        }

        Playlist p = null;
        for (Playlist playlist : playlists){
            if (playlist.getTitle().equals(playlistTitle)){
                p = playlist;
                break;
            }
        }

        if (p == null){
            throw new Exception("No playlist found");
        }

        if (creatorPlaylistMap.containsKey(u)){
            return p;
        }

        if (playlistListenerMap.containsKey(p)){
            if (!playlistListenerMap.get(p).contains(u)) {
                List<User> userList = playlistListenerMap.get(p);
                userList.add(u);
                playlistListenerMap.put(p, userList);
            }
            return p;
        }

        List<User> userList = new ArrayList<>();
        userList.add(u);
        playlistListenerMap.put(p, userList);
        return p;
    }

    public User findUser(String mobile){
        User u = null;
        for (User user : users){
            if (user.getMobile().equals(mobile)){
                u = user;
                break;
            }
        }

        return u;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User u = findUser(mobile);

        Song song = null;
        for (Song s : songs){
            if (s.getTitle().equals(songTitle)){
                song = s;
                break;
            }
        }

        if (u == null || song == null){
            throw new Exception("Song or user not found");
        }

        if (songLikeMap.containsKey(song)){
            if (!songLikeMap.get(song).contains(u)){
                List<User> userList = songLikeMap.get(song);
                userList.add(u);
                songLikeMap.put(song, userList);
            }
        }

        List<User> userList = new ArrayList<>();
        userList.add(u);
        songLikeMap.put(song, userList);

        song.setLikes(song.getLikes() + 1);

        songLikeMap.get(song).add(u);

        for(Album album:albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(song)){
                for(Artist artist:artistAlbumMap.keySet()){
                    if(artistAlbumMap.get(artist).contains(album)){
                        artist.setLikes(artist.getLikes()+1);
                        break;
                    }
                }
                break;
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        int maxLike = Integer.MIN_VALUE;
        Artist artist = null;
        for (Artist a : artists){
            if (a.getLikes() > maxLike){
                maxLike = a.getLikes();
                artist = a;
            }
        }

        assert artist != null;
        return artist.getName();
    }

    public String mostPopularSong() {
        int maxLike = Integer.MIN_VALUE;
        Song song = null;
        for (Song s : songs){
            if (s.getLikes() > maxLike){
                maxLike = s.getLikes();
                song = s;
            }
        }

        assert song != null;
        return song.getTitle();
    }
}
