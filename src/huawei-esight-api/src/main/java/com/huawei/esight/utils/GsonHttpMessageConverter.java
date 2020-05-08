package com.huawei.esight.utils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

public class GsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

  public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
  private Gson gson = JsonUtil.GSON;
  private String jsonPrefix;
  
  public GsonHttpMessageConverter() {
    super(new MediaType[]{MediaType.APPLICATION_JSON, new MediaType("application", "*+json")});
  }
  
  public void setGson(Gson gson) {
    Assert.notNull(gson, "\'gson\' is required");
    this.gson = gson;
  }
  
  public Gson getGson() {
    return this.gson;
  }
  
  public void setJsonPrefix(String jsonPrefix) {
    this.jsonPrefix = jsonPrefix;
  }
  
  public void setPrefixJson(boolean prefixJson) {
    this.jsonPrefix = prefixJson?")]}\', ":null;
  }
  
  public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    TypeToken token = this.getTypeToken(type);
    return this.readTypeToken(token, inputMessage);
  }
  
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    TypeToken token = this.getTypeToken(clazz);
    return this.readTypeToken(token, inputMessage);
  }

  protected TypeToken<?> getTypeToken(Type type) {
    return TypeToken.get(type);
  }
  
  private Object readTypeToken(TypeToken<?> token, HttpInputMessage inputMessage) throws IOException {
    InputStreamReader json = new InputStreamReader(inputMessage.getBody(), this.getCharset(inputMessage.getHeaders()));
    try {
      return this.gson.fromJson(json, token.getType());
    } catch (JsonParseException var5) {
      throw new HttpMessageNotReadableException("JSON parse error: " + var5.getMessage(), var5);
    }
  }
  
  private Charset getCharset(HttpHeaders headers) {
    return DEFAULT_CHARSET;
  }
  
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    
    
    Charset charset = this.getCharset(outputMessage.getHeaders());
    OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody(), charset);
    
    try {
      if(this.jsonPrefix != null) {
        writer.append(this.jsonPrefix);
      }
      
      this.gson.toJson(o, writer);
      
      writer.close();
    } catch (JsonIOException var7) {
      throw new HttpMessageNotWritableException("Could not write JSON: " + var7.getMessage(), var7);
    }
  }
  
  protected boolean supports(Class<?> clazz) {
    return true;
  }
  
}
