<%-- 
    Document   : uploadFile
    Created on : 17-jun-2015, 9:42:09
    Author     : Linda
--%>
<%@page import="org.apache.commons.io.FilenameUtils"%>
<%@page import="org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="java.io.File"%>
<%@page import="Shared.Data.INewsItem"%>
<%@page import="Controller.webController"%>

<jsp:useBean id="fileUpload" class="HelpClasses.HelpFile" scope="session" />
<jsp:setProperty name="fileUpload" property="*" />
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>File Uploaden</title>
    </head>
    <body>
        <%
            webController controller = new webController();

            String ID = request.getParameter("newsid");
            INewsItem item = null;
            try {
                item = controller.getNewsWithID(ID);
            } catch (Exception ex) {
                item = null;
            }

            File file;
            int maxFileSize = 50000 * 1024;
            int maxMemSize = 50000 * 1024;

            // Verify the content type
            String contentType = request.getContentType();
            if ((contentType.indexOf("multipart/form-data") >= 0)) {

                DiskFileItemFactory factory = new DiskFileItemFactory();
                // maximum size that will be stored in memory
                factory.setSizeThreshold(maxMemSize);
                // Location to save data that is larger than maxMemSize.
                factory.setRepository(new File("c:\\"));

                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload(factory);
                // maximum file size to be uploaded.
                upload.setSizeMax(maxFileSize);
                try {
                    // Parse the request to get file items.
                    List fileItems = upload.parseRequest(request);

                    // Process the uploaded file items
                    Iterator i = fileItems.iterator();

                    while (i.hasNext()) {
                        FileItem fi = (FileItem) i.next();
                        if (!fi.isFormField()) {
                            // Get the uploaded file parameters
                            String fieldName = fi.getFieldName();
                            String fileName = fi.getName();
                            boolean isInMemory = fi.isInMemory();
                            long sizeInBytes = fi.getSize();
                            // Write the file
                            if (fileName.lastIndexOf("\\") >= 0) {
                                file = new File(fileName.substring(fileName
                                        .lastIndexOf("\\")));
                            } else {
                                file = new File(fileName.substring(fileName
                                        .lastIndexOf("\\") + 1));
                            }
                            if (sizeInBytes < maxFileSize) {
                                String ext = FilenameUtils.getExtension(fileName)
                                        .toLowerCase();
                                System.out.println("Extentie = " + ext);
                                if (ext.equals("jpg") || ext.equals("png")
                                        || ext.equals("jpeg") || ext.equals("jfif")
                                        || ext.equals("exif") || ext.equals("tiff")
                                        || ext.equals("rif") || ext.equals("gif")
                                        || ext.equals("ppm") || ext.equals("pgm")
                                        || ext.equals("pbm") || ext.equals("pnm")
                                        || ext.equals("webp") || ext.equals("bpg")) {
                                    fi.write(file);
                                    boolean succeed = controller.sendPhoto(file
                                            .getPath(), item);
                                    if (succeed) {
                                        response.sendRedirect("news.jsp?newsid="
                                                + item.getId());
                                    } else {
                                        session.setAttribute("Error", "Versturen "
                                                + "van de foto is mislukt");
                                        response.sendRedirect("news.jsp?newsid="
                                                + item.getId());
                                    }
                                } else {
                                    session.setAttribute("Error", "Versturen van"
                                            + " het bestand is mislukt."
                                            + " Onjuiste extentie.");
                                    response.sendRedirect("news.jsp?newsid="
                                            + item.getId());
                                }
                            }else {
                                    session.setAttribute("Error", "Versturen van"
                                            + " het bestand is mislukt."
                                            + " Bestand is te groot.");
                                    response.sendRedirect("news.jsp?newsid="
                                            + item.getId());
                                }
                        }
                    }
                } catch (SizeLimitExceededException sleEx) {
                    session.setAttribute("Error", "Bestand te groot: "
                            + sleEx.getMessage());
                    response.sendRedirect("news.jsp?newsid=" + item.getId());
                } catch (Exception ex) {
                    session.setAttribute("Error", ex.getMessage());
                    response.sendRedirect("news.jsp?newsid=" + item.getId());
                }
            } else {
                out.println("<p>No file uploaded</p>");
            }
        %>
    </body>
</html>
