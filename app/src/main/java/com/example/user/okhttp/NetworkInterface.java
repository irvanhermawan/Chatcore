package com.example.user.okhttp;

public interface NetworkInterface {
    /**
     * Get string by username password
     * @param endpoint rest url
     * @param username username
     * @param password password
     * @return String
     */
    String getString(String endpoint, String username, String password);


    /**
     * Get string by bear token
     * @param endpoint rest url
     * @param token bearer token
     * @return String
     */
    String getString(String endpoint, String token);

    /**
     * Search call
     * @param query search query
     * @return String
     */
    String search(String query);
}