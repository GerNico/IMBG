package frontend;

import templater.PageGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author v.chibrikov
 */
public class Frontend extends HttpServlet {

    private String firstName = "",lastName="";
    private double one=0,two=0;

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException {

        Map<String, Object> pageVariables = new HashMap<>();
        pageVariables.put("firstName", firstName == null ? "" :firstName );
        pageVariables.put("lastName",  firstName == null ? "" :lastName );

        response.getWriter().println(PageGenerator.getPage("index.html", pageVariables));

        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

    }

    public void doPost(HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        firstName = request.getParameter("firstName");
        lastName =  request.getParameter("lastName");


        Map<String, Object> pageVariables = new HashMap<>();
        pageVariables.put("firstName", firstName == null ? "" :firstName );
        pageVariables.put("lastName",  firstName == null ? "" :lastName );
        System.out.println("First name :"+firstName);
        System.out.println("Last name :"+lastName);

        response.getWriter().println(PageGenerator.getPage("index.html", pageVariables));
    }
}
