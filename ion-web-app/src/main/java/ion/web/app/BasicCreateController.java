package ion.web.app;

import java.text.ParseException;
import java.util.HashSet;

import ion.core.IItem;
import ion.core.IonException;
import ion.viewmodel.view.ActionType;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.IFormViewModel;
import ion.viewmodel.view.ViewApplyMode;
import ion.web.app.jstl.Urls;
import ion.web.app.util.PageContext;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;

public class BasicCreateController extends FormController {
	protected void setupForm(String node, String cc, String container, String collection, Model model) throws IonException, ParseException {
		PageContext context = null;
		if (container != null)
			context = new PageContext(node, container, collection, meta, navmodel, urlfactory, data);
		else
			context = new PageContext(node, meta, navmodel, urlfactory, data);
		
		IItem item = data.getDummy((cc == null)?context.Class.getName():cc);
		data.initItem(item, true);
		
		if (container != null && context.CollectionProperty.BackReference() != null && !context.CollectionProperty.BackReference().isEmpty())
			item.Set(context.CollectionProperty.BackReference(), 
					parsePostedValue(item.getMetaClass().PropertyMeta(context.CollectionProperty.BackReference()), context.Id, 
					LocaleContextHolder.getLocale()));
		
		model.addAttribute("context",context);
		model.addAttribute("Title","ION: новый "+item.getMetaClass().getCaption());
		model.addAttribute("creationclass",cc);
		model.addAttribute("item",item);		
		model.addAttribute("breadcrumbs",context.BreadCrumbs(data));
		IFormViewModel vm = viewmodel.getCreationViewModel(node, item.getMetaClass());
		
		if (vm == null)
			vm = viewmodel.getCreationViewModel(item.getMetaClass());
			
		@SuppressWarnings("serial")
        HashSet<ActionType> defActions = new HashSet<ActionType>(){{ add(ActionType.SAVE); add(ActionType.CANCEL);}};
        
    @SuppressWarnings("serial")
		HashSet<FieldType> ignoreFTs = new HashSet<FieldType>(){{add(FieldType.COLLECTION);}};
		
		if (vm == null)
			vm = defaultViewModel(item.getMetaClass(), defActions, ignoreFTs);
		if (vm.getMode() == ViewApplyMode.OVERRIDE)
			vm = createOverrideModel(vm, defaultViewModel(item.getMetaClass(), defActions, ignoreFTs));
		model.addAttribute("viewmodel", vm);
		model.addAttribute("validators", viewmodel.getValidators());
		Urls.storage = filestorage;
		
		String formFile = customJspExist(node,context.Class.getName(),"form");
		if(formFile!= null){
			formFile = "../"+formFile+".jsp";
		} else {
			formFile = "form.jsp";
		}
		model.addAttribute("inputForm", formFile);
	}
}
