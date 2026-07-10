package com.naviapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Один элемент ответа Nominatim (https://nominatim.org/release-docs/latest/api/Search/).
 * Nominatim используется для геокодирования адресов.
 */
public class NominatimResult {

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("lat")
    public String lat;

    @SerializedName("lon")
    public String lon;

    @SerializedName("type")
    public String type;

    @SerializedName("class")
    public String category;
}
