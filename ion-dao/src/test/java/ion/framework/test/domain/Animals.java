package ion.framework.test.domain;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Id;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;

@Entity
@Table(name="t_animals")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="_type", discriminatorType=DiscriminatorType.STRING, length=200)
@DiscriminatorValue("Animals")
public class Animals {

@Column(name="f_name", length=200)
private String name;

@Id
@Column(name="f_animal_id", precision=11)
private Integer animalId;

public void setName(String v){
name = v;
}

public void setAnimalId(Integer v){
animalId = v;
}

public String getName(){
return name;
}

public Integer getAnimalId(){
return animalId;
}

}
