package liquibase.structure.core;

public class View extends Relation {

    public View() {
    }

    
    public Relation setSchema(Schema schema) {
        return super.setSchema(schema);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public String getDefinition() {
        return getAttribute("definition", String.class);
    }

    public void setDefinition(String definition) {
        this.setAttribute("definition", definition);
    }

    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        View that = (View) o;

        return getName().equalsIgnoreCase(that.getName());

    }

    
    public int hashCode() {
        return getName().toUpperCase().hashCode();
    }

    
    public String toString() {
        String viewStr = getName() + " (";
        for (int i = 0; i < getColumns().size(); i++) {
            if (i > 0) {
                viewStr += "," + getColumns().get(i);
            } else {
                viewStr += getColumns().get(i);
            }
        }
        viewStr += ")";
        return viewStr;
    }

    
    public View setName(String name) {
        return (View) super.setName(name);
    }


}
