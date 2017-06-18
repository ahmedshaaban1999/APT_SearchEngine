

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ViewServlet
 */
@WebServlet("/home")
public class ViewServlet extends HttpServlet implements ServletContextListener {
	
	private static final long serialVersionUID = 4782356487L;
    private Crawler crawler;
    private FrontBack ftback;
  
    public ViewServlet() {
        super();
        crawler = new Crawler("https://en.wikipedia.org/wiki/Main_Page", 5000, new String[] {"mode1"});
        ftback = new FrontBack(crawler.getCrawledList(),crawler.getLexion());
        crawler.actionSearch();
        System.out.println("Inatialized");
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		System.out.println("request received");
		String[] result = ftback.processQuery(request.getParameter("searchtext1").split("\\s"), "normal");
		System.out.println("result at servlet is "+result[0]+" and "+result[1]);
		ServletContext sc = this.getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher("/results.jsp");
		request.setAttribute("list", result);
		//request.setAttribute("length", result.length);
		rd.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		crawler.finalize();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("Crawler started");
	}

}
