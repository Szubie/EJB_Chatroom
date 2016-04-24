package net.javatutorial.chatserver.sockets;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Id;

@Entity
@Table(name = "users")
public class User implements Serializable {

	private static final long serialVersionUID = -8314035702649252239L;


	@GenericGenerator(name="gen",strategy="increment")
	@GeneratedValue(generator="gen")
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "ID", unique=true, nullable=false, precision = 15, scale = 0)
	private Long id;

	@Column(name="username", unique = true)
	private String username;

	@Column(name = "password")
	private String password;
	
	@Column(name = "salt")
	private String salt;

	public User() {
	}
	
	public User(String username, String password){
		this.username=username;
		this.password=password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return this.username;
	}

	public void setPassword(String pw) {
		this.password = pw;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public void setSalt(String salt){
		this.salt = salt;
	}
	
	public String getSalt(){
		return this.salt;
	}
	
	public Long getID(){
		return this.id;
	}
	
	public void setId(Long newID){
		this.id=newID;
	}
}