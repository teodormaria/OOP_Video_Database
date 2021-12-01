package entertainment;

import utils.Utils;

import java.util.ArrayList;

public abstract class Video {
    private final String title;
    private final int year;
    private final ArrayList<String> cast;
    private final ArrayList<Genre> genres;
    private int timesAddedToFavorites;
    private int timesWatched;

    public Video(final String title, final int year, final ArrayList<String> cast,
                 final ArrayList<String> genres) {
        this.title = title;
        this.year = year;
        this.cast = new ArrayList<>();
        this.cast.addAll(cast);
        this.genres = new ArrayList<>();
        for (String genre: genres) {
            this.genres.add(Utils.stringToGenre(genre));
        }
        this.timesAddedToFavorites = 0;
        this.timesWatched = 0;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public ArrayList<String> getCast() {
        return cast;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void addFavorite() {
        this.timesAddedToFavorites++;
    }

    public int getTimesAddedToFavorites() {
        return this.timesAddedToFavorites;
    }

    public int getTimesWatched() {
        return this.timesWatched;
    }

    public void addView() {
        this.timesWatched++;
    }

    public abstract Double getAverageRating();
}
