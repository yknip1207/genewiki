package org.gnf.wikiapi.query;

/**
 * Query for watching and unwatching pages.
 */
public class Watch extends RequestBuilder{

    private Watch() {
        action("watch");
    }

    public static Watch create() {
        return new Watch();
    }

    public Watch title(String title) {
        put("title", title);
        return this;
    }

    public Watch unwatch() {
        put("unwatch", "");
        return this;
    }

    public Watch token(String token) {
        put("token", token);
        return this;
    }

}
