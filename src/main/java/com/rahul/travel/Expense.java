package com.rahul.travel;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Expense {
  @Id
  private String id;
  private String tripId;
  private String title;
  private double amount;
  private String category;
  private LocalDate date;
  private String paidBy;
  @ElementCollection
  private List<String> sharedWith;

  public Expense() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  public List<String> getSharedWith() {
    return sharedWith;
  }

  public void setSharedWith(List<String> sharedWith) {
    this.sharedWith = sharedWith;
  }
}
