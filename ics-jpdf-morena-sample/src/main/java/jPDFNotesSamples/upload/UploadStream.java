/*
 * Created on Jan 22, 2008
 *
 */
package jPDFNotesSamples.upload;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

public class UploadStream extends OutputStream
{
    private DataOutputStream m_OutStream;
    private MessageDigest m_Digest;
    
    public UploadStream (DataOutputStream outStream) throws Exception
    {
        m_OutStream = outStream;
        m_Digest = MessageDigest.getInstance("MD5");
    }
    
    public void write(int b) throws IOException
    {
        m_Digest.update((byte)b);
        m_OutStream.write (b);
    }
    
    public void write(byte[] b) throws IOException
    {
        m_Digest.update (b);
        m_OutStream.write (b);
    }
    
    public void write(byte[] b, int off, int len) throws IOException
    {
        m_Digest.update (b, off, len);
        m_OutStream.write (b, off, len);
    }
    
    public String getMD5 ()
    {
        StringBuffer ret = new StringBuffer();
        byte md5sum[] = m_Digest.digest();

        for (int i = 0; i < md5sum.length; i++) 
        {
            ret.append(Integer.toHexString((md5sum[i] >> 4) & 0x0f));
            ret.append(Integer.toHexString(md5sum[i] & 0x0f));
        }
        
        return ret.toString();
    }
}
