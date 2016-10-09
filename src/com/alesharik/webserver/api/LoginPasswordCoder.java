package com.alesharik.webserver.api;

/**
 * This class used for encode login and password
 */
public final class LoginPasswordCoder {
    private LoginPasswordCoder() {
    }

    /**
     * Encode login and password into one encoded string  <br>
     * NOTE!If password and login have non-equals length then decode login and password is too difficult or impossible
     *
     * @param login    login to encode
     * @param password password to encode
     * @return encoded string
     */
    public static String encode(String login, String password) {
        StringBuilder sb = new StringBuilder();
        char[] log = login.toCharArray();
        char[] pass = password.toCharArray();
        for(int i = 0; i < Math.min(log.length, pass.length); i++) {
            sb.append(log[i]);
            sb.append(pass[i]);
        }

        int i;
        char[] chars;
        if(Integer.compare(Math.min(log.length, pass.length), log.length) == 0) {
            i = pass.length - log.length - 1;
            chars = pass;
        } else {
            i = log.length - pass.length - 1;
            chars = log;
        }

        if(i == -1) {
            return sb.toString();
        } else {
            for(; i < Math.max(log.length, pass.length); i++) {
                if(chars.length >= i) {
                    break;
                }
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    public static boolean isEquals(String login, String password, String logpass) {
        return encode(login, password).equals(logpass);
    }
}
