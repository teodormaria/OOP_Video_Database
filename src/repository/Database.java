package repository;

import actor.ActorsAwards;
import entertainment.Genre;
import fileio.Input;
import fileio.ActionInputData;
import fileio.ActorInputData;
import fileio.MovieInputData;
import fileio.SerialInputData;
import fileio.UserInputData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import users.User;
import entertainment.Movie;
import entertainment.Show;
import entertainment.Video;
import common.Constants;
import utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final public class Database {
    private static Database instance = null;
    private List<ActorInputData> actorsData;
    private List<User> users = new ArrayList<>();
    private List<ActionInputData> commandsData;
    private List<Movie> movies = new ArrayList<>();
    private List<Show> shows = new ArrayList<>();
    private final Map<Genre, Integer> genresViews = new HashMap<>();

    private Database() {
    }

    /**
     * @return singleton instance of database
     */
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public List<ActorInputData> getActorsData() {
        return actorsData;
    }

    public void setActorsData(final List<ActorInputData> actorsData) {
        this.actorsData = actorsData;
    }

    public List<User> getUsers() {
        return users;
    }

    /**
     * @param username string to be matched with username
     * @return user with specified username if exists, null if it couldn't be found
     */
    public User getUserByUsername(final String username) {
        for (User user: this.users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * @param title string to be matched with movie title
     * @return movie with specified title if exists, null if it couldn't be found
     */
    public Movie getMovieByTitle(final String title) {
        for (Movie movie: this.movies) {
            if (movie.getTitle().equalsIgnoreCase(title)) {
                return movie;
            }
        }
        return null;
    }

    /**
     * @param title string to be matched with show title
     * @return show with specified title if exists, null if it couldn't be found
     */
    public Show getShowByTitle(final String title) {
        for (Show show: this.shows) {
            if (show.getTitle().equalsIgnoreCase(title)) {
                return show;
            }
        }
        return null;
    }

    /**
     * @param name string to be matched with actor name
     * @return actor with specified name if exists, null if it couldn't be found
     */
    public ActorInputData getActorByName(final String name) {
        for (ActorInputData actor: this.actorsData) {
            if (actor.getName().equalsIgnoreCase(name)) {
                return actor;
            }
        }
        return null;
    }

    public void setUsers(final List<User> users) {
        this.users = users;
    }

    public void setCommandsData(final List<ActionInputData> commandsData) {
        this.commandsData = commandsData;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(final List<Movie> movies) {
        this.movies = movies;
    }

    public List<Show> getShows() {
        return shows;
    }

    public void setShows(final List<Show> shows) {
        this.shows = shows;
    }

    public Map<Genre, Integer> getGenresViews() {
        return genresViews;
    }

    /**
     * @param input represents input read at the beginning, most of the fields being deep copies
     */
    public void populateDatabase(final Input input) {
        this.setActorsData(input.getActors());
        this.setCommandsData(input.getCommands());
        for (UserInputData userData: input.getUsers()) {
            this.users.add(new User(userData.getUsername(), userData.getSubscriptionType(),
                    userData.getHistory(), userData.getFavoriteMovies()));
        }
        for (MovieInputData movieData: input.getMovies()) {
            this.movies.add(new Movie(movieData.getTitle(), movieData.getCast(),
                    movieData.getGenres(), movieData.getYear(), movieData.getDuration()));
        }
        for (SerialInputData serialData: input.getSerials()) {
            this.shows.add(new Show(serialData.getTitle(), serialData.getCast(),
                    serialData.getGenres(), serialData.getNumberSeason(), serialData.getSeasons(),
                    serialData.getYear()));
        }
        for (Genre genre: Genre.values()) {
            this.genresViews.put(genre, 0);
        }
        for (User user: this.getUsers()) {
            for (String video: user.getFavoriteVideos()) {
                Movie movie = getMovieByTitle(video);
                Show show = getShowByTitle(video);
                if (movie != null) {
                    movie.addFavorite();
                }
                if (show != null) {
                    show.addFavorite();
                }
            }
            for (String video: user.getHistory().keySet()) {
                Movie movie = getMovieByTitle(video);
                Show show = getShowByTitle(video);
                if (movie != null) {
                    if (user.getHistory().containsKey(video)) {
                        for (int i = 0; i < user.getHistory().get(video); i++) {
                            movie.addView();
                            for (Genre genre: movie.getGenres()) {
                                int prevValue = this.getGenresViews().get(genre);
                                this.getGenresViews().remove(genre);
                                this.getGenresViews().put(genre, prevValue + 1);
                            }
                        }
                    }
                }
                if (show != null) {
                    if (user.getHistory().containsKey(video)) {
                        for (int i = 0; i < user.getHistory().get(video); i++) {
                            show.addView();
                            for (Genre genre: show.getGenres()) {
                                int prevValue = this.getGenresViews().get(genre);
                                this.getGenresViews().remove(genre);
                                this.getGenresViews().put(genre, prevValue + 1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * empties database in order for it to be repopulated
     */
    public void emptyDatabase() {
        actorsData = new ArrayList<>();
        users = new ArrayList<>();
        commandsData = new ArrayList<>();
        movies = new ArrayList<>();
        shows = new ArrayList<>();
    }

    private JSONObject favorite(final ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
        assert user != null;
        int added = user.addFavorite(command.getTitle());
        JSONObject object = new JSONObject();
        object.put("id", command.getActionId());
        if (added == 1) {
            Movie movie = getMovieByTitle(command.getTitle());
            Show show = getShowByTitle(command.getTitle());
            if (movie != null) {
                movie.addFavorite();
            }
            if (show != null) {
                show.addFavorite();
            }
            object.put("message", "success -> " + command.getTitle() + " was added as favourite");
        } else if (added == -1) {
            object.put("message", "error -> " + command.getTitle()
                    + " is already in favourite list");
        } else {
            object.put("message", "error -> " + command.getTitle() + " is not seen");
        }
        return object;
    }

    private JSONObject watch(final ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
        Movie movie = getMovieByTitle(command.getTitle());
        Show show = getShowByTitle(command.getTitle());
        if (movie != null) {
            movie.addView();
            for (Genre genre: movie.getGenres()) {
                int prevValue = this.getGenresViews().get(genre);
                this.getGenresViews().remove(genre);
                this.getGenresViews().put(genre, prevValue + 1);
            }
        }
        if (show != null) {
            show.addView();
            for (Genre genre: show.getGenres()) {
                int prevValue = this.getGenresViews().get(genre);
                this.getGenresViews().remove(genre);
                this.getGenresViews().put(genre, prevValue + 1);
            }
        }
        assert user != null;
        int timesWatched = user.watch(command.getTitle());
        JSONObject object = new JSONObject();
        object.put("id", command.getActionId());
        object.put("message", "success -> " + command.getTitle()
                + " was viewed with total views of " + timesWatched);
        return object;
    }

    private JSONObject giveRating(final ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
        JSONObject object = new JSONObject();
        Show show = getShowByTitle(command.getTitle());
        Movie movie = getMovieByTitle(command.getTitle());
        int totalSeasons;
        if (show == null) {
            totalSeasons = 0;
        } else {
            totalSeasons = show.getNumberOfSeasons();
        }
        assert user != null;
        int isSuccess = user.addRating(command.getTitle(), totalSeasons, command.getSeasonNumber(),
                command.getGrade());
        if (isSuccess == 1) {
            assert movie != null;
            movie.addRating(command.getGrade());
        }
        if (isSuccess == 2) {
            assert show != null;
            show.getSeasons().get(command.getSeasonNumber() - 1).addRating(command.getGrade());
        }
        if (isSuccess == 1 || isSuccess == 2) {
            object.put("id", command.getActionId());
            object.put("message", "success -> "
                    + command.getTitle() + " was rated with "
                    + command.getGrade() +  " by "
                    + command.getUsername());
        } else if (isSuccess == -1) {
            object.put("id", command.getActionId());
            object.put("message", "error -> "
                    + command.getTitle() + " has been already rated");
        } else if (isSuccess == 0) {
            object.put("id", command.getActionId());
            object.put("message", "error -> "
                    + command.getTitle() + " is not seen");
        }
        return object;
    }

    private Double getActorAverage(final String actorName) {
        ActorInputData actor = getActorByName(actorName);
        int totalRated = 0;
        Double average = 0d;
        assert actor != null;
        for (String videoName: actor.getFilmography()) {
            Movie movie = getMovieByTitle(videoName);
            Show show = getShowByTitle(videoName);
            if (movie != null) {
                if (!movie.getRatings().isEmpty()) {
                    average += movie.getAverageRating();
                    totalRated++;
                }
            }
            if (show != null) {
                if (show.getAverageRating() != 0) {
                    average += show.getAverageRating();
                    totalRated++;
                }
            }
        }
        average = average / totalRated;

        return average;
    }

    private List<String> sortHashMapByValue(final HashMap<String, Double> unsortedMap,
                                            final String orderValues, final String orderKeys,
                                            final int number, final boolean alphabetical) {
        List<String> keys = new ArrayList<>(unsortedMap.keySet());
        List<Double> values = new ArrayList<>(unsortedMap.values());
        if (alphabetical) {
            if (orderKeys.equalsIgnoreCase(Constants.ASC)) {
                Collections.sort(keys);
            } else {
                keys.sort(Collections.reverseOrder());
            }
        }
        switch (orderValues) {
            case Constants.ASC -> Collections.sort(values);
            case Constants.DESC -> values.sort(Collections.reverseOrder());
            default -> {
            }
        }

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
        for (Double value : values) {
            Iterator<String> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                String key = keyIterator.next();
                if (unsortedMap.get(key).equals(value)) {
                    keyIterator.remove();
                    sortedMap.put(key, value);
                    break;
                }
            }
        }
        List<String> objects = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            if (sortedMap.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Double>> iterator = sortedMap.entrySet().iterator();
            Map.Entry<String, Double> value = iterator.next();
            objects.add(value.getKey());
            sortedMap.remove(value.getKey());
        }
        return objects;
    }

    private JSONObject averageActors(final ActionInputData action) {
        HashMap<String, Double> actorRatings = new HashMap<>();
        for (ActorInputData actor: this.getActorsData()) {
            if (!Double.isNaN(this.getActorAverage(actor.getName()))) {
                actorRatings.put(actor.getName(), this.getActorAverage(actor.getName()));
            }
        }
        List<String> actors = sortHashMapByValue(actorRatings, action.getSortType(),
                action.getSortType(), action.getNumber(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + actors);
        return object;
    }

    private int awardsNum(final ActorInputData actor) {
        int number = 0;
        for (Map.Entry<ActorsAwards, Integer> entry: actor.getAwards().entrySet()) {
            number = number + entry.getValue();
        }
        return number;
    }

    private JSONObject awardedActors(final ActionInputData action) {
        HashMap<String, Double> goodActors = new HashMap<>();
        for (ActorInputData actor: this.actorsData) {
            boolean hasAwards = true;
            for (String award: action.getFilters().get(Constants.AWARD_FILTER)) {
                ActorsAwards aw = Utils.stringToAwards(award);
                if (!actor.getAwards().containsKey(aw)) {
                    hasAwards = false;
                    break;
                }
            }
            if (hasAwards) {
                goodActors.put(actor.getName(), (double) this.awardsNum(actor));
            }
        }
        List<String> orderedActors = sortHashMapByValue(goodActors, action.getSortType(),
                action.getSortType(), this.getActorsData().size(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + orderedActors);
        return object;
    }

    private JSONObject filterWords(final ActionInputData action) {
        ArrayList<String> actors = new ArrayList<>();
        for (ActorInputData actor: this.actorsData) {
            String[]s;
            s = actor.getCareerDescription().toLowerCase().split(" |-|\\.|\\s(|\\s)|,");
            boolean hasWords = true;
            for (String word: action.getFilters().get(Constants.WORD_FILTER)) {
                boolean hasWord = false;
                for (String descriptionWord : s) {
                    if (descriptionWord.compareTo(word) == 0) {
                        hasWord = true;
                        break;
                    }
                }
                if (!hasWord) {
                    hasWords = false;
                    break;
                }
            }
            if (hasWords) {
                actors.add(actor.getName());
            }
        }
        if (Objects.equals(action.getSortType(), Constants.ASC)) {
            Collections.sort(actors);
        } else if (Objects.equals(action.getSortType(), Constants.DESC)) {
            actors.sort(Collections.reverseOrder());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + actors);
        return object;
    }

    private ArrayList<Video> filterYearGenre(final ActionInputData action, final boolean isMovie) {
        ArrayList<Video> goodVideos = new ArrayList<>();
        if (isMovie) {
            for (Movie movie: this.getMovies()) {
                boolean noYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0) == null);
                boolean noGenre = (action.getFilters().get(Constants.GENRE_FILTER).get(0) == null);
                if (!noYear && !noGenre) {
                    boolean respectsYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0).
                            equalsIgnoreCase(String.valueOf(movie.getYear())));
                    boolean respectsGenre = (movie.getGenres().contains(Utils.
                            stringToGenre(action.getFilters().get(Constants.GENRE_FILTER).get(0))));
                    if (respectsYear && respectsGenre) {
                        goodVideos.add(movie);
                    }
                } else if (!noGenre) {
                    boolean respectsGenre = (movie.getGenres().contains(Utils.stringToGenre(action.
                            getFilters().get(Constants.GENRE_FILTER).get(0))));
                    if (respectsGenre) {
                        goodVideos.add(movie);
                    }
                } else if (!noYear) {
                    boolean respectsYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0).
                            equalsIgnoreCase(String.valueOf(movie.getYear())));
                    if (respectsYear) {
                        goodVideos.add(movie);
                    }
                } else {
                    goodVideos.add(movie);
                }
            }
        } else {
            for (Show show : this.getShows()) {
                boolean noYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0) == null);
                boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if (!noYear && !noGenre) {
                    boolean respectsYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0).
                            equalsIgnoreCase(String.valueOf(show.getYear())));
                    boolean respectsGenre = (show.getGenres().contains(Utils.stringToGenre(action.
                            getFilters().get(Constants.GENRE_FILTER).get(0))));
                    if (respectsYear && respectsGenre) {
                        goodVideos.add(show);
                    }
                } else if (!noGenre) {
                    boolean respectsGenre = (show.getGenres().contains(Utils.stringToGenre(action.
                            getFilters().get(Constants.GENRE_FILTER).get(0))));
                    if (respectsGenre) {
                        goodVideos.add(show);
                    }
                } else if (!noYear) {
                    boolean respectsYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0).
                            equalsIgnoreCase(String.valueOf(show.getYear())));
                    if (respectsYear) {
                        goodVideos.add(show);
                    }
                } else {
                    goodVideos.add(show);
                }
            }
        }
        return goodVideos;
    }

    private JSONObject videosByRating(final ActionInputData action, final boolean isMovie) {
        HashMap<String, Double> videoRatings = new HashMap<>();
        ArrayList<Video> goodVideos = filterYearGenre(action, isMovie);
        for (Video video: goodVideos) {
            if (video.getAverageRating() != 0d) {
                videoRatings.put(video.getTitle(), video.getAverageRating());
            }
        }
        List<String> videos = sortHashMapByValue(videoRatings, action.getSortType(),
                action.getSortType(), action.getNumber(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject videosByFavorite(final ActionInputData action, final boolean isMovie) {
        HashMap<String, Double> videosFavorite = new HashMap<>();
        ArrayList<Video> goodVideos = filterYearGenre(action, isMovie);
        for (Video video: goodVideos) {
            if (video.getTimesAddedToFavorites() != 0d) {
                videosFavorite.put(video.getTitle(),
                        (double) video.getTimesAddedToFavorites());
            }
        }
        List<String> videos = sortHashMapByValue(videosFavorite, action.getSortType(),
                action.getSortType(), action.getNumber(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject videosByDuration(final ActionInputData action, final boolean isMovie) {
        HashMap<String, Double> videosDuration = new HashMap<>();
        ArrayList<Video> goodMovies = filterYearGenre(action, isMovie);
        for (Video video: goodMovies) {
            videosDuration.put(video.getTitle(), (double) video.getDuration());
        }
        List<String> videos = sortHashMapByValue(videosDuration, action.getSortType(),
                action.getSortType(), action.getNumber(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject videosByViews(final ActionInputData action, final boolean isMovie) {
        HashMap<String, Double> videosViews = new HashMap<>();
        ArrayList<Video> goodVideos = filterYearGenre(action, isMovie);
        for (Video video: goodVideos) {
            if (video.getTimesWatched() != 0d) {
                videosViews.put(video.getTitle(), (double) video.getTimesWatched());
            }
        }
        List<String> videos = sortHashMapByValue(videosViews, action.getSortType(),
                action.getSortType(), action.getNumber(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject usersByReviews(final ActionInputData action) {
        HashMap<String, Double> userReviews = new HashMap<>();
        for (User user: this.getUsers()) {
            if (user.getNumberOfRatings() != 0) {
                userReviews.put(user.getUsername(), (double) user.getNumberOfRatings());
            }
        }
        List<String> sortedUsers = sortHashMapByValue(userReviews, action.getSortType(),
                action.getSortType(), action.getNumber(), true);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + sortedUsers);
        return object;
    }

    private ArrayList<Video> getUnwatched(final String username) {
        User user = getUserByUsername(username);
        ArrayList<Video> videosCopy = new ArrayList<>();
        videosCopy.addAll(this.getMovies());
        videosCopy.addAll(this.getShows());
        assert user != null;
        if (!user.getHistory().isEmpty()) {
            for (String movieName: user.getHistory().keySet()) {
                Movie movie = getMovieByTitle(movieName);
                if (movie != null) {
                    videosCopy.remove(movie);
                }
            }
        }
        for (String showName: user.getHistory().keySet()) {
            Show show = getShowByTitle(showName);
            if (show != null) {
                videosCopy.remove(show);
            }
        }
        return videosCopy;
    }

    private JSONObject videosStandardRecommendation(final ActionInputData action) {
        ArrayList<Video> videosUnwatched = this.getUnwatched(action.getUsername());

        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        if (!videosUnwatched.isEmpty()) {
            object.put("message", "StandardRecommendation result: "
                    + videosUnwatched.get(0).getTitle());
        } else {
            object.put("message", "StandardRecommendation cannot be applied!");
        }
        return object;
    }

    private JSONObject videosBestRecommendation(final ActionInputData action) {
        ArrayList<Video> videosUnwatched = this.getUnwatched(action.getUsername());

        LinkedHashMap<String, Double> videosRatings = new LinkedHashMap<>();
        for (Video video: videosUnwatched) {
            videosRatings.put(video.getTitle(), video.getAverageRating());
        }
        List<String> videos = sortHashMapByValue(videosRatings, Constants.DESC, Constants.DESC,
                1, false);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        if (videosRatings.isEmpty()) {
            object.put("message", "BestRatedUnseenRecommendation cannot be applied!");
        } else {
            object.put("message", "BestRatedUnseenRecommendation result: " + videos.get(0));
        }
        return object;
    }

    private Genre mostWatchedGenre() {
        return Collections.max(this.getGenresViews().entrySet(),
                Map.Entry.comparingByValue()).getKey();
    }

    private ArrayList<Video> getVideosByGenre(final Genre genre) {
        ArrayList<Video> videos = new ArrayList<>();
        for (Movie movie: this.getMovies()) {
            if (movie.getGenres().contains(genre)) {
                videos.add(movie);
            }
        }
        for (Show show: this.getShows()) {
            if (show.getGenres().contains(genre)) {
                videos.add(show);
            }
        }
        return videos;
    }

    private boolean genreViewsNonNull() {
        for (Genre genre: this.getGenresViews().keySet()) {
            if (this.getGenresViews().get(genre) != 0) {
                return true;
            }
        }
        return false;
    }

    private JSONObject videosPopularRecommendation(final ActionInputData action) {
        User user = getUserByUsername(action.getUsername());
        assert user != null;
        if (user.getSubscriptionType().equalsIgnoreCase(Constants.BASIC)) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "PopularRecommendation cannot be applied!");
            return object;
        }
        Map<Genre, Integer> deletedValues = new HashMap<>();
        while (this.genreViewsNonNull()) {
            Genre popularGenre = this.mostWatchedGenre();
            ArrayList<Video> videosByGenre = this.getVideosByGenre(popularGenre);
            for (String videoTitle: user.getHistory().keySet()) {
                Movie movie = getMovieByTitle(videoTitle);
                Show show = getShowByTitle(videoTitle);
                if (movie != null) {
                    videosByGenre.remove(movie);
                }
                if (show != null) {
                    videosByGenre.remove(show);
                }
            }
            if (videosByGenre.isEmpty()) {
                deletedValues.put(popularGenre, this.getGenresViews().get(popularGenre));
                this.getGenresViews().remove(popularGenre);
            } else {
                for (Genre deletedGenre: deletedValues.keySet()) {
                    this.getGenresViews().put(deletedGenre, deletedValues.get(deletedGenre));
                }
                JSONObject object = new JSONObject();
                object.put("id", action.getActionId());
                object.put("message", "PopularRecommendation result: "
                        + videosByGenre.get(0).getTitle());
                return object;
            }
        }
        for (Genre deletedGenre: deletedValues.keySet()) {
            this.getGenresViews().put(deletedGenre, deletedValues.get(deletedGenre));
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "PopularRecommendation cannot be applied!");
        return object;
    }

    private JSONObject videosFavouriteRecommendation(final ActionInputData action) {
        User user = getUserByUsername(action.getUsername());
        assert user != null;
        if (user.getSubscriptionType().equalsIgnoreCase(Constants.BASIC)) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "FavoriteRecommendation cannot be applied!");
            return object;
        }
        ArrayList<Video> videosUnwatched = this.getUnwatched(action.getUsername());
        LinkedHashMap<String, Double> videosFavourite = new LinkedHashMap<>();
        for (Video video: videosUnwatched) {
            if (video.getTimesAddedToFavorites() != 0) {
                videosFavourite.put(video.getTitle(), (double) video.getTimesAddedToFavorites());
            }
        }
        List<String> videos = sortHashMapByValue(videosFavourite, Constants.DESC, Constants.ASC,
                1, false);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        if (videosFavourite.isEmpty()) {
            object.put("message", "FavoriteRecommendation cannot be applied!");
        } else {
            object.put("message", "FavoriteRecommendation result: " + videos.get(0));
        }
        return object;
    }

    private JSONObject videosSearchRecommendation(final ActionInputData action) {
        User user = getUserByUsername(action.getUsername());
        assert user != null;
        if (user.getSubscriptionType().equalsIgnoreCase(Constants.BASIC)) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "SearchRecommendation cannot be applied!");
            return object;
        }
        ArrayList<Video> videosByGenre =
                this.getVideosByGenre(Utils.stringToGenre(action.getGenre()));
        for (String videoName: user.getHistory().keySet()) {
            Movie movie = getMovieByTitle(videoName);
            Show show = getShowByTitle(videoName);
            if (movie != null) {
                videosByGenre.remove(movie);
            }
            if (show != null) {
                videosByGenre.remove(show);
            }
        }
        HashMap<String, Double> videoRatings = new HashMap<>();
        for (Video video: videosByGenre) {
            videoRatings.put(video.getTitle(), video.getAverageRating());
        }
        List<String> bestVideos = sortHashMapByValue(videoRatings, Constants.DESC, Constants.ASC,
                this.getMovies().size() + this.getShows().size(), true);
        if (bestVideos.isEmpty()) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "SearchRecommendation cannot be applied!");
            return object;
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "SearchRecommendation result: " + bestVideos);
        return object;

    }

    /**
     * iterates through the commands received and identifies which method should be applied
     * on the database
     * @return JSONArray to be written in output file
     */
    public JSONArray runCommands() {
        JSONArray output = new JSONArray();
        for (ActionInputData action: commandsData) {
            switch (action.getActionType()) {
                case Constants.COMMAND:
                    switch (action.getType()) {
                        case Constants.FAVORITE -> output.add(this.favorite(action));
                        case Constants.VIEW -> output.add(this.watch(action));
                        case Constants.RATING -> output.add(this.giveRating(action));
                        default -> {
                        }
                    }
                    break;
                case Constants.QUERY:
                    switch (action.getObjectType()) {
                        case Constants.ACTORS:
                            switch (action.getCriteria()) {
                                case Constants.AVERAGE -> output.add(this.averageActors(action));
                                case Constants.AWARDS -> output.add(this.awardedActors(action));
                                case Constants.FILTER_DESCRIPTIONS ->
                                        output.add(this.filterWords(action));
                                default -> {
                                }
                            }
                            break;
                        case Constants.MOVIES:
                            switch (action.getCriteria()) {
                                case Constants.RATINGS ->
                                        output.add(this.videosByRating(action, true));
                                case Constants.FAVORITE ->
                                        output.add(this.videosByFavorite(action, true));
                                case Constants.LONGEST ->
                                        output.add(this.videosByDuration(action, true));
                                case Constants.MOST_VIEWED ->
                                        output.add(this.videosByViews(action, true));
                                default -> {
                                }
                            }
                            break;
                        case Constants.SHOWS:
                            switch (action.getCriteria()) {
                                case Constants.RATINGS ->
                                        output.add(this.videosByRating(action, false));
                                case Constants.FAVORITE ->
                                        output.add(this.videosByFavorite(action, false));
                                case Constants.LONGEST ->
                                        output.add(this.videosByDuration(action, false));
                                case Constants.MOST_VIEWED ->
                                        output.add(this.videosByViews(action, false));
                                default -> {
                                }
                            }
                            break;
                        case Constants.USERS:
                            output.add(this.usersByReviews(action));
                            break;
                        default:
                            break;
                    }
                    break;
                case Constants.RECOMMENDATION:
                    switch (action.getType()) {
                        case Constants.STANDARD -> output.add(videosStandardRecommendation(action));
                        case Constants.BEST_UNSEEN -> output.add(videosBestRecommendation(action));
                        case Constants.POPULAR -> output.add(videosPopularRecommendation(action));
                        case Constants.FAVORITE ->
                                output.add(videosFavouriteRecommendation(action));
                        case Constants.SEARCH -> output.add(videosSearchRecommendation(action));
                        default -> {
                        }
                    }
                default:
                    break;
            }
        }
        return output;
    }
}
