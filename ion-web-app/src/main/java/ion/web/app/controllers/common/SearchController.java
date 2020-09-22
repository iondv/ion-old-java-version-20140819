package ion.web.app.controllers.common;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ion.core.IItem;
import ion.core.IProperty;
import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.db.search.IFulltextSearchAdapter;
import ion.web.app.BasicController;
import ion.web.app.UrlFactory;
import ion.web.app.service.IonWebAppService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController extends BasicController {
	@Autowired(required=false)
	IFulltextSearchAdapter adapter;
	
	@Autowired
	IonWebAppService data;
	
	@Override
	@ModelAttribute("Title")
	public String getTitle(Model model) {
		return "Результаты поиска";
	}	

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String results(@RequestParam(required=false) String pattern, @RequestParam(required=false) Integer page, Model model) throws UnsupportedEncodingException{
		if(adapter!=null){
			List<String> results = adapter.search(pattern, 20*((page == null?1:page) - 1), 20);
			List<SearchResult> list = new ArrayList<SearchResult>(results.size());
			for (String id: results){
				String[] parts = id.split("@");
				IItem i;
				List<IProperty> props = new LinkedList<IProperty>();
				try {
					i = data.getItem(parts[0], parts[1]);
					String nodes = navmodel.getNodeForClassname(parts[0]).replaceAll("\\.", ":");
					if (i != null){
						for (IProperty p : i.getProperties().values())
							if (p.Meta().IndexSearch() && (p.getType() == MetaPropertyType.STRING || p.getType() == MetaPropertyType.TEXT))
								props.add(p);
						list.add(new SearchResult(UrlFactory.MakeUrl(nodes+"/"+i.getItemId()), i.toString(), props));
					}
				} catch (IonException e) {
				}
			}
			model.addAttribute("results", list);
		}
		
		return ThemeDir()+"search";
	}
	
	public class SearchResult {
		private String url;
		private String title;
		private List<IProperty> properties;
		
		public SearchResult(String link, String title, List<IProperty> properties) {
			this.url = link;
			this.title = title;
			this.properties = properties;
		}
		public String getUrl() {
			return url;
		}
		public String getTitle(){
			return title;
		}
		public List<IProperty> getProperties() {
			return properties;
		}
	}
}
