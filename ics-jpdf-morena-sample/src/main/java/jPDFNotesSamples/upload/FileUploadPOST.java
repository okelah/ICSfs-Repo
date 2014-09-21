/*
 * Created on Jan 22, 2008
 *
 */
package jPDFNotesSamples.upload;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocketFactory;
import javax.swing.JOptionPane;

import com.qoppa.pdf.PDFException;
import com.qoppa.pdfNotes.PDFNotesBean;

public class FileUploadPOST
{
    private final static int CHUNKBUF_SIZE = 4096;
    private final byte chunkbuf[] = new byte[CHUNKBUF_SIZE];

    private String boundary = "-----------------------------" + getRandomString();
    protected String mimeType = "application/pdf";
    
    private final static Pattern pHttpStatus = Pattern.compile("^HTTP/\\d\\.\\d\\s+((\\d+)\\s+.*)$");
    private final static Pattern pClose = Pattern.compile("^Connection:\\s+close", Pattern.CASE_INSENSITIVE);
    private final static Pattern pProxyClose = Pattern.compile("^Proxy-Connection:\\s+close", Pattern.CASE_INSENSITIVE);
    private final static Pattern pContentLen = Pattern.compile("^Content-Length:\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);
    private final static Pattern pChunked = Pattern.compile("^Transfer-Encoding:\\s+chunked", Pattern.CASE_INSENSITIVE);
    private final static Pattern pContentTypeCs = Pattern.compile("^Content-Type:\\s+.*;\\s*charset=([^;\\s]+).*$", Pattern.CASE_INSENSITIVE);
    private final static Pattern pSetCookie = Pattern.compile("^Set-Cookie:\\s+(.*)$", Pattern.CASE_INSENSITIVE);

    private CookieJar cookies = new CookieJar();
    
    private final static String DUMMYMD5 = "DUMMYMD5DUMMYMD5DUMMYMD5DUMMYMD5";

    public boolean upload (PDFNotesBean notesBean, String docName, URL saveURL)
    {
        try
        {
            // Get the contents of the PDF document
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream ();
            notesBean.saveDocument (pdfStream);
            byte [] pdfContent = pdfStream.toByteArray();
            int contentLength = pdfContent.length;

            // Get the File header
            String fileHeader = getFileHeader(docName);
            contentLength += fileHeader.length();
            
            // Get the file tail
            String fileTail = getFileTail();
            contentLength += fileTail.length();

            // Create the HTTP header
            StringBuffer httpHeader = new StringBuffer();
            httpHeader.setLength(0);
            httpHeader.append("POST ");
            httpHeader.append(saveURL.getPath());
            
            httpHeader.append(" HTTP/1.1\r\n");
            
            // Header: General
            httpHeader.append("Host: " + saveURL.getHost() + "\r\n");
            httpHeader.append("Accept: */*\r\n");
    
            // We do not want gzipped or compressed responses, so we must
            // specify that here (RFC 2616, Section 14.3)
            httpHeader.append("Accept-Encoding: identity\r\n");
            httpHeader.append("Connection: close\r\n");
            
            httpHeader.append("Content-Type: multipart/form-data; boundary=");
            httpHeader.append(this.boundary.substring(2)).append("\r\n");
            httpHeader.append("Content-Length: ").append(contentLength).append("\r\n");
            
            // Blank line (end of header)
            httpHeader.append("\r\n");
            
            // Create socket to the server
            Socket sock;
            if (saveURL.getProtocol() != null && saveURL.getProtocol().toLowerCase().startsWith("https"))
            {
                int port = (-1 == saveURL.getPort()) ? 443 : saveURL.getPort();
                sock = SSLSocketFactory.getDefault().createSocket(saveURL.getHost(), port);
                echoDebug ("Created SSL socket to " + saveURL);
            }
            else
            {
                int port = (-1 == saveURL.getPort()) ? 80 : saveURL.getPort();
                sock = new Socket(saveURL.getHost(), port);
                echoDebug ("Created standard socket to " + saveURL);
            }
            DataOutputStream httpDataOut = new DataOutputStream (new BufferedOutputStream (sock.getOutputStream()));
            InputStream httpDataIn = sock.getInputStream();
            
            // Send http request to server
            httpDataOut.writeBytes(httpHeader.toString());
            echoDebug ("Wrote Header:");
            echoDebug (httpHeader.toString());
            
            httpDataOut.writeBytes(fileHeader);
            echoDebug ("\nWrote File Header:");
            echoDebug (fileHeader);
            
            // write the document
            UploadStream upStream = new UploadStream (httpDataOut);
            upStream.write(pdfContent);
            echoDebug ("Wrote the PDF file.");
            
            // File tail
            String tail = fileTail.replaceFirst(DUMMYMD5, upStream.getMD5());
            httpDataOut.writeBytes(tail);
            echoDebug("Wrote File Tail:");
            echoDebug(tail);
            
            // Flush and finish
            httpDataOut.flush();
            finishRequest(httpDataOut, httpDataIn, sock);
            echoDebug ("Flushed and closed");

            return true;
        }
        catch (IOException ioE)
        {
            JOptionPane.showMessageDialog (null, "Error saving file: " + ioE.getMessage());
            return false;
        }
        catch (PDFException pdfE)
        {
            JOptionPane.showMessageDialog(null, pdfE.getMessage());
            return false;
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog (null, "Error saving file: " + e.getMessage());
            return false;
        }
    }

    private void echoDebug (String msg)
    {
        System.out.println (msg);
    }
    
    private final String getRandomString() 
    {
        StringBuffer sbRan = new StringBuffer(11);
        String alphaNum = "1234567890abcdefghijklmnopqrstuvwxyz";
        int num;
        for (int i = 0; i < 11; i++) {
            num = (int) (Math.random() * (alphaNum.length() - 1));
            sbRan.append(alphaNum.charAt(num));
        }
        return sbRan.toString();
    }
    
    private final String getFileHeader(String uploadFilename) throws Exception 
    {
        StringBuffer sb = new StringBuffer();

        // Add any "user" variables here
        //sb.append(addPostVariable("user", "myid"));
        //sb.append(addPostVariable("pwd", "mypassword"));
        
        // Mime type
        sb.append(addPostVariable("mimetype[]", mimeType));

        // file name and info
        sb.append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"PDFFile\"; filename=\"");
        sb.append(uploadFilename);
        sb.append("\"\r\n");
        sb.append("Content-Type: ").append(mimeType).append("\r\n");

        // An empty line to finish the header.
        sb.append("\r\n");
        
        return sb.toString();
    }
    
