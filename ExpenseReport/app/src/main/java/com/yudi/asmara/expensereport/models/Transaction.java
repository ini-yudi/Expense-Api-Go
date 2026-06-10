package com.yudi.asmara.expensereport.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    private int id;

    @SerializedName("kategori_id")
    private int kategoriId;

    @SerializedName("nama_kategori")
    private String namaKategori;

    @SerializedName("icon")
    private String icon;

    @SerializedName("tipe")
    private String tipe;

    @SerializedName("nominal")
    private double nominal;

    @SerializedName("keterangan")
    private String keterangan;

    @SerializedName("tanggal_transaksi")
    private String tanggalTransaksi;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKategoriId() { return kategoriId; }
    public void setKategoriId(int kategoriId) { this.kategoriId = kategoriId; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }

    public double getNominal() { return nominal; }
    public void setNominal(double nominal) { this.nominal = nominal; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getTanggalTransaksi() { return tanggalTransaksi; }
    public void setTanggalTransaksi(String tanggalTransaksi) { this.tanggalTransaksi = tanggalTransaksi; }
}
