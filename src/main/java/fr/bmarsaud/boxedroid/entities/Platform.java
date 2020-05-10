package fr.bmarsaud.boxedroid.entities;

public enum Platform {
    DEFAULT("default"),
    GOOGLE_APIS("google_apis"),
    GOOGLE_APIS_PLAYSTORE("google_apis_playstore"),
    ANDROID_TV("android-tv"),
    ANDROID_WEAR("android-wear"),
    ANDROID_WEAR_CN("android-wear-cn");

    private String id;

    Platform(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
