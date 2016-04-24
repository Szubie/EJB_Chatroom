package net.javatutorial.chatserver.sockets;
import javax.ejb.Remote;

import org.hibernate.mapping.List;


@Remote(UserManager.class)
public interface UserManagerInterface {
	public User checkUserDetails(String user, String pw);
	public User getUser(String username);
	public void createNewUser(String username, String password);
	public boolean checkUsernameTaken(String username);
	//public boolean addFriend(String friend, String username);
	//public String getFriends(String username);
	//public boolean isFriend(User user, String friend);
	//public boolean isFriend(String username, String friend);
	
	public boolean addFriend(User user, User friend);
	public List getFriends(Long userID);
	public boolean isFriend(Long userID, Long friendID);
	
}