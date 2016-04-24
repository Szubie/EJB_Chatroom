package net.javatutorial.chatserver.pojos;

import java.io.StringReader;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class ChatMessage {

	public static class MessageEncoder implements Encoder.Text<ChatMessage> {
		@Override
		public void init(EndpointConfig config) {
		}

		@Override
		public String encode(ChatMessage message) throws EncodeException {
			return Json.createObjectBuilder()
					.add("username", message.getUsername())
					.add("type", message.getType())
					.add("message", message.getMessage()).build().toString();					
		}

		@Override
		public void destroy() {
		}
	}

	public static class MessageDecoder implements Decoder.Text<ChatMessage> {
		private JsonReaderFactory factory = Json
				.createReaderFactory(Collections.<String, Object> emptyMap());

		@Override
		public void init(EndpointConfig config) {
		}

		@Override
		public ChatMessage decode(String str) throws DecodeException {
			ChatMessage message = new ChatMessage();

			JsonReader reader = factory.createReader(new StringReader(str));
			JsonObject json = reader.readObject();
			message.setUsername(json.getString("username"));
			message.setMessage(json.getString("message"));
			message.setType(json.getString("type"));

			return message;
		}

		@Override
		public boolean willDecode(String str) {
			return true;
		}

		@Override
		public void destroy() {
		}
	}

	private String username;
	private String message;
	private String type;

	public ChatMessage() {
	}

	public ChatMessage(String username, String message) {
		super();
		this.username = username;
		this.message = message;
	}
	
	public ChatMessage(String username, String message, String type){
		super();
		this.username = username;
		this.message = message;
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}

}
