package net.javatutorial.chatserver.sockets;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Table(name = "UserRelationship")
public class UserRelationship implements Serializable {

	private static final long serialVersionUID = -8314035702649252339L;

    @ManyToOne
    @JoinColumn (name="userID", referencedColumnName="ID")
	private User userID;

    @ManyToOne
    @JoinColumn(name="friendID", referencedColumnName="ID")
	private User friendID;

	@Column(name = "status")
	private String status;
	
	@Id
	@Column(name="userrelationshipcol")
	private int userrelationshipcol;


	public UserRelationship() {
	}
	
	public UserRelationship(User userID, User friendID, String status){
		this.userID=userID;
		this.friendID=friendID;
		this.status=status;
	}
	
	public User getUser(){
		return this.userID;
	}
	
	public void setUser(User user){
		this.userID = user;
	}
	
	public User getFriend(){
		return this.friendID;
	}
	
	public void setFriend(User friend){
		this.friendID=friend;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}
    
    
}