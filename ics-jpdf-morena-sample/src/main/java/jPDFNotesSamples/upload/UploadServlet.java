package jPDFNotesSamples.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadServlet extends HttpServlet
{
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        // Initialize response
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart == false)
        {
            echoMessage ("POST is not a Multipart request", out);
            return;
        }
        
        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(65536);
        factory.setRepository(new File("c:\\temp"));
        
        Enumeration paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()) 
        {
            String paramName = (String)paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            System.out.println (paramValues);
        }


        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request /* FileItem */
        try
        {
            // Process the uploaded items
            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();

            while (iter.hasNext()) 
            {
                FileItem item = (FileItem) iter.next();
                if (item.isFormField() == false)
                {
                    String fileName = item.getName();

                    File uploadedFile = new File("c:\\tmp\\" + fileName);
                    item.write(uploadedFile);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            echoMessage("Error: " + e.getMessage(), out);
            e.printStackTrace();
        }
        
        // Close the response writer
        out.close();
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Error</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>This servlet only handles file uploads!</h1>");
        out.println("</body>");
        out.println("</html>");
            
        out.close();     
    }
    
    private void echoMessage (String message, PrintWriter out) throws IOException
    {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>" + message + "</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body>");
        out.println("</html>");
    }
}