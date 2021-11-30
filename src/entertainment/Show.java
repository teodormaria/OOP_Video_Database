package entertainment;

import java.util.ArrayList;

public class Show extends Video {

    private final int numberOfSeasons;
    private final ArrayList<Season> seasons;


    public Show(final String title, final ArrayList<String> cast,
                final ArrayList<String> genres,
                final int numberOfSeasons, final ArrayList<Season> seasons,
                final int year) {
        super(title, year, cast, genres);
        this.numberOfSeasons = numberOfSeasons;
        this.seasons = new ArrayList<>();
        this.seasons.addAll(seasons);
    }

    public int getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    public int getDuration() {
        int duration = 0;
        for (Season season: this.getSeasons()) {
            duration += season.getDuration();
        }
        return duration;
    }

    public Double getAverageRating() {
        Double average = 0d;
        for (Season season: this.getSeasons()) {
            average += season.averageRating();
        }
        average = average / this.getNumberOfSeasons();

        return average;
    }

}
