package com.gmail.upcovino.resteventsregistry.commons;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event implements Serializable {
	private static final long serialVersionUID = -9209880685041545499L;
	public static final SimpleDateFormat DATE_SDF = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat TIME_SDF = new SimpleDateFormat("HH:mm");
	public static final SimpleDateFormat DATETIME_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Integer id;			// è facoltativo: non sempre ce lo hai quando crei quest'oggetto (l'id sta nel DB)
	private String title;
	private Date startDate,
				 endDate;
	private String description;
	private String photoPath;	// è facoltativo: l'evento potrebbe non avere una foto
	private User owner;			// è facoltativo: non sempre serve associare l'intero oggetto User all'evento
	private String ownerEmail;	// è facoltativo: non sempre ce lo hai quando crei quest'oggetto (sta nel DB)
	
	
	
	public Event(String title, Date startDate, Date endDate, String description) {
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.description = description;
		this.id = null;
		this.owner = null;
		this.ownerEmail = null;
	}

	public Event(String title, Date startDate, Date endDate, String description, String photoPath) {
		this(title, startDate, endDate, description);
		this.photoPath = photoPath;
	}

	public Event(String title, Date startDate, Date endDate, String description, String photoPath, String ownerEmail) {
		this(title, startDate, endDate, description, photoPath);
		this.ownerEmail = ownerEmail;
	}
	
	public Event(int id, String title, Date startDate, Date endDate, String description) {
		this(title, startDate, endDate, description);
		this.id = id;
	}

	public Event(int id, String title, Date startDate, Date endDate, String description, String photoPath) {
		this(id, title, startDate, endDate, description);
		this.photoPath = photoPath;
	}

	public Event(int id, String title, Date startDate, Date endDate, String description, String photoPath, String ownerEmail) {
		this(id, title, startDate, endDate, description, photoPath);
		this.ownerEmail = ownerEmail;
	}

	public Event(int id, String title, Date startDate, Date endDate, String description, String photoPath, String ownerEmail, User owner) {
		this(id, title, startDate, endDate, description, photoPath, ownerEmail);
		this.owner = owner;
	}

	public Integer getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getFormattedStartDate() {
		return DATETIME_SDF.format(startDate);
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getFormattedEndDate() {
		return DATETIME_SDF.format(endDate);
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", title=" + title + ", startDate=" + getFormattedStartDate() + ", endDate=" + getFormattedEndDate() + ", description=" + description + ", photoPath=" + photoPath + ", owner=" + owner + "]";
	}

	public Event clone() {
		Event event = new Event(title, startDate, endDate, description);

		return event;
	}
}
