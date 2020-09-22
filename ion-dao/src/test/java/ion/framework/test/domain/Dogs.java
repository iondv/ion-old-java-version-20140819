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
@SecondaryTable(name="t_dogs", pkJoinColumns = @PrimaryKeyJoinColumn(name="f_animal_id"))
@org.hibernate.annotations.Table(appliesTo="t_dogs", fetch=FetchMode.SELECT)
@BatchSize(size=20)
@DiscriminatorValue("Dogs")
public class Dogs extends Mammals {

@Column(name="f_heigth", table="t_dogs", precision=11)
private Integer heigth;

public void setHeigth(Integer v){
heigth = v;
}

public Integer getHeigth(){
return heigth;
}

}
