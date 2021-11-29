package entertainment;

import fileio.MovieInputData;

import java.util.ArrayList;
import java.util.List;

public class Movie extends Video{
    private final int duration;
    private List<Double> ratings;
    private int timesAddedToFavorites;
    private int timesWatched;

    public Movie(final String title, final ArrayList<String> cast,
                 final ArrayList<String> genres, final int year,
                 final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
        this.ratings = new ArrayList<>();
        this.timesAddedToFavorites = 0;
        this.timesWatched = 0;
    }

    public int getDuration() {
        return duration;
    }

    public List<Double> getRatings() {
        return ratings;
    }

    public void setRatings(final List<Double> ratings) {
        this.ratings = ratings;
    }

    public void addRating(final Double rating) {
        this.getRatings().add(rating);
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

    public Double averageRating() {
        if (this.getRatings().isEmpty()) {
            return 0d;
        }
        Double average = 0d;
        for (Double rating : this.getRatings()) {
            average += rating;
        }
        average = average / this.getRatings().size();

        return average;
    }
}
