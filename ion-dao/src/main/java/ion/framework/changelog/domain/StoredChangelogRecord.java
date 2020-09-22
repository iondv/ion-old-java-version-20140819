package ion.framework.changelog.domain;

import ion.core.logging.ChangelogRecordType;
import ion.framework.changelog.domain.ChangeLogId;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "changelog")
@IdClass(ChangeLogId.class)	
public class StoredChangelogRecord {
	@Id
	@GeneratedValue
	public Long id;
	
	public String type;
	
	public String actor;
	
	public Date time;
	
	@Column(name="object_class")
	public String objectClass;
	
	@Column(name="object_id")
	public String objectId;
	
	@Lob
	@Column(name="attr_updates")
	public String attrUpdates;
	
	public StoredChangelogRecord(){
		this(null, null, null, null, "{}");
	}
	
	public StoredChangelogRecord(String a, String oclass, String oid){
		this(ChangelogRecordType.DELETION.getValue(), a,  oclass, oid, "{}");
	}
	
	public StoredChangelogRecord(String tp, String a, String oclass, String oid, String attr){
		this(null, tp, a, oclass, oid, attr);
	}
	
	public StoredChangelogRecord(Date time, String tp, String a, String oclass, String oid, String attr){
		id = null;
		
		type = tp;
		
		actor = a;
		
		this.time = time;
		
		objectClass = oclass;
		
		objectId = oid;
		
		attrUpdates = attr;
	}	
}
