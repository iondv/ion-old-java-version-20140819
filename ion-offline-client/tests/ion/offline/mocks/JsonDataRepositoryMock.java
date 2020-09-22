//package ion.offline.mocks;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Map;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonSyntaxException;
//import com.google.gson.reflect.TypeToken;
//
//import ion.core.IItem;
//import ion.core.IMetaRepository;
//import ion.core.IonException;
//import ion.core.data.Item;
//import ion.core.mocks.DataRepositoryMock;
//
//public class JsonDataRepositoryMock extends DataRepositoryMock {
//	
//	public JsonDataRepositoryMock(Map<String, Class<?>> domain, IMetaRepository metaRepository) {
//		super(domain, metaRepository);
//	}
//
//	@Override
//	public IItem GetItem(String classname, String id) throws IonException {
//		Gson gs = new GsonBuilder().serializeNulls().create();
//		try {
//			return new Item(Integer.toString(idCounter++), gs.fromJson(id, domain.get(classname)), metaRepository.GetStructure(classname), this);
//		} catch (JsonSyntaxException e) {
//			throw new IonException(e);
//		}
//	}
//	
//	@Override
//	public Collection<IItem> GetList(String classname, String data) throws IonException {
//		Gson gs = new GsonBuilder().serializeNulls().create();
//		try {
//			ArrayList<JsonObject> jl = gs.fromJson(data, new TypeToken<ArrayList<JsonObject>>(){}.getType());
//			Collection<IItem> result = new ArrayList<IItem>();
//			for(JsonObject jo:jl)
//				result.add(new Item(Integer.toString(idCounter++), gs.fromJson(jo, domain.get(classname)), metaRepository.GetStructure(classname), this));
//			return result;
//		} catch (JsonSyntaxException e) {
//			throw new IonException(e);
//		}
//	}
//}
