package ion.viewmodel.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FormTab implements IFormTab {

	String caption;
	
	List<IField> fullViewFields;
	List<IField> shortViewFields;
	
	public enum TabMode {
		FULLVIEW,
		SHORTVIEW
	}
	
	public FormTab(String caption,List<IField> fullFields,List<IField> shortFields) {
		this.caption = caption;
		this.fullViewFields = fullFields;
		this.shortViewFields = shortFields;
		Collections.sort(fullViewFields);
		Collections.sort(shortViewFields);
	}	
	
	public FormTab(String caption, List<IField> fullFields) {
		this(caption,fullFields,new ArrayList<IField>());
	}
	
	public FormTab(String caption) {
		this(caption,new ArrayList<IField>());
	}	

	@Override
	public String getCaption() {
		return caption;
	}
	
	public void addField(IField f, TabMode mode){
		switch (mode){
			case FULLVIEW:{
				fullViewFields.add(f);
				Collections.sort(fullViewFields);
			}break;
			case SHORTVIEW:{
				shortViewFields.add(f);
				Collections.sort(shortViewFields);
			}break;
		}
	}
	
	public void addFields(List<IField> fields, TabMode mode){
		switch (mode){
			case FULLVIEW:{
				fullViewFields.addAll(fields);
				Collections.sort(fullViewFields);
			}break;
			case SHORTVIEW:{
				shortViewFields.addAll(fields);
				Collections.sort(shortViewFields);
			}break;
		}
	}
	
	@Override
	public Collection<IField> getFullViewFields() {
		return fullViewFields;
	}

	@Override
	public Collection<IField> getShortViewFields() {
		return shortViewFields;
	}

}
