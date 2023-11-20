package com.suresh.rsr.model;

public class UserLeads {

    private String id;
    private String date;
    private String jobType;
    private String description;
    private String image;
    private String name;
    private String email;
    private String phone;
    private String reviewMsg;

    public UserLeads() {
    }

    public UserLeads(String id, String date, String jobType, String description, String image, String name, String email, String phone, String reviewMsg) {
        this.id = id;
        this.date = date;
        this.jobType = jobType;
        this.description = description;
        this.image = image;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.reviewMsg = reviewMsg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getReviewMsg() {
        return reviewMsg;
    }

    public void setReviewMsg(String reviewMsg) {
        this.reviewMsg = reviewMsg;
    }
}
