package com.gmail.upcovino.resteventsregistry.commons;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event implements Serializable {
	private static final long serialVersionUID = -9209880685041545499L;
	public static final SimpleDateFormat DATE_SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	public static final SimpleDateFormat TIME_SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
	public static final SimpleDateFormat DATETIME_SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy-HH:mm");

	private String id; // String uniqueID = UUID.randomUUID().toString();
	private String title;
	private Date date;
	private Date startTime, endTime;
	private String description;
	private String photo;
	private String userEmail;
	private User user;

	public Event(String title, Date date, Date startTime, Date endTime, String description) {
		this.title = title;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", title=" + title + ", date=" + DATE_SIMPLE_DATE_FORMAT.format(date) + ", startTime=" + TIME_SIMPLE_DATE_FORMAT.format(startTime) + ", endTime=" + TIME_SIMPLE_DATE_FORMAT.format(endTime) + ", description=" + description + ", photo=" + photo + ", user=" + user.toString() + "]";
	}

	public Event clone() {
		Event event = new Event(title, date, startTime, endTime, description);
		event.setId(id);
		event.setPhoto(photo);
		event.setUserEmail(userEmail);

		return event;
	}
}
