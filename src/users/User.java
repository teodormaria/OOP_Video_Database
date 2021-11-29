package users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
    private final String username;
    private final String subscriptionType;
    private Map<String, Integer> history;
    private ArrayList<String> favoriteVideos;
    private int numberOfRatings;
    private final Map<String, Double> moviesRatings;
    private final Map<String, ArrayList<Double>> showRatings;

    public User(final String username, final String subscriptionType,
                final Map<String, Integer> history,
                final ArrayList<String> favoriteVideos) {
        this.username = username;
        this.subscriptionType = subscriptionType;
        this.history = new HashMap<>();
        this.history.putAll(history);
        this.favoriteVideos = new ArrayList<>();
        this.favoriteVideos.addAll(favoriteVideos);
        this.numberOfRatings = 0;
        this.moviesRatings = new HashMap<>();
        this.showRatings = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    public Map<String, Integer> getHistory() {
        return history;
    }

    public ArrayList<String> getFavoriteVideos() {
        return favoriteVideos;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public Map<String, Double> getMoviesRatings() {
        return moviesRatings;
    }

    public Map<String, ArrayList<Double>> getShowRatings() {
        return showRatings;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public int addFavorite(final String video) {
        if (this.getFavoriteVideos().contains(video)) {
            return -1;
        }
        if (!this.getHistory().containsKey(video)) {
            return 0;
        }
        this.getFavoriteVideos().add(video);
        return 1;
    }

    public int watch(final String video) {
        if (this.getHistory().containsKey(video)) {
            int previousValue = this.getHistory().get(video);
            this.getHistory().remove(video);
            this.getHistory().put(video, previousValue + 1);
            return previousValue + 1;
        } else {
            this.getHistory().put(video, 1);
            return 1;
        }
    }

    public int addRating(final String video, final int totalSeasons, final int seasonNum,
                         final Double rating) {
        if (this.getHistory().containsKey(video)) {
            if (this.moviesRatings.containsKey(video)) {
                return -1;
            } else if (seasonNum == 0) {
                this.moviesRatings.put(video, rating);
                this.numberOfRatings++;

                return 1;
            }
            if (this.showRatings.containsKey(video)) {
                if (this.showRatings.get(video).get(seasonNum - 1) != 0d) {
                    return -1;
                } else {
                    this.showRatings.get(video).set(seasonNum - 1, rating);
                    this.numberOfRatings++;
                    return 2;
                }
            } else if (seasonNum != 0) {
                ArrayList<Double> showRating = new ArrayList<>();
                for (int i = 0; i < totalSeasons; i++) {
                    showRating.add(0d);
                }
                showRating.set(seasonNum - 1, rating);
                this.showRatings.put(video, showRating);
                this.numberOfRatings++;
                return 2;
            }
        }
        return 0;
    }
}
