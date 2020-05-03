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

    @Override
    public String toString() {
        return "{ errorMessage: " + errorMessage + " username: " + username + " uuid: " + uuid + " accessToken: " + accessToken + " }";
    }
}
