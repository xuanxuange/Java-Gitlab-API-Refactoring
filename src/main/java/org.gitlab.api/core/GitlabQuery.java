package org.gitlab.api.core;


import org.gitlab.api.http.Config;
import org.gitlab.api.http.GitlabHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * An abstract class to query list of {@link GitlabComponent} based on some query conditions (query parameters)
 *
 * @param <T> the expected {@link GitlabComponent} as the query result
 */
abstract class GitlabQuery<T extends GitlabComponent> {
    /**
     * The date formatter specifically for the Gitlab API
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**
     * The time formatter specifically for the Gitlab API
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * The type representing a array of the given {@link GitlabComponent}
     */
    private final Class<T[]> type;
    /**
     * The type of params is:
     * Tuple<name, Pair<value, URLEncoder.encode(value, "UTF-8")>>
     */
    private final List<Pair<String, Pair<String, String>>> params = new ArrayList<Pair<String, Pair<String, String>>>();
    /**
     *
     */
    private final Config config;

    /**
     * Construct the query by the Gitlab configuration and the expected type for the query response
     *
     * @param config - the Gitlab configuration to be used
     * @param type   - the expected array type for the query response
     */
    GitlabQuery(Config config, Class<T[]> type) {
        this.config = config;
        this.type = type;
    }

    /**
     * Get the tail url of the request
     *
     * @return the tail url of the request, e.g. /projects
     */
    public abstract String getTailUrl();

    /**
     * For a component to bind with the parent component after it is parsed
     *
     * @param component component to be bind
     */
    abstract void bind(T component);

    /**
     * Add pagination on top of the query
     *
     * @param pagination pagination object that defines page number and size
     * @return this {@link GitlabQuery} with the given pagination object
     */
    public abstract GitlabQuery<T> withPagination(Pagination pagination);

    /**
     * Get the config that is stored in current {@link GitlabQuery}
     *
     * @return the Gitlab configuration
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Issue a HTTP request to perform the query
     *
     * @return a list of component retrieved from the query
     * @throws GitlabException if {@link IOException} occurs or the response code is not in [200,400)
     */
    public List<T> query() {
        List<T> components = GitlabHttpClient.getList(config, getEntireUrl(), type);
        components.forEach(this::bind);
        return components;
    }

    /**
     * Get the entire url of the query
     *
     * @return entire url, e.g. /projects?owned=true
     */
    public String getEntireUrl() {
        return getTailUrl() + toString();
    }

    protected Class<T[]> getType() {
        return type;
    }

    /**
     * Appends a parameter to the query
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @return this
     */
    protected GitlabQuery<T> appendString(String name, String value) {
        if (value != null) {
            try {
                params.add(new Pair<>(name, new Pair<>(value, URLEncoder.encode(value, "UTF-8"))));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * add a integer type parameter to the query
     *
     * @param name  name of the parameter
     * @param value value of the parameter
     * @return GitlabQuery with the new parameter added
     */
    protected GitlabQuery<T> appendInt(String name, int value) {
        return appendString(name, String.valueOf(value));
    }

    /**
     * add a boolean type parameter to the query
     *
     * @param name  name of the parameter
     * @param value value of the parameter
     * @return GitlabQuery with the new parameter added
     */
    protected GitlabQuery<T> appendBoolean(String name, boolean value) {
        return appendString(name, String.valueOf(value));
    }

    /**
     * add a list of strings type parameter to the query
     *
     * @param name    name of the parameter
     * @param strings value of the parameter
     * @return GitlabQuery with the new parameter added
     */
    protected GitlabQuery<T> appendStrings(String name, List<String> strings) {
        return appendString(name, String.join(",", strings));
    }

    /**
     * add a list of integers type parameter to the query
     *
     * @param name name of the parameter
     * @param ints value of the parameter
     * @return GitlabQuery with the new parameter added
     */
    protected GitlabQuery<T> appendInts(String name, List<Integer> ints) {
        return appendString(name, ints.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    /**
     * add a date type parameter to the query
     *
     * @param name name of the parameter
     * @param date value of the parameter
     * @return GitlabQuery with the new parameter added
     */
    protected GitlabQuery<T> appendDate(String name, LocalDate date) {
        if (date != null) {
            return appendString(name, date.format(DATE_FORMATTER));
        }
        return this;
    }

    /**
     * add a datetime type parameter to the query
     *
     * @param name     name of the parameter
     * @param dateTime value of the parameter
     * @return GitlabQuery with the new parameter added
     */
    protected GitlabQuery<T> appendDateTime(String name, LocalDateTime dateTime) {
        if (dateTime != null) {
            ZonedDateTime time = dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
            return appendString(name, DATE_TIME_FORMATTER.format(time));
        }
        return this;
    }

    /**
     * add a pagination to the query
     *
     * @param pagination name of the parameter
     * @return GitlabQuery with the new pagination added
     */
    protected GitlabQuery<T> appendPagination(Pagination pagination) {
        appendInt("per_page", pagination.getPageSize());
        appendInt("page", pagination.getPageNumber());
        return this;
    }

    /**
     * Returns the string in the format URL query parameters
     * e.g. {@code ?key1=value1&key2=value2}
     *
     * @return the string in the format URL query parameters
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Pair<String, Pair<String, String>> param : params) {
            if (builder.length() == 0) {
                builder.append('?');
            } else {
                builder.append('&');
            }
            builder.append(param.first);
            builder.append('=');
            builder.append(param.second.second);
        }

        return builder.toString();
    }

    /**
     * The class representing a pair
     *
     * @param <T1> the class of the first element in the pair
     * @param <T2> the class of the second element in the pair
     */
    private static final class Pair<T1, T2> {
        /**
         * The first element
         */
        T1 first;
        /**
         * The second element
         */
        T2 second;

        /**
         * Create a new pair
         *
         * @param first  the first element in the pair
         * @param second the second element in the pair
         */
        private Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }
}
