package com.sim.alzheimermate.Models;

import com.sim.alzheimermate.Utils.SharedData;

/**
 * Created by Rym on 21/12/2017.
 */

public class EndPoints {
    private static final String ROOT_URL = SharedData.url;
    public static final String UPLOAD_URL_Per = ROOT_URL + "addPer";
    public static final String GET_PICS_URL_Per = ROOT_URL + "getper";
    public static final String UPLOAD_URL_Med = ROOT_URL + "addMed";
    public static final String GET_PICS_URL_Med = ROOT_URL + "getmed";
}