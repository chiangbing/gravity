package com.hanborq.gravity.metrics.analyzer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for latency display.
 */
public class LatencyServlet extends HttpServlet {

  private LatencyAnalyzer analyzer = null;


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (analyzer == null) {
      analyzer = (LatencyAnalyzer) getServletContext()
          .getAttribute(LatencyAnalyzer.CTX_KEY);
    }

    req.setAttribute("latency", analyzer.getHistogram().toJSON());
    req.getRequestDispatcher("/latency.jsp").forward(req, resp);
  }
}
