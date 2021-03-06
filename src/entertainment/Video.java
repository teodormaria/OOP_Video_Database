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

    /**
     * @return video title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return year that video was published
     */
    public int getYear() {
        return year;
    }

    /**
     * @return list of actors' names
     */
    public ArrayList<String> getCast() {
        return cast;
    }

    /**
     * @return video genres
     */
    public ArrayList<Genre> getGenres() {
        return genres;
    }

    /**
     * keeps count of times video was added to favorites
     */
    public void addFavorite() {
        this.timesAddedToFavorites++;
    }

    /**
     * @return times video was added to favorites
     */
    public int getTimesAddedToFavorites() {
        return this.timesAddedToFavorites;
    }

    /**
     * @return times video was watched
     */
    public int getTimesWatched() {
        return this.timesWatched;
    }

    /**
     * keeps track of how many times video was watched
     */
    public void addView() {
        this.timesWatched++;
    }

    /**
     * @return average rating for video
     */
    public abstract Double getAverageRating();

    /**
     * @return video duration
     */
    public abstract int getDuration();
}
