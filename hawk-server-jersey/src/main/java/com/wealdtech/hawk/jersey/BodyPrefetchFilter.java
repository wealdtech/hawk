package com.wealdtech.hawk.jersey;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Prefetch and store the full body of the request so that it can be read by
 * subsequent filters without upsetting message body readers.
 * <p/>Required if authentication systems, logging filters, or anything else
 * wnats access to the body of the request.
 */
public class BodyPrefetchFilter implements Filter
{
  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
    // TODO details of which content types should be read
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
    HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
    BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpServletRequest);
    chain.doFilter(bufferedRequest, httpServletResponse);
  }

  @Override
  public void destroy()
  {
    // TODO
  }

  private static final class BufferedRequestWrapper extends HttpServletRequestWrapper
  {
    private ByteArrayInputStream bais = null;
    private ByteArrayOutputStream baos = null;
    private BufferedServletInputStream bsis = null;
    private byte[] buffer = null;

    public BufferedRequestWrapper(HttpServletRequest req) throws IOException
    {
      super(req);
      // Read InputStream and store its content in a buffer
      InputStream is = req.getInputStream();
      this.baos = new ByteArrayOutputStream();
      byte buf[] = new byte[1024];
      int letti;

      while ((letti = is.read(buf)) > 0)
      {
        this.baos.write(buf, 0, letti);
      }
      this.buffer = this.baos.toByteArray();
    }

    @Override
    public ServletInputStream getInputStream()
    {
      // Generate a new InputStream by stored buffer
      this.bais = new ByteArrayInputStream(this.buffer);
      // Instantiate a subclass of ServletInputStream
      // (Only ServletInputStream or subclasses of it are accepted by
      // the servlet engine!)
      this.bsis = new BufferedServletInputStream(this.bais);
      return this.bsis;
    }

    String getRequestBody() throws IOException
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()));
      String line = null;
      StringBuilder inputBuffer = new StringBuilder();
      do
      {
        line = reader.readLine();
        if (null != line)
        {
          inputBuffer.append(line.trim());
        }
      }
      while (line != null);
      reader.close();

      return inputBuffer.toString().trim();
    }
  }

  /*
   * Subclass of ServletInputStream needed by the servlet engine. All
   * inputStream methods are wrapped and are delegated to the
   * ByteArrayInputStream (obtained as constructor parameter)!
   */
  private static final class BufferedServletInputStream extends ServletInputStream
  {
    private final ByteArrayInputStream bais;

    public BufferedServletInputStream(ByteArrayInputStream bais)
    {
      this.bais = bais;
    }

    @Override
    public int available()
    {
      return this.bais.available();
    }

    @Override
    public int read()
    {
      return this.bais.read();
    }

    @Override
    public int read(byte[] buf, int off, int len)
    {
      return this.bais.read(buf, off, len);
    }
  }
}
