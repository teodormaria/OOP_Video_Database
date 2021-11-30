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

import java.util.*;


final public class Database {
    private static Database instance = null;
    private List<ActorInputData> actorsData;
    private List<User> users = new ArrayList<>();
    private List<ActionInputData> commandsData;
    private List<Movie> movies = new ArrayList<>();
    private List<Show> shows = new ArrayList<>();
    private final Map<Genre,Integer> genresViews = new HashMap<>();

    private Database() {
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public List<ActorInputData> getActorsData() {
        return actorsData;
    }

    public void setActorsData(List<ActorInputData> actorsData) {
        this.actorsData = actorsData;
    }

    public List<User> getUsers() {
        return users;
    }

    public User getUserByUsername(final String username) {
        for (User user: this.users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public Movie getMovieByTitle(final String title) {
        for (Movie movie: this.movies) {
            if (movie.getTitle().equalsIgnoreCase(title)) {
                return movie;
            }
        }
        return null;
    }

    public Show getShowByTitle(final String title) {
        for (Show show: this.shows) {
            if (show.getTitle().equalsIgnoreCase(title)) {
                return show;
            }
        }
        return null;
    }

    public ActorInputData getActorByName(final String name) {
        for (ActorInputData actor: this.actorsData) {
            if (actor.getName().equalsIgnoreCase(name)) {
                return actor;
            }
        }
        return null;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<ActionInputData> getCommandsData() {
        return commandsData;
    }

    public void setCommandsData(List<ActionInputData> commandsData) {
        this.commandsData = commandsData;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Show> getShows() {
        return shows;
    }

    public void setShows(List<Show> shows) {
        this.shows = shows;
    }

    public Map<Genre, Integer> getGenresViews() {
        return genresViews;
    }

    public void populateDatabase(Input input) {
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

    public void emptyDatabase() {
        actorsData = new ArrayList<>();
        users = new ArrayList<>();
        commandsData = new ArrayList<>();
        movies = new ArrayList<>();
        shows = new ArrayList<>();
    }

    private JSONObject favorite(ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
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

    private JSONObject watch(ActionInputData command) {
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
                System.out.println(this.getGenresViews());
                System.out.println(genre);
                int prevValue = this.getGenresViews().get(genre);
                this.getGenresViews().remove(genre);
                this.getGenresViews().put(genre, prevValue + 1);
            }
        }
        int timesWatched = user.watch(command.getTitle());
        JSONObject object = new JSONObject();
        object.put("id", command.getActionId());
        object.put("message", "success -> " + command.getTitle()
                + " was viewed with total views of " + timesWatched);
        return object;
    }

    private JSONObject giveRating(ActionInputData command) {
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
        int isSuccess = user.addRating(command.getTitle(), totalSeasons, command.getSeasonNumber(),
                command.getGrade());
        if (isSuccess == 1) {
            movie.addRating(command.getGrade());
        }
        if (isSuccess == 2) {
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
                                            final String order, final int number,
                                            final Boolean alphabetical) {
        List<String> keys = new ArrayList<>(unsortedMap.keySet());
        List<Double> values = new ArrayList<>(unsortedMap.values());
        if (alphabetical) {
            Collections.sort(keys);
        }
        switch (order) {
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

    private JSONObject averageActors(ActionInputData action) {
        HashMap<String, Double> actorRatings = new HashMap<>();
        for (ActorInputData actor: this.getActorsData()) {
            actorRatings.put(actor.getName(), this.getActorAverage(actor.getName()));
        }
        List<String> actors = sortHashMapByValue(actorRatings, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + actors);
        return object;
    }

    private JSONObject awardedActors(ActionInputData action) {
        ArrayList<String> actors = new ArrayList<>();
        for (ActorInputData actor: this.actorsData) {
            boolean hasAwards = Boolean.TRUE;
            for (String award: action.getFilters().get(Constants.AWARD_FILTER)) {
                ActorsAwards aw = Utils.stringToAwards(award);
                if (!actor.getAwards().containsKey(aw)) {
                    hasAwards = Boolean.FALSE;
                    break;
                }
            }
            if (hasAwards) {
                actors.add(actor.getName());
            }
        }
        if (Objects.equals(action.getSortType(), Constants.ASC)) {
            Collections.sort(actors);
        } else if (Objects.equals(action.getSortType(), Constants.DESC)) {
            actors.sort(Collections.reverseOrder());
        }
        if (actors.size() < action.getNumber()) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + actors);
            return object;
        } else {
            ArrayList<String> nActors = new ArrayList<>();
            for (int i = 0; i < action.getNumber(); i++) {
                nActors.add(actors.get(i));
            }
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + nActors);
            return object;
        }
    }

    private JSONObject filterWords(ActionInputData action) {
        ArrayList<String> actors = new ArrayList<>();
        for (ActorInputData actor: this.actorsData) {
            boolean hasWords = Boolean.TRUE;
            for (String word: action.getFilters().get(Constants.WORD_FILTER)) {
                if (!actor.getCareerDescription().toLowerCase().contains(word)) {
                    hasWords = Boolean.FALSE;
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
        if (actors.size() < action.getNumber()) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + actors);
            return object;
        } else {
            ArrayList<String> nActors = new ArrayList<>();
            for (int i = 0; i < action.getNumber(); i++) {
                nActors.add(actors.get(i));
            }
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + nActors);
            return object;
        }
    }

    private ArrayList<Video> filterYearGenre(ActionInputData action, Boolean isMovie) {
        ArrayList<Video> goodVideos = new ArrayList<>();
        if (isMovie) {
            for (Movie movie: this.getMovies()) {
                boolean noYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0) == null);
                boolean noGenre = (action.getFilters().get(Constants.GENRE_FILTER).get(0) == null);
                if (!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0).
                            equalsIgnoreCase(String.valueOf(movie.getYear())));
                    Boolean respectsGenre = (movie.getGenres().contains(Utils.stringToGenre(action.getFilters().
                            get(Constants.GENRE_FILTER).get(0))));
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
                    Boolean respectsYear = (action.getFilters().get(Constants.YEAR_FILTER).get(0).
                            equalsIgnoreCase(String.valueOf(show.getYear())));
                    Boolean respectsGenre = (show.getGenres().contains(Utils.stringToGenre(action.
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

    private JSONObject moviesByRating(ActionInputData action) {
        HashMap<String, Double> movieRatings = new HashMap<>();
        ArrayList<Video> goodMovies = filterYearGenre(action, Boolean.TRUE);
        for(Video movie: goodMovies) {
            if (movie.getAverageRating() != 0d) {
                movieRatings.put(movie.getTitle(), movie.getAverageRating());
            }
        }
        List<String> videos = sortHashMapByValue(movieRatings,
                action.getSortType(), action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject showsByRating(ActionInputData action) {
        HashMap<String, Double> showsRatings = new HashMap<>();
        ArrayList<Video> goodShows = filterYearGenre(action, Boolean.FALSE);
        for(Video show: goodShows) {
            if (show.getAverageRating() != 0d) {
                showsRatings.put(show.getTitle(), show.getAverageRating());
            }
        }
        List<String> shows = sortHashMapByValue(showsRatings, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + shows);
        return object;
    }

    private JSONObject moviesByFavorite(ActionInputData action) {
        HashMap<String, Double> moviesFavorite = new HashMap<>();
        ArrayList<Video> goodMovies = filterYearGenre(action, Boolean.TRUE);
        for (Video movie: goodMovies) {
            if ( movie.getTimesAddedToFavorites() != 0d) {
                moviesFavorite.put(movie.getTitle(),
                        (double) movie.getTimesAddedToFavorites());
            }
        }
        List<String> videos = sortHashMapByValue(moviesFavorite, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject showsByFavorite(ActionInputData action) {
        HashMap<String, Double> showsFavorite = new HashMap<>();
        ArrayList<Video> goodShows = filterYearGenre(action, Boolean.FALSE);
        for (Video show: goodShows) {
            if (show.getTimesAddedToFavorites() != 0d) {
                showsFavorite.put(show.getTitle(),
                        (double) show.getTimesAddedToFavorites());
            }
        }
        List<String> videos = sortHashMapByValue(showsFavorite, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject moviesByDuration(ActionInputData action) {
        HashMap<String, Double> moviesDuration = new HashMap<>();
        ArrayList<Video> goodMovies = filterYearGenre(action, Boolean.TRUE);
        for (Video movie: goodMovies) {
            moviesDuration.put(movie.getTitle(), (double) ((Movie) movie).getDuration());
        }
        List<String> videos = sortHashMapByValue(moviesDuration, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject showsByDuration(ActionInputData action) {
        HashMap<String, Double> showsDuration = new HashMap<>();
        ArrayList<Video> goodShows = filterYearGenre(action, Boolean.FALSE);
        for (Video show: goodShows) {
            showsDuration.put(show.getTitle(), (double) ((Show) show).getDuration());
        }
        List<String> videos = sortHashMapByValue(showsDuration, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject moviesByViews(ActionInputData action) {
        HashMap<String, Double> moviesViews = new HashMap<>();
        ArrayList<Video> goodMovies = filterYearGenre(action, Boolean.TRUE);
        for (Video movie: goodMovies) {
            if (movie.getTimesWatched() != 0d) {
                moviesViews.put(movie.getTitle(), (double) movie.getTimesWatched());
            }
        }
        List<String> videos = sortHashMapByValue(moviesViews, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + videos);
        return object;
    }

    private JSONObject showsByViews(ActionInputData action) {
        HashMap<String, Double> showsViews = new HashMap<>();
        ArrayList<Video> goodShows = filterYearGenre(action, Boolean.FALSE);
        for (Video show: goodShows) {
            if (show.getTimesWatched() != 0d) {
                showsViews.put(show.getTitle(), (double) show.getTimesWatched());
            }
        }
        List<String> shows = sortHashMapByValue(showsViews, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + shows);
        return object;
    }

    private JSONObject usersByReviews(ActionInputData action) {
        HashMap<String, Double> userReviews = new HashMap<>();
        for (User user: this.getUsers()) {
            if (user.getNumberOfRatings() != 0) {
                userReviews.put(user.getUsername(), (double) user.getNumberOfRatings());
            }
        }
        List<String> users = sortHashMapByValue(userReviews, action.getSortType(),
                action.getNumber(), Boolean.TRUE);
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + users);
        return object;
    }

    private ArrayList<Video> getUnwatched(final String username) {
        User user = getUserByUsername(username);
        ArrayList<Video> videosCopy = new ArrayList<>();
        videosCopy.addAll(this.getMovies());
        videosCopy.addAll(this.getShows());
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
            if (show != null){
                videosCopy.remove(show);
            }
        }
        return videosCopy;
    }

    private JSONObject videosStandardRecommendation(ActionInputData action) {
        ArrayList<Video> videosUnwatched = this.getUnwatched(action.getUsername());

        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        if (!videosUnwatched.isEmpty()) {
            object.put("message", "StandardRecommendation result: "
                    + videosUnwatched.get(0).getTitle());
        } else {
            object.put("message", "StandardRecommendation result: " + videosUnwatched);
        }
        return object;
    }

    private JSONObject videosBestRecommendation(ActionInputData action) {
        ArrayList<Video> videosUnwatched = this.getUnwatched(action.getUsername());

        LinkedHashMap<String, Double> videosRatings = new LinkedHashMap<>();
        for (Video video: videosUnwatched) {
            videosRatings.put(video.getTitle(), video.getAverageRating());
        }
        List<String> videos = sortHashMapByValue(videosRatings, Constants.DESC,
                1, Boolean.FALSE);
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

    private ArrayList<Video> getVideosByGenre(Genre genre) {
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

    private boolean genreViewsNonNull () {
        for (Genre genre: this.getGenresViews().keySet()) {
            if (this.getGenresViews().get(genre) != 0) {
                return true;
            }
        }
        return false;
    }

    private JSONObject videosPopularRecommendation(ActionInputData action) {
        User user = getUserByUsername(action.getUsername());
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
                    if (videosByGenre.contains(movie)) {
                        videosByGenre.remove(movie);
                    }
                }
                if (show != null) {
                    if (videosByGenre.contains(show)) {
                        videosByGenre.remove(show);
                    }
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



    private JSONObject videosFavouriteRecommendation(ActionInputData action) {
        User user = getUserByUsername(action.getUsername());
        if (user.getSubscriptionType().equalsIgnoreCase(Constants.BASIC)) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "PopularRecommendation cannot be applied!");
            return object;
        }
        return null;
    }

    private JSONObject videosSearchRecommendation(ActionInputData action) {
        User user = getUserByUsername(action.getUsername());
        if (user.getSubscriptionType().equalsIgnoreCase(Constants.BASIC)) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "PopularRecommendation cannot be applied!");
            return object;
        }
        return null;
    }

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
                                case Constants.RATINGS -> output.add(this.moviesByRating(action));
                                case Constants.FAVORITE ->
                                        output.add(this.moviesByFavorite(action));
                                case Constants.LONGEST -> output.add(this.moviesByDuration(action));
                                case Constants.MOST_VIEWED ->
                                        output.add(this.moviesByViews(action));
                                default -> {
                                }
                            }
                            break;
                        case Constants.SHOWS:
                            switch (action.getCriteria()) {
                                case Constants.RATINGS -> output.add(this.showsByRating(action));
                                case Constants.FAVORITE -> output.add(this.showsByFavorite(action));
                                case Constants.LONGEST -> output.add(this.showsByDuration(action));
                                case Constants.MOST_VIEWED -> output.add(this.showsByViews(action));
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
