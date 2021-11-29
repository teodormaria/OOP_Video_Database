package entertainment;

import java.util.ArrayList;

public class Show extends Video {

    private final int numberOfSeasons;
    private final ArrayList<Season> seasons;
    private int timesAddedToFavorites;
    private int timesWatched;

    public Show(final String title, final ArrayList<String> cast,
                final ArrayList<String> genres,
                final int numberOfSeasons, final ArrayList<Season> seasons,
                final int year) {
        super(title, year, cast, genres);
        this.numberOfSeasons = numberOfSeasons;
        this.seasons = new ArrayList<>();
        this.seasons.addAll(seasons);
        this.timesAddedToFavorites = 0;
        this.timesWatched = 0;
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
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

    public int getDuration() {
        int duration = 0;
        for (Season season: this.getSeasons()) {
            duration += season.getDuration();
        }
        return duration;
    }

    public Double serialAverageRating() {
        Double average = 0d;
        for (Season season: this.getSeasons()) {
            average += season.averageRating();
        }
        average = average / this.getNumberOfSeasons();

        return average;
    }

}
