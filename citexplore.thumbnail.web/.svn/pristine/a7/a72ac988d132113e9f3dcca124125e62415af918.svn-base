<%@page import="citexplore.foundation.Config"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.io.OutputStream"%>
<%@page import="java.io.FileInputStream"%>
<%@page import="java.io.File"%>
<%@page import="citexplore.offlinedownload.ResourceStorage"%>
<%@page import="citexplore.offlinedownload.ResourceStatus"%>
<%@page import="citexplore.offlinedownload.Resource"%>
<%@page import="citexplore.thumbnail.ThumbnailProvider"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String url = request.getParameter("url");
	if(null == url){
		out.print("{\"response\":\"refused\"}");
		return;
	}
 	String filename = ThumbnailProvider.instance.thumbnail(url);
	if("".equals(filename)){
		filename = Config.getFolder(ThumbnailProvider.WORKING_PATH) + "blank.png";
	}
	response.setContentType("image/png");
	FileInputStream in = new FileInputStream(new File(filename));
	byte[] buffer = new byte[4096];
	int l = 0;
	OutputStream output = response.getOutputStream();
	while ((l = in.read(buffer)) != -1) {
		output.write(buffer, 0, l);
	}
	output.flush();
	in.close();
	output.close();
	out.clear();
	out = pageContext.pushBody(); 
%>