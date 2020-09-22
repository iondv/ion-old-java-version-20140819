package ion.web.app.jstl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import ion.core.IItem;
import ion.core.IonException;
import ion.core.data.Property;
import ion.viewmodel.view.DataField;
import ion.viewmodel.view.IField;
import ion.viewmodel.view.Validator;

public final class ItemAccess {
	
	public static Property property(IItem item, String name){
		try {
			return (Property)item.Property(name);
		} catch (IonException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String navItemId(IItem item){
		try {
			return URLEncoder.encode(item.getClassName()+":"+item.getItemId(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String rowItemId(IItem item){
		return item.getClassName()+"-"+item.getItemId();
	}
	
	public static String fieldId(IField field){
		return field.getProperty().replace(".", "-");
	}
	
	public static String fieldFunc(IField field){
		return field.getProperty().replace("_", "__").replace(".", "_");
	}	
	
	public static String prepareFieldExpression(String expr) throws IonException{
		Pattern extractor = Pattern.compile("[^a-zA-Z0-9_]\\.");
		return StringEscapeUtils.escapeJavaScript(extractor.matcher(" "+expr).replaceAll("this.").trim());		
	}

	public static Boolean isPropExists(IItem item, IField field) throws IonException {
		return item.getProperties().containsKey(field.getProperty());
	}
	
	@SuppressWarnings("unchecked")
	public static String fieldMask(IField field, Object validatorsList){
		Map<String,Validator> validators = (Map<String,Validator>)validatorsList;
		String result = null;
		if(field instanceof DataField){
			String dmask = ((DataField) field).getMask();
			if(dmask!=null && dmask!=""){
				result = dmask;
			}
		}
		if(field.getValidators()!=null){
			for(String v : field.getValidators()){
				if(validators.containsKey(v)){
					String vmask = validators.get(v).getMask();
					if(vmask!=null && vmask!=""){
						result = vmask;
					}
				}
			}
		}
		return result;
	}
}