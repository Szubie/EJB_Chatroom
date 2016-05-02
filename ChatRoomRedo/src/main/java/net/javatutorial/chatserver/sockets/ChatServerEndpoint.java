package net.javatutorial.chatserver.sockets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import javax.ejb.EJB;
import javax.annotation.ManagedBean;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.javatutorial.chatserver.pojos.ChatMessage;
import net.javatutorial.chatserver.pojos.ChatMessage.MessageDecoder;
import net.javatutorial.chatserver.pojos.ChatMessage.MessageEncoder;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//value = "/chat/{room}"

/*@OnOpen
public void open(final Session session, @PathParam("room") final String room) {
	log.info("session openend and bound to room: " + room);
	session.getUserProperties().put("room", room);
}*/

@ManagedBean
@ServerEndpoint(value = "/chat", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public class ChatServerEndpoint {
	private static final Set<Session> sessions = Collections
			.synchronizedSet(new HashSet<Session>());
			
	@EJB
    private UserManagerInterface userManager;

	@OnOpen
	public void onOpen(Session session) {
		//Expecting a "userID" type message to return and initialise the user.
	}

	@OnClose
	public void onClose(Session session) 
			throws IOException, EncodeException {
		sessions.remove(session);
		updateUserList();
	}

	@OnMessage
	public void onMessage(ChatMessage message, Session client)
			throws IOException, EncodeException {
		
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timestamp = sdf.format(cal.getTime());
        message.setTimestamp(timestamp);
        
		if(message.getType().equals("userID")){
			initialiseUser(client, message);
		}
		else if(message.getType().equals("globalChat")){
			globalChat(client, message);
		}
		else if(message.getType().equals("addFriend")){
			addFriend(client, message);
		}
		else if(message.getType().equals("removeFriend")){
			removeFriend(client, message);
		}
		else if(message.getType().equals("updatePrivacy")){
			updatePrivacy(client, message);
		}
		else if(message.getType().equals("friendFilter")){
			friendFilter(client, message);
		}
		else if(message.getType().equals("updateUsername")){
			updateUsername(client, message);
		}
		else{
			System.out.println("Unknown message type received");
		}
	}
	/*
	public void timedMessage(Session session){
		
	}
	*/
	
	public void initialiseUser(Session client, ChatMessage message)
			throws IOException, EncodeException {
		String username = message.getUsername();
		client.getUserProperties().put("Username", username);
		client.getUserProperties().put("Privacy", "All"); 	//Default to visible to all
		client.getUserProperties().put("ChatRoom", "Room0"); //Default chat room
		User user = userManager.getUser(username);
		Long userID = user.getID();
		client.getUserProperties().put("ID", userID);
		client.getUserProperties().put("FriendFilter", "Off");
		sessions.add(client);
		updateUserList();
		ChatMessage response = new ChatMessage("Server", "Welcome to the chat room! Add friends to see each other in 'friends only' privacy mode and to send private messages to each other using '@[user] [message]'.", "globalChat");
		client.getBasicRemote().sendObject(response);
	}
	
	public void globalChat(Session client, ChatMessage message)
			throws IOException, EncodeException {
		String messageText = message.getMessage();		
		if(messageText.equals("")){
			//Do nothing
		}
		else if(messageText.charAt(0) == '@'){
			processPrivateMessage(client, message);
		}
		else{
			for (Session session : sessions) {
				session.getBasicRemote().sendObject(message);
			}		
		}
	}
	
	public void processPrivateMessage(Session client, ChatMessage message)
			throws IOException, EncodeException {
		String thisUser = message.getUsername();
		String messageText = message.getMessage();
		String[] splitText = messageText.split(" ");
		String targetUser = splitText[0].substring(1, splitText[0].length());
		Long userID = (Long) client.getUserProperties().get("ID");
		Long friendID = userManager.getUser(targetUser).getID();
		
		if(thisUser.equals(targetUser)){
			for (Session session : sessions) {
				if(session.getUserProperties().get("Username").equals(thisUser)){
					session.getBasicRemote().sendObject(message);
				}
			}
		}
		else if(!(userManager.isFriend(userID, friendID) && userManager.isFriend(friendID, userID))){
			//client.getBasicRemote().sendObject(message);
			for (Session session : sessions) {
				if(session.getUserProperties().get("Username").equals(thisUser)){
					session.getBasicRemote().sendObject(message);
				}
			}
			ChatMessage response = new ChatMessage("Server", "You aren't friends with that user, or user does not exist.", "globalChat", message.getTimestamp());
			//client.getBasicRemote().sendObject(response);
			for (Session session : sessions) {
				if(session.getUserProperties().get("Username").equals(thisUser)){
					session.getBasicRemote().sendObject(response);
				}
			}
		}
		else{
			boolean found = false;
			for (Session session : sessions) {
				if(session.getUserProperties().get("Username").equals(message.getUsername())){
					session.getBasicRemote().sendObject(message);
				}
				if(session.getUserProperties().get("Username").equals(targetUser)){
					session.getBasicRemote().sendObject(message);
					found = true;
				}
			}
			if(found == false){
				message.setUsername("Server");
				message.setMessage("Could not find that user online.");
				client.getBasicRemote().sendObject(message);
			}
		}
	}
	
	public void addFriend(Session client, ChatMessage message)
			throws IOException, EncodeException {
		
		boolean success = false;
		String user = message.getUsername();
		String friend = message.getMessage();
		User userInstance = userManager.getUser(user);
		User friendInstance = userManager.getUser(friend);

		try{
			success = userManager.addFriend(userInstance, friendInstance);
		}
		catch(NullPointerException e){
			System.out.println("Couldn't find EJB");
		}
		
		if(success){
			message.setType("globalChat");
			message.setUsername("Server");
			message.setMessage(message.getMessage()+" has been sent a friend request.");
			//Notify the other person if they are online.
			ChatMessage response = null;
			Long userID = userInstance.getID();
			Long friendID = friendInstance.getID();
			if(userManager.isFriend(friendID, userID)){
				if(userManager.isFriend(userID,friendID)){
					message = new ChatMessage("Server", "You are now friends with "+user+"." ,"globalChat");
					response = new ChatMessage("Server", "You are now friends with "+user+"." ,"globalChat", message.getTimestamp());
				}
				else{
					response = new ChatMessage("Server", ""+user+" has accepted your friend request." ,"globalChat", message.getTimestamp());
				}
			}
			else{
				response = new ChatMessage("Server", ""+user+" has asked to be friends." ,"globalChat", message.getTimestamp());
			}
			//client.getBasicRemote().sendObject(message);
			for(Session session : sessions){
				if(session.getUserProperties().get("Username").equals(friend)){
					session.getBasicRemote().sendObject(response);
				}
				if(session.getUserProperties().get("Username").equals(user)){
					session.getBasicRemote().sendObject(message);
				}
			}
			
		}
		else{
			message.setType("globalChat");
			message.setUsername("Server");
			message.setMessage(message.getMessage()+" could not be added to your friends list.");
			//client.getBasicRemote().sendObject(message);
			for(Session session : sessions){
				if(session.getUserProperties().get("Username").equals(user)){
					session.getBasicRemote().sendObject(message);
				}
			}
		}
		updateUserList();
	}
	
	public void removeFriend(Session client, ChatMessage message)
			throws IOException, EncodeException {
		boolean success = false;
		String user = message.getUsername();
		String friend = message.getMessage();
		User userInstance = userManager.getUser(user);
		User friendInstance = userManager.getUser(friend);

		try{
			success = userManager.removeFriend(userInstance, friendInstance);
		}
		catch(NullPointerException e){
			System.out.println("Couldn't find EJB");
		}
		
		if(success){
			message.setType("globalChat");
			message.setUsername("Server");
			message.setMessage(message.getMessage()+" has been removed from your friends list.");
			for(Session session : sessions){
				if(session.getUserProperties().get("Username").equals(user)){
					session.getBasicRemote().sendObject(message);
				}
			}
		}
		else{
			message.setType("globalChat");
			message.setUsername("Server");
			message.setMessage(message.getMessage()+" could not be removed from your friends list.");
			for(Session session : sessions){
				if(session.getUserProperties().get("Username").equals(user)){
					session.getBasicRemote().sendObject(message);
				}
			}
		}
		updateUserList();		
	}
	
	public void updatePrivacy(Session client, ChatMessage message)
			throws IOException, EncodeException {
		String setting = message.getMessage();
		//client.getUserProperties().put("Privacy", setting);
		//Decided to make updates to privacy for a user on one browser apply to all. Avoids frustration.
		String username = ""+client.getUserProperties().get("Username");
		for(Session session : sessions){
			if(session.getUserProperties().get("Username").equals(username)){
				session.getUserProperties().put("Privacy", setting);
			}
		}
		updateUserList();
	}
	
	public void friendFilter(Session client, ChatMessage message)
			throws IOException, EncodeException {
		String userList = "";
		Long userID = (Long) client.getUserProperties().get("ID");
		
		if(message.getMessage().equals("true")){
			for(Session session : sessions){
				Long friendID = (Long) session.getUserProperties().get("ID");
				String thisUser = "" + session.getUserProperties().get("Username");
				if(userManager.isFriend(userID, friendID) && userManager.isFriend(friendID, userID)){
					userList+=thisUser+" ";
				}
			}
			userList = sortList(userList);
			userList = removeDuplicates(userList);
			message.setUsername("Server");
			message.setMessage(userList);
			message.setType("userList");
			client.getUserProperties().put("FriendFilter", "On");
			client.getBasicRemote().sendObject(message);
		}
		else{
			for(Session session : sessions){
				String thisUser = ""+ session.getUserProperties().get("Username");
				userList+=thisUser+" ";
			}
			userList = sortList(userList);
			userList = removeDuplicates(userList);
			message.setUsername("Server");
			message.setMessage(userList);
			message.setType("userList");
			client.getUserProperties().put("FriendFilter", "Off");
			client.getBasicRemote().sendObject(message);
		}
	}
	
	public void updateUserList()
			throws IOException, EncodeException{
		//Build a personalised online user list for each user
		for(Session session : sessions){
			Long userID = (Long) session.getUserProperties().get("ID");
			String userList = "";
			if(session.getUserProperties().get("FriendFilter").equals("Off")){
				for(Session otherSession : sessions){
					String otherUser = "" + otherSession.getUserProperties().get("Username");
					Long otherUserID = (Long) otherSession.getUserProperties().get("ID");
					if(otherSession.getUserProperties().get("Privacy").equals("All")){
						userList+=otherUser+" ";
					}
					else if(otherSession.getUserProperties().get("Privacy").equals("Friends only")){
						if(userManager.isFriend(userID, otherUserID) && userManager.isFriend(otherUserID, userID)){
							userList+=otherUser+" ";
						}
						//Should see yourself when friends only enabled. But not when invisible.
						//Or maybe not actually, you can see friends. Use that instead?
						if(session == otherSession){ 
							userList+=otherUser+" ";
						}
					}
				}
			}
			else{
				for(Session otherSession : sessions){
					String otherUser = "" + otherSession.getUserProperties().get("Username");
					Long otherUserID = (Long) otherSession.getUserProperties().get("ID");
					if(otherSession.getUserProperties().get("Privacy").equals("Friends only") || 
							otherSession.getUserProperties().get("Privacy").equals("All")){
						if(userManager.isFriend(userID, otherUserID) && userManager.isFriend(otherUserID, userID)){
							userList+=otherUser+" ";
						}
						//Shouldn't see yourself when filtering for friends.
					}
				}				
			}
			userList = sortList(userList);
			userList = removeDuplicates(userList);
			ChatMessage message = new ChatMessage("Server", userList, "userList");
			try{
				session.getBasicRemote().sendObject(message);
			}
			catch(Exception e){
				System.out.println("Couldn't send userList");
			}
		}
	}
	
	public String sortList(String userList){
		String[] splitList = userList.split(" ");
		Arrays.sort(splitList);
		userList = "";
		for(String string : splitList){
			userList+=string+" ";
		}
		return userList;
	}
	
	public String removeDuplicates(String userList){
		String[] splitList = userList.split(" ");
		for(int i=0; i<splitList.length; i++){
			for(int j=i+1; j<splitList.length; j++){
				if(splitList[i].equals(splitList[j])){
					splitList[j]="";
				}
			}
		}
		userList = "";
		for(String string : splitList){
			if(!(string.equals(""))){
				userList+=string+" ";
			}
		}
		return userList;		
	}
	
	public void updateUsername(Session client, ChatMessage message)
			throws IOException, EncodeException {
		String oldUsername = (String) client.getUserProperties().get("Username");
		String newUsername = message.getMessage();
		boolean success = false;
		success = userManager.updateUsername(oldUsername, newUsername);
		if(success){
			for(Session session : sessions){
				if(session.getUserProperties().get("Username").equals(oldUsername)){
					session.getUserProperties().put("Username", newUsername);
				}
			}
			message.setType("updateUsername");
			message.setMessage(newUsername);
			client.getBasicRemote().sendObject(message);
			ChatMessage response = new ChatMessage("Server", "Updated your username.", "globalChat", message.getTimestamp());
			client.getBasicRemote().sendObject(response);
			updateUserList();
		}
		else{
			ChatMessage response = new ChatMessage("Server", "Could not update your username: someone else is using that username.", "globalChat", message.getTimestamp());
			client.getBasicRemote().sendObject(response);
			message.setType("updateUsername");
			message.setMessage(oldUsername);
			client.getBasicRemote().sendObject(message);
		}
	}
}
