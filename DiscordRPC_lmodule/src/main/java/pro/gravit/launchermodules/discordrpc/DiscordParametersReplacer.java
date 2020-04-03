package pro.gravit.launchermodules.discordrpc;

public class DiscordParametersReplacer {
    public String username;
    public String profileName;
    public String userUUID;
    public String minecraftVersion;
    public String replace(String str)
    {
        String result = str;
        if(username != null) result = result.replaceAll("%username%", username);
        if(profileName != null) result = result.replaceAll("%profileName%", profileName);
        if(userUUID != null) result = result.replaceAll("%userUUID%", userUUID);
        if(minecraftVersion != null) result = result.replaceAll("%minecraftVersion%", minecraftVersion);
        return result;
    }
}
