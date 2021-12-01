package entertainment;

import java.util.ArrayList;
import java.util.List;

public class Movie extends Video {
    private final int duration;
    private List<Double> ratings;

    public Movie(final String title, final ArrayList<String> cast,
                 final ArrayList<String> genres, final int year,
                 final int duration) {
        super(title, year, cast, genres);
        this.duration = duration;
        this.ratings = new ArrayList<>();
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

    /**
    * @param rating  for rating to be added to ratings list
     */
    public void addRating(final Double rating) {
        this.getRatings().add(rating);
    }

    /**
     * @return average rating for movie
     */
    public Double getAverageRating() {
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
