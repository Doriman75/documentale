package com.almaviva.documentale.interceptors.aes;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.almaviva.documentale.engine.InternalServerError;

public abstract class AES
{
    byte[] iv = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    IvParameterSpec ivSpec = new IvParameterSpec(iv);    
    private SecretKeySpec key()
    {
        String password = "secret";
        while(password.length() < 16) password += password;
        password.substring(0, 16);
        return new SecretKeySpec(password.getBytes(Charset.forName("utf-8")), "AES");
    }

    private byte[] run(byte[] bytes, int mode)
    {
        try{
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");         
            cipher.init(mode, key(), ivSpec);
            return cipher.doFinal(bytes);    
        }catch(Exception e)
        {
            e.printStackTrace();
            throw new InternalServerError(e.getMessage());
        }
    }

    protected byte[] decrypt(byte[] bytes)
    {
        return run(bytes, Cipher.DECRYPT_MODE);
    }
    protected byte[] encrypt(byte[] bytes)
    {
        return run(bytes, Cipher.ENCRYPT_MODE);
    }
}