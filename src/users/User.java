package users;

import fileio.UserInputData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
    private UserInputData user;
    private int numberOfRatings;
    private Map<String,Double> moviesRatings;
    private Map<String, ArrayList<Double>> showRatings;

    public User(UserInputData input) {
        this.user = new UserInputData(input);
        this.numberOfRatings = 0;
        this.moviesRatings = new HashMap<>();
        this.showRatings = new HashMap<>();
    }

    public UserInputData getUser() {
        return user;
    }

    public void setUser(UserInputData user) {
        this.user = user;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public int addFavorite(final String video) {
        if(this.user.getFavoriteMovies().contains(video)) {
            return -1;
        }
        if(!this.user.getHistory().containsKey(video)) {
            return 0;
        }
        this.user.getFavoriteMovies().add(video);
        return 1;
    }

    public int watch(final String video) {
        if(this.user.getHistory().containsKey(video)) {
            int previousValue = this.user.getHistory().get(video);
            this.user.getHistory().remove(video);
            this.user.getHistory().put(video, previousValue + 1);
            return previousValue + 1;
        }
        else {
            this.user.getHistory().put(video, 1);
            return 1;
        }
    }

    public int addRating(final String video,final int totalSeasons, final int seasonNum, final Double rating) {
        if(this.user.getHistory().containsKey(video)) {
            if(this.moviesRatings.containsKey(video)) {
                return -1;
            }
            else if(seasonNum == 0) {
                this.moviesRatings.put(video, rating);
                this.numberOfRatings++;

                return 1;
            }
            if(this.showRatings.containsKey(video)) {
                if(this.showRatings.get(video).get(seasonNum - 1) != 0d) {
                    return -1;
                }
                else {
                    this.showRatings.get(video).set(seasonNum - 1, rating);
                    this.numberOfRatings++;
                    return 2;
                }
            }
            else if(seasonNum != 0) {
                ArrayList<Double> showRating = new ArrayList<>();
                for(int i = 0; i < totalSeasons; i++) {
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
