package repository;

import actor.ActorsAwards;
import fileio.*;
import net.sf.json.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import users.User;
import entertainment.Movie;
import entertainment.Show;
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

    private Database() {}

    public static Database getInstance() {
        if(instance == null){
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
        for(User user: this.users) {
            if(user.getUser().getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public Movie getMovieByTitle(final String title) {
        for(Movie movie: this.movies) {
            if(movie.getMovie().getTitle().equalsIgnoreCase(title)) {
                return movie;
            }
        }
        return null;
    }

    public Show getShowByTitle(final String title) {
        for(Show show: this.shows) {
            if(show.getSerial().getTitle().equalsIgnoreCase(title)) {
                return show;
            }
        }
        return null;
    }

    public ActorInputData getActorByName(final String name) {
        for(ActorInputData actor: this.actorsData) {
            if(actor.getName().equalsIgnoreCase(name)) {
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

    public void populateDatabase(Input input) {
        this.setActorsData(input.getActors());
        this.setCommandsData(input.getCommands());
        for(UserInputData userData: input.getUsers()) {
            this.users.add(new User(userData));
        }
        for(MovieInputData movieData: input.getMovies()) {
            this.movies.add(new Movie(movieData));
        }
        for(SerialInputData serialData: input.getSerials()) {
            this.shows.add(new Show(serialData));
        }
        for(User user: this.getUsers()) {
            for(String video: user.getUser().getFavoriteMovies()) {
                Movie movie = getMovieByTitle(video);
                Show show = getShowByTitle(video);
                if(movie != null) {
                    movie.addFavorite();
                }
                if(show != null) {
                    show.addFavorite();
                }
            }
            for(String video: user.getUser().getHistory().keySet()) {
                Movie movie = getMovieByTitle(video);
                Show show = getShowByTitle(video);
                if(movie != null) {
                    if(user.getUser().getHistory().containsKey(video)) {
                        for(int i = 0; i < user.getUser().getHistory().get(video); i++) {
                            movie.addView();
                        }
                    }
                }
                if(show != null) {
                    if(user.getUser().getHistory().containsKey(video)) {
                        for(int i = 0; i < user.getUser().getHistory().get(video); i++) {
                            show.addView();
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

    public JSONObject favorite(ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
        int added = user.addFavorite(command.getTitle());
        JSONObject object = new JSONObject();
        object.put("id", command.getActionId());
        if(added == 1) {
            Movie movie = getMovieByTitle(command.getTitle());
            Show show = getShowByTitle(command.getTitle());
            if(movie != null) {
                movie.addFavorite();
            }
            if(show != null) {
                show.addFavorite();
            }
            object.put("message", "success -> " + command.getTitle() + " was added as favourite");
        }
        else if (added == -1) {
            object.put("message", "error -> " + command.getTitle() + " is already in favourite list");
        }
        else {
            object.put("message", "error -> " + command.getTitle() + " is not seen");
        }
        return object;
    }

    public JSONObject watch(ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
        Movie movie = getMovieByTitle(command.getTitle());
        Show show = getShowByTitle(command.getTitle());
        if(movie != null) {
            movie.addView();
        }
        if(show != null) {
            show.addView();
        }
        int timesWatched = user.watch(command.getTitle());
        JSONObject object = new JSONObject();
        object.put("id", command.getActionId());
        object.put("message", "success -> " + command.getTitle() + " was viewed with total views of " + timesWatched);
        return object;
    }

    public JSONObject giveRating(ActionInputData command) {
        User user = getUserByUsername(command.getUsername());
        JSONObject object = new JSONObject();
        Show show = getShowByTitle(command.getTitle());
        Movie movie = getMovieByTitle(command.getTitle());
        int totalSeasons;
        if(show == null) {
            totalSeasons = 0;
        }
        else {
            totalSeasons = show.getSerial().getNumberSeason();
        }
        int isSuccess = user.addRating(command.getTitle(), totalSeasons, command.getSeasonNumber(), command.getGrade());
        if(isSuccess == 1) {
            movie.addRating(command.getGrade());
        }
        if(isSuccess == 2) {
            show.getSerial().getSeasons().get(command.getSeasonNumber() - 1).addRating(command.getGrade());
        }
        if(isSuccess == 1 || isSuccess == 2) {
            object.put("id", command.getActionId());
            object.put("message", "success -> "
                    + command.getTitle() + " was rated with "
                    + command.getGrade() +  " by "
                    + command.getUsername());
        }
        else if(isSuccess == -1) {
            object.put("id", command.getActionId());
            object.put("message", "error -> "
                    + command.getTitle() + " has been already rated");
        }
        else if(isSuccess == 0) {
            object.put("id", command.getActionId());
            object.put("message", "error -> "
                    + command.getTitle() + " is not seen");
        }
        return object;
    }

    public Double getActorAverage(final String actorName) {
        ActorInputData actor = getActorByName(actorName);
        int totalRated = 0;
        Double average = 0d;
        for(String videoName: actor.getFilmography()) {
            Movie movie = getMovieByTitle(videoName);
            Show show = getShowByTitle(videoName);
            if(movie != null) {
                if(!movie.getRatings().isEmpty()) {
                    average += movie.averageRating();
                    totalRated++;
                }
            }
            if(show != null) {
                if(show.serialAverageRating() != 0) {
                    average += show.serialAverageRating();
                    totalRated++;
                }
            }
        }
        average = average / totalRated;

        return average;
    }

    public LinkedHashMap<String, Double> sortHashMapByValue(HashMap<String, Double> unsortedMap, final String order) {
        List<String> keys = new ArrayList<>(unsortedMap.keySet());
        List<Double> values = new ArrayList<>(unsortedMap.values());
        Collections.sort(keys);
        switch(order) {
            case Constants.ASC:
                Collections.sort(values);
                break;
            case Constants.DESC:
                Collections.sort(values, Collections.reverseOrder());
                break;
            default:
                break;
        }

        LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();
        Iterator<Double> valuesIterator = values.iterator();
        while(valuesIterator.hasNext()) {
            Double value = valuesIterator.next();
            Iterator<String> keyIterator = keys.iterator();
            while(keyIterator.hasNext()) {
                String key = keyIterator.next();
                if(unsortedMap.get(key).equals(value)) {
                    keyIterator.remove();
                    sortedMap.put(key, value);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public LinkedHashMap<String, Integer> sortHashMapByValueInteger(HashMap<String, Integer> unsortedMap, final String order) {
        List<String> keys = new ArrayList<>(unsortedMap.keySet());
        List<Integer> values = new ArrayList<>(unsortedMap.values());
        Collections.sort(keys);
        switch(order) {
            case Constants.ASC:
                Collections.sort(values);
                break;
            case Constants.DESC:
                Collections.sort(values, Collections.reverseOrder());
                break;
            default:
                break;
        }

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
        Iterator<Integer> valuesIterator = values.iterator();
        while(valuesIterator.hasNext()) {
            Integer value = valuesIterator.next();
            Iterator<String> keyIterator = keys.iterator();
            while(keyIterator.hasNext()) {
                String key = keyIterator.next();
                if(unsortedMap.get(key).equals(value)) {
                    keyIterator.remove();
                    sortedMap.put(key, value);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public JSONObject averageActors(ActionInputData action) {
        HashMap<String, Double> actorRatings = new HashMap<>();
        for(ActorInputData actor: this.getActorsData()) {
            actorRatings.put(actor.getName(), this.getActorAverage(actor.getName()));
        }
        LinkedHashMap<String, Double> sortedByRatings = sortHashMapByValue(actorRatings, action.getSortType());
        List<String> actors = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Double>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Double> value = iterator.next();
            actors.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + actors);
        return object;
    }

    public JSONObject awardedActors(ActionInputData action) {
        ArrayList<String> actors = new ArrayList<>();
        for(ActorInputData actor: this.actorsData) {
            Boolean hasAwards = Boolean.TRUE;
            for(String award: action.getFilters().get(3)) {
                ActorsAwards aw = Utils.stringToAwards(award);
                if(!actor.getAwards().containsKey(aw)) {
                    hasAwards = Boolean.FALSE;
                }
            }
            if(hasAwards) {
                actors.add(actor.getName());
            }
        }
        if(action.getSortType() == Constants.ASC) {
            Collections.sort(actors);
        }
        else if(action.getSortType() == Constants.DESC) {
            Collections.sort(actors, Collections.reverseOrder());
        }
        if(actors.size() < action.getNumber()) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + actors);
            return object;
        }
        else {
            ArrayList<String> nActors = new ArrayList<>();
            for(int i = 0; i < action.getNumber(); i++) {
                nActors.add(actors.get(i));
            }
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + nActors);
            return object;
        }
    }

    public JSONObject filter(ActionInputData action) {
        ArrayList<String> actors = new ArrayList<>();
        for(ActorInputData actor: this.actorsData) {
            Boolean hasAwards = Boolean.TRUE;
            for(String word: action.getFilters().get(2)) {
                if(!actor.getCareerDescription().toLowerCase().contains(word)) {
                    hasAwards = Boolean.FALSE;
                }
            }
            if(hasAwards) {
                actors.add(actor.getName());
            }
        }
        if(action.getSortType() == Constants.ASC) {
            Collections.sort(actors);
        }
        else if(action.getSortType() == Constants.DESC) {
            Collections.sort(actors, Collections.reverseOrder());
        }
        if(actors.size() < action.getNumber()) {
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + actors);
            return object;
        }
        else {
            ArrayList<String> nActors = new ArrayList<>();
            for(int i = 0; i < action.getNumber(); i++) {
                nActors.add(actors.get(i));
            }
            JSONObject object = new JSONObject();
            object.put("id", action.getActionId());
            object.put("message", "Query result: " + nActors);
            return object;
        }
    }

    public JSONObject moviesByRating(ActionInputData action) {
        HashMap<String, Double> movieRatings = new HashMap<>();
        for(Movie movie: this.getMovies()) {
            if(movie.averageRating() != 0d) {
                Boolean noYear = (action.getFilters().get(0).get(0) == null);
                Boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if(!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                    Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsYear && respectsGenre) {
                        movieRatings.put(movie.getMovie().getTitle(), movie.averageRating());
                    }
                }
                else if(!noGenre) {
                    Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsGenre) {
                        movieRatings.put(movie.getMovie().getTitle(), movie.averageRating());
                    }
                } else if (!noYear) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                    if(respectsYear) {
                        movieRatings.put(movie.getMovie().getTitle(), movie.averageRating());
                    }
                }
                else {
                    movieRatings.put(movie.getMovie().getTitle(), movie.averageRating());
                }
            }
        }
        LinkedHashMap<String, Double> sortedByRatings = sortHashMapByValue(movieRatings, action.getSortType());
        List<String> movies = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Double>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Double> value = iterator.next();
            movies.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + movies);
        return object;
    }

    public JSONObject showsByRating(ActionInputData action) {
        HashMap<String, Double> showsRatings = new HashMap<>();
        for(Show show: this.getShows()) {
            if(show.serialAverageRating() != 0d) {
                Boolean noYear = (action.getFilters().get(0).get(0) == null);
                Boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if(!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                    Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsYear && respectsGenre) {
                        showsRatings.put(show.getSerial().getTitle(), show.serialAverageRating());
                    }
                }
                else if(!noGenre) {
                    Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsGenre) {
                        showsRatings.put(show.getSerial().getTitle(), show.serialAverageRating());
                    }
                } else if (!noYear) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                    if(respectsYear) {
                        showsRatings.put(show.getSerial().getTitle(), show.serialAverageRating());
                    }
                }
                else {
                    showsRatings.put(show.getSerial().getTitle(), show.serialAverageRating());
                }
            }
        }
        LinkedHashMap<String, Double> sortedByRatings = sortHashMapByValue(showsRatings, action.getSortType());
        List<String> shows = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Double>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Double> value = iterator.next();
            shows.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + shows);
        return object;
    }

    public JSONObject moviesByFavorite(ActionInputData action) {
        HashMap<String, Integer> moviesFavorite = new HashMap<>();
        for(Movie movie: this.getMovies()) {
            if(movie.getTimesAddedToFavorites() != 0) {
                Boolean noYear = (action.getFilters().get(0).get(0) == null);
                Boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if(!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                    Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsYear && respectsGenre) {
                        moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesAddedToFavorites());
                    }
                }
                else if(!noGenre) {
                    Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsGenre) {
                        moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesAddedToFavorites());
                    }
                } else if (!noYear) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                    if(respectsYear) {
                        moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesAddedToFavorites());
                    }
                }
                else {
                    moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesAddedToFavorites());
                }
            }
        }
        LinkedHashMap<String, Integer> sortedByRatings = sortHashMapByValueInteger(moviesFavorite, action.getSortType());
        List<String> movies = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            movies.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + movies);
        return object;
    }

    public JSONObject showsByFavorite(ActionInputData action) {
        HashMap<String, Integer> showsFavorite = new HashMap<>();
        for(Show show: this.getShows()) {
            if(show.getTimesAddedToFavorites() != 0) {
                Boolean noYear = (action.getFilters().get(0).get(0) == null);
                Boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if(!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                    Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsYear && respectsGenre) {
                        showsFavorite.put(show.getSerial().getTitle(), show.getTimesAddedToFavorites());
                    }
                }
                else if(!noGenre) {
                    Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsGenre) {
                        showsFavorite.put(show.getSerial().getTitle(), show.getTimesAddedToFavorites());
                    }
                } else if (!noYear) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                    if(respectsYear) {
                        showsFavorite.put(show.getSerial().getTitle(), show.getTimesAddedToFavorites());
                    }
                }
                else {
                    showsFavorite.put(show.getSerial().getTitle(), show.getTimesAddedToFavorites());
                }
            }
        }
        LinkedHashMap<String, Integer> sortedByRatings = sortHashMapByValueInteger(showsFavorite, action.getSortType());
        List<String> shows = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            shows.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + shows);
        return object;
    }

    public JSONObject moviesByDuration(ActionInputData action) {
        HashMap<String, Integer> moviesFavorite = new HashMap<>();
        for(Movie movie: this.getMovies()) {
            Boolean noYear = (action.getFilters().get(0).get(0) == null);
            Boolean noGenre = (action.getFilters().get(1).get(0) == null);
            if(!noYear && !noGenre) {
                Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                if(respectsYear && respectsGenre) {
                    moviesFavorite.put(movie.getMovie().getTitle(), movie.getMovie().getDuration());
                }
            }
            else if(!noGenre) {
                Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                if(respectsGenre) {
                    moviesFavorite.put(movie.getMovie().getTitle(), movie.getMovie().getDuration());
                }
            } else if (!noYear) {
                Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                if(respectsYear) {
                    moviesFavorite.put(movie.getMovie().getTitle(), movie.getMovie().getDuration());
                }
            }
            else {
                moviesFavorite.put(movie.getMovie().getTitle(), movie.getMovie().getDuration());
            }
        }
        LinkedHashMap<String, Integer> sortedByRatings = sortHashMapByValueInteger(moviesFavorite, action.getSortType());
        List<String> movies = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            movies.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + movies);
        return object;
    }

    public JSONObject showsByDuration(ActionInputData action) {
        HashMap<String, Integer> showsFavorite = new HashMap<>();
        for(Show show: this.getShows()) {
            Boolean noYear = (action.getFilters().get(0).get(0) == null);
            Boolean noGenre = (action.getFilters().get(1).get(0) == null);
            if(!noYear && !noGenre) {
                Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                if(respectsYear && respectsGenre) {
                    showsFavorite.put(show.getSerial().getTitle(), show.getDuration());
                }
            }
            else if(!noGenre) {
                Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                if(respectsGenre) {
                    showsFavorite.put(show.getSerial().getTitle(), show.getDuration());
                }
            } else if (!noYear) {
                Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                if(respectsYear) {
                    showsFavorite.put(show.getSerial().getTitle(), show.getDuration());
                }
            }
            else {
                showsFavorite.put(show.getSerial().getTitle(), show.getDuration());
            }
        }
        LinkedHashMap<String, Integer> sortedByRatings = sortHashMapByValueInteger(showsFavorite, action.getSortType());
        List<String> shows = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            shows.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + shows);
        return object;
    }

    public JSONObject moviesByViews(ActionInputData action) {
        HashMap<String, Integer> moviesFavorite = new HashMap<>();
        for(Movie movie: this.getMovies()) {
            if(movie.getTimesWatched() != 0) {
                Boolean noYear = (action.getFilters().get(0).get(0) == null);
                Boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if(!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                    Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsYear && respectsGenre) {
                        moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesWatched());
                    }
                }
                else if(!noGenre) {
                    Boolean respectsGenre = (movie.getMovie().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsGenre) {
                        moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesWatched());
                    }
                } else if (!noYear) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(movie.getMovie().getYear())));
                    if(respectsYear) {
                        moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesWatched());
                    }
                }
                else {
                    moviesFavorite.put(movie.getMovie().getTitle(), movie.getTimesWatched());
                }
            }
        }
        LinkedHashMap<String, Integer> sortedByRatings = sortHashMapByValueInteger(moviesFavorite, action.getSortType());
        System.out.println(sortedByRatings);
        List<String> movies = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            movies.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + movies);
        return object;
    }

    public JSONObject showsByViews(ActionInputData action) {
        HashMap<String, Integer> showsFavorite = new HashMap<>();
        for(Show show: this.getShows()) {
            if(show.getTimesWatched() != 0) {
                Boolean noYear = (action.getFilters().get(0).get(0) == null);
                Boolean noGenre = (action.getFilters().get(1).get(0) == null);
                if(!noYear && !noGenre) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                    Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsYear && respectsGenre) {
                        showsFavorite.put(show.getSerial().getTitle(), show.getTimesWatched());
                    }
                }
                else if(!noGenre) {
                    Boolean respectsGenre = (show.getSerial().getGenres().contains(action.getFilters().get(1).get(0)));
                    if(respectsGenre) {
                        showsFavorite.put(show.getSerial().getTitle(), show.getTimesWatched());
                    }
                } else if (!noYear) {
                    Boolean respectsYear = (action.getFilters().get(0).get(0).equalsIgnoreCase(String.valueOf(show.getSerial().getYear())));
                    if(respectsYear) {
                        showsFavorite.put(show.getSerial().getTitle(), show.getTimesWatched());
                    }
                }
                else {
                    showsFavorite.put(show.getSerial().getTitle(), show.getTimesWatched());
                }
            }
        }
        LinkedHashMap<String, Integer> sortedByRatings = sortHashMapByValueInteger(showsFavorite, action.getSortType());
        List<String> shows = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByRatings.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByRatings.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            shows.add(value.getKey());
            sortedByRatings.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + shows);
        return object;
    }

    public JSONObject usersByReviews(ActionInputData action) {
        HashMap<String, Integer> userReviews = new HashMap<>();
        for(User user: this.getUsers()) {
            if(user.getNumberOfRatings() != 0) {
                userReviews.put(user.getUser().getUsername(),user.getNumberOfRatings());
            }
        }
        LinkedHashMap<String, Integer> sortedByReviews = sortHashMapByValueInteger(userReviews, action.getSortType());
        List<String> users = new ArrayList<>();
        for(int i = 0; i < action.getNumber(); i++) {
            if(sortedByReviews.isEmpty()) {
                break;
            }
            Iterator<Map.Entry<String, Integer>> iterator = sortedByReviews.entrySet().iterator();
            Map.Entry<String, Integer> value = iterator.next();
            users.add(value.getKey());
            sortedByReviews.remove(value.getKey());
        }
        JSONObject object = new JSONObject();
        object.put("id", action.getActionId());
        object.put("message", "Query result: " + users);
        return object;
    }

    public JSONArray runCommands() {
        JSONArray output = new JSONArray();
        for(ActionInputData action: commandsData) {
            switch(action.getActionType()) {
                case Constants.COMMAND:
                    switch (action.getType()) {
                        case Constants.FAVORITE:
                            output.add(this.favorite(action));
                            break;
                        case Constants.VIEW:
                            output.add(this.watch(action));
                            break;
                        case Constants.RATING:
                            output.add(this.giveRating(action));
                            break;
                        default:
                    }
                    break;
                case Constants.QUERY:
                    switch(action.getObjectType()) {
                        case Constants.ACTORS:
                            switch(action.getCriteria()) {
                                case Constants.AVERAGE:
                                    output.add(this.averageActors(action));
                                    break;
                                case Constants.AWARDS:
                                    output.add(this.awardedActors(action));
                                    break;
                                case Constants.FILTER_DESCRIPTIONS:
                                    output.add(this.filter(action));
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case Constants.MOVIES:
                            switch(action.getCriteria()) {
                                case Constants.RATINGS:
                                    output.add(this.moviesByRating(action));
                                    break;
                                case Constants.FAVORITE:
                                    output.add(this.moviesByFavorite(action));
                                    break;
                                case Constants.LONGEST:
                                    output.add(this.moviesByDuration(action));
                                    break;
                                case Constants.MOST_VIEWED:
                                    output.add(this.moviesByViews(action));
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case Constants.SHOWS:
                            switch(action.getCriteria()) {
                                case Constants.RATINGS:
                                    output.add(this.showsByRating(action));
                                    break;
                                case Constants.FAVORITE:
                                    output.add(this.showsByFavorite(action));
                                    break;
                                case Constants.LONGEST:
                                    output.add(this.showsByDuration(action));
                                    break;
                                case Constants.MOST_VIEWED:
                                    output.add(this.showsByViews(action));
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case Constants.USERS:
                            output.add(this.usersByReviews(action));
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        return output;
    }
}
