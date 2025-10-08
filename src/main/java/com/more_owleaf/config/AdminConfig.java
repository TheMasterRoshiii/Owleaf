package com.more_owleaf.config;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class AdminConfig {
    @SerializedName("allowed_players")
    private List<String> allowedPlayers = new ArrayList<>();

    public List<String> getAllowedPlayers() {
        return allowedPlayers;
    }
}