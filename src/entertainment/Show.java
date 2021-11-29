package entertainment;

import fileio.SerialInputData;

public class Show {
    private SerialInputData serial;
    private int timesAddedToFavorites;
    private int timesWatched;

    public Show(final SerialInputData input) {
        this.serial = new SerialInputData(input);
        this.timesAddedToFavorites = 0;
        this.timesWatched = 0;
    }

    public SerialInputData getSerial() {
        return serial;
    }

    public void setSerial(final SerialInputData serial) {
        this.serial = serial;
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
        for (Season season: this.getSerial().getSeasons()) {
            duration += season.getDuration();
        }
        return duration;
    }

    public Double serialAverageRating() {
        Double average = 0d;
        for (Season season: this.getSerial().getSeasons()) {
            average += season.averageRating();
        }
        average = average / this.getSerial().getNumberSeason();

        return average;
    }

}
