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
@SecondaryTable(name="t_mammals", pkJoinColumns = @PrimaryKeyJoinColumn(name="f_animal_id"))
@org.hibernate.annotations.Table(appliesTo="t_mammals", fetch=FetchMode.SELECT)
@BatchSize(size=20)
@DiscriminatorValue("Mammals")
public class Mammals extends Animals {

@Column(name="f_age", table="t_mammals", precision=11)
private Integer age;

public void setAge(Integer v){
age = v;
}

public Integer getAge(){
return age;
}

}
