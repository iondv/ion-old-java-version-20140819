package ion.framework.test.domain;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Lob;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Id;

@Entity
@Table(name="t_cows")
@Inheritance(strategy = InheritanceType.JOINED)
public class Cows {

@Id
@Column(name="f_id", precision=11)
private Integer id;

@Column(name="f_name", length=200)
private String name;

@Lob
@Column(name="f_info", length=2000)
private String info;

public void setId(Integer v){
id = v;
}

public void setName(String v){
name = v;
}

public void setInfo(String v){
info = v;
}

public Integer getId(){
return id;
}

public String getName(){
return name;
}

public String getInfo(){
return info;
}

}
