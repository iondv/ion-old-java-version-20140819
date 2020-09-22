package ion.offline.server.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "data_package")
public class DataPackage {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id = null;
	
	@ManyToOne
	@JoinColumn(name = "point")
	private Point point = null;
	
	@Column(name = "generating")
	private Date generating = null;
	
	@Column(name = "directory")
	private String directory = null;
/*	
	@Column(name = "is_busy")
	private Boolean isBusy = null;
*/
	@Column(name = "is_packed")
	private Boolean isPacked = null;
	
	public DataPackage(){
		
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public Point getPoint() {
		return point;
	}
	public void setPoint(Point point) {
		this.point = point;
	}

	public Date getGenerating() {
		return generating;
	}
	public void setGenerating(Date generating) {
		this.generating = generating;
	}

	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
/*	
	public Boolean getIsBusy() {
		return (isBusy == null)?false:isBusy;
	}
	public void setIsBusy(Boolean isBusy) {
		this.isBusy = isBusy;
	}
*/	
	public Boolean getIsPacked() {
		return (isPacked == null)?false:isPacked;
	}
	public void setIsPacked(Boolean isPacked) {
		this.isPacked = isPacked;
	}
	
	public DataPackage(Point point, Date generating) {
		super();
		this.point = point;
		this.generating = generating;
	}
}
