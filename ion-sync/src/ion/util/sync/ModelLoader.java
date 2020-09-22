package ion.util.sync;

import ion.core.IonException;
import ion.core.MetaPropertyType;
import ion.framework.meta.plain.StoredClassMeta;
import ion.framework.meta.plain.StoredPropertyMeta;
import ion.framework.meta.plain.StoredUserTypeMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModelLoader {
	
	Gson gson = new GsonBuilder().serializeNulls().create();
	
	public StoredClassMeta StoredClassMetaFromJson(File file, File userTypesDirectory) throws IonException {
		StoredClassMeta result;
		FileInputStream fis = null;
		InputStreamReader sr = null;
		
		try {
			fis = new FileInputStream(file);
			sr = new InputStreamReader(fis,"utf-8");
			result = (StoredClassMeta)gson.fromJson(sr, StoredClassMeta.class);
		} catch (IOException e){
			throw new IonException(String.format("Не удалось загрузить класс с из файла %s", file.getAbsolutePath()));
		} finally {
			try {
				if (sr != null)
					sr.close();
			} catch (IOException e) {
			}
			try {
				if (fis != null)
					fis.close();
			} catch (IOException e) {
			}
		}
		if (userTypesDirectory != null) {
			expandUserTypes(result, userTypesDirectory);
		}
		return result;
	}
	
	public void expandUserTypes(StoredClassMeta classmeta, File userTypesDirectory) throws IonException {		
		for (StoredPropertyMeta prop: classmeta.properties) {
			if (prop != null) {
				if (prop.type.intValue() == MetaPropertyType.CUSTOM.getValue()) {					
					if (prop.ref_class == null)
						throw new IonException(String.format("Не указан пользовательский тип для атрибута '%s.%s'", classmeta.name, prop.name));
					
					if (!userTypesDirectory.exists())
						throw new IonException("Отсутствует директория пользовательских типов!");					
					
					String typefilename = prop.ref_class + ".type.json";
					File typefile = new File(userTypesDirectory, typefilename);
					StoredUserTypeMeta typemeta;
					if (!typefile.exists())
						throw new IonException(String.format("Не найден файл пользовательского типа '%s' для атрибута '%s.%s' (%s)", prop.ref_class, classmeta.name, prop.name, typefile.getAbsolutePath()));
					try {
						typemeta = (StoredUserTypeMeta)gson.fromJson(new InputStreamReader(new FileInputStream(typefile),"utf-8"), StoredUserTypeMeta.class);
					} catch (IOException e){
						throw new IonException(String.format("Не удалось пользовательский тип %s (%s.%s) из файла %s", 
								prop.ref_class, classmeta.name, prop.name, typefile.getAbsolutePath()));
					}
					prop.type = typemeta.type;
					if (prop.size == null)
						prop.size = typemeta.size;
					if (prop.decimals == null)
						prop.decimals = typemeta.decimals;
				}
			}
		}
	}
}