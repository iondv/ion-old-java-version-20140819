package ion.framework.digisign;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "digital_signatures")
public class DigitalSignature {

	@Id
	@GeneratedValue
	private Integer id;
	private Date ts;
	private String actor;
	private String className;
	private String action;
	private Integer part;
	private String objId;
	private String attributes;

	@Lob
	private byte[] data;
		
	@Lob
	private byte[] sign;
	
	public Integer getId() {
	  return id;
  }

	public void setId(Integer id) {
	  this.id = id;
  }
	
	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public Integer getPart() {
		return part;
	}

	public void setPart(Integer part) {
		this.part = part;
	}

	@Temporal(TemporalType.TIMESTAMP)
	/** timestamp */
	public Date getTs() {
	  return ts;
  }

	public void setTs(Date ts) {
	  this.ts = ts;
  }

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Column(name="obj_id")
	public String getObjId() {
	  return objId;
  }

	public void setObjId(String objId) {
	  this.objId = objId;
  }

	@Column(name = "class_name")
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}	

	public byte[] getSign() {
		return sign;
	}

	public void setSign(byte[] sign) {
		this.sign = sign;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DigitalSignature)) {
			return false;
		}
		return this.getId() == ((DigitalSignature)obj).getId();
	}
}
