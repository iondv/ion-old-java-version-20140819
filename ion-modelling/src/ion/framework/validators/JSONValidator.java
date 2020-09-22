package ion.framework.validators;

import java.util.ArrayList;

import ion.core.ConditionType;
import ion.core.MetaPropertyType;
import ion.core.OperationType;
//import ion.framework.meta.plain.SelectionProviderOption;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredCondition;
import ion.framework.meta.plain.StoredKeyValue;
import ion.framework.meta.plain.StoredMatrixEntry;
import ion.framework.meta.plain.StoredPermissions;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredSorting;
import ion.viewmodel.navigation.NodeType;
import ion.viewmodel.plain.StoredColumn;
import ion.viewmodel.plain.StoredField;
import ion.viewmodel.plain.StoredFormViewModel;
import ion.viewmodel.plain.StoredListViewModel;
import ion.viewmodel.plain.StoredNavNode;
import ion.viewmodel.plain.StoredNavSection;
import ion.viewmodel.plain.StoredPathChain;
import ion.viewmodel.plain.StoredTab;
import ion.viewmodel.view.CollectionFieldMode;
import ion.viewmodel.view.FieldSize;
import ion.viewmodel.view.FieldType;
import ion.viewmodel.view.ReferenceFieldMode;

public class JSONValidator {
	
