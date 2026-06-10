package com.yudi.asmara.expensereport.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Category implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("nama_kategori")
    private String namaKategori;

    @SerializedName("icon")
    private String icon;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    @Override
    public String toString() {
        return namaKategori;
    }
}
