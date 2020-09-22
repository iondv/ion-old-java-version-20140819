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
@SecondaryTable(name="t_birds", pkJoinColumns = @PrimaryKeyJoinColumn(name="f_animal_id"))
@org.hibernate.annotations.Table(appliesTo="t_birds", fetch=FetchMode.SELECT)
@BatchSize(size=20)
@DiscriminatorValue("Birds")
public class Birds extends Animals {

@Column(name="f_bird_type", table="t_birds", length=200)
private String birdType;

public void setBirdType(String v){
birdType = v;
}

public String getBirdType(){
return birdType;
}

}