	private String nvl(Object v){
		return (v == null)?"":v.toString();
	}
/*
	public String[] Validate(SelectionProviderOption o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.role == null || o.role.isEmpty())
			result.add(prefix+"Не указана роль опции списка выбора!");
		
		if (o.type == null || (!o.type.equals(SelectionProviderOption.TYPE_HQL) && !o.type.equals(SelectionProviderOption.TYPE_MATRIX)))
			result.add(prefix+"Неверный тип опции списка выбора!");

		if (o.type.equals(SelectionProviderOption.TYPE_HQL) && (o.hq == null || o.hq.isEmpty()))
			result.add(prefix+"Не указан запрос для списка выбора типа HQL!");

		if (o.type.equals(SelectionProviderOption.TYPE_MATRIX)){
			if (o.matrix.isEmpty())
				result.add(prefix+"Не указана матрица значений для списка выбора типа матрица!");
			for (StoredMatrixEntry me: o.matrix){
				String[] temp = Validate(me, prefix + " -Списки значений: ");
				for (String msg: temp)
					result.add(msg);
			}
		}
		
		if (o.type.equals(SelectionProviderOption.TYPE_HQL)){
			for (StoredKeyValue me: o.parameters){
				String[] temp = Validate(me, prefix + " -Параметры запроса: ");
				for (String msg: temp)
					result.add(msg);
			}		
		}
		
		return result.toArray(new String[result.size()]);
	}
*/	
	public String[] Validate(StoredCondition o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.operation != null){
			if (o.property != null && !o.property.isEmpty() && ConditionType.fromInt(o.operation) == null)
				result.add(prefix+"Неверно указан тип операции в условии!");
			if ((o.property == null || o.property.isEmpty()) && OperationType.fromInt(o.operation) == null)
				result.add(prefix+"Неверно указан тип операции в условии!");
		}
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredKeyValue o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.key == null || o.key.isEmpty())
			result.add(prefix+"Не указан ключ в паре ключ-значение!");
		return result.toArray(new String[result.size()]);
	}
		
	public String[] Validate(StoredMatrixEntry o, String prefix){
		ArrayList<String> result = new ArrayList<String>();

		prefix = prefix + "-"+nvl(o.comment);
		
		for (StoredCondition c: o.conditions){
			String[] temp = Validate(c, prefix + " -Условия применения: ");
			for (String msg: temp)
				result.add(msg);
		}
		
		for (StoredKeyValue me: o.result){
			String[] temp = Validate(me, prefix + " -Список значений: ");
			for (String msg: temp)
				result.add(msg);
		}		
		
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredPermissions o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.role == null || o.role.isEmpty())
			result.add(prefix + "Не указана роль для разрешений.");
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredPropertyMeta o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.name == null || o.name.isEmpty())
			result.add(prefix+"Не указано имя атрибута "+nvl(o.caption)+"["+nvl(o.name)+"]!");
		
		if (o.type == null)
			result.add(prefix+"Не указан тип значения атрибута "+nvl(o.caption)+"["+nvl(o.name)+"]!");
		else {
			MetaPropertyType t = MetaPropertyType.fromInt(o.type);
			if (t == null)
				result.add(prefix+"Неверно указан тип значения атрибута "+nvl(o.caption)+"["+nvl(o.name)+"]!");
			else {
				switch (t){
					case STRUCT:
					case REFERENCE:{
						if (o.ref_class == null || o.ref_class.isEmpty())
							result.add(prefix + "Для атрибута  "+nvl(o.caption)+"["+nvl(o.name)+"] ссылочного типа не указан класс объекта!");
					}break;
					case COLLECTION:{
						if (o.items_class == null || o.items_class.isEmpty())
							result.add(prefix + "Для атрибута  "+nvl(o.caption)+"["+nvl(o.name)+"] типа коллекция не указан класс объектов коллекции!");
						//if (o.back_ref == null || o.back_ref.isEmpty())
						//	result.add(prefix + "Для атрибута  "+nvl(o.caption)+"["+nvl(o.name)+"] типа коллекция не указан атрибут обратной ссылки!");
						
						if (o.sel_conditions != null)
						for (StoredCondition c: o.sel_conditions){
							String[] tmp = Validate(c, prefix+" -"+nvl(o.caption)+"["+nvl(o.name)+"] выборка: ");
							for (String msg: tmp)
								result.add(msg);
						}
						
						if (o.sel_sorting != null)
						for (StoredSorting s: o.sel_sorting){
							String[] tmp = Validate(s, prefix+" -"+nvl(o.caption)+"["+nvl(o.name)+"] сортировка: ");
							for (String msg: tmp)
								result.add(msg);
						}						
					}break;
					default:break;
				}
			}
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredSorting o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.property == null || o.property.isEmpty())
			result.add(prefix+"Не указано свойство для сортировки!");
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredClassMeta o){
		ArrayList<String> result = new ArrayList<String>();
		
		if (o.name == null || o.name.isEmpty())
			result.add("Не указано системное имя класса!");
		
		if (!o.is_struct && (o.key == null || o.key.isEmpty()) && (o.ancestor == null || o.ancestor.isEmpty()))
			result.add("Не указан ключевой атрибут класса "+nvl(o.caption)+"["+nvl(o.name)+"]!");
		
		for (StoredPropertyMeta sp: o.properties){
			String[] tmp = Validate(sp, nvl(o.caption)+"["+nvl(o.name)+"] : ");
			for (String msg: tmp)
				result.add(msg);					
		}
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredColumn o, String prefix){
		return Validate((StoredField)o, prefix);
	}
	
	public String[] Validate(StoredField o, String prefix){
		ArrayList<String> result = new ArrayList<String>();		
		FieldType t = (o.type == null)?FieldType.TEXT:FieldType.fromInt(o.type);
		if (t == null)
			result.add(prefix+"Неверно указан тип поля "+nvl(o.caption)+"!");
		else {
			if (t != FieldType.GROUP){
				if (o.property == null || o.property.isEmpty())
					result.add(prefix+"Не указано имя атрибута для поля "+nvl(o.caption)+"!");					
			}
			
			switch (t){
				case COLLECTION:{						
					CollectionFieldMode m = (o.mode == null)?CollectionFieldMode.LINK:CollectionFieldMode.fromInt(o.mode);
					switch (m){
						case LIST:{
							for (StoredField f: o.fields){
								String[] tmp = Validate(f,prefix+" -"+nvl(o.caption)+" ");
								for (String msg:tmp)
									result.add(msg);
							}
						}break;
						case TABLE:{
							for (StoredColumn c: o.columns){
								String[] tmp = Validate(c,prefix+" -"+nvl(o.caption)+" ");
								for (String msg:tmp)
									result.add(msg);									
							}								
						}break;
						default:break;
					}
					
					if (o.actions == null)
						result.add(prefix+"Не указаны допустимые действия для коллекции "+nvl(o.caption)+"!");
				}break;
				case REFERENCE:{
					ReferenceFieldMode m = (o.mode == null)?ReferenceFieldMode.STRING:ReferenceFieldMode.fromInt(o.mode);
					switch (m){
						case STRING:
						case LINK:{
							if (o.size != null && FieldSize.fromInt(o.size) == null)
								result.add(prefix+"Неверно указан размер поля "+nvl(o.caption)+"!");													
						}break;
						case INFO:{
							for (StoredField f: o.fields){
								String[] tmp = Validate(f,prefix+" -"+nvl(o.caption)+" ");
								for (String msg:tmp)
									result.add(msg);
							}							
						}break;
					}						
				}break;
				case GROUP:{
					for (StoredField f: o.fields){
						String[] tmp = Validate(f,prefix+" -"+nvl(o.caption)+" ");
						for (String msg:tmp)
							result.add(msg);
					}						
				}break;
				default:{
					if (o.size != null && FieldSize.fromInt(o.size) == null)
						result.add(prefix+"Неверно указан размер поля "+nvl(o.caption)+"!");				
				}break;
			}
		}
		
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredPathChain o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		if (o.class_name == null || o.class_name.isEmpty())
			result.add(prefix+"Не указан класс для хлебных крошек!");
		
		if (o.path.length == 0)
			result.add(prefix+"Не указан путь для хлебных крошек!");
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredTab o, String prefix){
		ArrayList<String> result = new ArrayList<String>();
		
		for (StoredField f: o.shortFields){
			String[] tmp = Validate(f,prefix+" -"+nvl(o.caption)+" ");
			for (String msg:tmp)
				result.add(msg);
		}
		
		for (StoredField f: o.fullFields){
			String[] tmp = Validate(f,prefix+" -"+nvl(o.caption)+" ");
			for (String msg:tmp)
				result.add(msg);
		}								
		
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredFormViewModel o){
		ArrayList<String> result = new ArrayList<String>();
		if (o.commands == null)
			result.add("Не указаны действия для представления формы!");

		for (StoredTab t: o.tabs){
			String[] tmp = Validate(t,"");
			for (String msg:tmp)
				result.add(msg);
		}								
				
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredListViewModel o){
		ArrayList<String> result = new ArrayList<String>();
		/*
		if (o.actions == null)
			result.add("Не указаны действия для представления списка!");
		*/
		for (StoredColumn c: o.columns){
			String[] tmp = Validate(c,"");
			for (String msg:tmp)
				result.add(msg);
		}								

// TODO: валидация детализации
//		for (StoredField f: o.details){
//			String[] tmp = Validate(f,"");
//			for (String msg:tmp)
//				result.add(msg);
//		}								
		
		return result.toArray(new String[result.size()]);
	}		
	
	public String[] Validate(StoredNavNode o){
		ArrayList<String> result = new ArrayList<String>();

		if (o.code == null || o.code.isEmpty())
			result.add("Не указан код для узла навигации "+nvl(o.caption)+"!");
		
		if (o.type == null)
			result.add("Не указан тип узла навигации "+nvl(o.caption)+"["+nvl(o.code)+"]!");
		else {
			NodeType t = NodeType.fromInt(o.type);
			switch (t){
				case GROUP:break;
				case CLASS:{
					if (o.classname == null || o.classname.isEmpty())
						result.add("Не указан класс объектов узла навигации "+nvl(o.caption)+"["+nvl(o.code)+"]!");
				}break;
				case CONTAINER:{
					if (o.classname == null || o.classname.isEmpty())
						result.add("Не указан класс объектов узла навигации "+nvl(o.caption)+"["+nvl(o.code)+"]!");
					if (o.container == null || o.container.isEmpty())
						result.add("Не указан контейнер узла навигации "+nvl(o.caption)+"["+nvl(o.code)+"]!");
					if (o.collection == null || o.collection.isEmpty())
						result.add("Не указана коллекция узла навигации "+nvl(o.caption)+"["+nvl(o.code)+"]!");
				
				}break;
			}
			
			switch (t){
				case CLASS:
				case CONTAINER:{
					for (StoredCondition c: o.conditions){
						String[] tmp = Validate(c,nvl(o.caption)+"["+nvl(o.code)+"] выборка:");
						for (String msg:tmp)
							result.add(msg);
					}
					
					for (StoredSorting s: o.sorting){
						String[] tmp = Validate(s,nvl(o.caption)+"["+nvl(o.code)+"] сортировка:");
						for (String msg:tmp)
							result.add(msg);
					}					
				}break;
				default:break;
			}
			
		}
		
		for (StoredPathChain pc: o.pathChains){
			String[] tmp = Validate(pc,nvl(o.caption)+"["+nvl(o.code)+"] ");
			for (String msg:tmp)
				result.add(msg);
		}								
		
		return result.toArray(new String[result.size()]);
	}
	
	public String[] Validate(StoredNavSection o){
		ArrayList<String> result = new ArrayList<String>();
		if(o.name == null || o.name.isEmpty())
			result.add("Не указано имя для секции навигации "+nvl(o.caption)+"!");
		return result.toArray(new String[result.size()]);
	}
}