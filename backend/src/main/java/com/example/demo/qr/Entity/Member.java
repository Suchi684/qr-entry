package com.example.demo.qr.Entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendees")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "entry_scanned", nullable = false)
    private boolean entryScanned;

    @Column(name = "food_scanned", nullable = false)
    private boolean foodScanned;

    @Column(name = "gift_scanned", nullable = false)
    private boolean giftScanned;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEntryScanned() {
        return entryScanned;
    }

    public void setEntryScanned(boolean entryScanned) {
        this.entryScanned = entryScanned;
    }

    public boolean isFoodScanned() {
        return foodScanned;
    }

    public void setFoodScanned(boolean foodScanned) {
        this.foodScanned = foodScanned;
    }

    public boolean isGiftScanned() {
        return giftScanned;
    }

    public void setGiftScanned(boolean giftScanned) {
        this.giftScanned = giftScanned;
    }
}
