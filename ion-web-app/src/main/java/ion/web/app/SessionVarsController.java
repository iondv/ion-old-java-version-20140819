package ion.web.app;

import ion.web.app.util.JSONResponse;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/setvars")
public class SessionVarsController extends BasicController {
	
	private Map<String, Class<?>> varDefs = new HashMap<String, Class<?>>();
	private DateFormat dateFormat;
	
	@Autowired
	private ContextHolder holder;
	
	public SessionVarsController() {};
	
	public void setDateFormat(String format) {
		if (!format.equalsIgnoreCase("unix"))
			dateFormat = new SimpleDateFormat(format);
		else
			dateFormat = null;
	}
	
	public void setVarDefinitions(Map<String, String> vars) throws ClassNotFoundException {
		for(Entry<String, String> entry: vars.entrySet()) {
			Class<?> type = Class.forName(entry.getValue());
			varDefs.put(entry.getKey().toLowerCase(), type);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String SetSessionVarsRedir(
			@RequestParam MultiValueMap<String, String> params,
			@RequestHeader(value = "referer", required = true) final String referer,
			ModelMap model) 
					throws InstantiationException, IllegalAccessException, ParseException {
		SetVars(params);
		model.clear();
		return "redirect:" + referer;
	}
	
	@RequestMapping(method = {RequestMethod.POST, RequestMethod.HEAD}, headers = "x-requested-with=XMLHttpRequest")
	public JSONResponse SetSessionVarsAjax(@RequestParam MultiValueMap<String, String> params, ModelMap model) {
		try {
			SetVars(params);
			return new JSONResponse();
		} catch (Exception e){
			return new JSONResponse(e.getMessage());
		}
	}

	protected void SetVars(MultiValueMap<String, String> vars) throws InstantiationException, IllegalAccessException, ParseException {
		for(Entry<String, List<String>> entry: vars.entrySet()) {
			String key = entry.getKey().toLowerCase();
			if (key.endsWith("[]"))
				key = key.substring(0, key.length() - 2);
			if (varDefs.containsKey(key)) {
				List<String> rawvalue = entry.getValue();
				Class<?> type = varDefs.get(key);
				Object value;
				if (type.isArray()) {
					Class<?> elementtype = type.getComponentType();
					Object[] arrvalue = new Object[rawvalue.size()];
					for(int i = 0; i < arrvalue.length; i++) {
						arrvalue[i] = ParseVar(elementtype, rawvalue.get(i));
					}
					value = arrvalue;
				} else {
					value = ParseVar(type, rawvalue.get(0));
				}
				holder.setValue(key, value);
			}
		}
	}
	
	protected Object ParseVar(Class<?> type, String rawValue)
			throws InstantiationException, IllegalAccessException, ParseException {
		if (Boolean.class == type)
			return Boolean.parseBoolean(rawValue);
		if (Byte.class == type)
			return Byte.parseByte(rawValue);
		if (Short.class == type)
			return Short.parseShort(rawValue);
		if (Integer.class == type)
			return Integer.parseInt(rawValue);
		if (Long.class == type)
			return Long.parseLong(rawValue);
		if (Float.class == type)
			return Float.parseFloat(rawValue);
		if (Double.class == type)
			return Double.parseDouble(rawValue);
		if (Date.class == type) {
			if (dateFormat == null)
				return new Date(Long.parseLong(rawValue));
			else
				return dateFormat.parse(rawValue);
		}
		return rawValue;
	}
}