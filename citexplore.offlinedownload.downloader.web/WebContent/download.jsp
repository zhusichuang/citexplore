<%@page import="citexplore.offlinedownload.downloader.Downloader"%>
<%@page import="com.fasterxml.jackson.databind.JsonNode"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!-- @author: Zhu, Sichuang -->
<!-- @author: Zhang, Yin -->
<%
	String json = request.getParameter("command");
	out.clear();
 	String url;
 	
	try {
		JsonNode jsonNode = new ObjectMapper().readTree(json);
		String command = jsonNode.get("command").asText();
		url = jsonNode.get("url").asText();
		
		if(!command.equals("download")){
			throw new Exception();
		}
		
		if(url.equals("")){
			throw new Exception();
		}
	} catch (Exception e) {
		out.print("{\"response\":\"refused\"}");
		out.flush();
		return;
	}
	
	Downloader.instance.download(url);
	out.print("{\"response\":\"accepted\"}");
	out.flush();
	return;
%>