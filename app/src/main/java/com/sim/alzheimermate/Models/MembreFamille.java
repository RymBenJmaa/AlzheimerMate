package com.sim.alzheimermate.Models;

/**
 * Created by Rym on 12/12/2017.
 */

public class MembreFamille {
    private String nom;
    private String prenom;
    private String lien;
    private String email;
    private int num_tel;
    private String image_per;

    public MembreFamille() {
    }

    public MembreFamille(String nom, String prenom, String lien, String email, int num_tel, String image_per) {
        this.nom = nom;
        this.prenom = prenom;
        this.lien = lien;
        this.email = email;
        this.num_tel = num_tel;
        this.image_per = image_per;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNum_tel() {
        return num_tel;
    }

    public void setNum_tel(int num_tel) {
        this.num_tel = num_tel;
    }

    public String getImage_per() {
        return image_per;
    }

    public void setImage_per(String image_per) {
        this.image_per = image_per;
    }

    @Override
    public String toString() {
        return nom + " " + prenom + " " + num_tel + " " + " " + email + " " + lien + " " + image_per;
    }
}
