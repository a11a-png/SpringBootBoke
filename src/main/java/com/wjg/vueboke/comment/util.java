package com.wjg.vueboke.comment;

import antlr.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class util {

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    //获取请求头中的token
    public static String getRequestToken(HttpServletRequest request){
        //从header中获取token
        String token=request.getHeader("token");
        //如果没有则从参数中获取
        if (token==null || token==""){
            token=request.getParameter("token");
        }
        return token;
    }
}
