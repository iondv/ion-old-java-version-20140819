package ion.web.app;

public class SectionContextHolder extends ContextHolder {
	
	public final static String SECTION = "currentSection";
	
	public void setDefaultSection(String defaultSection) {
		setDefault(SECTION, defaultSection);
	}

	public String getSection() {
		return String.valueOf(getValue(SECTION));
	}

	public void setSection(String section) {
		setValue(SECTION, section);
	}
}
