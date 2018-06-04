package com.sim.alzheimermate.Models;

/**
 * Created by Rym on 19/11/2017.
 */

public class Medicament {

    private String nom;
    private int nbPrises;
    private String heures_prises;
    private String image_med;


    public Medicament() {
    }

    public Medicament(String nom, int nbPrises, String heures_prises, String image_med) {
        this.nom = nom;
        this.nbPrises = nbPrises;
        this.heures_prises = heures_prises;
        this.image_med = image_med;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNbPrises() {
        return nbPrises;
    }

    public void setNbPrises(int nbPrises) {
        this.nbPrises = nbPrises;
    }

    public String getHeures_prises() {
        return heures_prises;
    }

    public void setHeures_prises(String heures_prises) {
        this.heures_prises = heures_prises;
    }

    public String getImage_med() {
        return image_med;
    }

    public void setImage_med(String image_med) {
        this.image_med = image_med;
    }

    @Override
    public String toString() {
        return nom + " " + nbPrises + " " + image_med;
    }
}
