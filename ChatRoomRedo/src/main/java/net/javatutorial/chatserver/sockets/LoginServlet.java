/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javatutorial.chatserver.sockets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("Login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = -8314035702649252239L;
    
    @EJB
    private UserManagerInterface userManager;
    
    private User user;
    
    protected HttpServletRequest request;
    protected HttpServletResponse response;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	doPost(req, resp);
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	this.response = resp;
    	this.request = req;
    	
    	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        resp.setHeader("Connection", "Keep-Alive");
        
        String loginButton="Log in";
        String signUpButton="Sign up";
        
        String buttonPressed=req.getParameter("button");
        
        String username = req.getParameter("Username");
        String password = req.getParameter("Password");
        
        if(buttonPressed==null){
            try {
                RequestDispatcher view = request.getRequestDispatcher("Login.xhtml");
                view.forward(req, resp);	            
            } finally {
                if (out != null) {
                    out.close();
                }        	
            }
        }
        
        else if (buttonPressed.equals(loginButton)){
        
	        user = (User) userManager.checkUserDetails(username, password);
	        if (user==null){
	            try {
	                req.setAttribute("styles", "Not a valid login");                  
	                RequestDispatcher view = request.getRequestDispatcher("Login.xhtml");
	                view.forward(req, resp);
	            } finally {
	                if (out != null) {
	                    out.close();
	                }
	            }
	        }
	        else{
	            try {
	                req.setAttribute("styles", username);                  
	                RequestDispatcher view = request.getRequestDispatcher("chatroom.xhtml");
	                view.forward(req, resp);
	            } finally {
	                if (out != null) {
	                    out.close();
	                }
	            }
	            
	        }
        }
        
        else if (buttonPressed.equals(signUpButton)){
	        boolean taken = userManager.checkUsernameTaken(username);
	        if (taken){
	            try {
	                req.setAttribute("styles", "Not a valid login");                  
	                RequestDispatcher view = request.getRequestDispatcher("Login.xhtml");
	                view.forward(req, resp);	            
	            } finally {
	                if (out != null) {
	                    out.close();
	                }
	              }
	            
	        }
	        else{
	            try {
	                userManager.createNewUser(username, password);
	                req.setAttribute("styles", username);                  
	                RequestDispatcher view = request.getRequestDispatcher("chatroom.xhtml");
	                view.forward(req, resp);
	                } finally {
	                if (out != null) {
	                    out.close();
	                }
	            }
	       }
       }
    }
}
