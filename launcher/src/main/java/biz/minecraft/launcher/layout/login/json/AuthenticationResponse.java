package biz.minecraft.launcher.layout.login.json;

/**
 * Player authentication response deserialization class.
 * https://auth.minecraft.biz/authenticate
 */
public class AuthenticationResponse {

    private String errorMessage;
    private String username;
    private String uuid;
    private String accessToken;
    private String token;

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getToken() { return token; }

    @Override
    public String toString() {
        return "{ errorMessage: " + errorMessage + " username: " + username + " uuid: " + uuid + " accessToken: " + accessToken + " token: " + token + " }";
    }
}
