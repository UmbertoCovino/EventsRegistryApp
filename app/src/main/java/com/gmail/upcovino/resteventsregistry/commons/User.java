package com.gmail.upcovino.resteventsregistry.commons;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
	private static final long serialVersionUID = -2926305862149034310L;
	private String name, surname, email, password, photo;
	private ArrayList<Event> events;

	public User(String name, String surname, String email, String password) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
		events = new ArrayList<Event>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<Event> events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", surname=" + surname + ", email=" + email + ", password=" + password + ", photo=" + photo + "]";
	}

	/*	Questo metodo serve nei seguenti metodi delle seguenti classi per evitare che il client riceva oggetti User contenenti
     * 	le password valide per accedere al sistema:
     * 		EventsRegistryJSON		getEvents()
     * 		EventJSON				getEvent()
     * 		EventUserJSON			getUser()
     *
     * 		UsersRegistryJSON		getUsers()
     * 		UserJSON				getUser()
     * 		UserEventsJSON			getEvents()		qui per ora non l'ho aggiunto
     */
	public User cloneWithoutPassword() {
		User user = new User(name, surname, email, "");
		user.setPhoto(photo);
		user.setEvents(events);

		return user;
	}
}
