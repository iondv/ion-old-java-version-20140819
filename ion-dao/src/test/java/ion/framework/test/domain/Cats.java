package ion.framework.test.domain;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.DiscriminatorValue;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.BatchSize;

@Entity
@SecondaryTable(name="t_cats", pkJoinColumns = @PrimaryKeyJoinColumn(name="f_animal_id"))
@org.hibernate.annotations.Table(appliesTo="t_cats", fetch=FetchMode.SELECT)
@BatchSize(size=20)
@DiscriminatorValue("Cats")
public class Cats extends Mammals {

@Column(name="f_fur_color", table="t_cats", length=200)
private String furColor;

public void setFurColor(String v){
furColor = v;
}

public String getFurColor(){
return furColor;
}

}
