package ion.web.app;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class GSONMessageConverter extends AbstractHttpMessageConverter<Object> {
	
    private Gson gson = new GsonBuilder()
    					.serializeNulls()
    					.serializeSpecialFloatingPointValues()	
    					.setDateFormat(DateFormat.SHORT)
    					.create();

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");	
    
    public GSONMessageConverter(){
        super(new MediaType("application", "json", DEFAULT_CHARSET));
    }    

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
        try{
            return gson.fromJson(IOUtils.toString(inputMessage.getBody(), DEFAULT_CHARSET.displayName()), clazz);
        }catch(JsonSyntaxException e){
            throw new HttpMessageNotReadableException("Could not read JSON: " + e.getMessage(), e);
        }		
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
				Writer w = new OutputStreamWriter(outputMessage.getBody(),"UTF-8");
        gson.toJson(t, w);
        outputMessage.getBody().flush();
        w.close();
        outputMessage.getBody().close();
	}
}
