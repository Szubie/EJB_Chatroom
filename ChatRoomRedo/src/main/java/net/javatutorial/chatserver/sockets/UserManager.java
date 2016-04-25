package net.javatutorial.chatserver.sockets;

import javax.ejb.Stateless;

import org.hibernate.Query;
import org.hibernate.Session;  
import org.hibernate.SessionFactory;
import org.hibernate.mapping.List;  

@Stateless
public class UserManager implements UserManagerInterface {
	
	SessionFactory sessionFactory = HibernateUtil.getSessionFactory(); 

    public User checkUserDetails(String user, String pw) {
    	Session session = sessionFactory.openSession(); 
    	User result = (User) session.createQuery("select u from User u where u.username = '"+user+"'").uniqueResult();
    	session.close();
    	if(result==null){
    		return null;
    	}
    	
    	boolean match=false;
    	String storedPW = ""+result.getSalt()+"$"+result.getPassword();
    	try{
    		match= PasswordHasher.check(pw,""+storedPW);
    	}
    	catch(Exception e){
    		System.out.println(e.getClass());
    	}
    	if(match){
    		return result;
    	}
    	else{
    		return null;
    	}
    }
    
    public User getUser(String username){
    	Session session = sessionFactory.openSession(); 
    	User result = (User) session.createQuery("select u from User u where u.username = '"+username+"'").uniqueResult();
    	session.close();
    	if(result==null){
    		return null;
    	}
    	else{
    		return result;
    	}
    }

    public void createNewUser(String username, String password) {
    	try{
    		password = PasswordHasher.getSaltedHash(password);
    	}
    	catch(Exception e){
    		System.out.println(e.getClass());
    	}    	
    	String[] splitPWSalt = password.split("\\$");
    	password = splitPWSalt[1];
    	String salt = splitPWSalt[0];
    	
    	Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	User user = new User();
    	user.setUsername(username);
    	user.setPassword(password);
    	user.setSalt(salt);
    	session.persist(user);
    	session.getTransaction().commit();
    	session.close();
    	
    }
    
    public boolean updateUsername(String username, String newName){ 	

    	if(newName.equals("")){
    		return false;
    	}
    	Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	if(!checkUsernameTaken(newName)){
			try{
		    	User user = getUser(username);
		    	user.setUsername(newName);
		    	session.merge(user);
		    	session.getTransaction().commit();
		    	return true;
			}
			catch(Exception e){
				System.out.println("Failed to change username");
				return false;
			}
			finally{
				session.close();
			}
		}
    	return false;
    }

    public boolean checkUsernameTaken(String username) {
    	Session session = sessionFactory.openSession();
    	User result = (User) session.createQuery("select u from User u where u.username = '"+username+"'").uniqueResult();
    	session.close();
    	if(result!=null){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    public boolean addFriend(User user, User friend){
    	if(user == null || friend == null){
    		return false;
    	}
    	
    	Long userID = user.getID();
    	Long friendID = friend.getID();
		if(userID == friendID){ //Can't add yourself as a friend
			return false;
		}
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		if(isFriend(userID,friendID)==false){
			UserRelationship userRelationship = new UserRelationship(user, friend,"friend");
			session.merge(userRelationship);
			session.getTransaction().commit();
			session.close();
			return true;
		}
		session.getTransaction().rollback();
		session.close();
		return false;
    }

	public List getFriends(Long userID){
    	Session session = sessionFactory.openSession();
    	List friendList = (List) 
    			session.createQuery("select friend from UserRelationship r where r.userID = '"+userID+"'").list();
    	session.close();
    	return friendList;		
	}
    
    public boolean isFriend(Long userID, Long friendID){
       	Session session = sessionFactory.openSession();
       	UserRelationship relationship = (UserRelationship) 
       			session.createQuery("select r from UserRelationship r where r.userID = '"+userID+"' and r.friendID = '"+friendID+"'").uniqueResult();
   		session.close();
   		
   		if(relationship == null){
   			return false;
   		}
   		if(relationship.getStatus().equals("friend")){
   			return true;
   		}
   		else{
   			return false;
   		}
    }
    
    public boolean removeFriend(User user, User friend){
    	if(user == null || friend == null){
    		return false;
    	}
    	
    	Long userID = user.getID();
    	Long friendID = friend.getID();
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		if(isFriend(userID,friendID)){
			Query query = session.createQuery("delete from UserRelationship r where r.userID = '"+userID+"' and r. friendID = '"+friendID+"'");
			query.executeUpdate();
			session.getTransaction().commit();
			session.close();
			return true;
		}
		session.getTransaction().rollback();
		session.close();
		return false;    	
    }

}