    private String getFileTail () 
    {
        StringBuffer tail = new StringBuffer ("\r\n");
        tail.append (addPostVariable("md5sum[]", DUMMYMD5));
        tail.append (boundary);
        tail.append ("--\r\n");
        
        return tail.toString();
    }

    
    private final StringBuffer addPostVariable(String name, String value) 
    {
        StringBuffer sb = new StringBuffer();
        return sb.append(boundary).append("\r\n").append(
                "Content-Disposition: form-data; name=\"").append(name).append(
                "\"\r\nContent-Transfer-Encoding: 8bit\r\n\r\n").append(value)
                .append("\r\n");
    }
    private int finishRequest(DataOutputStream httpDataOut, InputStream httpDataIn, Socket sock) throws Exception
    {
        boolean readingHttpBody = false;
        boolean gotClose = false;
        boolean gotChunked = false;
        boolean gotContentLength = false;
        int status = 0;
        int clen = 0;
        String line = "";
        byte[] body = new byte[0];
        String charset = "ISO-8859-1";

        echoDebug ("");
        echoDebug ("Response: ");

        StringBuffer sbHttpResponseBody = new StringBuffer();
        try 
        {
            // && is evaluated from left to right so !stop must come first!
            while (((!gotContentLength) || (clen > 0))) 
            {
                if (readingHttpBody) 
                {
                    // Read the http body
                    if (gotChunked) 
                    {
                        // Read the chunk header.
                        // This is US-ASCII! (See RFC 2616, Section 2.2)
                        line = readLine(httpDataIn, "US-ASCII", false);
                        if (null == line)
                            throw new Exception("unexpected EOF");

                        // Handle a single chunk of the response
                        // We cut off possible chunk extensions and ignore them.
                        // The length is hex-encoded (RFC 2616, Section 3.6.1)
                        int len = Integer.parseInt(line.replaceFirst(";.*", "")
                                .trim(), 16);
                        if (len == 0) {
                            // RFC 2616, Section 3.6.1: A length of 0 denotes
                            // the last chunk of the body.

                            // This code wrong if the server sends chunks
                            // with trailers! (trailers are HTTP Headers that
                            // are send *after* the body. These are announced
                            // in the regular HTTP header "Trailer".
                            // Fritz: Never seen them so far ...
                            // TODO: Implement trailer-handling.
                            break;
                        }

                        // Loop over the chunk (len == length of the chunk)
                        while (len > 0) {
                            int rlen = (len > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                    : len;
                            int ofs = 0;
                            if (rlen > 0) {
                                while (ofs < rlen) {
                                    int res = httpDataIn.read( this.chunkbuf, ofs, rlen - ofs);
                                    if (res < 0)
                                        throw new Exception("unexpected EOF");
                                    len -= res;
                                    ofs += res;
                                }
                                if (ofs < rlen)
                                    throw new Exception("short read");
                                if (rlen < CHUNKBUF_SIZE)
                                    body = byteAppend(body, this.chunkbuf, rlen);
                                else
                                    body = byteAppend(body, this.chunkbuf);
                            }
                        }
                        // Got the whole chunk, read the trailing CRLF.
                        readLine(httpDataIn, false);
                    } 
                    else 
                    {
                        // Not chunked. Use either content-length (if available)
                        // or read until EOF.
                        if (gotContentLength) 
                        {
                            // Got a Content-Length. Read exactly that amount of
                            // bytes.
                            while (clen > 0) {
                                int rlen = (clen > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                        : clen;
                                int ofs = 0;
                                if (rlen > 0) {
                                    while (ofs < rlen) {
                                        int res = httpDataIn.read( this.chunkbuf, ofs, rlen - ofs);
                                        if (res < 0)
                                            throw new Exception("unexpected EOF");
                                        clen -= res;
                                        ofs += res;
                                    }
                                    if (ofs < rlen)
                                        throw new Exception("short read");
                                    if (rlen < CHUNKBUF_SIZE)
                                        body = byteAppend(body, this.chunkbuf, rlen);
                                    else
                                        body = byteAppend(body, this.chunkbuf);
                                }
                            }
                        } 
                        else 
                        {
                            // No Content-length available, read until EOF
                            // 
                            while (true) {
                                byte[] lbuf = readLine(httpDataIn, true);
                                if (null == lbuf)
                                    break;
                                body = byteAppend(body, lbuf);
                            }
                            break;
                        }
                    }
                    echoDebug (new String (body));
                } 
                else 
                {
                    // readingHttpBody is false, so we are still in headers.
                    // Headers are US-ASCII (See RFC 2616, Section 2.2)
                    String tmp = readLine(httpDataIn, "US-ASCII", false);
                    if (null == tmp)
                        throw new Exception("unexpected EOF");
                    echoDebug (tmp);
                    if (status == 0) 
                    {
                        Matcher m = pHttpStatus.matcher(tmp);
                        if (m.matches()) 
                        {
                            status = Integer.parseInt(m.group(2));
                            //responseMsg = m.group(1);
                        }
                        else {
                            // The status line must be the first line of the
                            // response. (See RFC 2616, Section 6.1) so this
                            // is an error.

                            // Then, we throw the exception.
                            throw new Exception( "HTTP response did not begin with status line.");
                        }
                    }
                    // Handle folded headers (RFC 2616, Section 2.2). This is
                    // handled after the status line, because that line may
                    // not be folded (RFC 2616, Section 6.1).
                    if (tmp.startsWith(" ") || tmp.startsWith("\t"))
                        line += " " + tmp.trim();
                    else
                        line = tmp;

                    if (pClose.matcher(line).matches())
                        gotClose = true;
                    if (pProxyClose.matcher(line).matches())
                        gotClose = true;
                    if (pChunked.matcher(line).matches())
                        gotChunked = true;
                    Matcher m = pContentLen.matcher(line);
                    if (m.matches()) {
                        gotContentLength = true;
                        clen = Integer.parseInt(m.group(1));
                    }
                    m = pContentTypeCs.matcher(line);
                    if (m.matches())
                        charset = m.group(1);
                    m = pSetCookie.matcher(line);
                    if (m.matches())
                        this.cookies.parseCookieHeader(m.group(1));
                    if (line.length() == 0) {
                        // RFC 2616, Section 6. Body is separated by the
                        // header with an empty line.
                        readingHttpBody = true;
                    }
                }
            } // while

            if (gotClose)
            {
                // RFC 2868, section 8.1.2.1
                cleanRequest(httpDataOut, httpDataIn, sock);
            }
            // Convert the whole body according to the charset.
            // The default for charset ISO-889-1, but overridden by
            // the charset attribute of the Content-Type header (if any).
            // See RFC 2616, Sections 3.4.1 and 3.7.1.
            sbHttpResponseBody.append(new String(body, charset));
        } 
        catch (Exception e) 
        {
            throw e;
        }
        return status;
    }
    public static byte[] byteAppend(byte[] buf1, byte[] buf2) {
        byte[] ret = new byte[buf1.length + buf2.length];
        System.arraycopy(buf1, 0, ret, 0, buf1.length);
        System.arraycopy(buf2, 0, ret, buf1.length, buf2.length);
        return ret;
    }

    public static byte[] byteAppend(byte[] buf1, byte[] buf2, int len) {
        if (len > buf2.length)
            len = buf2.length;
        byte[] ret = new byte[buf1.length + len];
        System.arraycopy(buf1, 0, ret, 0, buf1.length);
        System.arraycopy(buf2, 0, ret, buf1.length, len);
        return ret;
    }
    
    void cleanRequest(DataOutputStream httpDataOut, InputStream httpDataIn, Socket sock) throws Exception 
    {
        Exception localException = null;

        try {
            // Throws java.io.IOException
            httpDataOut.close();
        } catch (NullPointerException e) {
            // httpDataOut is already null ...
        } catch (IOException e) {
            localException = new Exception(e);
        } finally {
            httpDataOut = null;
        }

        try {
            // Throws java.io.IOException
            httpDataIn.close();
        } catch (NullPointerException e) {
            // httpDataIn is already null ...
        } catch (IOException e) {
            if (localException != null) {
                localException = new Exception(e);
            }
        } finally {
            httpDataIn = null;
        }

        try {
            // Throws java.io.IOException
            sock.close();
        } catch (NullPointerException e) {
            // sock is already null ...
        } catch (IOException e) {
            if (localException != null) {
                localException = new Exception(e);
            }
        } finally {
            sock = null;
        }

        if (localException != null) {
            throw localException;
        }
    }

    public static String readLine(InputStream inputStream, String charset,
            boolean includeCR) throws IOException {
        byte[] line = readLine(inputStream, includeCR);
        return (null == line) ? null : new String(line, charset);
    }
    
    public static byte[] readLine(InputStream inputStream, boolean includeCR) throws IOException 
    {
        int len = 0;
        int buflen = 128; // average line length
        byte[] buf = new byte[buflen];
        byte[] ret = null;
        int b;
        while (true) 
        {
            b = inputStream.read();
            switch (b) {
                case -1:
                    if (len > 0) {
                        ret = new byte[len];
                        System.arraycopy(buf, 0, ret, 0, len);
                        return ret;
                    }
                    return null;
                case 10:
                    if ((len > 0) && (buf[len - 1] == 13)) {
                        if (includeCR) {
                            ret = new byte[len + 1];
                            if (len > 0)
                                System.arraycopy(buf, 0, ret, 0, len);
                            ret[len] = 10;
                        } else {
                            len--;
                            ret = new byte[len];
                            if (len > 0)
                                System.arraycopy(buf, 0, ret, 0, len);
                        }
                        return ret;
                    }
                default:
                    buf[len++] = (byte) b;
                    if (len >= buflen) {
                        buflen *= 2;
                        byte[] tmp = new byte[buflen];
                        System.arraycopy(buf, 0, tmp, 0, len);
                        buf = tmp;
                    }
            }
        }
    }
}
