package com.example.firebase.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "theaters")
public class Theater {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "location")
    private String location; // Quận/huyện

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "total_screens")
    private int totalScreens; // Số phòng chiếu

    public Theater() {}

    public Theater(String name, String location, String address, String phone, int totalScreens) {
        this.name = name;
        this.location = location;
        this.address = address;
        this.phone = phone;
        this.totalScreens = totalScreens;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getTotalScreens() { return totalScreens; }
    public void setTotalScreens(int totalScreens) { this.totalScreens = totalScreens; }
}
